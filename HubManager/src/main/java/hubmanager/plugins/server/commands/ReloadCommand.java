package hubmanager.plugins.server.commands;

import hubmanager.plugins.server.main.HubManager;
import hubmanager.plugins.server.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ReloadCommand implements CommandExecutor, Listener {
	
	private HubManager plugin;
	
	public ReloadCommand(HubManager pl) {
		plugin = pl;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("hubmanager.reload")) {
					plugin.reloadConfig();
					plugin.loadConfig();
					plugin.pM.sendMessage(sender, "&aHubManager Config reloaded.");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("setspawn")) {
				if (sender.hasPermission("hubmanager.setspawn")) {
					if (sender instanceof Player) {
						Location newSpawn = ((Player) sender).getLocation();
						String spawnString = Utils.convertLocationToString(newSpawn, false, true);
						plugin.getConfig().set("Spawn.Location", spawnString);
						plugin.saveConfig();
						plugin.spawn = newSpawn;
						plugin.pM.sendMessage(sender, "&aSpawn set.");
						return true;
					}
				}
			}
		}
		return false;
	}

}
