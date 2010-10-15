package cmd;

import java.util.LinkedHashSet;

import exceptions.ParseException;

public class CommandParser {
	
	LinkedHashSet<Command> commands;
	
	public CommandParser()
	{ this( new LinkedHashSet<Command>() ); }
	
	public CommandParser( LinkedHashSet<Command> commands )
	{
		this.commands = commands;
	}
	
	public void addCommand( Command cmd )
	{ this.commands.add( cmd ); }
	
	public Command parse( String str ) throws ParseException
	{
		for( Command cmd : this.commands )
		{
			if( cmd.parse( str ) )
				return cmd;
		}
		
		return null;
	}
	
}
