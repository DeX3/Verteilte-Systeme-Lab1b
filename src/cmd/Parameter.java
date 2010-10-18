package cmd;

import exceptions.ParseException;
import exceptions.ValidationException;

/**
 * Represents a parameter that a command can have. Can also
 * be used for command-line arguments.
 * 
 * @param <T>
 *            Type for the value of the parameter
 */
public abstract class Parameter<T> {
	
	/** The name of the parameter. */
	String name;
	
	/** The description of the parameter. */
	String description;
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the parameter.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the description of the parameter.
	 * 
	 * @return the description
	 */
	public String getDescription()
	{ return this.description; }
	
	/**
	 * Sets the description.
	 * 
	 * @param description
	 *            the new description
	 */
	public void setDescription( String description )
	{ this.description = description; }

	/** The value of the parameter. */
	T value;
	
	/**
	 * Gets the value of the parameter.
	 * 
	 * @return the value of the parameter
	 */
	public T getValue()
	{ return this.value; }
	
	/**
	 * Sets the value of the parameter.
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue( T value )
	{ this.value = value; }
	
	/**
	 * Instantiates a new parameter.
	 * 
	 * @param name
	 *            the name of the parameter
	 */
	public Parameter( String name )
	{
		this.name = name;
	}
	
	/**
	 * Parses the given input string and tries to set the value
	 * of this parameter.
	 * 
	 * @param str
	 *            the input string
	 * @throws ParseException
	 *             If something goes wrong while parsing
	 */
	public abstract void parse( String str ) throws ParseException;
	
	/**
	 * Validates this parameter's value.
	 * 
	 * @throws ValidationException
	 *             If the parameter's value is invalid
	 */
	public abstract void validate() throws ValidationException;
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter<?> other = (Parameter<?>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return " [" + name + "=" + value + "]";
	}
	
	
	
	
}
