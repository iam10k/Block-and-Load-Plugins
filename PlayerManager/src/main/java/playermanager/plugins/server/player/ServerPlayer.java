package playermanager.plugins.server.player;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import playermanager.plugins.server.main.PlayerManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

public class ServerPlayer {

    // For all servers
    private int id; // ID in database
    private String name; // Username
    private String uuid; // Users Unique ID
	private String nick = ""; // Users Nickname
    private String dateJoined; // Date joined
    private int credits; // Number of credits
    private int totalVotes; // Number of votes
	private String playerRank; // Player rank
	private String staffRank; // Staff Rank
	private double multiplier; // XP Multiplier
    private ArrayList<String> ips; // List of all the ips a player has connected with
	private int banLevel; // Users ban rating (0-NotBanned 1-Banned 2-CompletelyBanned)
	private boolean muted; // Is the player muted
	private String boughtPerks; // The perks the player has bought with XP
	private String badges; // Badges the player has earned
	private int timesSpoken; // Times the player has said something in chat

	private PlayerManager plugin;

    public ServerPlayer(PlayerManager plug, Player pl, boolean newPlayer) {
        Date date = new Date();

        // Load known data
        plugin = plug;
        name = pl.getName();
        uuid = pl.getUniqueId().toString();

        // If the player is a noob add them to users database
        if (newPlayer) {
            String query = "INSERT INTO users (username, uuid, nick_name, date_joined, player_rank, staff_rank, ips, bought_perks, badges) VALUES ('" +
					name + "', '" + uuid + "', '', '" + plugin.dateFormat.format(date) + "', 'default'," +
					" 'null', '" + pl.getAddress().getAddress().getHostAddress() + "', '', '')";
			plugin.updateSQL(query);
        }

        // Get data from the database using UUID
        String query = "SELECT * FROM users WHERE uuid='" + uuid + "'";

        try {
            ResultSet rs = plugin.querySQL(query);
            rs.first();
            id = rs.getInt("id");
            name = rs.getString("username");
			nick = rs.getString("nick_name");
            dateJoined = rs.getString("date_joined");
            credits = rs.getInt("credits");
            totalVotes = rs.getInt("total_votes");
			playerRank = rs.getString("player_rank");
			staffRank = rs.getString("staff_rank");
			multiplier = rs.getDouble("multiplier");
            ips = ipsStringToArray(rs.getString("ips"));
			banLevel = rs.getInt("ban_level");
			muted = rs.getBoolean("muted");
			boughtPerks = rs.getString("bought_perks");
			badges = rs.getString("badges");
			timesSpoken = rs.getInt("times_spoken");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        plugin.closeConnection();
    }

	/**
	 * Used to load a players data who is not online.
	 */
    public ServerPlayer(PlayerManager plug, String pl) {

        // Load known data
        plugin = plug;
        name = pl;

        // Get data from the database using username
        String query = "SELECT * FROM users WHERE username='" + name + "'";

        try {
            ResultSet rs = plugin.querySQL(query);
            rs.first();
            id = rs.getInt("id");
            uuid = rs.getString("uuid");
			nick = rs.getString("nick_name");
            dateJoined = rs.getString("date_joined");
            credits = rs.getInt("credits");
            totalVotes = rs.getInt("total_votes");
			playerRank = rs.getString("player_rank");
			staffRank = rs.getString("staff_rank");
			multiplier = rs.getDouble("multiplier");
			ips = ipsStringToArray(rs.getString("ips"));
			banLevel = rs.getInt("ban_level");
			muted = rs.getBoolean("muted");
			boughtPerks = rs.getString("bought_perks");
			badges = rs.getString("badges");
			timesSpoken = rs.getInt("times_spoken");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        plugin.closeConnection();
    }

    public void playerQuit() {
        String query = "UPDATE users SET " +
                "credits=" + credits + "," +
				"total_votes=" + totalVotes + "," +
                "ips='" + ipsArrayToString() + "'," +
				"times_spoken=" + timesSpoken + "" +
                " WHERE id=" + id + "";
        plugin.updateSQL(query);

		plugin.getLogger().log(Level.INFO, "Saving player data: " + name);
    }

    public void playerSaveGame() {
        String query = "UPDATE users SET " +
                "credits=" + credits + "," +
				"total_votes=" + totalVotes + "," +
				"bought_perks='" + boughtPerks + "'," +
				"badges='" + badges + "'" +
                " WHERE id=" + id + "";
        plugin.updateSQL(query);
    }

	public void playerSave() {
		String query = "UPDATE users SET " +
				"nick_name='" + nick + "'," +
				"player_rank='" + playerRank + "'," +
				"staff_rank='" + staffRank + "'," +
				"multiplier=" + multiplier + "," +
				"ban_level=" + banLevel + "," +
				"muted=" + muted + "" +
				" WHERE id=" + id + "";
		plugin.updateSQL(query);
	}

	public void playerSaveData() {
		String query = "UPDATE users SET " +
				"credits=" + credits + "," +
				"times_spoken=" + timesSpoken + "" +
				" WHERE id=" + id + "";
		plugin.updateSQL(query);
	}

    private ArrayList<String> ipsStringToArray(String s) {
        ArrayList<String> temp = new ArrayList<String>();
        if (s.contains(",")) {
            String[] strings = s.split(",");
            for (int x = 0; x < strings.length; x++) {
                temp.add(strings[x]);
            }
        } else {
            temp.add(s);
        }
        return temp;
    }

    private String ipsArrayToString() {
        String temp = "";
        for (int x = 0; x < ips.size(); x++) {
            temp += ips.get(x);
            if (x < ips.size() - 1) {
                temp += ",";
            }
        }
        return temp;
    }

    public void addIP(String ip) {
        for (String s : ips) {
            if (s.equals(ip)) {
                return;
            }
        }
        ips.add(ip);
    }

    private String getIPsPlayers() {
        String temp = "";

        for (String ip : ips) {
            try {
                String query = "SELECT players FROM ips WHERE ip='" + ip + "'";
                ResultSet rs = plugin.querySQL(query);
                if (rs.first()) {
                    String[] players = rs.getString("players").split(",");
                    temp += "&7" + ip + "%N%";
                    for (int x = 0; x < players.length; x++) {
                        temp += "  &8- &3" + players[x] + "%N%";
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            plugin.closeConnection();
        }

        return temp;
    }

    public String convertPlaytimeMinutes(int time) {
		if (time == 0) {
			return "0 Minutes";
		}

        String s = "";
        int years = time / 525949;
		if (years == 1) { s += years + " Year  "; }
		else if (years > 1) { s += years + " Years  "; }

		int days = (time - (years * 525949)) / 1440;
		if (days == 1) { s += days + " Day  "; }
		else if (days > 1) { s += days + " Days  "; }

		int hours = (time - (years * 525949) - (days * 1440)) / 60;
		if (hours == 1) { s += hours + " Hour  "; }
		else if (hours > 1) { s += hours + " Hours  "; }

		int minutes = (time - (years * 525949) - (days * 1440) - (hours * 60));
		if (minutes == 1) { s += minutes + " Minute"; }
		else if (minutes > 1) { s += minutes + " Minutes"; }
        return s;
    }

	public String convertPlaytimeSeconds(int time) {
		if (time == 0) {
			return "0 Seconds";
		}

		String s = "";
		int years = time / 31560000;
		if (years == 1) { s += years + " Year  "; }
		else if (years > 1) { s += years + " Years  "; }

		int days = (time - (years * 31560000)) / 86400;
		if (days == 1) { s += days + " Day  "; }
		else if (days > 1) { s += days + " Days  "; }

		int hours = (time - (years * 31560000) - (days * 86400)) / 3600;
		if (hours == 1) { s += hours + " Hour  "; }
		else if (hours > 1) { s += hours + " Hours  "; }

		int minutes = (time - (years * 31560000) - (days * 86400) - (hours * 3600)) / 60;
		if (minutes == 1) { s += minutes + " Minute  "; }
		else if (minutes > 1) { s += minutes + " Minutes  "; }

		int seconds = (time - (years * 31560000) - (days * 86400) - (hours * 3600) - (minutes * 60));
		if (seconds == 1) { s += seconds + " Second"; }
		else if (seconds > 1) { s += seconds + " Seconds"; }
		return s;
	}

	// BEGIN ALL SERVER METHODS
	/**
	 * Get PlayerManager plugin
	 * @return PlayerManager plugin
	 */
	public PlayerManager getPlugin() { return plugin; }

	/**
	 * Get the players ID in the database
	 * @return id in the database
	 */
	public int getID() { return id; }

	/**
	 * Get the players Minecraft Unique ID
	 * @return the players unique id
	 */
    public String getUUID() { return uuid; }

	/**
	 * Get the users Minecraft name
	 * @return the players username
	 */
    public String getName() { return name; }

	/**
	 * Get the users  nickname
	 * @return the players nickname if valid
	 */
	public String getNick() {
		if (nick != null && !nick.equals("")) {
			return nick;
		}
		return name;
	}

	/**
	 * Get the string format of the date the player joined the network
	 * @return date joined the network MM/DD/YYYY
	 */
	public String getDateJoined() { return dateJoined; }

	/**
	 * Get the players total credits
	 * @return the players total credits
	 */
	public int getCredits() { return credits; }

	/**
	 * Get the a players total votes
	 * @return the players total votes
	 */
	public int getTotalVotes() { return totalVotes; }

	/**
	 * Get the players rank
	 * @return string of the players rank
	 */
	public String getPlayerRank() { return playerRank; }

	/**
	 * Get the players rank
	 * @return string of the players rank
	 */
	public String getStaffRank() { return staffRank; }

	/**
	 * Get the players XP multiplier
	 * @return double of players multiplier
	 */
	public double getMultiplier() { return multiplier; }

	/**
	 * Get the players ranks
	 * @return string[] of the players ranks
	 */
	public String[] getRanks() {
		if (staffRank == null || staffRank.equals("null")) {
			String[] ranks = new String[1];
			ranks[0] = playerRank;
			return ranks;
		} else if (playerRank.equals("default") && !staffRank.equals("null")) {
			String[] ranks = new String[1];
			ranks[0] = staffRank;
			return ranks;
		} else {
			String[] ranks = new String[2];
			ranks[0] = playerRank;
			ranks[1] = staffRank;
			return ranks;
		}
	}

	/**
	 * Get the players ban level
	 * @return int of players ban level
	 */
	public int getBanLevel() { return banLevel; }

	/**
	 * Get the players mute state
	 * @return boolean of player mute state
	 */
	public boolean getMuteState() { return muted; }

	/**
	 * Get the perks the player has bought with XP
	 * @return ArrayList of perk ids
	 */
	public ArrayList<String> getBoughtPerks() {
		ArrayList<String> temp = new ArrayList<String>();
		if (boughtPerks != null && !boughtPerks.equals("")) {
			for (String s : boughtPerks.split(",")) {
				temp.add(s);
			}
			return temp;
		}
		temp.add("");
		return temp;
	}

	/**
	 * Get the badges the player has earned
	 * @return ArrayList of badge ids
	 */
	public ArrayList<String> getBadges() {
		ArrayList<String> temp = new ArrayList<String>();
		if (badges != null && !badges.equals("")) {
			for (String s : badges.split(",")) {
				temp.add(s);
			}
			return temp;
		}
		temp.add("");
		return temp;
	}

	/**
	 * Get the number of times a player has spoke in chat
	 * @return int of times spoken
	 */
	public int getTimesSpoken() { return timesSpoken; }

	/**
	 * Set the users nickname
	 * @param nickname will be new nickname
	 * @return users nickname
	 */
	public String setNick(String nickname) {
		nick = nickname;
		playerSave();
		return nick;
	}

	/**
	 * Add credits to the player
	 * @param add amount of credits to add
	 * @return players total credits
	 */
	public int addCredits(int add) {
		credits += (int) (add * multiplier);
		sendMessage("    &a+" + plugin.format((int) (add * multiplier)) + " B&&aL XP!");
		return credits;
	}

	/**
	 * Remove credits to the player
	 * @param rem amount of credits to remove
	 * @return players total credits
	 */
	public int removeCredits(int rem) {
		credits -= rem;
		sendMessage("    &c-" + plugin.format(rem) + " B&&cL XP!");
		return credits;
	}

	/**
	 * Add a vote to the player
	 * @return players total votes
	 */
	public int addVote() {
		totalVotes++;
		credits += 250;
		return totalVotes;
	}

	/**
	 * Set the players new rank
	 * @param newPlayerRank string name of rank
	 * @return new rank string
	 */
	public String setPlayerRank(String newPlayerRank) {
		playerRank = newPlayerRank;
		playerSave();
		return playerRank;
	}

	/**
	 * Set the players new staff rank
	 * @param newStaffRank string name of rank
	 * @return new rank string
	 */
	public String setStaffRank(String newStaffRank) {
		staffRank = newStaffRank;
		playerSave();
		return staffRank;
	}

	/**
	 * Set new multipler
	 * @param newMultiplier double for new multiplier
	 * @return new multipler value
	 */
	public double setMultiplier(Double newMultiplier) {
		multiplier = newMultiplier;
		if (badges.contains("0002ALPHA")) {
			multiplier += 1.5;
		} else if (badges.contains("0001ALPHA")) {
			multiplier += 1.0;
		} else if (badges.contains("0000ALPHA")) {
			multiplier += 0.5;
		}
		playerSave();
		return multiplier;
	}

	/**
	 * Ban the player
	 * @param level is 1 or 2
	 * @return new ban level
	 */
	public int banPlayer(int level) {
		banLevel = level;
		playerSave();
		return banLevel;
	}

	/**
	 * Unban the player
	 * @return new ban level
	 */
	public int unbanPlayer() {
		banLevel = 0;
		playerSave();
		return banLevel;
	}

	/**
	 * Mute the player
	 * @return if the player is muted
	 */
	public boolean mutePlayer() {
		muted = true;
		playerSave();
		return muted;
	}

	/**
	 * Unmute the player
	 * @return player mute state
	 */
	public boolean unmutePlayer() {
		muted = false;
		playerSave();
		return muted;
	}

	/**
	 * Add bought perk to player
	 * @param newPerk perk that the player bought with xp
	 * @return all the players perks
	 */
	public ArrayList<String> addBoughtPerk(String newPerk) {
		boughtPerks += "," + newPerk;
		playerSaveGame();
		ArrayList<String> temp = new ArrayList<String>();
		for (String s : boughtPerks.split(",")) {
			temp.add(s);
		}
		return temp;
	}

	/**
	 * Add badge to player
	 * @param newBadge to add to the player
	 * @return all the players badges
	 */
	public ArrayList<String> addBadge(String newBadge) {
		badges += "," + newBadge;
		playerSaveGame();
		ArrayList<String> temp = new ArrayList<String>();
		for (String s : badges.split(",")) {
			temp.add(s);
		}
		return temp;
	}

	/**
	 * Increment the number of times the player has spoken
	 * @return total times the player has spoken
	 */
	public int addTimesSpoken() {
		timesSpoken++;
		return timesSpoken;
	}

	public void sendMessage(String msg) {
		plugin.sendMessage(uuid, ChatColor.translateAlternateColorCodes('&', msg));
	}
	// END ALL SERVER METHODS

}
