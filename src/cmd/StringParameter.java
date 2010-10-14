package cmd;

import exceptions.ParseException;
import exceptions.ValidationException;

public class StringParameter extends Parameter<String> {

	public StringParameter( String name )
	{
		super( name );
	}
	
	@Override
	public void parse(String str) throws ParseException {
		this.value = str;

	}

	@Override
	public void validate() throws ValidationException {
		
	}

}
