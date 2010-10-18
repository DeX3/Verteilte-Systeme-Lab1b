package threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {	
	private static ExecutorService pool = null;
	
	private ThreadPool()
	{ }
	
	public static synchronized ExecutorService getPool()
	{
		if( pool == null )
			pool = Executors.newCachedThreadPool();
		
		return pool;
	}
}
