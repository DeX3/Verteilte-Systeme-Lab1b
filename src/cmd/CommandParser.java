package cmd;

import java.util.Arrays;
import java.util.LinkedHashSet;

import exceptions.ParseException;
import exceptions.ValidationException;


/**
 * Parser for commands, that the user can enter.
 */
public class CommandParser {
	
	/** The commands, the parser understands. */
	LinkedHashSet<Command> commands;
	
	/**
	 * Instantiates a new command parser.
	 */
	public CommandParser()
	{ this( new LinkedHashSet<Command>() ); }
	
	/**
	 * Instantiates a new command parser.
	 * 
	 * @param commands
	 *            the commands, that this parser understands
	 */
	public CommandParser( LinkedHashSet<Command> commands )
	{
		this.commands = commands;
	}
	
	/**
	 * Adds the given command to the list of commands of this parser.
	 * 
	 * @param cmd
	 *            the cmd
	 */
	public void addCommand( Command cmd )
	{ this.commands.add( cmd ); }
	
	/**
	 * Adds all of the given commands to the list of commands of this parser.
	 * 
	 * @param cmds
	 *            the commands
	 */
	public void addCommands( Command...cmds )
	{
		this.commands.addAll( Arrays.asList(cmds) );
	}
	
	/**
	 * Parses the given input string and tries to match it with
	 * one of the known commands. If such a command is found,
	 * it will be parsed with the given input string. Exceptions
	 * may occur when parsing the command. Returns the command,
	 * that matched the input. If no matching command
	 * is found, null is returned.
	 * 
	 * @param str
	 *            the input string
	 * @return the command that matched the input string
	 * @throws ParseException
	 *             If there's a problem parsing the command
	 * @throws ValidationException
	 *             If there's a problem validating the parameters for the command
	 */
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
