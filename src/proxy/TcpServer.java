package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import threading.Stoppable;
import threading.ThreadPool;
import entities.User;
import exceptions.ServerException;

public class TcpServer implements Stoppable {

	int port;
	ConcurrentHashMap<String, User> users;
	
	protected boolean stopping;
	protected static final int SO_TIMEOUT = 100;
	protected static final Logger logger = Logger.getLogger( TcpServer.class.getName() );
	
	public boolean isStopping()
	{ return this.stopping; }
	
	public ConcurrentHashMap<String, User> getUsers()
	{ return this.users; }
	
	public TcpServer( int port, ConcurrentHashMap<String, User> users )
	{
		this.port = port;
		stopping = false;
		this.users = users;
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
				//this.activeClients.add( tcpHandler );
				ThreadPool.getPool().execute( tcpHandler );
			}catch( SocketTimeoutException stex )
			{ continue; }
			
		}catch( IOException ioex )
		{ throw new ServerException( ioex ); }
	}
	
	public void stop()
	{
		this.stopping = true;
	}

}
