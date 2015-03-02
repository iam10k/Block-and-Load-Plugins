package chatmanager.plugins.server.listeners;

import chatmanager.plugins.server.main.ChatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatListener implements Listener {

	private ChatManager plugin;

	public ChatListener(ChatManager pl) {
		plugin = pl;
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if (!e.isCancelled()) {
			plugin.sendChatMessage(e.getPlayer(), e.getMessage());
			e.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onMSG(PlayerCommandPreprocessEvent e) {
		if (!e.isCancelled()) {
			if (plugin.pM.getServerPlayer(e.getPlayer()).getMuteState()) {
				String cmd = e.getMessage().toLowerCase().replace("/", "");
				if (plugin.cmds.contains(cmd)) {
					plugin.pM.sendMessage(e.getPlayer(), "&cYou are currently muted!");
					e.setCancelled(true);
				}
			}
		}
	}

	/*
	private boolean blockChat(String msg, Player player) {
		String chatString = msg.toLowerCase();
		for (String s : plugin.urls) {
			if (chatString.contains(s)) {
				plugin.pM.sendMessage(player, "&cMessage blocked! &3Reason: Contains URL");
				return true;
			}
		}

		for (String s : plugin.terms) {
			if (chatString.contains(s)) {
				plugin.pM.sendMessage(player, "&cMessage blocked! &3Reason: Contains a blocked term");
				return true;
			}
		}

		int count = 0;
		for (int x = 0; x < chatString.length(); x++) {
			if (Character.isDigit(chatString.charAt(x))) {
				if (count++ > 7) {
					plugin.pM.sendMessage(player, "&cMessage blocked! &3Reason: Contains to many numbers");
					return true;
				}
			}
		}

		double charUpperCase = 0.0;
		for (int x = 0; x < msg.length(); x++) {
			if (Character.getType(msg.charAt(x)) == Character.UPPERCASE_LETTER) {
				charUpperCase++;
				if (charUpperCase/(double)msg.length() >= 0.5 && msg.length() > 4) {
					plugin.pM.sendMessage(player, "&cMessage blocked! &3Reason: Try to turn off caps lock.");
					return true;
				}
			}
		}
		return false;
	}
	*/

}
