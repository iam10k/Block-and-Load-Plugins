package blockbattles.plugins.server.commands;

import blockbattles.plugins.server.main.BlockBattles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import playermanager.plugins.server.player.BBPlayer;

public class StatsCommand implements CommandExecutor, Listener {

	private BlockBattles plugin;


	public StatsCommand(BlockBattles pl) {
		plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		BBPlayer bbPlayer = plugin.pM.getBBPlayer((Player) sender);

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("pvp")) {
				String[] msgs = new String[8];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  Total Kills: &3" + bbPlayer.getKills();
				msgs[2] = "&7  Total Sword Kills: &3" + bbPlayer.getSwordKills();
				msgs[3] = "&7  Total Bow Kills: &3" + bbPlayer.getBowKills();
				msgs[4] = "&7  Total Deaths: &3" + bbPlayer.getDeaths();
				msgs[5] = "&7  Current Killstreak: &3" + bbPlayer.getKillstreak();
				msgs[6] = "&7  Longest Killstreak: &3" + bbPlayer.getLongestKillStreak();
				msgs[7] = "&6---------------------------------------------";
				plugin.pM.sendMessage(sender, msgs);

				return true;
			} else if (args[0].equalsIgnoreCase("playtime")) {
				String[] msgs = new String[5];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  Playtime: &3" + bbPlayer.convertPlaytimeMinutes(bbPlayer.getPlaytime());
				msgs[2] = "&7  Last Login: &3" + bbPlayer.getLastLogin();
				msgs[3] = "&7  Last Logout: &3" + bbPlayer.getLastQuit();
				msgs[4] = "&6---------------------------------------------";
				plugin.pM.sendMessage(sender, msgs);

				return true;
			} else {
				String[] msgs = new String[4];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  /stats pvp &3- to display pvp stats";
				msgs[2] = "&7  /stats playtime &3- to display playtime stats";
				msgs[3] = "&6---------------------------------------------";
				plugin.pM.sendMessage(sender, msgs);

				return true;
			}
		} else {
			String[] msgs = new String[5];
			msgs[0] = "&6---------------------------------------------";
			msgs[1] = "&7  /stats pvp &3- to display pvp stats";
			msgs[2] = "&7  /stats game &3- to display gameplay stats";
			msgs[3] = "&7  /stats playtime &3- to display playtime stats";
			msgs[4] = "&6---------------------------------------------";
			plugin.pM.sendMessage(sender, msgs);

			return true;
		}
	}
}
