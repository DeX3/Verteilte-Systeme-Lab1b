package cmd;

import exceptions.ParseException;
import exceptions.ValidationException;

/**
 * Represents a string parameter.
 */
public class StringParameter extends Parameter<String> {

	/**
	 * Instantiates a new string parameter.
	 * 
	 * @param name
	 *            the name of the parameter
	 */
	public StringParameter( String name )
	{
		super( name );
	}
	
	/**
	 * Instantiates a new string parameter.
	 * 
	 * @param name
	 *            the name of the parameter
	 * @param description
	 *            the description of the parameter
	 */
	public StringParameter( String name, String description )
	{
		this( name );
		this.description = description;
	}
	
	/**
	 * @see cmd.Parameter#parse(java.lang.String)
	 */
	@Override
	public void parse(String str) throws ParseException {
		this.value = str;

	}

	/**
	 * @see cmd.Parameter#validate()
	 */
	@Override
	public void validate() throws ValidationException {
	}
	
	

}
