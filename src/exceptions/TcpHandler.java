package exceptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cmd.Command;
import cmd.CommandParser;
import cmd.StringParameter;

import entities.User;

public class TcpHandler implements Runnable {
	
	protected static final Logger logger = Logger.getLogger( TcpHandler.class.getName() );

	Socket client;
	ConcurrentHashMap<String, User> users;
	
	public TcpHandler( Socket client, ConcurrentHashMap<String, User> users )
	{
		this.client = client;
		this.users = users;
	}
	
	@Override
	public void run() {
		
		BufferedReader br = null;
		
		CommandParser cmdParser = new CommandParser();
		Command login = new Command( "login" );
		login.addParameter( new StringParameter( "username" ) );
		login.addParameter( new StringParameter( "password" ) );
		
		
		try{
			br = new BufferedReader( new InputStreamReader( this.client.getInputStream() ) );
			
			String command = br.readLine();
			
			
			
		}catch( SocketTimeoutException stex )
		{ }	
		catch( IOException ioex )
		{
			logger.warning( "Couldn't read from client: " + ioex.getMessage() );
			return;
		}
		
		
	}

}
