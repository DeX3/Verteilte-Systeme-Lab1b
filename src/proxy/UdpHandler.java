package proxy;

import java.net.DatagramPacket;

public class UdpHandler implements Runnable {

	DatagramPacket packet;
	
	public UdpHandler( DatagramPacket packet )
	{
		this.packet = packet;
	}
	
	@Override
	public void run() {
		
		
		
	}

}
