package fileserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import net.TcpHelper;
import proxy.TcpHandler;
import cmd.Command;
import cmd.CommandParser;
import cmd.StringParameter;

public class FileTcpHandler implements Runnable {
	protected static final Logger logger = Logger.getLogger( TcpHandler.class.getName() );

	Socket client;

	private FileTcpServer srv;

	private File shareDir;
	
	protected static final Command CMD_LIST;
	protected static final Command CMD_DOWNLOAD;
	
	protected static final CommandParser cmdParser;
	
	static{

		CMD_LIST = new Command( "list" );
		
		CMD_DOWNLOAD = new Command( "download" );
		CMD_DOWNLOAD.addParameter( new StringParameter( "filename" ) );
		
		cmdParser = new CommandParser();
		cmdParser.addCommands( CMD_LIST, CMD_DOWNLOAD );
	}
	
	
	public FileTcpHandler( Socket client, FileTcpServer srv )
	{
		this.client = client;
		this.srv = srv;
		this.shareDir = srv.getShareDir();
	}
	
	@Override
	public void run() {
		TcpHelper proxy = null;
		Command cmd = null;
		
		while( !srv.isStopping() )
		{
			try
			{
				proxy = new TcpHelper( this.client, cmdParser );
				cmd = proxy.receiveCommand();
				break;
			}catch( SocketTimeoutException stex )
			{ continue; }
			catch( IOException ioex )
			{
				logger.warning( "Couldn't read from proxy: " + ioex.getMessage() );
				return;
			}
		}
		
		if( cmd == CMD_LIST )
		{
			for( File f : this.shareDir.listFiles() )
			{
				proxy.sendLine( f.getName() );
			}
		}else if( cmd == CMD_DOWNLOAD )
		{
			doDownload( proxy, cmd );
		}
		

		try{
			proxy.close();
		}catch( IOException ioex )
		{ logger.warning( "Couldn't send list of files" ); }
	}

	private void doDownload(TcpHelper proxy, Command cmd) {
		
		File f = new File( "shared/" + cmd.getParameter( "filename" ).getValue() );
		
		if( !f.exists() )
		{
			proxy.sendLine( "The requested file does not exist." );
			return;
		}
		
		if( !f.canRead() )
		{
			proxy.sendLine( "The requested file could not be read." );
			return;
		}
		
		proxy.sendLine( "File size: " + f.length() );
		
		try{
			if( proxy.receiveAck() )
			{
			
				BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream(f) ) );
				
				proxy.sendLine( "Begin file: " + f.getName() );
				
				String line;
				while( (line=br.readLine()) != null )
				{
					proxy.sendLine( line );
				}
			}else
				return;
		}catch( FileNotFoundException fnfex )
		{
			proxy.sendLine( "The requested file does not exist." );
			return;
		}catch( IOException ioex )
		{
			proxy.sendLine( "The requested file could not be read." );
			return;
		}
		
		
		
	}
}
