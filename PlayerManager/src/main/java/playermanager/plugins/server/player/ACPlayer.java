package playermanager.plugins.server.player;

import org.bukkit.entity.Player;
import playermanager.plugins.server.main.PlayerManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

public class ACPlayer extends ServerPlayer {

	private String lastLogin; // Last Login, date and time
	private String lastQuit; // Last Logout, date and time
	private int playtime; // Playtime in minutes
	private int kills; // Kills
	private int deaths; // Deaths
	private int killstreak = 0; // Current Kill Streak
	private int longestKillStreak; // Longest Kill Streak
	private int timesHolding; // Number of times with Diamond Armor
	private int totalTimeHolding; // Total time holding the Armor in seconds
	private int longestTimeHolding; // Longest period of time holding Armor in seconds
	private int chestsOpened; // Number of chests opened

	private Date armorOn;

	public ACPlayer (PlayerManager plugin, Player pl, boolean newPlayer) {
		super(plugin, pl, false); // Cannot be true, it would create duplicates

		// If the player is a noob add them to users database
		if (newPlayer) {
			Date date = new Date();
			String query = "INSERT INTO armorcontrol (id, last_login, last_quit) VALUES ('" +
					getID() + "', '" + plugin.dateTimeFormat.format(date) +
					"', '" + plugin.dateTimeFormat.format(date) + "')";
			plugin.updateSQL(query);
		}

		// Get data from the database using id
		String query = "SELECT * FROM armorcontrol WHERE id='" + getID() + "'";

		try {
			ResultSet rs = plugin.querySQL(query);
			rs.first();
			lastLogin = getPlugin().dateTimeFormat.format(new Date());
			lastQuit = rs.getString("last_quit");
			playtime = rs.getInt("playtime");
			kills = rs.getInt("kills");
			deaths = rs.getInt("deaths");
			longestKillStreak = rs.getInt("longest_killstreak");
			timesHolding = rs.getInt("times_holding");
			totalTimeHolding = rs.getInt("total_holding");
			longestTimeHolding = rs.getInt("longest_holding");
			chestsOpened = rs.getInt("chests_opened");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		getPlugin().closeConnection();
	}

	public void playerQuit() {
		super.playerQuit();
		updateTotalPlayTime();
		String query = "UPDATE armorcontrol SET " +
				"last_login='" + lastLogin + "'," +
				"last_quit='" + lastQuit + "'," +
				"playtime=" + playtime + "," +
				"kills=" + kills + "," +
				"deaths=" + deaths + "," +
				"longest_killstreak=" + longestKillStreak + "," +
				"times_holding=" + timesHolding + "," +
				"total_holding=" + totalTimeHolding + "," +
				"longest_holding=" + longestTimeHolding + "," +
				"chests_opened=" + chestsOpened + "" +
				" WHERE id=" + getID() + "";
		getPlugin().updateSQL(query);
	}

	public void playerSaveData() {
		super.playerSaveData();
		String query = "UPDATE armorcontrol SET " +
				"playtime=" + playtime + "," +
				"kills=" + kills + "," +
				"deaths=" + deaths + "," +
				"longest_killstreak=" + longestKillStreak + "," +
				"times_holding=" + timesHolding + "," +
				"total_holding=" + totalTimeHolding + "," +
				"longest_holding=" + longestTimeHolding + "," +
				"chests_opened=" + chestsOpened + "" +
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
	 * The player is now the Juggernaut
	 */
	public void obtainArmor() {
		Date date = new Date();
		armorOn = date;
		timesHolding++;
	}

	/**
	 * The player has lost the Diamond Armor
	 */
	public void loseArmor() {
		Date now = new Date();
		int timeHeld = (int) (now.getTime() - armorOn.getTime())/1000;
		totalTimeHolding += timeHeld;
		if (timeHeld > longestTimeHolding) { longestTimeHolding = timeHeld; }
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
	 * Get the players total number of kills in ArmorControl
	 * @return int of the total kills
	 */
	public int getKills() { return kills; }

	/**
	 * Get the players total number of deaths in ArmorControl
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
	 * Get the number of times the player has held all the diamond armor
	 * @return int number of times holding armor
	 */
	public int getTimesHolding() { return timesHolding; }

	/**
	 * Get the total time in seconds that the player has held the armor
	 * @return int of seconds the player has held armor for
	 */
	public int getTotalTimeHolding() { return totalTimeHolding; }

	/**
	 * Get the longest period of time the player has been Juggernaut for
	 * @return int of seconds for longest time
	 */
	public int getLongestTimeHolding() { return longestTimeHolding; }

	/**
	 * Get the players total number of chests opened
	 * @return int of the number of chests opened
	 */
	public int getChestsOpened() { return chestsOpened; }

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

	/**
	 * Add a chest opened
	 * @return total number of chests opened
	 */
	public int addChestOpened() {
		chestsOpened++;
		return chestsOpened;
	}
}
