package proxy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import threading.ThreadPool;
import cmd.Command;
import cmd.CommandLineParser;
import cmd.CommandParser;
import cmd.IntegerParameter;
import entities.User;
import exceptions.ParseException;
import exceptions.ValidationException;


public class Proxy {
	
	int tcpPort,
		udpPort,
		fileserverTimeout,
		checkPeriod;
	
	TcpServer tcpServer = null;
	
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
	
	protected final static CommandLineParser cmdLineParser;
	protected final static IntegerParameter PRM_TCPPORT;
	protected final static IntegerParameter PRM_UDPPORT;
	protected final static IntegerParameter PRM_FILESERVERTIMEOUT;
	protected final static IntegerParameter PRM_CHECKPERIOD;
	
	
	protected final static CommandParser cmdProxy;
	protected final static Command CMD_FILESERVERS;
	protected final static Command CMD_USERS;
	protected final static Command CMD_EXIT;
	
	
	
	static{
		cmdLineParser = new CommandLineParser(  "java Proxy", "Proxy for the first lab of Distributed Systems LU" );
		PRM_TCPPORT = new IntegerParameter( "tcpPort", MIN_PORT, MAX_PORT, "the port to be used for instantiating a java.net.ServerSocket (handling TCP connection requests from clients)." );
		PRM_UDPPORT = new IntegerParameter( "udpPort", MIN_PORT, MAX_PORT, "the port to be used for instantiating a java.net.DatagramSocket (handling UDP requests from fileservers)." );
		PRM_FILESERVERTIMEOUT = new IntegerParameter( "fileserverTimeout", MIN_TIMEOUT, MAX_TIMEOUT, "the period in milliseconds each fileserver has to send an isAlive packet (only containing the fileserver's TCP port). If no such packet is received within this time, the fileserver is assumed to be offline and is no longer available for handling requests." );
		PRM_CHECKPERIOD = new IntegerParameter( "checkPeriod", MIN_CHECKPERIOD, MAX_CHECKPERIOD, "specifies that the test whether a fileserver has timed-out or not (see fileserverTimeout) is repeated every checkPeriod milliseconds." );
		cmdLineParser.addParameters( PRM_TCPPORT, PRM_UDPPORT, PRM_FILESERVERTIMEOUT, PRM_CHECKPERIOD );
		
		
		cmdProxy = new CommandParser();
		cmdProxy.addCommand( CMD_FILESERVERS = new Command( "fileservers") );
		cmdProxy.addCommand( CMD_USERS = new Command( "users" ) );
		cmdProxy.addCommand( CMD_EXIT = new Command( "exit") );
	}
	
	ConcurrentHashMap<String, User> users;
	ConcurrentHashMap<String, User> loggedIn;
	
	public Proxy( int tcpPort, int udpPort, int fileserverTimeout, int checkPeriod, ConcurrentHashMap<String, User> users )
	{
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		this.fileserverTimeout = fileserverTimeout;
		this.checkPeriod = checkPeriod;

		this.users = users;
		this.loggedIn = new ConcurrentHashMap<String, User>();
	}
	
	public static void main( String...args )
	{
		logger.info( "Proxy starting up" );
		parseArgs( args );
		
		
		logger.info( "Reading properties file \"" + PROPERTIES_FILE + "\"" );
		
		ConcurrentHashMap<String,User> users = readUsers();
		logger.info( "Done reading " + users.size() + " user entries" );
		
		Proxy p = new Proxy( PRM_TCPPORT.getValue(), PRM_UDPPORT.getValue(), PRM_FILESERVERTIMEOUT.getValue(), PRM_CHECKPERIOD.getValue(), users );
		p.start();
		
		System.out.println( "Available commands are !fileservers, !users and !exit" );
		
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		String input = null;
		
		for(;;)
		{
			try
			{
				input = br.readLine();
				
				try{
					Command cmd = cmdProxy.parse( input );
					
					if( cmd == null )
					{
						System.out.println( "Unknown command: \"" + input + "\"" );
					}else if( cmd == CMD_USERS )
					{
						for( String name : users.keySet() )
						{
							User u = users.get( name );
							System.out.println( name + " " + (u.isOnline()? "online" : "offline") + " Credits: " + u.getCredits() );
						}
					}
					else if( cmd == CMD_EXIT )
						break;
					
				}catch( ParseException pex )
				{
					System.out.println( pex.getMessage() );
				}catch( ValidationException vex )
				{ assert false : "Validation exception in non-validating command"; }
				
			}catch( IOException ioex )
			{ logger.severe( "Couldn't read from stdin" ); }
			
		}
		
		logger.info( "Shutting down" );
		p.stop();
	}
	
	private static void parseArgs( String...args )
	{
		try
		{
			cmdLineParser.parse( args );
		}catch( ParseException pex )
		{
			logger.severe( "Command parse error: " + pex.getMessage() );
			System.out.println( cmdLineParser.getUsageString() );
			System.exit( 1 );
		}
		catch( ValidationException vex )
		{
			logger.severe( "Parameter validation error: " + vex.getMessage() );
			System.out.println( cmdLineParser.getUsageString() );
			System.exit( 1 );
		}
	}
	
	private static ConcurrentHashMap<String, User> readUsers()
	{
		
		try{
			ConcurrentHashMap<String, User> users = null;
			users = User.readUsers( PROPERTIES_FILE );
			
			return users;
		}catch( FileNotFoundException fnfex )
		{
			logger.severe( "The file \"" + PROPERTIES_FILE + "\" could not be found" );
			System.exit( 1 );
		}catch( IOException ioex )
		{
			logger.severe( "The file \"" + PROPERTIES_FILE + "\" could not be read" );
			System.exit( 1 );
		}catch( ParseException pex )
		{
			logger.severe( "Couldn't parse properties file: " + pex.getMessage() );
			System.exit( 1 );
		}
		
		return null;
	}
	
	private void stop() {
		if( tcpServer != null )
			tcpServer.stop();
		
		ThreadPool.getPool().shutdown();
	}

	
	public void start()
	{
		logger.info( "Started proxy at ports tcp: " + this.tcpPort + ", udp: " + this.udpPort );
		
		this.tcpServer = new TcpServer( this.tcpPort, this.users );
		ThreadPool.getPool().execute( this.tcpServer );
	}
	
}
