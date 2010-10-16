package entities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.ParseException;

public class User {
	
	String name;
	String password;
	int credits;
	AtomicBoolean online;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getCredits() {
		return credits;
	}

	public void setCredits(int credits) {
		this.credits = credits;
	}
	
	public boolean isOnline() {
		return this.online.get();
	}


	public User( String name )
	{
		this.name = name;
		this.online = new AtomicBoolean( false );
	}
	
	public boolean logon()
	{ return this.online.compareAndSet( false, true ); }
	
	public boolean logoff()
	{ return this.online.compareAndSet( true, false ); }
	

	public static ConcurrentHashMap<String,User> readUsers( String propertiesFile ) throws IOException, FileNotFoundException, ParseException
	{
		ConcurrentHashMap<String, User> ret = new ConcurrentHashMap<String, User>();
		
		InputStream in = ClassLoader.getSystemResourceAsStream( propertiesFile );
		
		if (in != null)
		{
			Properties users = new java.util.Properties();
			users.load(in);
			
			Pattern pKey = Pattern.compile( "(.*?)(\\.credits)?" );
			
			Set<String> keys = users.stringPropertyNames();
			for (String key : keys) {
				
				Matcher m = pKey.matcher( key );
				if( !m.matches() )
					throw new ParseException( "Invalid key in properties file: \"" + key + "\"" );
					

				String username = m.group(1);
				User u = ret.get( username );
				if( u == null )
					u = new User( username );
				
				if( m.group(2) == null )		//credits-entry
				{	
					try{
						u.credits = Integer.parseInt( users.getProperty( key ) );
					}catch( NumberFormatException nfex )
					{ throw new ParseException( "Value for key \"" + key + "\" is not valid" ); }
					
				}else
				{
					u.password = users.getProperty( username ); 
				}
				
				ret.put( u.name, u );
				
			}
			
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
