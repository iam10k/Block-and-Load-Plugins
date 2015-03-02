package factionsmanager.plugins.server.commands;

import factionsmanager.plugins.server.main.FactionsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import playermanager.plugins.server.player.FACPlayer;

public class XrayBanCommand implements CommandExecutor, Listener {

	private FactionsManager plugin;


	public XrayBanCommand(FactionsManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			if (sender.hasPermission("factionsmanager.ban")) {
				Player playerToBan = null;
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (p.getName().equalsIgnoreCase(args[0])) {
						playerToBan = p;
						break;
					}
				}
				if (playerToBan == null) {
					plugin.pM.sendMessage(sender, "&c&cThe player you were looking for could not be found. " +
							"Seriously, we looked everywhere. :(");
					return true;
				}

				FACPlayer facPlayer = plugin.pM.getFACPlayer(playerToBan);

				if (!facPlayer.miningBanned()) {
					facPlayer.setMiningBanned(false);
					plugin.pM.sendMessage(sender, "&7" + facPlayer.getName() + "&c, banned from mining for xraying.");
					return true;
				} else {
					facPlayer.setMiningBanned(true);
					plugin.pM.sendMessage(sender, "&7" + facPlayer.getName() + "&c, unbanned from mining for xraying.");
					return true;
				}
			} else {
				plugin.pM.sendMessage(sender, "&cPermission denied.");
				return true;
			}
		}
		return false;
	}
}
