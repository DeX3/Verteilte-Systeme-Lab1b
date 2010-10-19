package proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.TcpHelper;
import cmd.Command;
import cmd.CommandParser;
import cmd.IntegerParameter;
import cmd.StringParameter;
import entities.FileserverInfo;
import entities.IPEndPoint;
import entities.User;


public class TcpHandler implements Runnable {

	protected static final Logger logger = Logger.getLogger(TcpHandler.class
			.getName());

	Socket client;
	ConcurrentHashMap<String, User> users;
	ConcurrentHashMap<IPEndPoint, FileserverInfo> fileservers;
	TcpServer srv;
	User user;

	protected static final Command CMD_LOGIN;
	protected static final Command CMD_CREDITS;
	protected static final Command CMD_BUY;
	protected static final Command CMD_LIST;
	protected static final Command CMD_DOWNLOAD;
	protected static final Command CMD_EXIT;

	protected static final CommandParser cmdParser;

	static {
		CMD_LOGIN = new Command("login");
		CMD_LOGIN.addParameter(new StringParameter("username"));
		CMD_LOGIN.addParameter(new StringParameter("password"));

		CMD_CREDITS = new Command("credits");

		CMD_BUY = new Command("buy");
		CMD_BUY.addParameter(new IntegerParameter("credits", 1,
				Integer.MAX_VALUE));

		CMD_LIST = new Command("list");

		CMD_DOWNLOAD = new Command("download");
		CMD_DOWNLOAD.addParameter(new StringParameter("filename"));

		CMD_EXIT = new Command("exit");

		cmdParser = new CommandParser();
		cmdParser.addCommands(CMD_LOGIN, CMD_CREDITS, CMD_BUY, CMD_LIST,
				CMD_DOWNLOAD, CMD_EXIT);
	}

	public TcpHandler( Socket client, TcpServer srv ) {
		this.client = client;
		this.users = srv.getUsers();
		this.fileservers = srv.getFileservers();
		this.srv = srv;
	}

	@Override
	public void run() {

		User user = null;

		while (!srv.isStopping()) {
			TcpHelper client = null;
			Command cmd = null;

			try {
				client = new TcpHelper(this.client, cmdParser);
				cmd = client.receiveCommand();
			} catch (SocketTimeoutException stex) {
				continue;
			} catch (IOException ioex) {
				logger.warning("Couldn't read from client: "
						+ ioex.getMessage());
				return;
			}

			if (cmd == CMD_LOGIN) {
				
				if( user != null && user.isOnline() )
				{
					if( user.logout() )
						client.sendLine( "You logged out as " + user.getName() );
				}
				
				user = doLogin(client, cmd);
			} else if (cmd == CMD_EXIT) {
				if (user != null && user.logout())
					logger.info(user.getName() + " logged out");
			} else if (cmd == CMD_CREDITS && expectLogin(user, client)) {
				client.sendLine("You have " + user.getCredits()
						+ " credits left.");
			} else if (cmd == CMD_BUY && expectLogin(user, client)) {
				int toBuy = (Integer) cmd.getParameter("credits").getValue();

				client
						.sendLine("You now have " + user.buy(toBuy)
								+ " credits.");
			} else if (cmd == CMD_LIST && expectLogin(user, client)) {
				doList(client);
			} else if (cmd == CMD_DOWNLOAD && expectLogin(user, client)) {
				doDownload(client, cmd);
			}

		}
	}

	private void doDownload( TcpHelper client, Command cmd ) {

		FileserverInfo srvInfo = getLeastUsedServer();

		try {
			TcpHelper srv = srvInfo.createTcpHelper();

			srv.sendLine(cmd.toString());
			String strSize = srv.receiveLine();
			Pattern p = Pattern.compile("File size: (\\d+)");
			Matcher m = p.matcher(strSize);

			if (!m.matches()) // Matcht nicht -> Fehlermeldung
			{
				client.sendLine(strSize);
				return;
			}

			long size = Long.parseLong(m.group(1));

			if (user.getCredits() < size) {
				client
						.sendLine("You do not have sufficient credits to download this file (needed: "
								+ size + ")");
				srv.sendNack();
				return;
			}

			srv.sendAck();
			client.sendLine("Begin file: " + size + " "
					+ cmd.getParameter("filename").getValue());

			srvInfo.use(user.pay(size));

			String line;
			while ((line = srv.receiveLine()) != null) {
				client.sendLine(line);
			}

		} catch (IOException ioex) {
			client.sendLine("Couldn't communicate with server: "
					+ ioex.getMessage());
			return;
		}
	}

	private User doLogin( TcpHelper client, Command cmd ) {
		String username = (String) cmd.getParameter("username").getValue();
		String password = (String) cmd.getParameter("password").getValue();
		User u = this.users.get(username);

		if (u != null && u.getPassword().equals(password)) {
			if (u.login()) {
				this.user = u;

				logger.info(u.getName() + " logged in");

				client.sendLine("Successfully logged in.");
				client.sendLine("Welcome " + u.getName()
						+ ", you currently have " + u.getCredits()
						+ " credits on your account.");
				client
						.sendLine("Available commands are !credits, !buy <credits>, !list, !download <filename> and !exit.");
			} else
			{
				client.sendLine("This user is already logged in from another client");
				return null;
			}
		} else {
			client.sendLine("Wrong username or password.");
			return null;
		}

		return u;
	}

	private void doList( TcpHelper client ) {
		FileserverInfo srvInfo = getLeastUsedServer();

		if (srvInfo == null)
			client
					.sendLine("There is currently no server online that can fulfill your request.");
		else {
			try {
				TcpHelper srv = srvInfo.createTcpHelper();
				srv.sendLine("!list");
				srv.tunnelTo( client );
				srv.close();
			} catch (IOException ioex) {
				client.sendLine("Couldn't communicate with the server: "
						+ ioex.getMessage());
			}
		}
	}

	protected FileserverInfo getLeastUsedServer() {
		FileserverInfo srv = null;
		int minUse = Integer.MAX_VALUE;

		for (FileserverInfo server : this.fileservers.values()) {
			if (server.getUsage() < minUse)
				srv = server;
		}

		return srv;
	}

	protected boolean expectLogin( User u, TcpHelper tcp ) {
		if (u == null || !u.isOnline()) {
			tcp.sendLine("You have to be logged in to use this command");
			return false;
		}

		return true;
	}

}
