package cmd;

import java.util.Arrays;
import java.util.LinkedHashSet;

import exceptions.ParseException;
import exceptions.ValidationException;

public class CommandLineParser {
	
	LinkedHashSet<Parameter<?>> parameters;
	
	String name, description;
	
	public void addParameter( Parameter<?> p )
	{
		this.parameters.add( p );
	}
	
	public void addParameters( Parameter<?>...params )
	{
		this.parameters.addAll( Arrays.asList(params) );
	}
	
	public CommandLineParser( String programName, String programDescription )
	{
		this( new LinkedHashSet<Parameter<?>>() );
		
		this.name = programName;
		this.description = programDescription;
	}
	
	public CommandLineParser( LinkedHashSet<Parameter<?>> parameters )
	{
		this.parameters = parameters;
	}
	
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
	
	public Parameter<?> getParameter( String name )
	{
		for( Parameter<?> p : this.parameters )
		{
			if( p.getName().equals( name ) )
				return p;
		}
		
		return null;
	}
	
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
