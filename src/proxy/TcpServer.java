package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

import threading.ThreadPool;
import entities.FileserverInfo;
import entities.IPEndPoint;
import entities.User;
import exceptions.ServerException;

public class TcpServer implements Runnable {

	int port;
	ConcurrentHashMap<String, User> users;
	
	protected boolean stopping;
	private ConcurrentHashMap<IPEndPoint, FileserverInfo> fileservers;
	protected static final int SO_TIMEOUT = 100;
	protected static final Logger logger = Logger.getLogger( TcpServer.class.getName() );
	
	public boolean isStopping()
	{ return this.stopping; }
	
	public ConcurrentHashMap<String, User> getUsers()
	{ return this.users; }
	
	public TcpServer( int port,
					  ConcurrentHashMap<String, User> users,
					  ConcurrentHashMap<IPEndPoint, FileserverInfo> fileservers )
	{
		this.port = port;
		stopping = false;
		this.users = users;
		this.fileservers = fileservers;
	}
	
	@Override
	public void run() {
		
		try{
			ServerSocket srv = new ServerSocket( this.port );
			srv.setSoTimeout( SO_TIMEOUT );
			
			while( !this.stopping )
			try{
				Socket client = srv.accept();
				client.setSoTimeout( 100 );
				logger.info( "Client connected: " + client );
				
				TcpHandler tcpHandler = new TcpHandler( client, this );
				
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

	public ConcurrentHashMap<IPEndPoint, FileserverInfo> getFileservers() {
		return this.fileservers;
	}

}
