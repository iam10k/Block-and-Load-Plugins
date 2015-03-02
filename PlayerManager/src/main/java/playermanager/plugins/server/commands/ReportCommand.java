package playermanager.plugins.server.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class ReportCommand implements CommandExecutor, Listener {

	private PlayerManager plugin;


	public ReportCommand(PlayerManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 2) {
			ServerPlayer reported = plugin.getServerPlayer(args[0]);
			if (reported != null) {
				String reportReason = "";
				for (int x = 1; x < args.length; x++) {
					reportReason += ChatColor.stripColor(args[x]) + " ";
				}
				if (reportReason.length() > 4) {
					if (sender instanceof Player) {
						ServerPlayer reporter = plugin.getServerPlayer((Player) sender);
						if (!reportExists(reportReason, reported.getID(), reporter.getID())) {
							if (reported.getID() == reporter.getID()) {
								plugin.sendMessage(sender, "&aReport cannot be filed on yourself.");
								return true;
							}

							addReport(reporter.getID(), reported.getID(), reportReason, plugin.server);
							for (Player player : plugin.getServer().getOnlinePlayers()) {
								if (player.hasPermission("playermanager.notify")) {
									plugin.sendMessage(player, "&2Report: &cFilled /reports " + reported.getName());
								}
							}
						}
					} else {
						if (!reportExists(reportReason, reported.getID(), 0)) {
							addReport(0, reported.getID(), reportReason, plugin.server);
							for (Player player : plugin.getServer().getOnlinePlayers()) {
								if (player.hasPermission("playermanager.notify")) {
									plugin.sendMessage(player, "&2Report: &cFilled /reports " + reported.getName());
								}
							}
						}
					}
					plugin.sendMessage(sender, "&aReport has been submitted successfully.");
					return true;
				} else {
					plugin.sendMessage(sender, "&cPlease enter valid report reason.");
					return true;
				}
			} else {
				plugin.sendMessage(sender, "&cThe player you were looking for could not be found. " +
						"Seriously, we looked everywhere. :(");
				return true;
			}
		}
		return false;
	}

	private void addReport(int reporterID, int reportedID, String reason, String server) {
		Date date = new Date();
		String query = "INSERT INTO reports (reporter_id, reported_id, reason, server, date) VALUES (" +
				reporterID + ", " + reportedID + ", '" + reason + "', '" + server + "', '" + plugin.dateTimeReportFormat.format(date) + "')";
		plugin.updateSQL(query);
	}

	private boolean reportExists(String report, int reportedID, int reporterID) {
		// Get data from the database using reported id
		String query = "SELECT * FROM reports WHERE (reported_id=" + reportedID + " AND reporter_id=" + reporterID + ")";

		try {
			ResultSet rs = plugin.querySQL(query);
			if (rs != null) {
				while (rs.next()) {
					if (rs.getString("reason").toLowerCase().contains(report.toLowerCase())) {
						return true;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		plugin.closeConnection();
		return false;
	}
}
