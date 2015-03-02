package playermanager.plugins.server.player;

import org.bukkit.entity.Player;
import playermanager.plugins.server.main.PlayerManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

public class ESPlayer extends ServerPlayer {

	private String lastLogin; // Last Login, date and time
	private String lastQuit; // Last Logout, date and time
	private int playtime; // Playtime in minutes
	private int kills; // Kills
	private int deaths; // Deaths
	private int killstreak = 0; // Current Kill Streak
	private int longestKillStreak; // Longest Kill Streak
	private int mostKillsOneGame; // Most kills in one game
	private int timesKillingThePresident; // Times the player has killed the President
	private int wins; // Wins
	private int losses; // Losses
	private int blocksPlaced; // Total blocks placed
	private int blocksMined; // Total blocks mined
	private int blocksPlacedThisGame = 0; // Total blocks placed this game
	private int blocksMinedThisGame = 0; // Total blocks mined this game
	private int mostBlocksPlaced; // Total blocks placed in one game
	private int mostBlocksMined; // Total blocks mined in one game


	public ESPlayer(PlayerManager plugin, Player pl, boolean newPlayer) {
		super(plugin, pl, false); // Cannot be true, it would create duplicates

		// If the player is a noob add them to users database
		if (newPlayer) {
			Date date = new Date();
			String query = "INSERT INTO escort (id, last_login, last_quit) VALUES ('" +
					getID() + "', '" + plugin.dateTimeFormat.format(date) +
					"', '" + plugin.dateTimeFormat.format(date) + "')";
			plugin.updateSQL(query);
		}

		// Get data from the database using id
		String query = "SELECT * FROM escort WHERE id='" + getID() + "'";

		try {
			ResultSet rs = plugin.querySQL(query);
			rs.first();
			lastLogin = getPlugin().dateTimeFormat.format(new Date());
			lastQuit = rs.getString("last_quit");
			playtime = rs.getInt("playtime");
			kills = rs.getInt("kills");
			deaths = rs.getInt("deaths");
			longestKillStreak = rs.getInt("longest_killstreak");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		getPlugin().closeConnection();
	}

	public void playerQuit() {
		super.playerQuit();
		updateTotalPlayTime();
		String query = "UPDATE escort SET " +
				"last_login='" + lastLogin + "'," +
				"last_quit='" + lastQuit + "'," +
				"playtime=" + playtime + "," +
				"kills=" + kills + "," +
				"deaths=" + deaths + "," +
				"longest_killstreak=" + longestKillStreak + "," +
				" WHERE id=" + getID() + "";
		getPlugin().updateSQL(query);
	}

	public void playerSaveData() {
		super.playerSaveData();
		String query = "UPDATE escort SET " +
				"playtime=" + playtime + "," +
				"kills=" + kills + "," +
				"deaths=" + deaths + "," +
				"longest_killstreak=" + longestKillStreak + "," +
				" WHERE id=" + getID() + "";
		getPlugin().updateSQL(query);
	}

	/**
	 * Update a players total play time. Use this when the player quits
	 */
	private void updateTotalPlayTime() {
		// Create date based on the last login
		Date lastLoginDate = new Date();
		try {
			lastLoginDate = getPlugin().dateTimeFormat.parse(lastLogin);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Get time between now and lastJoin
		Date now = new Date();
		lastQuit = getPlugin().dateTimeFormat.format(now);
		playtime += (int)((now.getTime() - lastLoginDate.getTime())/1000)/60;
	}

	/**
	 * Calculate the players total playtime if they are in game
	 * @return the players new total playtime
	 */
	private int calculateTotalPlayTime() {
		// Create date based on the last login
		Date lastLoginDate = new Date();
		try {
			lastLoginDate = getPlugin().dateTimeFormat.parse(lastLogin);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Get time between now and lastJoin
		Date now = new Date();
		return playtime + (int)((now.getTime() - lastLoginDate.getTime())/1000)/60;
	}

	/**
	 * Get the last login Date and Time
	 * @return String MM/dd/yyyy HH:mm:ss
	 */
	public String getLastLogin() { return lastLogin; }

	/**
	 * Get the last logout Date and Time
	 * @return String MM/dd/yyyy HH:mm:ss
	 */
	public String getLastQuit() { return lastQuit; }

	/**
	 * Get the players total playtime
	 * @return int total minutes of playtime
	 */
	public int getPlaytime() { return calculateTotalPlayTime(); }

	/**
	 * Get the players total number of kills in Escort
	 * @return int of the total kills
	 */
	public int getKills() { return kills; }

	/**
	 * Get the players total number of deaths in Escort
	 * @return int of the total deaths
	 */
	public int getDeaths() { return deaths; }

	/**
	 * Get the players killsteak in the current game
	 * @return int of the killstreak
	 */
	public int getKillstreak() { return killstreak; }

	/**
	 * Get the players longest killstreak ever
	 * @return int longest killstreak ever
	 */
	public int getLongestKillStreak() { return longestKillStreak; }

	/**
	 * Add a kill for the player
	 * @return total number of kills
	 */
	public int addKill() {
		kills++;
		killstreak++;
		if (killstreak > longestKillStreak) { longestKillStreak = killstreak; }
		return kills;
	}

	/**
	 * Add a death for the player
	 * @return total number of deaths
	 */
	public int addDeath() {
		deaths++;
		if (killstreak > longestKillStreak) { longestKillStreak = killstreak; }
		killstreak = 0;
		return deaths;
	}
}
