package exceptions;

public class ValidationException extends Exception {
	private static final long serialVersionUID = -373652893092488081L;
	
	public ValidationException()
	{ super(); }
	
	public ValidationException( String msg )
	{ super( msg ); }
}
