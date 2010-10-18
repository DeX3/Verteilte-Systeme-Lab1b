package cmd;

import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.ParseException;
import exceptions.ValidationException;

/**
 * Represents a command the user can enter into.
 */
public class Command {

	/** The command itself. */
	String cmd;
	
	/** The parameters for this command. */
	LinkedHashSet<Parameter<?>> parameters;
	
	/**
	 * Instantiates a new command.
	 * 
	 * @param cmd
	 *            the command (Note that the '!' will automatically be prefixed to the command)
	 */
	public Command( String cmd )
	{
		this( cmd, new LinkedHashSet<Parameter<?>>() );
	}
	
	/**
	 * Instantiates a new command.
	 * 
	 * @param cmd
	 *            the command (Note that the '!' will automatically be prefixed to the command)
	 * @param parameters
	 *            the parameters for this command
	 */
	public Command( String cmd, LinkedHashSet<Parameter<?>> parameters )
	{
		this.cmd = cmd;
		this.parameters = parameters;
	}
	
	/**
	 * Adds a new parameter to this command.
	 * 
	 * @param p
	 *            the parameter
	 */
	public void addParameter( Parameter<?> p )
	{ this.parameters.add( p ); }
	
	/**
	 * Gets the parameter with the specified name.
	 * 
	 * @param name
	 *            the name
	 * @return the parameter
	 */
	public Parameter<?> getParameter( String name )
	{
		for( Parameter<?> p : this.parameters )
		{
			if( p.getName().equals( name ) )
				return p;
		}
		
		return null;
	}
	
	/**
	 * Parses the given input string. If input string matches this command, and all
	 * Parameters are correctly specified, this method will return true. This will
	 * set all parameters of this command to their respective values in the input string.
	 * If the input does not match this command, false is returned. If the input string
	 * matches the command, but there is a problem with one of the parameters, an exception
	 * is thrown.
	 * 
	 * @param str
	 *            the input string
	 * @return true, if the input string matches this command
	 * @throws ParseException
	 *             If one of the parameters cannot be parsed
	 * @throws ValidationException
	 *             If one of the parameters value's didn't match the expectations
	 */
	public boolean parse( String str ) throws ParseException, ValidationException
	{
		//Note: Commands always start with an '!'
		if( !str.startsWith( "!" + cmd) )
			return false;
		
		Pattern p = this.createPattern();	//Create pattern for this command
		Matcher m = p.matcher( str );		//Pattern will contain 3 groups for each parameter
											//group(1) is always the whole parameter
											//group(2) is the value of the parameter without quotes, if enclosed in quotes
											//group(3) is the value of the parameter, if not enclosed in quotes
		if( !m.matches() )
			throw new ParseException( "Couldn't parse parameters for command \"" + this.cmd + "\"");
		
		int i = 1;
		for( Parameter<?> param : this.parameters )
		{
			//If main group is null => parameter is missing
			if( m.group(i) == null )
				throw new ParseException( "Invalid number of arguments" );
			
			//Get the parameter value
			String value = m.group( i+1 );
			
			//If this group was null, the other group will give the parameters value
			if( value == null )
				value = m.group( i+2 );
			
			param.parse( value );
			param.validate();
			
			//Jump to the next parameter-group
			i += 3;
		}
		
		return true;
	}
	
	/**
	 * Creates a regex-pattern for this command, matching it to "!", the
	 * command name and its parameters. The pattern contains 3 groups per
	 * parameter, group(2) will match the parameter (without the quotes),
	 * if it is within quotes, group(3) will match if it is not within
	 * quotes.
	 * 
	 * @return the created pattern
	 */
	protected Pattern createPattern()
	{
		StringBuilder sb = new StringBuilder( "!" );
		sb.append( Matcher.quoteReplacement( this.cmd ) );
		
		for( int i=0 ; i < this.parameters.size() ; i++ )
			sb.append( "\\s+(\"(.+?)\"|([^\"\\s]+?))" );
		
		sb.append( "\\s*" );
		
		return Pattern.compile( sb.toString() );		
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( "!" );
		sb.append( this.cmd );
		sb.append( " " );
		
		for( Parameter<?> p : this.parameters )
		{
			sb.append( "\"" );
			sb.append( p.getValue() );
			sb.append( "\" " );
		}
		
		return sb.toString().trim();
	}
	
	
}
