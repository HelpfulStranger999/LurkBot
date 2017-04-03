package com.gmail.helpfulstranger999.lurkbot;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.pircbotx.Configuration;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteJDBCLoader;

/**
 * This API creates and manages an instance of PircBotX which logs the user into a list of streamers. <br />
 * See {@link com.gmail.helpfulstranger999.lurkbot.LurkBot#start()} for starting the bot
 * @author HelpfulStranger999
 * @version 1.0.5
 */
public class LurkBot {
	
	private final static Logger logger = LoggerFactory.getLogger(LurkBot.class);
	
	private Connection conn;
	
	private ArrayList<String> streamers = new ArrayList<String>();
	
	private String nickname;
	private String oauth;
	
	private PircBotX bot;
	private MultiBotManager manager;
	
	/**
	 * The start method opens the connection to the database, get the list from it, and starts an instance of PircBotX
	 * 
	 * @author HelpfulStranger999
	 * @version 1.0.3
	 * @param username
	 * @param password
	 */
	public void start (String username, String password) {
		
		// Set the username and password
		this.nickname = username;
		this.oauth = password;
		
		// Open the connection
		logger.info("Establishing connection to database.");
		try {
			SQLiteJDBCLoader.initialize();
		} catch (Exception e) {
			logger.error("An error occurred opening database: ");
			e.printStackTrace();
		}
		
		SQLiteDataSource source = new SQLiteDataSource();
		source.setUrl("jdbc:sqlite:lurkbot.sqlite");
		
		try {
			conn = source.getConnection();
		} catch (SQLException e) {
			logger.error("An error occurred getting connection to database: ");
			e.printStackTrace();
		}
		
		// Get all the streamers in the database
		logger.info("Retrieving list of streamers");
		try {
			ResultSet set = this.prepareQuery("SELECT NAME FROM STREAMERS");
			while(set.next()) {
				streamers.add(set.getString("NAME"));
			}
		} catch (SQLException e) {
			logger.error("An exception occurred while accessing database: ");
			e.printStackTrace();
		}
		
		// Create Configuration
		logger.info("Creating bot");
		Configuration.Builder config = new Configuration.Builder();
		
		// Set static configs
		config.setAutoNickChange(false)
			.setOnJoinWhoEnabled(false);
		config.setCapEnabled(true)
			.addCapHandler(new EnableCapHandler("twitch.tv/membership"))
			.addCapHandler(new EnableCapHandler("twitch.tv/tags"));
		
		// Join channels
		config.addAutoJoinChannel("#" + this.getNickname().toLowerCase());
		for(String streamer : this.streamers) {
			config.addAutoJoinChannel("#" + streamer.toLowerCase());
		}
					
		// Log into the Twitch chatroom
		config.addServer("irc.chat.twitch.tv", 80);
		config.setServerPassword(this.oauth);
		config.setName(this.getNickname());
		
		// Add command listeners
		config.addListener(new AdministrativeListener(this))
			.addListener(new ManipulateListListener(this));
		
		// Set reconnect configurations
		config.setAutoReconnect(true).setAutoReconnectDelay(3000).setAutoReconnectAttempts(100);
		
		// Start the bot
		this.bot = new PircBotX(config.buildConfiguration());
		this.manager = new MultiBotManager();
		this.manager.addBot(this.bot);
		this.manager.start();
	}
	
	/**
	 * 
	 * The initialize method initializes the bot, creating necessary tables where missing and setting up other variables and resources where needed. Should be called in an installer.
	 * 
	 * @return A boolean based on the success or failure of the operation
	 * @author HelpfulStranger999
	 * @version 1.0.2
	 * @since 1.0.0
	 */
	
	public boolean initialize () {
		try {
			SQLiteJDBCLoader.initialize();
		} catch (Exception e) {
			logger.error("An error occurred opening database: ");
			e.printStackTrace();
			return false;
		}
		SQLiteDataSource source = new SQLiteDataSource();
		source.setUrl("jdbc:sqlite:lurkbot.sqlite");
		Connection connection;
		try {
			connection = source.getConnection();
		} catch (SQLException e) {
			logger.error("An error occurred getting connection to database: ");
			e.printStackTrace();
			return false;
		}
		try {
			connection.prepareStatement("CREATE TABLE IF NOT EXISTS STREAMERS (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT NOT NULL)").executeUpdate();
		} catch (SQLException e) {
			logger.error("An error occurred while creating database: ");
			e.printStackTrace();
			return false;
		}
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("An error occurred while closing database: ");
			e.printStackTrace();
			return false;
		}
		logger.info("Bot initialized");
		return true;
	}
	
	/**
	 * This method returns the username or nickname of the lurker
	 * 
	 * @author HelpfulStranger999
	 * @version 1.0.0
	 * @since 1.0.0
	 * @return The username or nickname of the lurker
	 */
	public String getNickname() {
		return this.nickname;
	}

	/**
	 * This method shuts the bot down and closes any open resources
	 * 
	 * @author HelpfulStranger999
	 * @version 1.0.1
	 * @since 1.0.0
	 */
	public void shutdown () {
		if(this.manager != null) {
			this.manager.stop();
		}
		if(this.getConnection() != null) {
			try {
				this.getConnection().close();
			} catch (SQLException e) {
				logger.error("An error occurred closing connection to database: ");
				e.printStackTrace();
			}
		}
		logger.info("Bot shutdown");
	}
	
	/**
	 * Stops the bot, and starts it again.
	 * 
	 * @author HelpfulStranger999
	 * @version 1.0.1
	 * @since 1.0.0
	 */
	public void restart () {
		this.shutdown();
		this.start(this.getNickname(), this.oauth);
		logger.info("Bot restarted");
	}
	
	/**
	 * Returns the logger for the lurkbot
	 * @return Logger
	 * @author HelpfulStranger999
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	public Logger getLogger () {
		return logger;
	}
	
	/**
	 * Returns the connection to the database
	 * @return Connection
	 * @version 1.0.0
	 * @since 1.0.0
	 * @author HelpfulStranger999
	 */
	public Connection getConnection () {
		return this.conn;
	}
	
	/**
	 * Creates a PreparedStatement and executes an update
	 * @param String sql to execute
	 * @return int relative to the success of the operation
	 * @throws SQLException
	 */
	public int prepareUpdate (String sql) throws SQLException {
		return this.getConnection().prepareStatement(sql).executeUpdate();
	}
	
	/**
	 * Creates a PreparedStatement and executes a query
	 * @param sql
	 * @return ResultSet
	 * @throws SQLException
	 * @author HelpfulStranger999
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	public ResultSet prepareQuery (String sql) throws SQLException {
		return this.getConnection().prepareStatement(sql).executeQuery();
	}
	
	/**
	 * Returns an ArrayList of streamers
	 * @return ArrayList<String>
	 * @author HelpfulStranger999
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	public ArrayList<String> getStreamers () {
		return this.streamers;
	}

}
