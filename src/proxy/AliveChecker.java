package proxy;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import entities.FileserverInfo;
import entities.IPEndPoint;


public class AliveChecker extends TimerTask {
	
	int fileserverTimeout;
	int checkPeriod;
	private ConcurrentHashMap<IPEndPoint, FileserverInfo> fileservers;
	Timer timer;
	
	protected static final Logger logger = Logger.getLogger( AliveChecker.class.getName() );

	public AliveChecker( int checkPeriod, int fileserverTimeout, ConcurrentHashMap<IPEndPoint, FileserverInfo> fileservers )
	{
		this.checkPeriod = checkPeriod;
		this.fileserverTimeout = fileserverTimeout;
		this.fileservers = fileservers;
		this.timer = new Timer();
	}

	public void start()
	{
		this.timer.schedule( this, checkPeriod, checkPeriod );
	}
	
	public void stop()
	{
		this.timer.cancel();
	}
	
	@Override
	public void run() {
		
		for( Iterator<Map.Entry<IPEndPoint,FileserverInfo>> fsIter = fileservers.entrySet().iterator();
			 fsIter.hasNext(); )
		{
			Map.Entry<IPEndPoint,FileserverInfo> entry = fsIter.next();
			FileserverInfo info = entry.getValue();
			
			if( System.currentTimeMillis()-info.getLastActive() > fileserverTimeout )
				info.setOnline( false );
			else
				info.setOnline( true );
		}
		
	}

}
