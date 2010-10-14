package exceptions;

public class ParseException extends Exception {
	private static final long serialVersionUID = -2465209767410312442L;

	public ParseException() {}
	
	public ParseException( String msg )
	{
		super( msg );
	}
}
