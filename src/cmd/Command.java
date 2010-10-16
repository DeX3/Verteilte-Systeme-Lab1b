package cmd;

import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.ParseException;
import exceptions.ValidationException;

public class Command {

	String cmd;
	LinkedHashSet<Parameter<?>> parameters;
	
	public Command( String cmd )
	{
		this( cmd, new LinkedHashSet<Parameter<?>>() );
	}
	
	public Command( String cmd, LinkedHashSet<Parameter<?>> parameters )
	{
		this.cmd = cmd;
		this.parameters = parameters;
	}
	
	public void addParameter( Parameter<?> p )
	{ this.parameters.add( p ); }
	
	public Parameter<?> getParameter( String name )
	{
		for( Parameter<?> p : this.parameters )
		{
			if( p.getName().equals( name ) )
				return p;
		}
		
		return null;
	}
	
	public boolean parse( String str ) throws ParseException, ValidationException
	{
		if( !str.startsWith( "!" + cmd) )
			return false;
		
		Pattern p = this.createPattern();
		Matcher m = p.matcher( str );
		
		if( !m.matches() )
			throw new ParseException( "Couldn't parse parameters for command \"" + this.cmd + "\"");
		
		int i = 1;
		for( Parameter<?> param : this.parameters )
		{
			if( m.group(i) == null )
				throw new ParseException( "Inavlid number of arguments" );
			
			String value = m.group( i+1 );
			
			if( value == null )
				value = m.group( i+2 );
			
			
			param.parse( value );
			
			param.validate();
			
			i += 3;
		}
		
		return true;
	}
	
	/**
	 * Creates a regex-pattern for this command, matching it the "!", the
	 * command name and its parameters. The pattern contains 3 groups per
	 * parameter, group(2) will match the parameter (without the quotes),
	 * if it is within quotes, group(3) will match, if it is not within
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

	@Override
	public String toString() {
		return "Command [cmd=" + cmd + ", parameters=" + parameters + "]";
	}
	
	
}
