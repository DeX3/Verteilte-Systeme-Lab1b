package threading;


public abstract class DefaultRunnable implements Runnable {
	
	protected Throwable throwable;
	protected boolean stopping;
	
	@Override
	public void run() {
		try
		{
			runSafe();
		}catch( Throwable t )
		{
			this.throwable = t;
			return;
		}
	}
	
	public Throwable getThrowable() {
		return this.throwable;
	}
	
	public void throwIfError() throws Throwable
	{
		if( this.throwable != null )
			throw this.throwable;
	}
	
	public abstract void runSafe() throws Throwable;
	
	public abstract boolean isRunning();
	
	public abstract Throwable stop();
	
}
