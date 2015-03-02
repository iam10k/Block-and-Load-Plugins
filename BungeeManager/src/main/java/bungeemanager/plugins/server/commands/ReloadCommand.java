package bungeemanager.plugins.server.commands;

import bungeemanager.plugins.server.main.BungeeManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.logging.Level;

public class ReloadCommand extends Command {
	
	private BungeeManager plugin;
	
	public ReloadCommand(BungeeManager pl) {
		super("bungeemanager", "bungeemanager.reload");
		plugin = pl;
	}

	public void execute(CommandSender sender, String[] args) {
		if ((sender instanceof ProxiedPlayer)) {
			ProxiedPlayer p = (ProxiedPlayer)sender;
			if (p.hasPermission("bungeemanager.reload")) {
				if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
					plugin.loadConfig();
					plugin.getLogger().log(Level.INFO, ChatColor.GOLD + "BungeeManager Reloaded.");
				}
			}
		}
	}
}
