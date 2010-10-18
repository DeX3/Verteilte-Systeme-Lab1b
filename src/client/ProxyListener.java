package client;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.TcpHelper;

/**
 * Class for listening to the proxy as client.
 * Sets up a permanent tcp-connection to the proxy 
 * and listens for data.
 */
public class ProxyListener implements Runnable {
	
	/** The Constant logger. */
	protected static final Logger logger = Logger.getLogger( ProxyListener.class.getName() );
	
	/** The Constant SO_TIMEOUT. */
	protected static final int SO_TIMEOUT = 100;
	
	
	/** Hostname of the proxy */
	String proxyHost;
	
	/** TCP-Port of the proxy. */
	int proxyTCPPort;
	
	/** Socket, created for communicating to the proxy */
	Socket sProxy;
	
	/** TcpHelper, created for communicating to the proxy */
	TcpHelper proxy;	
	
	/** thread for running the listener. */
	Thread th;
	
	/** Set to true, if listening is to be stopped */
	boolean stopping;
	
	/** directory for file downloads */
	File downloadDir;
	
	/**
	 * Instantiates a new proxy listener.
	 * 
	 * @param downloadDir
	 *            the directory for file downloads
	 * @param proxyHost
	 *            the proxy host
	 * @param proxyTCPPort
	 *            the proxy tcp port
	 */
	public ProxyListener( File downloadDir, String proxyHost, int proxyTCPPort) {
		this.downloadDir = downloadDir;
		this.proxyHost = proxyHost;
		this.proxyTCPPort = proxyTCPPort;
		this.th = null;
	}
	
	/**
	 * Opens the connection to the proxy and starts listening.
	 * 
	 * @return A TcpHelper object that can be used to communicate with the proxy
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public TcpHelper start() throws IOException
	{
		sProxy = new Socket( this.proxyHost, this.proxyTCPPort );
		sProxy.setSoTimeout( SO_TIMEOUT );
		
		proxy = new TcpHelper( sProxy );
		
		this.th = new Thread( this );
		this.stopping = false;
		this.th.start();
		
		return proxy;
	}

	/**
	 * Stops listening.
	 */
	public void stop() {
		this.stopping = true;
		
		try {
			this.th.join();
		} catch (InterruptedException e) {	}
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		//Pattern used for file downloads
		Pattern pFile = Pattern.compile( "Begin file: (\\d+) (.*)" );
		
		try{
			while( !stopping )
			{
				try{
					String input = proxy.receiveLine();
					
					if( input == null )			//If the proxy has closed the connection
					{
						System.out.println( "Connection to proxy closed by proxy" );
						System.exit( 0 );
					}else
					{
						Matcher m = pFile.matcher( input );
					
						//Check if the proxy wants to signal a file transfer
						if( m.matches() )
						{
							//The proxy is going to send a file line-by-line
							
							//Parse file name and size
							String filename = downloadDir.getAbsolutePath() + "/" + m.group(2);
							long size = Long.parseLong( m.group(1) );
							
							//Create the output file
							File output = new File( filename );
							PrintStream ps = new PrintStream( output );
							
							String line;
							while( output.length() < size )
							{
								//Read line by line and print it to the file
								line = proxy.receiveLine();
								ps.println( line );
								ps.flush();		//Ensure that the new line gets written to the disk, so output.length() gets updated
							}
							
							//Close the output file
							ps.close();
							
							System.out.println( "Successfully downloaded \"" + filename + "\"" );
						}else
							System.out.println( input );	//Else, proxy just sends a message => print it
					}
				}catch( SocketTimeoutException stex )
				{ }
			}
			
			//After listening is stopped, proxy connection can be closed
			proxy.close();
			
		}catch( IOException ioex )
		{
			logger.severe( "Unable to listen for proxy connections: " + ioex.getMessage() );
			return;
		}
		
	}
	
}
