package playermanager.plugins.server.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;

public class AddCreditsCommand implements CommandExecutor, Listener {

	private PlayerManager plugin;

	public AddCreditsCommand(PlayerManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2) {
			ServerPlayer serverPlayer = plugin.getServerPlayer(args[0]);
			if (serverPlayer != null) {
				if (sender.hasPermission("playermanager.addcredits")) {
					int credits = Integer.parseInt(args[1]);
					if (credits > 0) {
						serverPlayer.addCredits(credits);
						plugin.sendMessage(sender, "&7" + serverPlayer.getName() + "&a, has been given &3" + credits + " &aB&&aL XP.");
						return true;
					} else {
						plugin.sendMessage(sender, "&cInvalid number of credits");
						return true;
					}
				} else {
					plugin.sendMessage(sender, "&cPermission denied.");
					return true;
				}
			} else {
				plugin.sendMessage(sender, "&cThe player you were looking for could not be found. " +
						"Seriously, we looked everywhere. :(");
				return true;
			}
		}
		return false;
	}
}
