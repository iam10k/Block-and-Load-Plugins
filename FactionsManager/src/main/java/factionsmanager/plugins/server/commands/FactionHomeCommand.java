package factionsmanager.plugins.server.commands;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import factionsmanager.plugins.server.main.FactionsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class FactionHomeCommand implements CommandExecutor, Listener {

	private FactionsManager plugin;

	public FactionHomeCommand(FactionsManager pl) {
		plugin = pl;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}

		if (args.length == 1) {
			if (sender.hasPermission("factions.admin")) {
				Faction faction = Factions.i.getBestTagMatch(args[0]);
				if (faction != null) {
					if (faction.getHome() != null) {
						((Player) sender).teleport(faction.getHome());
						return true;
					} else {
						plugin.pM.sendMessage(sender, "&cFaction does not have a home set.");
						return true;
					}
				} else {
					plugin.pM.sendMessage(sender, "&cCould not locate faction.");
					return true;
				}
			}
		}
		return false;
	}

}
