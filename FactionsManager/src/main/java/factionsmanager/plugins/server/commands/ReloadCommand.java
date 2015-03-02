package factionsmanager.plugins.server.commands;

import factionsmanager.plugins.server.main.FactionsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

public class ReloadCommand implements CommandExecutor, Listener {
	
	private FactionsManager plugin;
	
	public ReloadCommand(FactionsManager pl) {
		plugin = pl;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("factionsmanager.reload")) {
					plugin.reloadConfig();
					plugin.reloadXP();
					plugin.pM.sendMessage(sender,"&7[&aFactionsManager&7] &aConfig reloaded.");
					return true;
				}
			}
		}
		return false;
	}

}
