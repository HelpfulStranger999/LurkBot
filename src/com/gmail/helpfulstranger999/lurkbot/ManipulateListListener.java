package com.gmail.helpfulstranger999.lurkbot;

import java.sql.SQLException;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class ManipulateListListener extends ListenerAdapter {
	
	private LurkBot bot;

	public ManipulateListListener (LurkBot bot) {
		this.bot = bot;
	}
	
	@Override
	public void onMessage (MessageEvent event) {
		
		// Check if this is the lurker's channel.
		if(event.getChannel().getName().substring(1).equalsIgnoreCase(this.bot.getNickname())) {
			
			// Check if this is the lurker running the command.
			if(event.getTags().get("badges").contains("broadcaster/1")) {
				
				// Begin parsing the message
				String[] message = event.getMessage().split("\\s+");
				
				// If this is the lurk command
				if(message[0].equalsIgnoreCase("!lurk")) {
					String subcommand = message[1];
					
					// If the sub command is the add command, add the streamer and restart the bot
					if(subcommand.equalsIgnoreCase("add")) {
						String streamer = message[2];
						event.respondWith("Adding " + streamer + " to the list of channels");
						bot.getLogger().info("Adding " + streamer + " to the list of channels");
						
						try {
							bot.prepareUpdate("INSERT INTO STREAMERS (NAME) VALUES (\'" + streamer + "\');");
						} catch (SQLException e) {
							bot.getLogger().error("An error occurred adding streamer!");
							e.printStackTrace();
						}
						
						bot.restart();
					}
					
					// If the sub command is the add command, add the streamer and restart the bot
					if(subcommand.equalsIgnoreCase("addall")) {
						event.respondWith("Adding listed streamers to the list of channels");
						bot.getLogger().info("Adding listed streamers to the list of channels");
						
						try {
							for(int i = 2; i < message.length; i ++) {
								bot.prepareUpdate("INSERT INTO STREAMERS (NAME) VALUES (\'" + message[i] + "\');");
							}
						} catch (SQLException e) {
							bot.getLogger().error("An error occurred adding streamers!");
							e.printStackTrace();
						}
						
						bot.restart();
					}
					
					// If the sub command is the remove command, remove the streamer and restart the bot
					if(subcommand.equalsIgnoreCase("remove")) {	
						String streamer = message[2];
						event.respondWith("Removing " + streamer + " from the list of channels");
						bot.getLogger().info("Removing " + streamer + " from the list of channels");
						
						try {
							bot.prepareUpdate("DELETE FROM STREAMERS WHERE NAME = \'" + streamer + "\');");
						} catch (SQLException e) {
							bot.getLogger().error("An error occurred removing streamer!");
							e.printStackTrace();
						}
						
						bot.restart();
					}
				}
			}
		}
	}

}
