package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import net.TcpHelper;
import cmd.CommandLineParser;
import cmd.CustomParameter;
import cmd.IntegerParameter;
import cmd.StringParameter;
import cmd.Validator;
import exceptions.ParseException;
import exceptions.ValidationException;

/**
 * Main class for the client, contains a main method that starts the client.
 */
public class Client {
	
	/** logger */
	public static final Logger logger = Logger.getLogger( Client.class.getName() );

	/** Minimum allowed port. */
	public static final int MIN_PORT = 1025;
	
	/** Maximum allowed port. */
	public static final int MAX_PORT = 65536;
	
	/* Command-line parser commands. */
	protected static final CommandLineParser cmdLineParser;
	protected static final CustomParameter PRM_DOWNLOADDIR;
	protected static final StringParameter PRM_PROXYHOST;
	protected static final IntegerParameter PRM_PROXYTCPPORT;
	
	/* Initializer for command line parser */
	static{
		cmdLineParser = new CommandLineParser( "java client.Client", "Client for the first lab of Distributed Systems LU" );
		
		PRM_DOWNLOADDIR = new CustomParameter( "downloadDir", "the directory to put downloaded files",
											   new Validator() {
													@Override
													public void validate( String value ) throws ValidationException {
														File f = new File( value );
														
														if( !f.isDirectory() || !f.exists() || !f.canRead() )
															throw new ValidationException( "The given path is not a valid directory." );
													}
		  									   } );
		PRM_PROXYHOST = new StringParameter( "proxyHost", "the host name (or an IP address) where the Proxy is running." );
		PRM_PROXYTCPPORT = new IntegerParameter( "proxyTCPPort", MIN_PORT, MAX_PORT, "the TCP port where the server is listening for client connections." );
		
		cmdLineParser.addParameters( PRM_DOWNLOADDIR, PRM_PROXYHOST, PRM_PROXYTCPPORT );
	}

	/**
	 * The main method for the client.
	 * 
	 * Parses the arguments, initiates the connection to the proxy,
	 * and then listens for commands on stdin
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main( String...args )
	{	
		try{
			cmdLineParser.parse( args );
			
		}catch( ParseException pex )
		{
			logger.severe( "Command parse error: " + pex.getMessage() );
			System.out.println( cmdLineParser.getUsageString() );
			return;
		}catch( ValidationException vex )
		{
			logger.severe( "Parameter validation error: " + vex.getMessage() );
			System.out.println( cmdLineParser.getUsageString() );
			return;
		}
		
		
		ProxyListener proxy = null;
		TcpHelper tcpProxy = null;
		
		try
		{
			//Create and start the ProxyListener
			proxy = new ProxyListener( new File( PRM_DOWNLOADDIR.getValue() ),
									   PRM_PROXYHOST.getValue(),
									   PRM_PROXYTCPPORT.getValue() );
			tcpProxy = proxy.start();
		}catch( IOException ioex )
		{
			logger.severe( "Couldn't initiate connection to proxy" );
			System.exit( 1 );
		}
		
		System.out.println( "Please login to the system using !login <username> <password> (!exit to quit)" );
		
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		String input = null;
		
		
		//Read commands until the users quits using the exit-command
		for(;;)
		{
			try
			{
				input = br.readLine();
				
				tcpProxy.sendLine( input );
				
				if( "!exit".equals( input ) )
					break;
				
			}catch( IOException ioex )
			{ logger.severe( "Couldn't read from stdin" ); }
		}
		
		System.out.println( "Shutting down..." );
		proxy.stop();		//Stop the proxy again
	}
}
