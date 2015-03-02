package armorcontrol.plugins.server.commands;

import armorcontrol.plugins.server.main.ArmorControl;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import playermanager.plugins.server.player.ACPlayer;

public class StatsCommand implements CommandExecutor, Listener {

	private ArmorControl plugin;


	public StatsCommand(ArmorControl pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		ACPlayer acPlayer = plugin.pM.getACPlayer((Player) sender);

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("pvp")) {
				String[] msgs = new String[6];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  Total Kills: &3" + acPlayer.getKills();
				msgs[2] = "&7  Total Deaths: &3" + acPlayer.getDeaths();
				msgs[3] = "&7  Current Killstreak: &3" + acPlayer.getKillstreak();
				msgs[4] = "&7  Longest Killstreak: &3" + acPlayer.getLongestKillStreak();
				msgs[5] = "&6---------------------------------------------";
				plugin.pM.sendMessage(sender, msgs);

				return true;
			} else if (args[0].equalsIgnoreCase("game")) {
				String[] msgs = new String[6];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  Juggernaut: &3" + acPlayer.getTimesHolding() + " Times";
				msgs[2] = "&7  Juggernaut Time: &3" + acPlayer.convertPlaytimeSeconds(acPlayer.getTotalTimeHolding());
				msgs[3] = "&7  Longest Juggernaut Time: &3" + acPlayer.convertPlaytimeSeconds(acPlayer.getLongestTimeHolding());
				msgs[4] = "&7  Chests Opened: &3" + acPlayer.getChestsOpened();
				msgs[5] = "&6---------------------------------------------";
				plugin.pM.sendMessage(sender, msgs);

				return true;
			} else if (args[0].equalsIgnoreCase("playtime")) {
				String[] msgs = new String[5];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  Playtime: &3" + acPlayer.convertPlaytimeMinutes(acPlayer.getPlaytime());
				msgs[2] = "&7  Last Login: &3" + acPlayer.getLastLogin();
				msgs[3] = "&7  Last Logout: &3" + acPlayer.getLastQuit();
				msgs[4] = "&6---------------------------------------------";
				plugin.pM.sendMessage(sender, msgs);

				return true;
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
