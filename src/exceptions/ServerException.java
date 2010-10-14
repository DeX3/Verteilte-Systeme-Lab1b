package exceptions;

public class ServerException extends RuntimeException {
	private static final long serialVersionUID = -1258299793337808311L;

	public ServerException( Throwable cause )
	{
		super( cause );
	}
}
