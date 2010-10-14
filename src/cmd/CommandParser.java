package cmd;

import java.util.LinkedHashSet;

public class CommandParser {
	
	LinkedHashSet<Parameter<?>> parameters;
	
	public CommandParser( LinkedHashSet<Command> parameters )
	{
		this.parameters = parameters;
	}
	
}
