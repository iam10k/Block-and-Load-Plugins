package playermanager.plugins.server.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;

public class NickCommand implements CommandExecutor, Listener {

	private PlayerManager plugin;


	public NickCommand(PlayerManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2) {
			if (sender.hasPermission("playermanager.nickname")) {
				ServerPlayer serverPlayer = plugin.getServerPlayer(args[0]);
				if (serverPlayer != null) {
					String nick = args[1];
					if (args[1].equalsIgnoreCase("off")) {
						nick = "";
					}
					serverPlayer.setNick(nick);
					for (Player p : plugin.getServer().getOnlinePlayers()) {
						if (p.getUniqueId().toString().equals(serverPlayer.getUUID())) {
							String name = plugin.getRankPrefix(p, serverPlayer) + plugin.getRankSuffix(p, serverPlayer) + serverPlayer.getNick();
							p.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
						}
					}
					plugin.sendMessage(sender, "&7" + serverPlayer.getName() + "&c, nickname set.");
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
