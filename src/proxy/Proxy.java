package proxy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cmd.CommandLineParser;
import cmd.IntegerParameter;
import entities.User;
import exceptions.ParseException;
import exceptions.ValidationException;


public class Proxy {
	
	int tcpPort,
		udpPort,
		fileserverTimeout,
		checkPeriod;
	
	
	/* Constants */
	public static final int MIN_PORT = 1025;
	public static final int MAX_PORT = 65536;
	
	public static final int MIN_TIMEOUT = 0;
	public static final int MAX_TIMEOUT = Integer.MAX_VALUE;
	
	public static final int MIN_CHECKPERIOD = 0;
	public static final int MAX_CHECKPERIOD = Integer.MAX_VALUE;
	
	public static final String PROPERTIES_FILE = "user.properties";
	
	protected static final Logger logger = Logger.getLogger( Proxy.class.getName() );
	/* End constants */
	
	ConcurrentHashMap<String, User> users;
	ConcurrentHashMap<String, User> loggedIn;
	
	public Proxy( int tcpPort, int udpPort, int fileserverTimeout, int checkPeriod, ConcurrentHashMap<String, User> users )
	{
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.fileserverTimeout = fileserverTimeout;
		this.checkPeriod = checkPeriod;

		this.users = new ConcurrentHashMap<String, User>();
		this.loggedIn = new ConcurrentHashMap<String, User>();
	}
	
	public static void main( String...args )
	{
		logger.info( "Proxy starting up" );
		
		CommandLineParser clp = new CommandLineParser( "java Proxy", "Proxy for the first lab of Distributed Systems LU" );
		
		clp.addParameter( new IntegerParameter( "tcpPort", MIN_PORT, MAX_PORT, "the port to be used for instantiating a java.net.ServerSocket (handling TCP connection requests from clients)." ) );
		clp.addParameter( new IntegerParameter( "udpPort", MIN_PORT, MAX_PORT, "the port to be used for instantiating a java.net.DatagramSocket (handling UDP requests from fileservers)." ) );
		clp.addParameter( new IntegerParameter( "fileserverTimeout", MIN_TIMEOUT, MAX_TIMEOUT, "the period in milliseconds each fileserver has to send an isAlive packet (only containing the fileserver's TCP port). If no such packet is received within this time, the fileserver is assumed to be offline and is no longer available for handling requests." ) );
		clp.addParameter( new IntegerParameter( "checkPeriod", MIN_CHECKPERIOD, MAX_CHECKPERIOD, "specifies that the test whether a fileserver has timed-out or not (see fileserverTimeout) is repeated every checkPeriod milliseconds." ) );
		
		try
		{
			clp.parse( args );
		}catch( ParseException pex )
		{
			logger.severe( "Command parse error: " + pex.getMessage() );
			System.out.println( clp.getUsageString() );
			return;
		}
		catch( ValidationException vex )
		{
			logger.severe( "Parameter validation error: " + vex.getMessage() );
			System.out.println( clp.getUsageString() );
			return;
		}
		
		logger.info( "Reading properties file \"" + PROPERTIES_FILE + "\"" );
		HashMap<String,User> users = null;
		
		try{
			users = User.readUsers( PROPERTIES_FILE );
		}catch( FileNotFoundException fnfex )
		{
			logger.severe( "The file \"" + PROPERTIES_FILE + "\" could not be found" );
			return;
		}catch( IOException ioex )
		{
			logger.severe( "The file \"" + PROPERTIES_FILE + "\" could not be read" );
			return;
		}catch( ParseException pex )
		{
			logger.severe( "Couldn't parse properties file: " + pex.getMessage() );
			return;
		}

		logger.info( "Done reading " + users.size() + " user entries" );
		
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		
		String input = null;
		
		for(;;)
		{
			try
			{
				System.out.println( "Enter command (!fileservers, !users or !exit):" );
				input = br.readLine();
				
				if( "!fileservers".equalsIgnoreCase( input ) )
				{
					
				}else if( "!users".equalsIgnoreCase( input ) )
				{
					
				}else if( "!exit".equalsIgnoreCase( input ) )
					break;
				else
					System.out.println( "Unknown command: \"" + input + "\"" );
				
			}catch( IOException ioex )
			{ logger.severe( "Couldn't read from stdin" ); }
			
		}
		
		logger.info( "Shutting down" );
	}
	
	public void start()
	{
		ThreadPool.getPool().execute( new TcpServer( this.tcpPort ) );
		
	}
	
}
