package cmd;

import exceptions.ValidationException;

/**
 * Helper interface for validating CustomParameters
 */
public interface Validator {
	
	/**
	 * Validate the value of a parameter.
	 * 
	 * @param value
	 *            the value to validate
	 * @throws ValidationException
	 *             If the value is invalid
	 */
	public void validate( String value ) throws ValidationException;
}
