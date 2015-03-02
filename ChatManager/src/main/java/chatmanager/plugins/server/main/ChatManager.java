package chatmanager.plugins.server.main;

import chatmanager.plugins.server.util.FactionsHook;
import chatmanager.plugins.server.listeners.ChatListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChatManager extends JavaPlugin {

	public PlayerManager pM;

	private Boolean factions = false;
	private FactionsHook factionsHook;

	public String chatTemplate = "%FACTION%%TEAM%&7%PLAYER%&8: &f%MSG%";
	//public ArrayList<String> urls;
	//public ArrayList<String> terms;
	public ArrayList<String> cmds;

	public void onEnable() {
		Plugin playerManager = getServer().getPluginManager().getPlugin("PlayerManager");
		if (playerManager == null) {
			onDisable();
		}
		pM = (PlayerManager)playerManager;

		for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
			if (plugin.getName().equalsIgnoreCase("factions")) {
				factions = true;
				factionsHook = new FactionsHook(this);
			}
		}

		loadSettings();

		getServer().getPluginManager().registerEvents(new ChatListener(this), this);
	}

	private void loadSettings() {
		//urls = new ArrayList<String>();
		//terms = new ArrayList<String>();
		cmds = new ArrayList<String>();

		// Get settings from the database
		String query = "SELECT * FROM chatsettings";

		try {
			ResultSet rs = pM.querySQL(query);
			while (rs.next()) {
				if (rs.getString("chat_template") != null) {
					chatTemplate = rs.getString("chat_template");
				}
				/*
				if (rs.getString("blocked_terms") != null) {
					for (String s : rs.getString("blocked_terms").split(",")) {
						terms.add(s);
					}
				}
				if (rs.getString("block_urls") != null) {
					for (String s : rs.getString("block_urls").split(" ")) {
						urls.add(s);
					}
				}
				*/
				if (rs.getString("msg_commands") != null) {
					for (String s : rs.getString("msg_commands").split(",")) {
						cmds.add(s);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pM.closeConnection();
	}

	public void sendChatMessage(Player sender, String msg) {
		ServerPlayer serverPlayerSender = pM.getServerPlayer(sender);
		// Check if player is not muted
		if (!serverPlayerSender.getMuteState()) {
			// Go through each player to send
			if (factions) {
				factionsHook.sendMessage(sender, msg);
			} else {
				String temp = chatTemplate;

				// Clear faction, won't be used here
				temp = temp.replace("%FACTION%", "");

				// Set team color
				temp = temp.replace("%TEAM%", pM.getTeam(sender));

				// Set player name
				temp = temp.replace("%PLAYER%", sender.getDisplayName());

				// Translate the current part of the message
				temp = ChatColor.translateAlternateColorCodes('&', temp);

				// See if the msg can be translated
				temp = temp.replace("%MSG%", translateMSG(sender, msg));

				// Send to each player and the console
				getServer().getConsoleSender().sendMessage(temp);
				for (Player receiver : getServer().getOnlinePlayers()) {
					receiver.sendMessage(temp);
				}
			}
			serverPlayerSender.addTimesSpoken();
		} else {
			pM.sendMessage(sender, "&cYou are currently muted!");
		}
	}

	public String translateMSG(Player player, String msg) {
		String newMSG = msg;
		if (player.hasPermission("chatmanager.color")) {
			if (!player.hasPermission("chatmanager.color.magic")) {
				newMSG = newMSG.replace("&k", "");
				newMSG = newMSG.replace("&K", "");
			}
			return ChatColor.translateAlternateColorCodes('&', newMSG);
		}
		return newMSG;
	}
}
