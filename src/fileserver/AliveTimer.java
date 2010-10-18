package fileserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class AliveTimer extends TimerTask {

	protected static final Logger logger = Logger.getLogger( AliveTimer.class.getName() );
	
	Timer timer;
	int alivePeriod;
	int tcpPort;
	DatagramPacket packet;

	private DatagramSocket socket;
	
	public AliveTimer( int alivePeriod, int tcpPort, String proxyHost, int proxyPort ) throws UnknownHostException
	{
		this.timer = new Timer();
		this.alivePeriod = alivePeriod;
		
		this.packet = new DatagramPacket( this.intToBytes(tcpPort), 4 );
		this.packet.setAddress( InetAddress.getByName(proxyHost) );
		this.packet.setPort( proxyPort );
	}
	
	public void start() throws SocketException
	{
		this.socket = new DatagramSocket( tcpPort );
		this.timer.schedule( this, 0, alivePeriod );
	}
	
	public void stop()
	{
		this.timer.cancel();
		this.socket.close();
	}

	@Override
	public void run() {
		
		try
		{
			socket.send( packet );	
		}catch( IOException ioex )
		{
			logger.warning( "Couldn't send alive-packet: " + ioex.getMessage() );
		}
	}
	
	protected byte[] intToBytes( int value )
	{
		return new byte[] {
							(byte)(value >>> 24),
							(byte)(value >>> 16),
							(byte)(value >>> 8),
							(byte)value
		    			   };
	}
	
}
