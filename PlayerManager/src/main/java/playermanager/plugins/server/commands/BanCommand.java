package playermanager.plugins.server.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;

public class BanCommand  implements CommandExecutor, Listener {

	private PlayerManager plugin;


	public BanCommand(PlayerManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			if (sender.hasPermission("playermanager.ban")) {
				ServerPlayer serverPlayer = plugin.getServerPlayer(args[0]);
				if (serverPlayer != null) {
					// If this is the hacking server or not
					if (plugin.server.equals("hacking") || plugin.server.equals("hub")) {
						serverPlayer.banPlayer(2);
						plugin.sendMessage(sender, "&7" + serverPlayer.getName() + "&c, banned from the network.");
						Player p = plugin.getPlayer(serverPlayer.getUUID());
						if (p != null) {
							p.kickPlayer(ChatColor.RED + "You have been banned!");
						}
						return true;
					} else {
						// Prevent lowering ban level
						if (serverPlayer.getBanLevel() != 2) {
							serverPlayer.banPlayer(1);
						}
						plugin.sendMessage(sender, "&7" + serverPlayer.getName() + "&c, banned from non cheating servers.");
						Player p = plugin.getPlayer(serverPlayer.getUUID());
						if (p != null) {
							p.kickPlayer(ChatColor.RED + "You have been banned!");
						}
						return true;
					}
				} else {
					plugin.sendMessage(sender, "&c&cThe player you were looking for could not be found. " +
							"Seriously, we looked everywhere. :(");
					return true;
				}
			} else {
				plugin.sendMessage(sender, "&cPermission denied.");
				return true;
			}
		}
		return false;
	}
}
