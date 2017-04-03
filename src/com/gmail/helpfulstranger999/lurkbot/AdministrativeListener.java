package com.gmail.helpfulstranger999.lurkbot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class AdministrativeListener extends ListenerAdapter {

	private LurkBot bot;
	
	public AdministrativeListener (LurkBot bot) {
		this.bot = bot;
	}
	
	@Override
	public void onMessage (MessageEvent event) {
		
		// First, check if this is in the lurker's channel
		if(event.getChannel().getName().substring(1).equalsIgnoreCase(this.bot.getNickname())) {
			
			// Next, check if this is the lurker running the commands
			if(event.getTags().get("badges").contains("broadcaster/1")) {
				
				// Both checks passed, so begin parsing the command.
				String[] message = event.getMessage().split("\\s+");
				
				// Check if it is a lurk command.
				if(message[0].equalsIgnoreCase("!lurk")) {	
					String subcommand = message[1];
					
					// If the sub command is list,
					if(subcommand.equalsIgnoreCase("list")) {
						bot.getLogger().info("Listing streamers");
						
						// Begin creating a reply, and iterate through the current streams
						String reply = "You are currently lurking in the following channels: ";
						for(int i = 0; i < this.bot.getStreamers().size(); i ++) {
							String streamer = this.bot.getStreamers().get(i);
							if(!(reply.contains(streamer))) {
								if(i > 0) reply += ", ";
								reply += streamer;
							}
						}
						
						// Reply
						event.respondWith(reply);
						
						// Clear the string to fix a weird bug
						reply = "";
						bot.getLogger().debug(reply);
					}
					
					// If the sub command is restart, restart the bot
					if(subcommand.equalsIgnoreCase("restart")) {
						bot.getLogger().info("Restarting bot");
						event.respondWith("Restarting the bot, please wait.");
						this.bot.restart();
					}
					
					// If the sub command is stop, stop the bot
					if(subcommand.equalsIgnoreCase("stop")) {
						bot.getLogger().info("Stopping bot");
						event.respondWith("Stopping the bot...");
						this.bot.shutdown();
					}
				}
			}
		}
	}

}
