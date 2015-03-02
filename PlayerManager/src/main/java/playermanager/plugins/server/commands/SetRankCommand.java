package playermanager.plugins.server.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;

public class SetRankCommand implements CommandExecutor, Listener {

	private PlayerManager plugin;


	public SetRankCommand(PlayerManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 2) {
			if (args[0].equalsIgnoreCase("player")) {
				if (sender.hasPermission("playermanager.setrank")) {
					ServerPlayer serverPlayer = plugin.getServerPlayer(args[1]);
					if (serverPlayer != null) {
						serverPlayer.setPlayerRank(args[2].toLowerCase());
						plugin.sendMessage(sender, "&7" + serverPlayer.getName() + "&a, player rank has been set.");
						plugin.setGroups(serverPlayer.getName(), serverPlayer.getRanks());
						return true;
					} else {
						plugin.sendMessage(sender, "&c&cThe player you were looking for could not be found. " +
								"Seriously, we looked everywhere. :(");
						return true;
					}
				} else {
					plugin.sendMessage(sender, "&cPermission denied.");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("staff")) {
				if (sender.hasPermission("playermanager.setrank")) {
					ServerPlayer serverPlayer = plugin.getServerPlayer(args[1]);
					if (serverPlayer != null) {
						serverPlayer.setStaffRank(args[2].toLowerCase());
						plugin.sendMessage(sender, "&7" + serverPlayer.getName() + "&a, staff rank has been set.");
						plugin.setGroups(serverPlayer.getName(), serverPlayer.getRanks());
						return true;
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
		}
		return false;
	}
}
