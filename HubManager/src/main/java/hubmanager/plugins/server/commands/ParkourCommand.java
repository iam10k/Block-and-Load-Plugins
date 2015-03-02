package hubmanager.plugins.server.commands;

import hubmanager.plugins.server.main.HubManager;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ParkourCommand implements CommandExecutor, Listener {

	private HubManager plugin;

	public ParkourCommand(HubManager pl) {
		plugin = pl;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (plugin.parkour.contains(sender.getName())) {
				plugin.parkour.remove(sender.getName());
				((Player)sender).setAllowFlight(true);
				plugin.pM.sendMessage(sender, "&aParkour mode disabled.");
				return true;
			} else {
				plugin.parkour.add(sender.getName());
				((Player)sender).setGameMode(GameMode.SURVIVAL);
				((Player)sender).setAllowFlight(false);
				plugin.pM.sendMessage(sender, "&aParkour mode enabled.");
				return true;
			}
		}
		return false;
	}

}
