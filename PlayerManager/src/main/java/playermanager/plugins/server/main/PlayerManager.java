package playermanager.plugins.server.main;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import playermanager.plugins.server.commands.*;
import playermanager.plugins.server.listeners.EventsListener;
import playermanager.plugins.server.player.ACPlayer;
import playermanager.plugins.server.player.BBPlayer;
import playermanager.plugins.server.player.FACPlayer;
import playermanager.plugins.server.player.ServerPlayer;
import playermanager.plugins.server.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class PlayerManager extends JavaPlugin {

    private MYSQL mysql;

	public String server;

    public DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public DateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	public DateFormat dateTimeReportFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public ArrayList<ServerPlayer> onlinePlayers = new ArrayList<ServerPlayer>();

	//Blocks
	private HashSet<Material> transparent = new HashSet<Material>(55);
	private HashSet<Material> interact = new HashSet<Material>(30);

	public NumberFormat df = NumberFormat.getInstance();
	private Random rand = new Random();

    public void onEnable() {
        String host = getConfig().getString("Connection.Database.host");
        String port = getConfig().getString("Connection.Database.port");
        String database = getConfig().getString("Connection.Database.database");
        String user = getConfig().getString("Connection.Database.user");
        String password = getConfig().getString("Connection.Database.password");

		server = getConfig().getString("Server.name").toLowerCase();

        mysql = new MYSQL(this,host,port,database,user,password);

		// Add defaults and load config
		configDefaults();
		loadConfig();

		// Register Events & Commands
		getServer().getPluginManager().registerEvents(new EventsListener(this), this);
		getCommand("ban").setExecutor(new BanCommand(this));
		getCommand("unban").setExecutor(new UnbanCommand(this));
		getCommand("mute").setExecutor(new MuteCommand(this));
		getCommand("unmute").setExecutor(new UnmuteCommand(this));
		getCommand("setrank").setExecutor(new SetRankCommand(this));
		getCommand("setmultiplier").setExecutor(new SetMultiplierCommand(this));
		getCommand("addbadge").setExecutor(new AddBadgeCommand(this));
		getCommand("addcredits").setExecutor(new AddCreditsCommand(this));
		getCommand("player").setExecutor(new PlayerCommand(this));
		getCommand("nick").setExecutor(new NickCommand(this));
		getCommand("playermanager").setExecutor(new ReloadCommand(this));
		getCommand("report").setExecutor(new ReportCommand(this));
		getCommand("reports").setExecutor(new ReportsCommand(this));
		getCommand("mem").setExecutor(new MemCommand(this));

		getServer().getScheduler().runTaskTimer(this, new DataSaver(this), 30000L, 30000L);


		// Load Freecam Block
		loadInteractBlocks();
		loadTransparentBlocks();

		// Load Announcer
		restartAnnouncements();
    }

	public void onDisable() {
		for (Player player : getServer().getOnlinePlayers()) {
			removePlayer(player);
			player.kickPlayer(ChatColor.RED + "Server is restarting, rejoin in a minute!");
		}
	}

	/**
	 * Copy config defaults
	 */
	private void configDefaults() {
		getConfig().addDefault("Server.name", "");
		getConfig().addDefault("Server.restartCommand", "screen -S server ./run.sh");
		getConfig().addDefault("Connection.Database.host", "localhost");
		getConfig().addDefault("Connection.Database.port", 3306);
		getConfig().addDefault("Connection.Database.host", "blockandload");
		getConfig().addDefault("Connection.Database.host", "minecraft");
		getConfig().addDefault("Connection.Database.host", "password");
		getConfig().addDefault("Announcements.list", Arrays.asList("a" , "b"));
		getConfig().addDefault("Announcements.broadcastInterval", 40);
		getConfig().addDefault("AutoDeOp.playersAllowedOp", Arrays.asList("EnkyHD"));

		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	/**
	 * Load/Reload config
	 */
	public void loadConfig() {
		reloadConfig();
	}

    // BEGIN MYSQL METHODS
    public ResultSet querySQL(String query) { return mysql.querySQL(query); }

    public void updateSQL(String update) { mysql.updateSQL(update); }

    /**
     * Not recommended for use.
     * Use querySQL and updateSQL
     */
    @Deprecated
    public MYSQL getMysql() { return mysql; }

    public void closeConnection() { mysql.closeConnection(); }
    // END MYSQL METHODS


	// BEGIN ONLINEPLAYER METHODS
	public ServerPlayer addPlayer(Player pl) {
		if (server.equals("hub")) {
			if (!isInUsersDatabaseByUUID(pl.getUniqueId().toString()).equals("")) {
				return addPlayer(pl, false);
			} else {
				return addPlayer(pl, true);
			}
		} else {
			if (isInGameDatabase(server, getIDFromUUID(pl.getUniqueId().toString()))) {
				return addPlayer(pl, false);
			} else {
				return addPlayer(pl, true);
			}
		}
	}

	public ServerPlayer addPlayer(Player pl, boolean newPlayer) {
		if (!newPlayer) {
			if (server.equals("hub")) {
				ServerPlayer player = new ServerPlayer(this, pl, false);
				onlinePlayers.add(player);
				return player;
			} else if (server.equals("armorcontrol")) {
				ACPlayer player = new ACPlayer(this, pl, false);
				onlinePlayers.add(player);
				return player;
			} else if (server.equals("factions")) {
				FACPlayer player = new FACPlayer(this, pl, false);
				onlinePlayers.add(player);
				return player;
			} else if (server.equals("blockbattles")) {
				BBPlayer player = new BBPlayer(this, pl, false);
				onlinePlayers.add(player);
				return player;
			}
		} else {
			if (server.equals("hub")) {
				ServerPlayer player = new ServerPlayer(this, pl, true);
				onlinePlayers.add(player);
				return player;
			} else if (server.equals("armorcontrol")) {
				ACPlayer player = new ACPlayer(this, pl, true);
				onlinePlayers.add(player);
				return player;
			} else if (server.equals("factions")) {
				FACPlayer player = new FACPlayer(this, pl, true);
				onlinePlayers.add(player);
				return player;
			} else if (server.equals("blockbattles")) {
				BBPlayer player = new BBPlayer(this, pl, true);
				onlinePlayers.add(player);
				return player;
			}
		}
		return null;
	}

	public void removePlayer(Player pl) {
		ServerPlayer player = null;
		if (server.equals("hub")) {
			player = getServerPlayer(pl);
		} else if (server.equals("armorcontrol")) {
			player = getACPlayer(pl);
		} else if (server.equals("factions")) {
			player = getFACPlayer(pl);
		} else if (server.equals("blockbattles")) {
			player = getBBPlayer(pl);
		}
		if (player != null) {
			player.playerQuit();
			onlinePlayers.remove(player);
		}
	}

	public void savePlayer(Player pl) {
		ServerPlayer player = null;
		if (server.equals("hub")) {
			player = getServerPlayer(pl);
		} else if (server.equals("armorcontrol")) {
			player = getACPlayer(pl);
		} else if (server.equals("factions")) {
			player = getFACPlayer(pl);
		} else if (server.equals("blockbattles")) {
			player = getBBPlayer(pl);
		}
		if (player != null) {
			player.playerSaveData();
		}
	}

	public int kickIfBanned(ServerPlayer serverPlayer) {
		if (server.equals("hub")) {
			if (serverPlayer.getBanLevel() == 2) {
				return 2;
			}
		} else if (server.equals("hacking")) {
			if (serverPlayer.getBanLevel() == 2) {
				return 2;
			}
		} else {
			if (serverPlayer.getBanLevel() == 1) {
				return 1;
			}
		}
		return 0;
	}
	// END ONLINEPLAYER METHODS


	// BEGIN GETPLAYER METHODS
    /**
     * @param pl is an online player
     * @return null should not return but will if player is not online
     */
    public ServerPlayer getServerPlayer(Player pl) {
        for (ServerPlayer serPl : onlinePlayers) {
            if (serPl.getUUID().equals(pl.getUniqueId().toString())) {
                return serPl;
            }
        }
        return null;
    }

    /**
     * @param name of online or offline player - ignores case
     * @return null if player does not exist
     */
    public ServerPlayer getServerPlayer(String name) {
        for (ServerPlayer serPl : onlinePlayers) {
            if (serPl.getName().equalsIgnoreCase(name)) {
                return serPl;
            }
        }
        String correctName = isInUsersDatabase(name);
        if (!correctName.equals("")) {
            return new ServerPlayer(this, correctName);
        }
        return null;
    }

	/**
	 * @param pl is an online player
	 * @return null should not return but will if player is not online
	 */
	public ACPlayer getACPlayer(Player pl) {
		for (ServerPlayer serPl : onlinePlayers) {
			if (serPl.getUUID().equals(pl.getUniqueId().toString())) {
				return (ACPlayer) serPl;
			}
		}
		return null;
	}

	/**
	 * @param pl is an online player
	 * @return null should not return but will if player is not online
	 */
	public FACPlayer getFACPlayer(Player pl) {
		for (ServerPlayer serPl : onlinePlayers) {
			if (serPl.getUUID().equals(pl.getUniqueId().toString())) {
				return (FACPlayer) serPl;
			}
		}
		return null;
	}

	/**
	 * @param pl is an online player
	 * @return null should not return but will if player is not online
	 */
	public BBPlayer getBBPlayer(Player pl) {
		for (ServerPlayer serPl : onlinePlayers) {
			if (serPl.getUUID().equals(pl.getUniqueId().toString())) {
				return (BBPlayer) serPl;
			}
		}
		return null;
	}

    /**
     * @param uuid is unique id for online player
     * @return online Player - null if player is not online
     */
    public Player getPlayer(String uuid) {
        for (Player pl : getServer().getOnlinePlayers()) {
            if (pl.getUniqueId().toString().equals(uuid)) {
                return pl;
            }
        }
        return null;
    }
	// END GET PLAYER METHODS


	// BEGIN DATABASE METHODS
    /**
     * @param name of possible player
     * @return Corrected case of name if player is in database -
     * "" if player was not found in database
     */
    public String isInUsersDatabase(String name) {
        String query = "SELECT username FROM users";
        ResultSet rs = querySQL(query);
        try {
            while (rs.next()) {
                if (rs.getString("username").equalsIgnoreCase(name)) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

	/**
	 * @param uuid of possible player
	 * @return Corrected case of name if player is in database -
	 * "" if player was not found in database
	 */
	public String isInUsersDatabaseByUUID(String uuid) {
		String query = "SELECT username,uuid FROM users";
		ResultSet rs = querySQL(query);
		try {
			while (rs.next()) {
				if (rs.getString("uuid").equals(uuid)) {
					return rs.getString("username");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @param id of possible player
	 * @return true if player has played the gamemode
	 */
	public boolean isInGameDatabase(String game, int id) {
		String query = "SELECT id FROM " + game.toLowerCase();
		ResultSet rs = querySQL(query);
		try {
			while (rs.next()) {
				if (rs.getInt("id") == id) {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Get the players username from the database using their id
	 * @return players username
	 */
	public String getNameFromID(int id) {
		String query = "SELECT username FROM users WHERE id=" + id;
		ResultSet rs = querySQL(query);
		try {
			if (rs.next()) {
				return rs.getString("username");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Get the players id from the database using their uuid
	 * @return players id
	 */
	public int getIDFromUUID(String uuid) {
		String query = "SELECT id FROM users WHERE uuid='" + uuid + "'";
		ResultSet rs = querySQL(query);
		try {
			if (rs.next()) {
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
    // END DATABASE METHODS


	// BEGIN OTHER METHODS
	public String getRankPrefix(Player player, ServerPlayer serverPlayer) {
		String playerRank = serverPlayer.getPlayerRank();
		String staffRank = serverPlayer.getStaffRank();
		String prefix = "";
		if (!playerRank.equals("default") || (staffRank != null && !staffRank.equals("") && !staffRank.equals("null"))) {
			prefix += getPrefix(player);
		}
		return prefix;
	}

	public String getRankSuffix(Player player, ServerPlayer serverPlayer) {
		String playerRank = serverPlayer.getPlayerRank();
		String staffRank = serverPlayer.getStaffRank();
		String suffix = "";
		if (!playerRank.equals("default") || (staffRank != null && !staffRank.equals("") && !staffRank.equals("null"))) {
			suffix += getSuffix(player);
		}
		return suffix;
	}

	/**
	 * Return the color char such as &c for the players team
	 * @param player is online player
	 * @return &7 by default team otherwise
	 */
	public String getTeam(Player player) {
		return "";
	}

	public String format(double x) {
		return df.format(x);
	}

	public String format(long x) {
		return df.format(x);
	}
	// END OTHER METHODS

    // BEGIN SEND MESSAGES TO PLAYER
    /**
     * @param uuid is for valid online player
     * @param msg will be formatted with ChatColor.translate
     */
    public void sendMessage(String uuid, String msg) {
		if (getPlayer(uuid) != null) {
			getPlayer(uuid).sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}
	}

    /**
     * @param uuid is for valid online player
     * @param msgs will be formatted with ChatColor.translate
     */
    public void sendMessage(String uuid, String[] msgs) {
		if (getPlayer(uuid) != null) {
			for (String msg : msgs) {
				getPlayer(uuid).sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			}
		}
	}

	/**
	 * @param uuid is for valid online player
	 * @param msgs will be formatted with ChatColor.translate
	 */
	public void sendMessage(String uuid, ArrayList<String> msgs) {
		if (getPlayer(uuid) != null) {
			for (String msg : msgs) {
				getPlayer(uuid).sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			}
		}
	}

    /**
     * @param pl is for valid online player
     * @param msg will be formatted with ChatColor.translate
     */
    public void sendMessage(CommandSender pl, String msg) {
		pl.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

    /**
     * @param pl is for valid online player
     * @param msgs will be formatted with ChatColor.translate
     */
    public void sendMessage(CommandSender pl, String[] msgs) {
		for (String msg : msgs) {
			pl.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}
	}

	/**
	 * @param pl is for valid online player
	 * @param msgs will be formatted with ChatColor.translate
	 */
	public void sendMessage(CommandSender pl, ArrayList<String> msgs) {
		for (String msg : msgs) {
			pl.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}
	}

	/**
	 * @param pl is for valid online player
	 * @param msg will be formatted with ChatColor.translate
	 */
	public void sendMessage(Player pl, String msg) {
		pl.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

	/**
	 * @param pl is for valid online player
	 * @param msgs will be formatted with ChatColor.translate
	 */
	public void sendMessage(Player pl, String[] msgs) {
		for (String msg : msgs) {
			pl.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}
	}

	/**
	 * @param pl is for valid online player
	 * @param msgs will be formatted with ChatColor.translate
	 */
	public void sendMessage(Player pl, ArrayList<String> msgs) {
		for (String msg : msgs) {
			pl.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}
	}
    // END SEND MESSAGES TO PLAYER


	// BEGIN PERMISSIONS METHODS
	public void enableAntiXray(Player p) {
		if (getFACPlayer(p).enableOrebfusactor()) {
			addPermission(p, "^Orebfuscator.deobfuscate");
		} else {
			removePermission(p, "Orebfuscator.deobfuscate");
		}
	}

	public void setGroups(Player player, String[] groups) {
		if (groups.length == 1) {
			ApiLayer.setGroup(player.getWorld().getName(), CalculableType.USER, player.getName(), groups[0]);
		} else if (groups.length == 2) {
			ApiLayer.setGroup(player.getWorld().getName(), CalculableType.USER, player.getName(), groups[0]);
			ApiLayer.addGroup(player.getWorld().getName(), CalculableType.USER, player.getName(), groups[1]);
		}
	}

	public void setGroups(String player, String[] groups) {
		if (groups.length == 1) {
			ApiLayer.setGroup("world", CalculableType.USER, player, groups[0]);
		} else if (groups.length == 2) {
			ApiLayer.setGroup("world", CalculableType.USER, player, groups[0]);
			ApiLayer.addGroup("world", CalculableType.USER, player, groups[1]);
		}
	}

	public String getPrefix(Player player) {
		return ApiLayer.getValue(player.getWorld().getName(), CalculableType.USER, player.getName(), "prefix");
	}

	public String getSuffix(Player player) {
		return ApiLayer.getValue(player.getWorld().getName(), CalculableType.USER, player.getName(), "suffix");
	}

	public void addPermission(Player player, String string) {
		ApiLayer.addPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), Permission.loadFromString(string));
	}

	public void removePermission(Player player, String string) {
		ApiLayer.removePermission(player.getWorld().getName(), CalculableType.USER, player.getName(), string);
	}
	// END PERMISSIONS METHODS


	// BEGIN BLOCK FREECAM METHODS
	public void loadInteractBlocks() {
		interact.clear();
		addInteractBlock(Material.DISPENSER);
		addInteractBlock(Material.NOTE_BLOCK);
		addInteractBlock(Material.BED_BLOCK);
		addInteractBlock(Material.CHEST);
		addInteractBlock(Material.TRAPPED_CHEST);
		addInteractBlock(Material.WORKBENCH);
		addInteractBlock(Material.FURNACE);
		addInteractBlock(Material.BURNING_FURNACE);
		addInteractBlock(Material.WOODEN_DOOR);
		addInteractBlock(Material.LEVER);
		addInteractBlock(Material.REDSTONE_ORE);
		addInteractBlock(Material.STONE_BUTTON);
		addInteractBlock(Material.JUKEBOX);
		addInteractBlock(Material.CAKE_BLOCK);
		addInteractBlock(Material.DIODE_BLOCK_ON);
		addInteractBlock(Material.DIODE_BLOCK_OFF);
		addInteractBlock(Material.TRAP_DOOR);
		addInteractBlock(Material.FENCE_GATE);
		addInteractBlock(Material.ENCHANTMENT_TABLE);
		addInteractBlock(Material.BREWING_STAND);
		addInteractBlock(Material.DRAGON_EGG);
		addInteractBlock(Material.ENDER_CHEST);
		addInteractBlock(Material.COMMAND);
		addInteractBlock(Material.BEACON);
		addInteractBlock(Material.WOOD_BUTTON);
		addInteractBlock(Material.ANVIL);
		addInteractBlock(Material.TRAPPED_CHEST);
		addInteractBlock(Material.REDSTONE_COMPARATOR_ON);
		addInteractBlock(Material.REDSTONE_COMPARATOR_OFF);
		addInteractBlock(Material.HOPPER);
		addInteractBlock(Material.DROPPER);
	}

	public HashSet<Material> getTransparentBlocks() { return transparent; }

	public void loadTransparentBlocks() {
		transparent.clear();
		//ToDo: add extras to config file
		addTransparentBlock(Material.AIR);
		/* Misc */
		addTransparentBlock(Material.CAKE_BLOCK);

		/* Redstone Material */
		addTransparentBlock(Material.REDSTONE);
		addTransparentBlock(Material.REDSTONE_WIRE);

		/* Redstone Torches */
		addTransparentBlock(Material.REDSTONE_TORCH_OFF);
		addTransparentBlock(Material.REDSTONE_TORCH_ON);

		/* Diodes (Repeaters) */
		addTransparentBlock(Material.DIODE_BLOCK_OFF);
		addTransparentBlock(Material.DIODE_BLOCK_ON);
		addInteractBlock(Material.REDSTONE_COMPARATOR_ON);
		addInteractBlock(Material.REDSTONE_COMPARATOR_OFF);

		/* Blocks */
		addInteractBlock(Material.ANVIL);
		addInteractBlock(Material.HOPPER);

		/* Power Sources */
		addTransparentBlock(Material.DETECTOR_RAIL);
		addTransparentBlock(Material.LEVER);
		addTransparentBlock(Material.STONE_BUTTON);
		addTransparentBlock(Material.WOOD_BUTTON);
		addTransparentBlock(Material.STONE_PLATE);
		addTransparentBlock(Material.WOOD_PLATE);

		/* Nature Material */
		addTransparentBlock(Material.RED_MUSHROOM);
		addTransparentBlock(Material.BROWN_MUSHROOM);

		addTransparentBlock(Material.RED_ROSE);
		addTransparentBlock(Material.YELLOW_FLOWER);

		addTransparentBlock(Material.FLOWER_POT);

		/* Greens */
		addTransparentBlock(Material.LONG_GRASS);
		addTransparentBlock(Material.VINE);
		addTransparentBlock(Material.WATER_LILY);

		/* Seedy things */
		addTransparentBlock(Material.MELON_STEM);
		addTransparentBlock(Material.PUMPKIN_STEM);
		addTransparentBlock(Material.CROPS);
		addTransparentBlock(Material.NETHER_WARTS);

		/* Semi-nature */
		addTransparentBlock(Material.SNOW);
		addTransparentBlock(Material.FIRE);
		addTransparentBlock(Material.WEB);
		addTransparentBlock(Material.TRIPWIRE);
		addTransparentBlock(Material.TRIPWIRE_HOOK);

		/* Stairs */
		addTransparentBlock(Material.COBBLESTONE_STAIRS);
		addTransparentBlock(Material.BRICK_STAIRS);
		addTransparentBlock(Material.SANDSTONE_STAIRS);
		addTransparentBlock(Material.NETHER_BRICK_STAIRS);
		addTransparentBlock(Material.SMOOTH_STAIRS);

		/* Wood Stairs */
		addTransparentBlock(Material.BIRCH_WOOD_STAIRS);
		addTransparentBlock(Material.WOOD_STAIRS);
		addTransparentBlock(Material.JUNGLE_WOOD_STAIRS);
		addTransparentBlock(Material.SPRUCE_WOOD_STAIRS);

		/* Lava & Water */
		addTransparentBlock(Material.LAVA);
		addTransparentBlock(Material.STATIONARY_LAVA);
		addTransparentBlock(Material.WATER);
		addTransparentBlock(Material.STATIONARY_WATER);

		/* Saplings and bushes */
		addTransparentBlock(Material.SAPLING);
		addTransparentBlock(Material.DEAD_BUSH);

		/* Construction Material */
		/* Fences */
		addTransparentBlock(Material.FENCE);
		addTransparentBlock(Material.FENCE_GATE);
		addTransparentBlock(Material.IRON_FENCE);
		addTransparentBlock(Material.NETHER_FENCE);

		/* Ladders, Signs */
		addTransparentBlock(Material.LADDER);
		addTransparentBlock(Material.SIGN);
		addTransparentBlock(Material.SIGN_POST);
		addTransparentBlock(Material.WALL_SIGN);

		/* Bed */
		addTransparentBlock(Material.BED_BLOCK);
		addTransparentBlock(Material.BED);

		/* Pistons */
		addTransparentBlock(Material.PISTON_EXTENSION);
		addTransparentBlock(Material.PISTON_MOVING_PIECE);
		addTransparentBlock(Material.RAILS);

		/* Torch & Trapdoor */
		addTransparentBlock(Material.TORCH);
		addTransparentBlock(Material.TRAP_DOOR);

		/* Carpet */
		addTransparentBlock(Material.CARPET);
	}

	public void addTransparentBlock(Material mat) { transparent.add(mat); }

	public void addInteractBlock(Material mat) { getInteractBlocks().add(mat); }

	public HashSet<Material> getInteractBlocks() { return interact; }
	// END BLOCK FREECAM METHODS.


	// BEGIN ANNOUCNER METHOD
	public void Announce(int announcement) {
		String msg = getConfig().getStringList("Announcements.list").get(announcement);
		for (String s : formatAnnouncement(msg)) {
			getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', s));
		}
	}

	public void restartAnnouncements() {
		int time = getConfig().getInt("Announcements.broadcastInterval") * 20;
		getServer().getScheduler().cancelTasks(this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Announcer(this), time, time);
	}

	private ArrayList<String> formatAnnouncement(String s) {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add("%BAR%======================== %TITLE%Tips %BAR%========================");
		temp.addAll(Arrays.asList(s.split("%N%")));

		color(temp);
		return temp;
	}

	private void color(ArrayList<String> lines) {
		String barColor = randomColor();
		String otherColor = barColor;
		String c1 = barColor;
		String c2 = barColor;
		String c3 = barColor;
		while (barColor.equalsIgnoreCase(otherColor)) {
			otherColor = randomColor();
		}
		while (c1.equals(barColor) || c1.equals(otherColor)) {
			c1 = randomColor();
		}
		while (c2.equals(barColor) || c2.equals(otherColor) || c2.equals(c1)) {
			c2 = randomColor();
		}
		while (c3.equals(barColor) || c3.equals(otherColor) || c3.equals(c1) || c3.equals(c2)) {
			c3 = randomColor();
		}

		for (int x = 0; x < lines.size(); x++) {
			lines.set(x, lines.get(x).replace("%BAR%" , barColor));
			lines.set(x, lines.get(x).replace("%TITLE%", otherColor));
			lines.set(x, lines.get(x).replace("%RC1%", c1));
			lines.set(x, lines.get(x).replace("%RC2%", c2));
			lines.set(x, lines.get(x).replace("%RC3%", c3));
		}
	}

	private String randomColor() {
		int x = rand.nextInt(10);
		if (x == 0) { return "&2"; }
		if (x == 1) { return "&3"; }
		if (x == 2) { return "&4"; }
		if (x == 3) { return "&6"; }
		if (x == 4) { return "&9"; }
		if (x == 5) { return "&a"; }
		if (x == 6) { return "&b"; }
		if (x == 7) { return "&c"; }
		if (x == 8) { return "&d"; }
		else { return "&e"; }
	}
	// END ANNOUNCER METHOD
}
