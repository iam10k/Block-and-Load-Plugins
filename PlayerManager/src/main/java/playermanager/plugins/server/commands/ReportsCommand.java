package playermanager.plugins.server.commands;

import de.bananaco.bpermissions.imp.Commands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReportsCommand implements CommandExecutor, Listener {

	private PlayerManager plugin;


	public ReportsCommand(PlayerManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			if (sender.hasPermission("playermanager.lookupreports")) {
				if (args[0].equalsIgnoreCase("top")) {
					plugin.sendMessage(sender, mostReportsAll());
					return true;
				} else if (args[0].equalsIgnoreCase("recent")) {
					plugin.sendMessage(sender, mostRecent());
					return true;
				} else {
					ServerPlayer lookup = plugin.getServerPlayer(args[0]);
					if (lookup != null) {
						sendToPlayer(sender, lookupPlayer(lookup.getID()), 1);
						return true;
					} else {
						plugin.sendMessage(sender, "&cThe player you were looking for could not be found. " +
								"Seriously, we looked everywhere. :(");
						return true;
					}
				}
			} else {
				plugin.sendMessage(sender, "&cPermission denied.");
				return true;
			}
		}

		if (args.length == 2) {
			if (sender.hasPermission("playermanager.lookupreports")) {
				// Remove a report by its id#
				if (args[0].equalsIgnoreCase("remove")) {
					int reportID = 0;
					try {
					reportID = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						plugin.sendMessage(sender, "&cInvalid report ID entered.");
						return true;
					}

					if (reportID != 0) {
						removeReport(reportID);
						plugin.sendMessage(sender, "&aReport " + reportID + " removed!");
						return true;
					}
					plugin.sendMessage(sender, "&cInvalid report ID entered.");
					return true;
				}

				// Clear all of the players reports
				if (args[0].equalsIgnoreCase("clear")) {
					ServerPlayer lookup = plugin.getServerPlayer(args[1]);
					if (lookup != null) {
						clearPlayer(lookup.getID());
						plugin.sendMessage(sender, "&aReports cleared for " + lookup.getName() + ".");
						return true;
					} else {
						plugin.sendMessage(sender, "&cThe player you were looking for could not be found. " +
								"Seriously, we looked everywhere. :(");
						return true;
					}
				} else {
					ServerPlayer lookup = plugin.getServerPlayer(args[0]);
					if (lookup != null) {
						int page = 1;
						try {
							page = Integer.parseInt(args[1]);
						} catch (NumberFormatException e) {
							page = 1;
						}
						sendToPlayer(sender, lookupPlayer(lookup.getID()), page);
						return true;
					} else {
						plugin.sendMessage(sender, "&cThe player you were looking for could not be found. " +
								"Seriously, we looked everywhere. :(");
						return true;
					}
				}
			} else {
				plugin.sendMessage(sender, "&cPermission denied.");
				return true;
			}
		}
		return false;
	}

	private void removeReport(int id) {
		String query = "DELETE FROM reports WHERE id=" + id;
		plugin.updateSQL(query);
	}

	private void clearPlayer(int reportedID) {
		String query = "DELETE FROM reports WHERE reported_id=" + reportedID;
		plugin.updateSQL(query);
	}

	private void sendToPlayer(CommandSender sender, ArrayList<String> lines, int page) {
		if (page * 5 > lines.size() + 4) {
			page = 1;
		}

		ArrayList<String> temp = new ArrayList<String>();

		temp.add(lines.remove(0));
		for (int x = (page-1)*5,z = 0; x < lines.size() && z < 10; x++, z++) {
			temp.add(lines.get(x));
		}

		if (lines.size() > 5) {
			temp.add("   &6Page " + page + " out of " + ((lines.size()/5) + 1));
		}

		plugin.sendMessage(sender, temp);
	}

	private ArrayList<String> lookupPlayer(int reportedID) {
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("&7=========== &6" + plugin.getNameFromID(reportedID) + " &7===========");

		// Get data from the database using reported id
		String query = "SELECT * FROM reports WHERE reported_id=" + reportedID + "";

		try {
			ResultSet rs = plugin.querySQL(query);
			while (rs.next()) {
				String temp = "   ";
				temp += "&a" + rs.getInt("id") + "&7-";
				temp += "&c" + rs.getString("date") + "&7-";
				temp += "&a" + rs.getString("server") + "&7-";
				if (rs.getInt("reporter_id") == 0) {
					temp += "&3Console" + "&7-";
				} else {
					temp += "&3" + plugin.getNameFromID(rs.getInt("reporter_id")) + "&7-";
				}
				temp += "&6 " + rs.getString("reason");
				lines.add(temp);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		plugin.closeConnection();
		if (lines.size() == 1) {
			lines.add("&cNo reports found for this user.");
		}
		return lines;
	}

	private ArrayList<String> mostReportsAll() {
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("&7=========== &6Top Reported Players &7===========");

		// Get data from the database using reported id
		String query = "SELECT reported_id, count(*) AS count FROM reports GROUP BY reported_id ORDER BY count(*) DESC LIMIT 10";

		try {
			ResultSet rs = plugin.querySQL(query);
			while (rs.next()) {
				String temp = "   ";
				temp += "&3" + plugin.getNameFromID(rs.getInt("reported_id")) + "&7-";
				temp += "&6 " + rs.getInt("count");
				lines.add(temp);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		plugin.closeConnection();
		return lines;
	}

	private ArrayList<String> mostRecent() {
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("&7=========== &6Current Reported Players &7===========");

		// Get data from the database using reported id
		String query = "SELECT * FROM reports ORDER BY date DESC LIMIT 10";

		try {
			ResultSet rs = plugin.querySQL(query);
			while (rs.next()) {
				String temp = "   ";
				temp += "&a" + rs.getInt("id") + "&7-";
				temp += "&c" + rs.getString("date") + "&7-";
				temp += "&a" + rs.getString("server") + "&7-";
				temp += "&3" + plugin.getNameFromID(rs.getInt("reported_id")) + "&7-";
				temp += "&6 " + rs.getString("reason");
				lines.add(temp);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		plugin.closeConnection();
		if (lines.size() == 1) {
			lines.add("&cNo reports found at this time.");
		}
		return lines;
	}
}
