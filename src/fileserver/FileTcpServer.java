package fileserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

import proxy.TcpServer;
import threading.ThreadPool;
import entities.User;
import exceptions.ServerException;

public class FileTcpServer implements Runnable {

	int port;
	ConcurrentHashMap<String, User> users;
	File shareDir;
	
	
	protected boolean stopping;
	protected static final int SO_TIMEOUT = 100;
	protected static final Logger logger = Logger.getLogger( TcpServer.class.getName() );
	
	public boolean isStopping()
	{ return this.stopping; }
	
	public File getShareDir()
	{ return this.shareDir; }
	
	public FileTcpServer( int port, File shareDir )
	{
		this.port = port;
		stopping = false;
		this.shareDir = shareDir;
	}
	
	@Override
	public void run() {
		
		try{
			ServerSocket srv = new ServerSocket( this.port );
			srv.setSoTimeout( SO_TIMEOUT );
			
			while( !this.stopping )
			try{
				Socket proxy = srv.accept();
				proxy.setSoTimeout( 100 );
				logger.info( "Connection from proxy: " + proxy );
				
				FileTcpHandler tcpHandler = new FileTcpHandler( proxy, this );
				
				ThreadPool.getPool().execute( tcpHandler );
			}catch( SocketTimeoutException stex )
			{ continue; }
			catch( RejectedExecutionException reex )
			{ break; }
			
		}catch( IOException ioex )
		{ throw new ServerException( ioex ); }
	}
	
	public void stop()
	{
		this.stopping = true;
	}	
}
