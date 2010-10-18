package entities;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import net.TcpHelper;

/**
 * Holds some meta-information about a specific fileserver
 */
public class FileserverInfo {
	
	/** The end point containing ip+port of the fileserver. */
	IPEndPoint endPoint;
	
	/** The usage of this fileserver. */
	AtomicLong usage;
	
	/** true if the fileserver is currently online. */
	AtomicBoolean online;
	
	/** Specifies the time, the fileserver has last been active. */
	AtomicLong lastActive;
	
	/**
	 * Gets the end point.
	 * 
	 * @return the end point
	 */
	public IPEndPoint getEndPoint() {
		return endPoint;
	}
	
	/**
	 * Sets the end point.
	 * 
	 * @param endPoint
	 *            the new end point
	 */
	public void setEndPoint(IPEndPoint endPoint) {
		this.endPoint = endPoint;
	}
	
	/**
	 * Gets the usage.
	 * 
	 * @return the usage
	 */
	public long getUsage() {
		return usage.get();
	}
	
	/**
	 * Sets the usage.
	 * 
	 * @param usage
	 *            the new usage
	 */
	public void setUsage(AtomicLong usage) {
		this.usage = usage;
	}
	
	/**
	 * Gets the last active.
	 * 
	 * @return the last active
	 */
	public long getLastActive() {
		return lastActive.get();
	}
	
	/**
	 * Adds the given amount of credits to the servers usage and returns
	 * the new usage
	 * 
	 * @param credits
	 *            the credits
	 * @return the new usage
	 */
	public long use( long credits )
	{
		return this.usage.addAndGet( credits );
	}
	
	/**
	 * Instantiates a new fileserver info.
	 * 
	 * @param endPoint
	 *            the end point
	 */
	public FileserverInfo( IPEndPoint endPoint ) {
		this.endPoint = endPoint;
		this.usage = new AtomicLong( 0 );
		this.online = new AtomicBoolean( true );
		this.lastActive = new AtomicLong( System.currentTimeMillis() );
	}
	
	/**
	 * Sets the servers last active time to the current time (System.currentTimeMillis())
	 */
	public void active()
	{
		this.lastActive.set( System.currentTimeMillis() );
	}
	
	/**
	 * Sets the server's status and returns its old status
	 * 
	 * @param online
	 *            true to set the server to online
	 * @return the old status of the server
	 */
	public boolean setOnline( boolean online )
	{
		return this.online.getAndSet( online );
	}
	
	/**
	 * Checks if the server is online.
	 * 
	 * @return true, if is online
	 */
	public boolean isOnline()
	{
		return this.online.get();
	}
	
	/**
	 * Creates a tcp helper, that can be used to communicate to the server.
	 * 
	 * @return the tcp helper
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public TcpHelper createTcpHelper() throws IOException
	{
		Socket s = new Socket( this.endPoint.getAddress(), this.endPoint.getPort() );
		s.setSoTimeout( 100 );
		
		return new TcpHelper( s );
	}
}
