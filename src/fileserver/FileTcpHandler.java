package fileserver;

import java.io.File;
import java.io.IOException;
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
			File f = new File( "shared/" + cmd.getParameter( "filename" ).getValue() );
			
			proxy.sendFile( f );
		}
		

		try{
			proxy.close();
		}catch( IOException ioex )
		{ logger.warning( "Couldn't send list of files" ); }
	}
}
