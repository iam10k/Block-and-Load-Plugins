package blockbattles.plugins.server.commands;

import blockbattles.plugins.server.game.ArenaPlayer;
import blockbattles.plugins.server.main.BlockBattles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class LeaveCommand implements CommandExecutor, Listener {

	private BlockBattles plugin;


	public LeaveCommand(BlockBattles pl) {
		plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		Player player = (Player) sender;

		ArenaPlayer arenaPlayer = plugin.getArenaPlayer((Player) sender);

		if (arenaPlayer.getArena() != null) {
			arenaPlayer.getArena().removePlayer(arenaPlayer);
			plugin.pM.sendMessage(player, "&aYou have left the arena.");
			return true;
		}
		plugin.pM.sendMessage(player, "&cYou are not in an arena.");
		return true;
	}
}