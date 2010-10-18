package fileserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import threading.ThreadPool;

import cmd.Command;
import cmd.CommandLineParser;
import cmd.CommandParser;
import cmd.CustomParameter;
import cmd.IntegerParameter;
import cmd.StringParameter;
import cmd.Validator;
import exceptions.ParseException;
import exceptions.ValidationException;

public class Fileserver {
	
	protected static final Logger logger = Logger.getLogger( Fileserver.class.getName() );
	
	public static final int MIN_PORT = 1025;
	public static final int MAX_PORT = 65536;
	
	protected static final CommandLineParser cmdLineParser;
	protected static final CustomParameter PRM_SHAREDFILESDIR;
	protected static final IntegerParameter PRM_TCPPORT;
	protected static final StringParameter PRM_PROXYHOST;
	protected static final IntegerParameter PRM_PROXYUDPPORT;
	protected static final IntegerParameter PRM_ALIVEPERIOD;
	
	protected static final CommandParser cmdParser;
	protected static final Command CMD_EXIT;
	
	static{
		cmdLineParser = new CommandLineParser( "java fileserver.Fileserver", "Proxy for the first lab of Distributed Systems LU" );
		
		PRM_SHAREDFILESDIR = new CustomParameter( "sharedFilesDir",
												  "the directory that contains all the files clients can download.",
												  new Validator() {
													@Override
													public void validate( String value ) throws ValidationException {
														File f = new File( value );
														
														if( !f.isDirectory() || !f.exists() || !f.canRead() )
															throw new ValidationException( "The given path is not a valid directory." );
													}
												  } );
		
		PRM_TCPPORT = new IntegerParameter( "tcpPort", MIN_PORT, MAX_PORT, "the port to be used for instantiating a ServerSocket (handling the TCP requests from the Proxy)." );
		PRM_PROXYHOST = new StringParameter( "proxyHost", "the host name (or an IP address) where the Proxy is running." );
		PRM_PROXYUDPPORT = new IntegerParameter( "udpPort", MIN_PORT, MAX_PORT, "the UDP port where the Proxy is listening for fileserver datagrams." );
		PRM_ALIVEPERIOD = new IntegerParameter( "alivePeriod", 0, Integer.MAX_VALUE, "the period in ms the fileserver needs to send an isAlive datagram to the Proxy." );
		
		cmdLineParser.addParameters( PRM_SHAREDFILESDIR, PRM_TCPPORT, PRM_PROXYHOST, PRM_PROXYUDPPORT, PRM_ALIVEPERIOD );
		
		cmdParser = new CommandParser();
		CMD_EXIT = new Command( "exit" );
		cmdParser.addCommand( CMD_EXIT );
	}


	private File shareDir;

	private int tcpPort;

	private String proxyHost;

	private int proxyUdpPort;

	private int alivePeriod;

	private FileTcpServer fileserver;

	private AliveTimer timer;
	
	public Fileserver( File shareDir, int tcpPort, String proxyHost, int proxyUdpPort, int alivePeriod )
	{
		this.shareDir = shareDir;
		this.tcpPort = tcpPort;
		this.proxyHost = proxyHost;
		this.proxyUdpPort = proxyUdpPort;
		this.alivePeriod = alivePeriod;
	}
	
	public static void main( String...args )
	{
		logger.info( "Fileserver starting up..." );
		
		parseArgs( args );
		
		Fileserver srv = new Fileserver( new File( PRM_SHAREDFILESDIR.getValue() ),
										 PRM_TCPPORT.getValue(),
										 PRM_PROXYHOST.getValue(),
										 PRM_PROXYUDPPORT.getValue(),
										 PRM_ALIVEPERIOD.getValue() );
		
		try
		{
			srv.start();
		}catch( UnknownHostException uhex )
		{
			logger.severe( uhex.getMessage() );
			System.exit( 1 );
		}catch( SocketException ex )
		{
			logger.severe( "Couldn't initialize alve-timer: " + ex.getMessage() );
		}
		

		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		Command cmd = null;
		
		while( cmd != CMD_EXIT )
		{
			try{
				String input = br.readLine();
				if( input == null )
					break;
				
				cmd = cmdParser.parse( input );
				if( cmd == null )
				{
					System.out.println( "Unknown command: \"" + input + "\"" );
				}
			}catch( IOException ioex )
			{ logger.warning( "Couldn't read from stdin" ); }
			catch( ParseException pex )
			{ System.out.println( pex.getMessage() ); }
			catch( ValidationException vex )
			{ assert false : "Validation exception in non-validating command"; }
		}
		
		logger.info( "Shutting down..." );

		srv.stop();
	}
	
	
	private void stop() {
		this.fileserver.stop();
		this.timer.stop();
		ThreadPool.getPool().shutdown();
	}

	private void start() throws UnknownHostException, SocketException {
		
		this.fileserver = new FileTcpServer( this.tcpPort, this.shareDir );
		ThreadPool.getPool().execute( this.fileserver );
		
		
		
		this.timer = new AliveTimer( this.alivePeriod,
									 this.tcpPort,
									 this.proxyHost,
									 this.proxyUdpPort );
		this.timer.start();
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
}
