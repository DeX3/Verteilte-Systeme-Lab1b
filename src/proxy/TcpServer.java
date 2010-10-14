package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import exceptions.ServerException;
import exceptions.TcpHandler;

public class TcpServer implements Runnable {

	int port;
	boolean stopping;
	
	protected static final int SO_TIMEOUT = 100;
	
	public TcpServer( int port )
	{
		this.port = port;
		stopping = false;
	}
	
	@Override
	public void run() {
		
		try{
			ServerSocket srv = new ServerSocket( this.port );
			srv.setSoTimeout( SO_TIMEOUT );
			
			while( !stopping )
			try{
				Socket client = srv.accept();
				
				ThreadPool.getPool().execute( new TcpHandler( client ) );
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
