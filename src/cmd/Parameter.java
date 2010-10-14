package cmd;

import exceptions.ParseException;
import exceptions.ValidationException;

public abstract class Parameter<T> {
	String name;
	String description;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription()
	{ return this.description; }
	
	public void setDescription( String description )
	{ this.description = description; }

	T value;
	
	public T getValue()
	{ return this.value; }
	
	public void setValue( T value )
	{ this.value = value; }
	
	public Parameter( String name )
	{
		this.name = name;
	}
	
	public abstract void parse( String str ) throws ParseException;
	
	public abstract void validate() throws ValidationException;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

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
	
	
}
