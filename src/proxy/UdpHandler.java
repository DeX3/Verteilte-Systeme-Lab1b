package proxy;

import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import entities.FileserverInfo;
import entities.IPEndPoint;


public class UdpHandler implements Runnable {

	DatagramPacket packet;
	ConcurrentHashMap<IPEndPoint, FileserverInfo> fileservers;

	protected static final Logger logger = Logger.getLogger( UdpHandler.class.getName() );
	
	
	public UdpHandler( DatagramPacket packet, ConcurrentHashMap<IPEndPoint, FileserverInfo> fileservers )
	{
		this.packet = packet;
		this.fileservers = fileservers;
	}
	
	@Override
	public void run() {
		
		int tcpPort = bytesToInt( packet.getData() );
		
		IPEndPoint server = new IPEndPoint( packet.getAddress(), tcpPort );
		
		FileserverInfo old = this.fileservers.put( server, new FileserverInfo( server ) );
		
		if( old == null )
			logger.info( "New server registered (" + server.toString() + ")" );
	}

	protected int bytesToInt( byte[] b )
	{
		return	(b[0] << 24) +
				((b[1] & 0xFF) << 16) +
				((b[2] & 0xFF) << 8) +
				(b[3] & 0xFF);
	}
	
}
