package playermanager.plugins.server.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;

public class SetMultiplierCommand implements CommandExecutor, Listener {

	private PlayerManager plugin;


	public SetMultiplierCommand(PlayerManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2) {
			ServerPlayer serverPlayer = plugin.getServerPlayer(args[0]);
			if (serverPlayer != null) {
				if (sender.hasPermission("playermanager.setmultiplier")) {
					double multiplier = Double.parseDouble(args[1]);
					if (multiplier > 0) {
						serverPlayer.setMultiplier(multiplier);
						plugin.sendMessage(sender, "&7" + serverPlayer.getName() + "&a, player multiplier has been set.");
						return true;
					} else {
						plugin.sendMessage(sender, "&cThe multiplier entered seems to be invalid or < 0.");
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
