package chatmanager.plugins.server.util;

import chatmanager.plugins.server.main.ChatManager;
import com.earth2me.essentials.Essentials;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.ChatMode;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FactionsHook {

	private ChatManager plugin;
	private Essentials essentials = null;

	public FactionsHook(ChatManager pl) {
		plugin = pl;
		Plugin essentialsPlugin = plugin.getServer().getPluginManager().getPlugin("Essentials");
		if (essentialsPlugin != null) {
			essentials = (Essentials) essentialsPlugin;
		}
	}

	// Nothing
	public void sendMessage(Player sender, String msg) {
		FPlayer uSender = FPlayers.i.get(sender.getName());
		if (uSender.getChatMode().equals(ChatMode.ALLIANCE)) {
			int x = 0;
			for (Player receiver : plugin.getServer().getOnlinePlayers()) {
				FPlayer uReceiver = FPlayers.i.get(receiver.getName());
				if (uReceiver.getRelationTo(uSender).equals(Relation.ALLY) ||
						uReceiver.getRelationTo(uSender).equals(Relation.MEMBER) || uReceiver.isSpyingChat()) {
					String temp = plugin.chatTemplate;

					// Get the faction relationship
					ChatColor relationShipColor;
					relationShipColor = uReceiver.getFaction().getColorTo(uSender.getFaction());
					temp = temp.replace("%FACTION%", relationShipColor + "* ");

					// Clear team, won't be used here
					temp = temp.replace("%TEAM%", "");

					// Set player name
					temp = temp.replace("%PLAYER%", sender.getDisplayName());

					// Translate the current part of the message
					temp = ChatColor.translateAlternateColorCodes('&', "&8[&dAlly&8]" + temp);

					// See if the msg can be translated
					temp = temp.replace("%MSG%", plugin.translateMSG(sender, msg));

					// Send to receiver and log once
					receiver.sendMessage(temp);
					if (x == 0) {
						plugin.getServer().getConsoleSender().sendMessage(temp);
						x++;
					}
				}
			}
		} else if (uSender.getChatMode().equals(ChatMode.FACTION)) {
			int x = 0;
			for (Player receiver : plugin.getServer().getOnlinePlayers()) {
				FPlayer uReceiver = FPlayers.i.get(receiver.getName());
				if (uReceiver.getRelationTo(uSender).equals(Relation.MEMBER) || uReceiver.isSpyingChat()) {
					String temp = plugin.chatTemplate;

					// Get the faction relationship
					ChatColor relationShipColor;
					relationShipColor = uReceiver.getFaction().getColorTo(uSender.getFaction());
					temp = temp.replace("%FACTION%", relationShipColor + "* ");

					// Clear team, won't be used here
					temp = temp.replace("%TEAM%", "");

					// Set player name
					temp = temp.replace("%PLAYER%", sender.getDisplayName());

					// Translate the current part of the message
					temp = ChatColor.translateAlternateColorCodes('&', "&8[&aFChat&8] " + temp);

					// See if the msg can be translated
					temp = temp.replace("%MSG%", plugin.translateMSG(sender, msg));

					// Send to receiver and log once
					receiver.sendMessage(temp);
					if (x == 0) {
						plugin.getServer().getConsoleSender().sendMessage(temp);
						x++;
					}
				}
			}
		} else {
			int x = 0;
			for (Player receiver : plugin.getServer().getOnlinePlayers()) {
				if (!receiver.getName().equalsIgnoreCase(sender.getName())) {
					if (!essentials.getUser(sender).isIgnoreExempt()) {
						if (essentials.getUser(receiver).isIgnoredPlayer(essentials.getUser(sender))) {
							continue;
						}
					}
				}

				String temp = plugin.chatTemplate;

				// Get the faction relationship
				ChatColor relationShipColor;
				FPlayer uReceiver = FPlayers.i.get(receiver.getName());
				relationShipColor = uReceiver.getFaction().getColorTo(uSender.getFaction());
				temp = temp.replace("%FACTION%", relationShipColor + "* ");

				// Clear team, won't be used here
				temp = temp.replace("%TEAM%", "");

				// Set player name
				temp = temp.replace("%PLAYER%", sender.getDisplayName());

				// Translate the current part of the message
				temp = ChatColor.translateAlternateColorCodes('&', temp);

				// See if the msg can be translated
				temp = temp.replace("%MSG%", plugin.translateMSG(sender, msg));

				// Send to receiver and log once
				receiver.sendMessage(temp);
				if (x == 0) {
					plugin.getServer().getConsoleSender().sendMessage(temp);
					x++;
				}
			}
		}
	}
}
