package factionsmanager.plugins.server.commands;

import factionsmanager.plugins.server.main.FactionsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import playermanager.plugins.server.player.FACPlayer;

public class XrayCommand implements CommandExecutor, Listener {

	private FactionsManager plugin;


	public XrayCommand(FactionsManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			if (sender.hasPermission("factionsmanager.xraycheck")) {
				Player playerToLookup = null;
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (p.getName().equalsIgnoreCase(args[0])) {
						playerToLookup = p;
						break;
					}
				}
				if (playerToLookup == null) {
					plugin.pM.sendMessage(sender, "&c&cThe player you were looking for could not be found. " +
							"Seriously, we looked everywhere. :(");
					return true;
				}

				FACPlayer facPlayer = plugin.pM.getFACPlayer(playerToLookup);

				String[] temp = new String[11];
				temp[0] = "&6=======================";
				temp[1] = "&7Stone: &a" + facPlayer.getStone();
				temp[2] = "&7Coal: &a" + facPlayer.getCoal();
				temp[3] = "&7Lapis: &a" + facPlayer.getLapis();
				temp[4] = "&7Redstone: &a" + facPlayer.getRedstone();
				temp[5] = "&7Iron: &a" + facPlayer.getIron();
				temp[6] = "&7Gold: &a" + facPlayer.getGold();
				temp[7] = "&7Emerald: &a" + facPlayer.getEmerald();
				temp[8] = "&7Diamond: &a" + facPlayer.getDiamond();
				temp[9] = "&3Percentage: &a" + facPlayer.mineRate();
				temp[10] = "&3Percentage Weighted: &a" + facPlayer.mineRateWeighted();

				plugin.pM.sendMessage(sender, temp);

				return true;

			} else {
				plugin.pM.sendMessage(sender, "&cPermission denied.");
				return true;
			}
		}
		return false;
	}
}
