package cmd;

import java.util.Arrays;
import java.util.LinkedHashSet;

import exceptions.ParseException;
import exceptions.ValidationException;

/**
 * A simple parser for command-line arguments. Only expects
 * fixed arguments. No switches or optional arguments are
 * allowed.
 */
public class CommandLineParser {
	
	/** The parameters. */
	LinkedHashSet<Parameter<?>> parameters;
	
	/** Name and description for the program, used for usage-messages. */
	String name, description;
	
	/**
	 * Adds the given parameter.
	 * 
	 * @param p
	 *            the parameter
	 */
	public void addParameter( Parameter<?> p )
	{
		this.parameters.add( p );
	}
	
	/**
	 * Adds all of the given parameters
	 * 
	 * @param params
	 *            the parameters to add
	 */
	public void addParameters( Parameter<?>...params )
	{
		this.parameters.addAll( Arrays.asList(params) );
	}
	
	/**
	 * Instantiates a new command line parser.
	 * 
	 * @param programName
	 *            the program name
	 * @param programDescription
	 *            the program description
	 */
	public CommandLineParser( String programName, String programDescription )
	{
		this( new LinkedHashSet<Parameter<?>>() );
		
		this.name = programName;
		this.description = programDescription;
	}
	
	/**
	 * Instantiates a new command line parser.
	 * 
	 * @param parameters
	 *            the parameters
	 */
	public CommandLineParser( LinkedHashSet<Parameter<?>> parameters )
	{
		this.parameters = parameters;
	}
	
	/**
	 * Parses the given arguments.
	 * 
	 * @param args
	 *            the arguments to parse
	 * @throws ParseException
	 *             If there's a problem parsing the arguments
	 * @throws ValidationException
	 *             If there's a problem validating the arguments
	 */
	public void parse( String[] args ) throws ParseException, ValidationException
	{
		int i = 0;
		
		if( args.length != this.parameters.size() )
			throw new ParseException( "Invalid number of arguments" );
		
		for( Parameter<?> p : parameters )
		{
			p.parse( args[i++] );
			
			p.validate();
		}
	}
	
	/**
	 * Gets the parameter with the specified name
	 * 
	 * @param name
	 *            the name of the parameter to get
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
	 * Gets the usage string.
	 * 
	 * @return the usage string
	 */
	public String getUsageString()
	{
		StringBuilder sb = new StringBuilder( "Usage: " );
		
		sb.append( name );
		for( Parameter<?> p : this.parameters )
		{
			sb.append( " " );
			sb.append( p.getName() );
		}
		
		sb.append( "\r\n\r\n" );
		sb.append( description );
		sb.append( "\r\n\r\n" );
		
		for( Parameter<?> p : this.parameters )
		{
			sb.append( p.getName() );
			sb.append( "\t" );
			sb.append( p.getDescription() );
			sb.append( "\r\n" );
		}
		
		return sb.toString();	
	}
}
