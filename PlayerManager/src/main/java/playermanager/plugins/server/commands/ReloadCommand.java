package playermanager.plugins.server.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;

public class ReloadCommand implements CommandExecutor, Listener {

    private PlayerManager plugin;

    public ReloadCommand(PlayerManager pl) {
        plugin = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("playermanager.reload")) {
            plugin.loadConfig();
            plugin.restartAnnouncements();
            plugin.sendMessage(sender, "&7[&aPlayerManager&7] &cReloaded configs!");
			return true;
        }
        return false;
    }
}
