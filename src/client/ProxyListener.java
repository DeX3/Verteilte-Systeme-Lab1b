package client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import threading.DefaultRunnable;

import net.TcpHelper;

/**
 * Class for listening to the proxy as client.
 * Sets up a permanent tcp-connection to the proxy 
 * and listens for data.
 */
public class ProxyListener extends DefaultRunnable {
	
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
		this.th.start();
		
		return proxy;
	}

	/**
	 * Stops listening.
	 */
	public Throwable stop() {
		this.stopping = true;
		
		try {
			this.th.join();
		} catch (InterruptedException e) {	}
		
		return this.throwable;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void runSafe() throws IOException {
		
		//Pattern used for file downloads
		Pattern pFile = Pattern.compile( "Begin file: (\\d+) (.*)" );
		
		while( !stopping )
		{
			try{
				String input = proxy.receiveLine();
				
				if( input == null )			//If the proxy has closed the connection
				{
					throw new IOException( "Connection to proxy closed by proxy" );
				}else
				{
					Matcher m = pFile.matcher( input );
			
					//Check if the proxy wants to signal a file transfer
					if( m.matches() )
					{
						//The proxy is going to send a file
						
						//Parse file name and size
						String filename = downloadDir.getAbsolutePath() + "/" + m.group(2);
						long size = Long.parseLong( m.group(1) );
						
						proxy.receiveFile( new File(filename), size );
						
						System.out.println( "Successfully downloaded \"" + filename + "\"" );
					}else
						System.out.println( input );	//Else, proxy just sends a message => print it
				}
			}catch( SocketTimeoutException stex )
			{ }
		}
		
		//After listening is stopped, proxy connection can be closed
		proxy.close();
	}
	
	public boolean isRunning()
	{
		if( th == null )
			return false;
		return th.isAlive();
	}
	
	@Override
	public void throwIfError() throws IOException
	{
		if( this.throwable != null )
			throw (IOException)this.throwable;
	}
	
}
