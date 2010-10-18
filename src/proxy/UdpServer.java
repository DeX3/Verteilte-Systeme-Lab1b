package proxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

import threading.ThreadPool;
import entities.FileserverInfo;
import entities.IPEndPoint;
import exceptions.ServerException;

public class UdpServer implements Runnable {
	
	int port;
	boolean stopping;
	private ConcurrentHashMap<IPEndPoint, FileserverInfo> fileservers;
	
	
	protected static final int SO_TIMEOUT = 100;
	
	public static final Logger logger = Logger.getLogger( UdpServer.class.getName() );
	
	public UdpServer( int port, ConcurrentHashMap<IPEndPoint, FileserverInfo> fileservers )
	{
		this.port = port;
		this.fileservers = fileservers;
		stopping = false;
	}

	@Override
	public void run() {
		
		DatagramSocket srv = null;
		try{
			srv = new DatagramSocket( this.port );
			srv.setSoTimeout( SO_TIMEOUT );
		}catch( IOException ioex )
		{ throw new ServerException( ioex ); }
			
		byte[] buf = new byte[4];
		DatagramPacket packet = new DatagramPacket( buf , buf.length );
		
		while( !stopping )
		{
			try{
				try
				{
					srv.receive( packet );
					
					UdpHandler handler = new UdpHandler( packet, this.fileservers );
					
					ThreadPool.getPool().execute( handler );
				}
				catch ( SocketTimeoutException stex )
				{ continue; }
				catch( RejectedExecutionException reex )
				{ break; }
				
			}catch( IOException e )
			{ 
				logger.warning( "Couldn't receive datagram" );
			}
		}
		
	}

	public void stop() {
		this.stopping = true;
	}
	
	
	
}
