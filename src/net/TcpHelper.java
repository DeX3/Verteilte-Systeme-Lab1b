package net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import cmd.Command;
import cmd.CommandParser;
import exceptions.ParseException;
import exceptions.ValidationException;

/**
 * Helper class. Encapsulates a socket to simplify communication.
 */
public class TcpHelper {

	/** The underlying socket. */
	Socket socket;
	
	/** The reader used to read from the socket. */
	BufferedReader br;
	
	/** The printstream used to write to the socket. */
	PrintStream ps;
	
	/** Optional commandparser to parse commands right away. */
	CommandParser cmdParser;
	
	/** Constant for receiving positive acknowledge-messages. */
	public static final String STR_ACK = "ACK";
	
	/** Constant for receiving negative acknowledge-messages. */
	public static final String STR_NACK = "NACK";
	
	public static final int BUFFER_SIZE = 1024;
	
	/**
	 * Gets the command-parser.
	 * 
	 * @return the command-parser
	 */
	public CommandParser getCmdParser() {
		return cmdParser;
	}

	/**
	 * Sets the command-parser.
	 * 
	 * @param cmdParser
	 *            the new command-parser
	 */
	public void setCmdParser(CommandParser cmdParser) {
		this.cmdParser = cmdParser;
	}
	
	/**
	 * Instantiates a new tcp helper.
	 * 
	 * @param socket
	 *            the socket to encapsulate
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public TcpHelper( Socket socket ) throws IOException
	{
		this( socket, null );
	}
	
	/**
	 * Instantiates a new tcp helper.
	 * 
	 * @param socket
	 *            the socket to encapsulate
	 * @param cmdParser
	 *            the command parser, used to parse commands, coming in from the socket
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public TcpHelper( Socket socket, CommandParser cmdParser ) throws IOException
	{
		this.socket = socket;
		this.cmdParser = cmdParser;
		this.br = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
		this.ps = new PrintStream( socket.getOutputStream(), true );
	}
	
	/**
	 * Receive command a command and parse it right away.
	 * 
	 * @return the command, after it has been parsed by the given command parser
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Command receiveCommand() throws IOException
	{
		//Read a line
		String input = br.readLine();

		if( input == null )
			return null;
		
		try{
			
			//Parse the command right away
			Command cmd = cmdParser.parse( input );
			
			if( cmd == null )
			{
				ps.println( "Unknown command: " + input );
			}else
			
			return cmd;
		}catch( ParseException pex )
		{
			ps.println( pex.getMessage() );
		}catch( ValidationException vex )
		{
			ps.println( vex.getMessage() );
		}
		
		return null;
	}
	
	/**
	 * Receive a line from the socket.
	 * 
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String receiveLine() throws IOException
	{
		return br.readLine();
	}
	
	/**
	 * Send a positive acknowledge-message.
	 */
	public void sendAck()
	{
		this.sendLine( STR_ACK );
	}
	
	/**
	 * Send a negative acknowledge-message.
	 */
	public void sendNack()
	{
		this.sendLine( STR_NACK );
	}
	
	/**
	 * Receive an acknowledge message.
	 * 
	 * @return true, if a positive acknowledge-message is received. Otherwise, false.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean receiveAck() throws IOException
	{
		String str = this.receiveLine();
		return STR_ACK.equals( str );
	}
	
	/**
	 * Send line of text through the socket.
	 * 
	 * @param str
	 *            the string to send
	 */
	public void sendLine( String str )
	{
		this.ps.println( str );
		this.ps.flush();
	}
	
	/**
	 * Closes the underlying socket connection.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void close() throws IOException
	{
		this.socket.close();
	}
	
	/**
	 * Forwards all incoming data from this TcpHelper to the given TcpHelper.
	 * This method blocks, until the underlying socket is closed.
	 * 
	 * @param receiver
	 *            the receiver
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void tunnelTo( TcpHelper receiver ) throws IOException
	{
		String input = null;
		
		for(;;)
		{
			try
			{
				input = this.receiveLine();
				
				if( input == null )
					break;
				
				receiver.sendLine( input );
			}catch( SocketTimeoutException stex )
			{ continue; }
		}
	}
	
	public void sendFile( File f )
	{		
		if( !f.exists() )
		{
			this.sendLine( "The requested file does not exist." );
			return;
		}
		
		if( !f.canRead() )
		{
			this.sendLine( "The requested file could not be read." );
			return;
		}
		
		//Send file size, so the proxy can check, wether the user has enough credits to download the file
		this.sendLine( "File size: " + f.length() );
		
		try{
			//Wait for a positive answer
			if( this.receiveAck() )
			{
				FileInputStream fis = new FileInputStream(f);
				
				byte[] buf = new byte[BUFFER_SIZE];
				int read;
				while( (read=fis.read( buf )) != -1 )
				{
					this.ps.write( buf, 0, read );
				}
			}else
				return;
		}catch( FileNotFoundException fnfex )
		{
			this.sendLine( "The requested file does not exist." );
			return;
		}catch( IOException ioex )
		{
			this.sendLine( "The requested file could not be read (" + ioex.getMessage() + ")" );
			return;
		}
	}
	
	public void receiveFile( File output, long size ) throws IOException
	{
		output.createNewFile();
	
		PrintStream ps = new PrintStream( output );
		InputStream is = this.socket.getInputStream();
		
		byte[] buf = new byte[BUFFER_SIZE];
		int read;
		while( output.length() < size )
		{
			//Read line by line and print it to the file
			read = is.read( buf, 0, buf.length );
			ps.write( buf, 0, read );
			ps.flush();		//Ensure that the new line gets written to the disk, so output.length() gets updated
		}
		
		//Close the output file
		ps.close();
	}
}