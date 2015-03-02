package playermanager.plugins.server.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;

public class UnmuteCommand implements CommandExecutor, Listener {

	private PlayerManager plugin;


	public UnmuteCommand(PlayerManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			if (sender.hasPermission("playermanager.unmute")) {
				ServerPlayer serverPlayer = plugin.getServerPlayer(args[0]);
				if (serverPlayer != null) {
					serverPlayer.unmutePlayer();
					plugin.sendMessage(sender, "&7" + serverPlayer.getName() + "&c, has been unmuted.");
					plugin.sendMessage(serverPlayer.getUUID(), "&aYou have been unmuted.");
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
		return false;
	}
}
