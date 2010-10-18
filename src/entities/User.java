package entities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.ParseException;


/**
 * Contains some meta-data about a user.
 */
public class User {
	
	/** The name. */
	String name;
	
	/** The password. */
	String password;
	
	/** The credits. */
	AtomicLong credits;
	
	/** The online. */
	AtomicBoolean online;
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 * 
	 * @param password
	 *            the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the credits.
	 * 
	 * @return the credits
	 */
	public long getCredits() {
		return credits.get();
	}

	/**
	 * Sets the credits.
	 * 
	 * @param credits
	 *            the new credits
	 */
	public void setCredits(int credits) {
		this.credits.set( credits );
	}
	
	/**
	 * Checks if is online.
	 * 
	 * @return true, if is online
	 */
	public boolean isOnline() {
		return this.online.get();
	}

	/**
	 * Adds the specified amount of credits to the users credits.
	 * 
	 * @param credits
	 *            the credits to add
	 * @return the new amount of credits
	 */
	public long buy( long credits )
	{
		return this.credits.addAndGet( credits );
	}
	
	/**
	 * Subtracts the specified amount of credits from the users credits.
	 * 
	 * @param credits
	 *            the credits to subtract
	 * @return the new amount of credits
	 */
	public long pay( long credits )
	{
		return this.credits.addAndGet( -credits );
	}
	
	/**
	 * Instantiates a new user.
	 * 
	 * @param name
	 *            the name
	 */
	public User( String name )
	{
		this.name = name;
		this.online = new AtomicBoolean( false );
		this.credits = new AtomicLong( 0 );
	}
	
	/**
	 * Login.
	 * 
	 * @return true, if successful
	 */
	public boolean login()
	{ return this.online.compareAndSet( false, true ); }
	
	/**
	 * Logout.
	 * 
	 * @return true, if successful
	 */
	public boolean logout()
	{ return this.online.compareAndSet( true, false ); }
	

	/**
	 * Reads a list of users from the specified properties-file.
	 * Returns the users in form of a ConcurrentHashMap, that indexes
	 * the users by their names.
	 * 
	 * @param propertiesFile
	 *            the properties file to read
	 * @return the concurrent hash map filled with the read users
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FileNotFoundException
	 *             If the properties file cannot be found
	 * @throws ParseException
	 *             If the properties file cannot be parsed
	 */
	public static ConcurrentHashMap<String,User> readUsers( String propertiesFile ) throws IOException, FileNotFoundException, ParseException
	{
		ConcurrentHashMap<String, User> ret = new ConcurrentHashMap<String, User>();
		
		InputStream in = ClassLoader.getSystemResourceAsStream( propertiesFile );
		
		if (in != null)
		{
			Properties users = new java.util.Properties();
			users.load(in);
			
			//Properties have the form "<name>=<password>" or "<name>.credits=<credits>"
			
			//Pattern to match keys
			Pattern pKey = Pattern.compile( "(.*?)(\\.credits)?" );
			
			Set<String> keys = users.stringPropertyNames();
			for (String key : keys) {
				
				Matcher m = pKey.matcher( key );
				if( !m.matches() )
					throw new ParseException( "Invalid key in properties file: \"" + key + "\"" );
					
				
				String username = m.group(1);	//The first group always contains the user's name
				User u = ret.get( username );
				if( u == null )
					u = new User( username );
				
				if( m.group(2) == null )		//If there is no second group, this is a password-entry
				{	
					u.setPassword( users.getProperty( username ) );
					
				}else	//Else, this is a credits entry
				{
					try{
						u.setCredits( Integer.parseInt( users.getProperty( key ) ) );
					}catch( NumberFormatException nfex )
					{ throw new ParseException( "Value for key \"" + key + "\" is not valid" ); }
				}
				
				ret.put( u.name, u );
				
			}
			
			//Check if all users have their passwords set
			for( String username : ret.keySet() )
			{
				if( ret.get(username).getPassword() == null )
					throw new ParseException( "Incomplete information: User \"" + username + "\" does not have a password" );
			}
		} else {
			throw new FileNotFoundException( "The file \"" + propertiesFile + "\" could not be found" );
		}
		
		return ret;
	}
}
