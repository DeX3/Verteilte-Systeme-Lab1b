package entities;

import java.net.InetAddress;

/**
 * Simple container class for ip + port.
 */
public class IPEndPoint {
	
	/** The address. */
	InetAddress address;
	
	/** The port. */
	int port;
	
	/**
	 * Gets the address.
	 * 
	 * @return the address
	 */
	public InetAddress getAddress() {
		return address;
	}
	
	/**
	 * Sets the address.
	 * 
	 * @param address
	 *            the new address
	 */
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	
	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Sets the port.
	 * 
	 * @param port
	 *            the new port
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * Instantiates a new iP end point.
	 * 
	 * @param address
	 *            the address
	 * @param port
	 *            the port
	 */
	public IPEndPoint(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + port;
		return result;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IPEndPoint other = (IPEndPoint) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return address.getHostAddress() + ":" + port;
	}
	
	
	
}
