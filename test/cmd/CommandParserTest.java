package cmd;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import exceptions.ParseException;
import exceptions.ValidationException;

public class CommandParserTest {
	
	CommandParser cmdParser;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Before
	public void setUp()
	{		
		Command login = new Command( "login" );
		login.addParameter( new StringParameter( "username" ) );
		login.addParameter( new StringParameter( "password" ) );
		
		Command credits = new Command( "credits" );
		
		Command buy = new Command( "buy" );
		buy.addParameter( new IntegerParameter( "credits", 1, Integer.MAX_VALUE ) );

		Command list = new Command( "list" );
		
		Command download = new Command( "download" );
		download.addParameter( new StringParameter( "filename" ) );
		
		Command exit = new Command( "exit" );
		
		cmdParser = new CommandParser();
		cmdParser.addCommands( login, credits, buy, list, download, exit );
		
	}

	@Test
	public void testParseLogin() throws ParseException, ValidationException {
		System.out.println( cmdParser.parse( "!login blah 1234" ) );
	}
	
	@Test
	public void testParseLogin2() throws ParseException, ValidationException
	{
		System.out.println( cmdParser.parse( "!login \"user name\" 1234" ) );
	}
	
	@Test(expected=ParseException.class)
	public void testParseLoginFail() throws ParseException, ValidationException
	{
		System.out.println( cmdParser.parse( "!login user" ) );
	}
	
	@Test(expected=ValidationException.class)
	public void testParseBuyFailValidation() throws ParseException, ValidationException
	{
		System.out.println( cmdParser.parse( "!buy \"-10\"" ) );
	}
	
	@After
	public void tearDown()
	{
		
	}

}
