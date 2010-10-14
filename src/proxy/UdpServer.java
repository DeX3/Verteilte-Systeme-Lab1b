package proxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import exceptions.ServerException;

public class UdpServer implements Runnable {
	
	int port;
	boolean stopping;
	
	protected static final int SO_TIMEOUT = 100;
	
	public static final Logger logger = Logger.getLogger( UdpServer.class.getName() );
	
	public UdpServer( int port )
	{
		this.port = port;
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
			
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket( buf , buf.length );
		
		while( !stopping )
		{
			try{
				
				
				try
				{
					srv.receive( packet );
				}
				catch ( SocketTimeoutException stex )
				{ continue; }
				
			}catch( IOException e )
			{ 
				logger.warning( "Couldn't receive datagram" );
			}
		}
		
	}
	
	
	
}
