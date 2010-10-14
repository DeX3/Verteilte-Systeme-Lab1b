package cmd;

import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Command {

	Pattern p;
	LinkedHashSet<Parameter<?>> parameters;
	
	public Command( String regexPattern, LinkedHashSet<Parameter<?>> parameters )
	{
		this.p = Pattern.compile( regexPattern );
		this.parameters = parameters;
	}
	
	public boolean parse( String cmd )
	{
		Matcher m = p.matcher( cmd );
		
		if( !m.matches() )
			return false;
		
		
	}
}
