package cmd;

import java.util.Arrays;
import java.util.LinkedHashSet;

import exceptions.ParseException;
import exceptions.ValidationException;

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
	
	public void addCommands( Command...cmds )
	{
		this.commands.addAll( Arrays.asList(cmds) );
	}
	
	public Command parse( String str ) throws ParseException, ValidationException
	{
		for( Command cmd : this.commands )
		{
			if( cmd.parse( str ) )
				return cmd;
		}
		
		return null;
	}
	
}
