package playermanager.plugins.server.player;

import org.bukkit.entity.Player;
import playermanager.plugins.server.main.PlayerManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

public class FACPlayer extends ServerPlayer {

	private String lastLogin; // Last Login, date and time
	private String lastQuit; // Last Logout, date and time
	private int playtime; // Playtime in minutes
	private int killsSword; // Kills sword
	private int killsBow; // Kills bow
	private int deaths; // Deaths
	private int killstreak = 0; // Current Kill Streak
	private int longestKillStreak; // Longest Kill Streak
	private int blocksPlaced; // Number of blocks placed
	private int blocksBroken; // Number of blocks broken
	private int stone = 0; // Stone Broken
	private int coal = 0; // Coal Broken
	private int lapis = 0; // Lapis Broken
	private int redstone = 0; // Redstone Broken
	private int iron = 0; // Iron Broken
	private int gold = 0; // Gold Broken
	private int emerald = 0; // Emerald  Broken
	private int diamond = 0; // Diamond Broken
	private String miningBanned = ""; // String date that they were caught xraying

	public FACPlayer(PlayerManager plugin, Player pl, boolean newPlayer) {
		super(plugin, pl, false); // Cannot be true, it would create duplicates

		// If the player is a noob add them to users database
		if (newPlayer) {
			Date date = new Date();
			String query = "INSERT INTO factions (id, last_login, last_quit, mining_banned) VALUES ('" +
					getID() + "', '" + plugin.dateTimeFormat.format(date) +
					"', '" + plugin.dateTimeFormat.format(date) + "','')";
			plugin.updateSQL(query);
		}

		// Get data from the database using id
		String query = "SELECT * FROM factions WHERE id='" + getID() + "'";

		try {
			ResultSet rs = plugin.querySQL(query);
			rs.first();
			lastLogin = getPlugin().dateTimeFormat.format(new Date());
			lastQuit = rs.getString("last_quit");
			playtime = rs.getInt("playtime");
			killsSword = rs.getInt("kills_sword");
			killsBow = rs.getInt("kills_bow");
			deaths = rs.getInt("deaths");
			killstreak = rs.getInt("killstreak");
			longestKillStreak = rs.getInt("longest_killstreak");
			blocksPlaced = rs.getInt("blocks_placed");
			blocksBroken = rs.getInt("blocks_broken");
			stone = rs.getInt("stone_mined");
			coal = rs.getInt("coal_mined");
			lapis = rs.getInt("lapis_mined");
			redstone = rs.getInt("redstone_mined");
			iron = rs.getInt("iron_mined");
			gold = rs.getInt("gold_mined");
			emerald = rs.getInt("emerald_mined");
			diamond = rs.getInt("diamond_mined");
			miningBanned = rs.getString("mining_banned");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		getPlugin().closeConnection();
	}

	public void playerQuit() {
		super.playerQuit();
		updateTotalPlayTime();
		String query = "UPDATE factions SET " +
				"last_login='" + lastLogin + "'," +
				"last_quit='" + lastQuit + "'," +
				"playtime=" + playtime + "," +
				"kills_sword=" + killsSword + "," +
				"kills_bow=" + killsBow + "," +
				"deaths=" + deaths + "," +
				"killstreak=" + killstreak + "," +
				"longest_killstreak=" + longestKillStreak + "," +
				"blocks_placed=" + blocksPlaced + "," +
				"blocks_broken=" + blocksBroken + "," +
				"stone_mined=" + stone + "," +
				"coal_mined=" + coal + "," +
				"lapis_mined=" + lapis + "," +
				"redstone_mined=" + redstone + "," +
				"iron_mined=" + iron + "," +
				"gold_mined=" + gold + "," +
				"emerald_mined=" + emerald + "," +
				"diamond_mined=" + diamond + "," +
				"mining_banned='" + miningBanned + "'" +
				" WHERE id=" + getID() + "";
		getPlugin().updateSQL(query);
	}

	public void playerSaveData() {
		super.playerSaveData();
		String query = "UPDATE factions SET " +
				"playtime=" + playtime + "," +
				"kills_sword=" + killsSword + "," +
				"kills_bow=" + killsBow + "," +
				"deaths=" + deaths + "," +
				"killstreak=" + killstreak + "," +
				"longest_killstreak=" + longestKillStreak + "," +
				"blocks_placed=" + blocksPlaced + "," +
				"blocks_broken=" + blocksBroken + "," +
				"stone_mined=" + stone + "," +
				"coal_mined=" + coal + "," +
				"lapis_mined=" + lapis + "," +
				"redstone_mined=" + redstone + "," +
				"iron_mined=" + iron + "," +
				"gold_mined=" + gold + "," +
				"emerald_mined=" + emerald + "," +
				"diamond_mined=" + diamond + "," +
				"mining_banned='" + miningBanned + "'" +
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
	 * Get the players total number of kills in Factions
	 * @return int of the total kills
	 */
	public int getKills() { return killsSword + killsBow; }

	/**
	 * Get the players total number of sword kills in Factions
	 * @return int of sword kills
	 */
	public int getSwordKills() { return killsSword; }

	/**
	 * Get the players total number of bow kills in Factions
	 * @return int of bow kills
	 */
	public int getBowKills() { return killsBow; }

	/**
	 * Get the players total number of deaths in Factions
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
	 * Get the number of blocks placed by the player
	 * @return int number of blocks placed
	 */
	public int getBlocksPlaced() { return blocksPlaced; }

	/**
	 * Get the total number of blocks broken by the player
	 * @return int number of blocks broken
	 */
	public int getBlocksBroken() { return blocksBroken; }

	/**
	 * Get Stone Mined
	 * @return Stone
	 */
	public int getStone() { return stone; }

	/**
	 * Add 1 to Stone Mined
	 * @return Stone
	 */
	public int addStone() { return stone++; }

	/**
	 * Get Coal Mined
	 * @return Coal
	 */
	public int getCoal() { return coal; }

	/**
	 * Add 1 to Coal Mined
	 * @return Coal
	 */
	public int addCoal() { return coal++; }

	/**
	 * Get Lapis Mined
	 * @return Lapis
	 */
	public int getLapis() { return lapis; }

	/**
	 * Add 1 to Lapis Mined
	 * @return Lapis
	 */
	public int addLapis() { return lapis++; }

	/**
	 * Get Redstone Mined
	 * @return Redstone
	 */
	public int getRedstone() { return redstone; }

	/**
	 * Add 1 to Redstone Mined
	 * @return Redstone
	 */
	public int adddRedstone() { return redstone++; }

	/**
	 * Get Iron Mined
	 * @return Iron
	 */
	public int getIron() { return iron; }

	/**
	 * Add 1 to Iron Mined
	 * @return Iron
	 */
	public int addIron() { return iron++; }

	/**
	 * Get Gold Mined
	 * @return Gold
	 */
	public int getGold() { return gold; }

	/**
	 * Add 1 to Gold Mined
	 * @return Gold
	 */
	public int addGold() { return gold++; }

	/**
	 * Get Emerald Mined
	 * @return Emerald
	 */
	public int getEmerald() { return emerald; }

	/**
	 * Add 1 to Emerald Mined
	 * @return Emerald
	 */
	public int addEmerald() { return emerald++; }

	/**
	 * Get Diamond Mined
	 * @return Diamond
	 */
	public int getDiamond() { return diamond; }

	/**
	 * Add 1 to Diamond Mined
	 * @return Diamond
	 */
	public int addDiamond() { return diamond++; }

	public double oreTotal() { return coal + lapis + redstone + iron + gold + emerald + diamond; }

	public double oreTotalWeighted() { return coal*0.5 + lapis + redstone*0.96 + iron*.65 + gold*3.55 + emerald*4 + diamond*4.5; }

	public double mineRate() {
		if (stone == 0) {
			return oreTotal();
		} else {
			return oreTotal() / stone * 100;
		}
	}

	public double mineRateWeighted() {
		if (stone == 0) {
			return oreTotalWeighted();
		} else {
			return oreTotalWeighted() / stone * 100;
		}
	}

	/**
	 * Check if this player should have orebfusactor enabled
	 * @return true if they need it
	 */
	public boolean enableOrebfusactor() {
		return oreTotal() + stone > 200 && mineRateWeighted() > 55;
	}

	/**
	 * Check if the player is banned from mining
	 * @return true if they are
	 */
	public boolean miningBanned() {
		if (miningBanned != null && !miningBanned.equals("null") && !miningBanned.equals("")) {
			// Create date based on the miningBanned string
			Date miningDate = new Date();
			try {
				miningDate = getPlugin().dateTimeFormat.parse(miningBanned);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			Date now = new Date();

			if (now.getTime() - miningDate.getTime() >= 604800000) {
				miningBanned = "";
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Add a sword kill for the player
	 * @return total number of sword kills
	 */
	public int addSwordKill() {
		killsSword++;
		killstreak++;
		if (killstreak > longestKillStreak) { longestKillStreak = killstreak; }
		return killsSword;
	}

	/**
	 * Add a bow kill for the player
	 * @return total number of bow kills
	 */
	public int addBowKill() {
		killsBow++;
		killstreak++;
		if (killstreak > longestKillStreak) { longestKillStreak = killstreak; }
		return killsBow;
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
	 * Add a block placed
	 * @return total number of blocks placed
	 */
	public int addBlockPlaced() {
		blocksPlaced++;
		return blocksPlaced;
	}

	/**
	 * Add a block broken
	 * @return total number of blocks broken
	 */
	public int addBlockBroken() {
		blocksBroken++;
		return blocksBroken;
	}

	/**
	 * Set a player banned from mining
	 */
	public void setMiningBanned(boolean unban) {
		Date now = new Date();
		miningBanned = getPlugin().dateTimeFormat.format(now);
		if (unban) {
			miningBanned = "";
		}
	}
}
