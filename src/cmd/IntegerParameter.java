package cmd;

import exceptions.ParseException;
import exceptions.ValidationException;

/**
 * Represents an integer-parameter.
 */
public class IntegerParameter extends Parameter<Integer> {

	/** The min value used for validating. */
	int minValue;
	
	/** The max value used for validating. */
	int maxValue;
	
	/**
	 * Instantiates a new integer parameter.
	 * 
	 * @param name
	 *            the name of the parameter
	 */
	public IntegerParameter(String name) {
		super(name);
	}
	
	/**
	 * Instantiates a new integer parameter.
	 * 
	 * @param name
	 *            the name of the parameter
	 * @param min
	 *            the mininum value of the parameter
	 * @param max
	 *            the maximum value of the parameter
	 */
	public IntegerParameter( String name, int min, int max )
	{
		super(name);
		this.minValue = min;
		this.maxValue = max;
	}
	
	/**
	 * Instantiates a new integer parameter.
	 * 
	 * @param name
	 *            the name of the parameter
	 * @param min
	 *            the minimum value of the parameter
	 * @param max
	 *            the maximum value of the parameter
	 * @param description
	 *            the description of the parameter
	 */
	public IntegerParameter( String name, int min, int max, String description )
	{
		this( name, min, max );
		this.description = description;
	}

	/**
	 * @see cmd.Parameter#parse(java.lang.String)
	 */
	@Override
	public void parse(String str) throws ParseException {
		
		try{
			this.value = Integer.parseInt( str );
		}catch( NumberFormatException nfex )
		{ throw new ParseException( "Value for parameter " + this.name + " is not valid" ); }
	}

	/**
	 * @see cmd.Parameter#validate()
	 */
	@Override
	public void validate() throws ValidationException {
		if( this.value < this.minValue )
			throw new ValidationException( "Parameter \"" + this.name + "\" has to be at least " + minValue );
		
		if( this.value > this.maxValue )
			throw new ValidationException( "Parameter \"" +  this.name + "\" has a maximum of " + maxValue );
	}

}
