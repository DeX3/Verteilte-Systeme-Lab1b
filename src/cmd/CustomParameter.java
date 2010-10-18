package cmd;

import exceptions.ParseException;
import exceptions.ValidationException;

/**
 * Represents a custom parameter for a command. A custom validator
 * can be specified to validate the parameter value. However, the
 * parameters type will always be String.
 */
public class CustomParameter extends Parameter<String> {

	/** The validator for the parameter's value. */
	Validator validator;
	
	/**
	 * Instantiates a new custom parameter.
	 * 
	 * @param name
	 *            the name of the parameter
	 * @param description
	 *            the description of the parameter
	 * @param v
	 *            the validator for the parameter's value
	 */
	public CustomParameter( String name, String description, Validator v )
	{
		super( name );
		this.description = description;
		this.validator = v;
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
		this.validator.validate( this.value );
	}

}
