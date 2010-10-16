package cmd;

import exceptions.ParseException;
import exceptions.ValidationException;

public class IntegerParameter extends Parameter<Integer> {

	int minValue;
	int maxValue;
	
	public IntegerParameter(String name) {
		super(name);
	}
	
	public IntegerParameter( String name, int min, int max )
	{
		super(name);
		this.minValue = min;
		this.maxValue = max;
	}
	
	public IntegerParameter( String name, int min, int max, String description )
	{
		this( name, min, max );
		this.description = description;
	}

	@Override
	public void parse(String str) throws ParseException {
		
		try{
			this.value = Integer.parseInt( str );
		}catch( NumberFormatException nfex )
		{ throw new ParseException( "Value for parameter " + this.name + " is not valid" ); }
	}

	@Override
	public void validate() throws ValidationException {
		if( this.value < this.minValue )
			throw new ValidationException( "Parameter \"" + this.name + "\" has to be at least " + minValue );
		
		if( this.value > this.maxValue )
			throw new ValidationException( "Parameter \"" +  this.name + "\" has a maximum of " + maxValue );
	}

}
