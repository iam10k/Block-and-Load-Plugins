package factionsmanager.plugins.server.commands;

import factionsmanager.plugins.server.main.FactionsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import playermanager.plugins.server.player.FACPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StatsCommand implements CommandExecutor, Listener {

	private FactionsManager plugin;


	public StatsCommand(FactionsManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		FACPlayer facPlayer = plugin.pM.getFACPlayer((Player) sender);

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("pvp")) {
				String[] msgs = new String[8];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  Total Kills: &3" + facPlayer.getKills();
				msgs[2] = "&7  Total Sword Kills: &3" + facPlayer.getSwordKills();
				msgs[3] = "&7  Total Bow Kills: &3" + facPlayer.getBowKills();
				msgs[4] = "&7  Total Deaths: &3" + facPlayer.getDeaths();
				msgs[5] = "&7  Current Killstreak: &3" + facPlayer.getKillstreak();
				msgs[6] = "&7  Longest Killstreak: &3" + facPlayer.getLongestKillStreak();
				msgs[7] = "&6---------------------------------------------";
				plugin.pM.sendMessage(sender, msgs);

				return true;
			} else if (args[0].equalsIgnoreCase("game")) {
				String[] msgs = new String[4];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  Blocks Placed: &3" + facPlayer.getBlocksPlaced();
				msgs[2] = "&7  Blocks Broken: &3" + facPlayer.getBlocksBroken();
				msgs[3] = "&6---------------------------------------------";
				plugin.pM.sendMessage(sender, msgs);

				return true;
			} else if (args[0].equalsIgnoreCase("playtime")) {
				String[] msgs = new String[5];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  Playtime: &3" + facPlayer.convertPlaytimeMinutes(facPlayer.getPlaytime());
				msgs[2] = "&7  Last Login: &3" + facPlayer.getLastLogin();
				msgs[3] = "&7  Last Logout: &3" + facPlayer.getLastQuit();
				msgs[4] = "&6---------------------------------------------";
				plugin.pM.sendMessage(sender, msgs);

				return true;
			} else if (args[0].equalsIgnoreCase("top")) {
				String[] msgs = new String[5];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  /stats top <arg> &3- to display to stats";
				msgs[2] = "&7  Args: &3swordkills, bowkills, deaths,";
				msgs[3] = "&7     &3killstreak, blocksplaced, blocksmined";
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
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("top")) {
				if (args[1].equalsIgnoreCase("swordkills")) {
					String query = "SELECT id,kills_sword FROM factions ORDER BY kills_sword DESC LIMIT 10";
					ResultSet rs = plugin.pM.querySQL(query);

					String[] temp = new String[11];
					temp[0] = "&6------------ &7Top Sword Kills &6------------";

					int count = 1;
					try {
						while (rs.next() && count < 11) {
							temp[count] = "&7%COUNT%. &c%NAME% &7- &a%KILLS% &7kills";
							temp[count] = temp[count].replaceAll("%COUNT%", "" + count);
							temp[count] = temp[count].replaceAll("%NAME%", plugin.pM.getNameFromID(rs.getInt("id")));
							temp[count] = temp[count].replaceAll("%KILLS%", "" + rs.getInt("kills_sword"));
							count++;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					plugin.pM.sendMessage(sender, temp);
					plugin.pM.closeConnection();

					return true;
				} else if (args[1].equalsIgnoreCase("bowkills")) {
					String query = "SELECT id,kills_bow FROM factions ORDER BY kills_bow DESC LIMIT 10";
					ResultSet rs = plugin.pM.querySQL(query);

					String[] temp = new String[11];
					temp[0] = "&6------------ &7Top Bow Kills &6------------";

					int count = 1;
					try {
						while (rs.next() && count < 11) {
							temp[count] = "&7%COUNT%. &c%NAME% &7- &a%KILLS% &7kills";
							temp[count] = temp[count].replaceAll("%COUNT%", "" + count);
							temp[count] = temp[count].replaceAll("%NAME%", plugin.pM.getNameFromID(rs.getInt("id")));
							temp[count] = temp[count].replaceAll("%KILLS%", "" + rs.getInt("kills_bow"));
							count++;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					plugin.pM.sendMessage(sender, temp);
					plugin.pM.closeConnection();

					return true;
				} else if (args[1].equalsIgnoreCase("deaths")) {
					String query = "SELECT id,deaths FROM factions ORDER BY deaths DESC LIMIT 10";
					ResultSet rs = plugin.pM.querySQL(query);

					String[] temp = new String[11];
					temp[0] = "&6------------ &7Top Deaths &6------------";

					int count = 1;
					try {
						while (rs.next() && count < 11) {
							temp[count] = "&7%COUNT%. &c%NAME% &7- &a%DEATHS% &7deaths";
							temp[count] = temp[count].replaceAll("%COUNT%", "" + count);
							temp[count] = temp[count].replaceAll("%NAME%", plugin.pM.getNameFromID(rs.getInt("id")));
							temp[count] = temp[count].replaceAll("%DEATHS%", "" + rs.getInt("deaths"));
							count++;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					plugin.pM.sendMessage(sender, temp);
					plugin.pM.closeConnection();

					return true;
				} else if (args[1].equalsIgnoreCase("killstreak")) {
					String query = "SELECT id,killstreak FROM factions ORDER BY killstreak DESC LIMIT 10";
					ResultSet rs = plugin.pM.querySQL(query);

					String[] temp = new String[11];
					temp[0] = "&6------------ &7Top Killstreak &6------------";

					int count = 1;
					try {
						while (rs.next() && count < 11) {
							temp[count] = "&7%COUNT%. &c%NAME% &7- &a%KILLSTREAK% &7killstreak";
							temp[count] = temp[count].replaceAll("%COUNT%", "" + count);
							temp[count] = temp[count].replaceAll("%NAME%", plugin.pM.getNameFromID(rs.getInt("id")));
							temp[count] = temp[count].replaceAll("%KILLSTREAK%", "" + rs.getInt("killstreak"));
							count++;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					plugin.pM.sendMessage(sender, temp);
					plugin.pM.closeConnection();

					return true;
				} else if (args[1].equalsIgnoreCase("blocksplaced")) {
					String query = "SELECT id,blocks_placed FROM factions ORDER BY blocks_placed DESC LIMIT 10";
					ResultSet rs = plugin.pM.querySQL(query);

					String[] temp = new String[11];
					temp[0] = "&6------------ &7Top Blocks Placed &6------------";

					int count = 1;
					try {
						while (rs.next() && count < 11) {
							temp[count] = "&7%COUNT%. &c%NAME% &7- &a%BLOCKS% &7blocks placed";
							temp[count] = temp[count].replaceAll("%COUNT%", "" + count);
							temp[count] = temp[count].replaceAll("%NAME%", plugin.pM.getNameFromID(rs.getInt("id")));
							temp[count] = temp[count].replaceAll("%BLOCKS%", "" + rs.getInt("blocks_placed"));
							count++;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					plugin.pM.sendMessage(sender, temp);
					plugin.pM.closeConnection();

					return true;
				} else if (args[1].equalsIgnoreCase("blocksmined")) {
					String query = "SELECT id,blocks_broken FROM factions ORDER BY blocks_broken DESC LIMIT 10";
					ResultSet rs = plugin.pM.querySQL(query);

					String[] temp = new String[11];
					temp[0] = "&6------------ &7Top Blocks Mined &6------------";

					int count = 1;
					try {
						while (rs.next() && count < 11) {
							temp[count] = "&7%COUNT%. &c%NAME% &7- &a%BLOCKS% &7blocks mined";
							temp[count] = temp[count].replaceAll("%COUNT%", "" + count);
							temp[count] = temp[count].replaceAll("%NAME%", plugin.pM.getNameFromID(rs.getInt("id")));
							temp[count] = temp[count].replaceAll("%BLOCKS%", "" + rs.getInt("blocks_broken"));
							count++;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					plugin.pM.sendMessage(sender, temp);
					plugin.pM.closeConnection();

					return true;
				} else {
					String[] msgs = new String[5];
					msgs[0] = "&6---------------------------------------------";
					msgs[1] = "&7  /stats top <arg> &3- to display to stats";
					msgs[2] = "&7  Args: &3swordkills, bowkills, deaths,";
					msgs[3] = "&7     &3killstreak, blocksplaced, blocksmined";
					msgs[4] = "&6---------------------------------------------";
					plugin.pM.sendMessage(sender, msgs);

					return true;
				}
			} else {
				String[] msgs = new String[6];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  /stats pvp &3- to display pvp stats";
				msgs[2] = "&7  /stats game &3- to display gameplay stats";
				msgs[3] = "&7  /stats playtime &3- to display playtime stats";
				msgs[4] = "&7  /stats top &3- to display top stats";
				msgs[5] = "&6---------------------------------------------";
				plugin.pM.sendMessage(sender, msgs);

				return true;
			}
		} else {
			String[] msgs = new String[6];
			msgs[0] = "&6---------------------------------------------";
			msgs[1] = "&7  /stats pvp &3- to display pvp stats";
			msgs[2] = "&7  /stats game &3- to display gameplay stats";
			msgs[3] = "&7  /stats playtime &3- to display playtime stats";
			msgs[4] = "&7  /stats top &3- to display top stats";
			msgs[5] = "&6---------------------------------------------";
			plugin.pM.sendMessage(sender, msgs);

			return true;
		}
	}
}
