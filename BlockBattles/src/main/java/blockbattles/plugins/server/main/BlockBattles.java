package blockbattles.plugins.server.main;

import blockbattles.plugins.server.classselector.ClassSelectorManager;
import blockbattles.plugins.server.commands.BlockBattlesCommand;
import blockbattles.plugins.server.commands.EnchantCommand;
import blockbattles.plugins.server.commands.LeaveCommand;
import blockbattles.plugins.server.commands.StatsCommand;
import blockbattles.plugins.server.enchantments.DamageEnchantment;
import blockbattles.plugins.server.game.Arena;
import blockbattles.plugins.server.game.ArenaPlayer;
import blockbattles.plugins.server.game.GameClass;
import blockbattles.plugins.server.listeners.EventsListener;
import blockbattles.plugins.server.util.SignUpdater;
import blockbattles.plugins.server.util.Utils;
import com.rit.sucy.EnchantmentAPI;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import playermanager.plugins.server.guishop.GUIShopCommand;
import playermanager.plugins.server.main.PlayerManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BlockBattles extends JavaPlugin {

	public PlayerManager pM;

	public int xpPerSwordKill = 0;
	public int xpPerBowKill = 0;
	public int xpWinRound = 0;
	public int xpWinGame = 0;
	public int xpLoseRound = 0;
	public int xpLoseGame = 0;

	public Location lobby;

	private ArrayList<Arena> arenas = new ArrayList<Arena>();
	private ArrayList<GameClass> classes = new ArrayList<GameClass>();
	private ArrayList<ArenaPlayer> players = new ArrayList<ArenaPlayer>();

	public void onEnable() {
		Plugin plugin = getServer().getPluginManager().getPlugin("PlayerManager");
		if (plugin == null) {
			onDisable();
		}
		pM = (PlayerManager)plugin;

		// Config & Settings
		configDefaults();
		loadConfig();

		// Listener and Commands
		getServer().getPluginManager().registerEvents(new EventsListener(this), this);
		getServer().getPluginManager().registerEvents(new ClassSelectorManager(this), this);
		getCommand("blockbattles").setExecutor(new BlockBattlesCommand(this));
		getCommand("enchant").setExecutor(new EnchantCommand(this));
		getCommand("stats").setExecutor(new StatsCommand(this));
		getCommand("leave").setExecutor(new LeaveCommand(this));

		// Create GUIShop, one line does everything else for you
		getCommand("xpshop").setExecutor(new GUIShopCommand(this, pM, false));

		// Start sign updater
		new SignUpdater(this);

		// Add Enchanments
		EnchantmentAPI.registerCustomEnchantment(new DamageEnchantment(this));
	}

	public void onDisable() {
		for (Arena a : arenas) {
			a.closeArena();
		}
	}

	/**
	 * Copy config defaults
	 */
	private void configDefaults() {
		getConfig().addDefault("Lobby.location", "");
		getConfig().addDefault("XP.perSwordKill", 0);
		getConfig().addDefault("XP.perBowKill", 0);
		getConfig().addDefault("XP.winRound", 0);
		getConfig().addDefault("XP.winGame", 0);
		getConfig().addDefault("XP.loseRound", 0);
		getConfig().addDefault("XP.loseGame", 0);
		getConfig().addDefault("Arenas.list", "");
		getConfig().addDefault("Classes.list", "");
		for (int x = 0; x < 27; x++) {
			getConfig().addDefault("XPShop.slot" + x + ".material", "AIR");
		}
		getConfig().options().copyDefaults(true);
		saveConfig();

		File classFolder = new File(getDataFolder() + File.separator + "classes");
		if (!classFolder.exists()) {
			classFolder.mkdir();
		}

		File arenaFolder = new File(getDataFolder() + File.separator + "arenas");
		if (!arenaFolder.exists()) {
			arenaFolder.mkdir();
		}
	}

	/**
	 * Load/Reload config
	 */
	private void loadConfig() {
		reloadConfig();

		lobby = Utils.convertStringToLocation(getConfig().getString("Lobby.location", "0 0 0 0 0"), true);

		xpPerSwordKill = getConfig().getInt("XP.perSwordKill", 1);
		xpPerBowKill = getConfig().getInt("XP.perBowKill", 2);
		xpWinRound = getConfig().getInt("XP.winRound", 3);
		xpWinGame = getConfig().getInt("XP.winGame", 4);
		xpLoseRound = getConfig().getInt("XP.loseRound", 1);
		xpLoseGame = getConfig().getInt("XP.loseGame", 2);

		for (String arena : getConfig().getStringList("Arenas.list")) {
			if (getArenaConfig(arena) != null) {
				arenas.add(new Arena(this, getArenaConfig(arena)));
			}
		}

		for (String className : getConfig().getStringList("Classes.list")) {
			if (getClassConfig(className) != null) {
				classes.add(new GameClass(getClassConfig(className)));
			}
		}
	}

	/**
	 * Cancel a task with id
	 * @param id is int of a repeating task
	 */
	public void cancelTask(int id) { getServer().getScheduler().cancelTask(id); }

	/**
	 * Get the config for the arena
	 * @param arenaName is the name of the arena
	 * @return FileConfiguration if arena config exists
	 */
	public FileConfiguration getArenaConfig(String arenaName) {
		File arenaFile = new File(getDataFolder().getAbsolutePath() + File.separator + "arenas", arenaName + ".yml");
		if (arenaFile.exists()) {
			return YamlConfiguration.loadConfiguration(arenaFile);
		}
		return null;
	}

	/**
	 * Get the config for the class
	 * @param className is the name of the class
	 * @return FileConfiguration if class config exists
	 */
	public FileConfiguration getClassConfig(String className) {
		File classFile = new File(getDataFolder() + File.separator + "classes" + File.separator + className + ".yml");
		if (classFile.exists()) {
			return YamlConfiguration.loadConfiguration(classFile);
		}
		return null;
	}

	/**
	 * Create an Arena
	 * @param id the file name
	 * @param name Arena display name
	 * @param teamSize team size
	 * @return true if arena was created
	 */
	public boolean createArena(String id, String name, int teamSize) {
		if (getArenaConfig(id) == null) {
			// Add to plugin config
			List<String> arenaList = getConfig().getStringList("Arenas.list");
			arenaList.add(id);
			getConfig().set("Arenas.list", arenaList);
			saveConfig();

			// Make config for arena
			File arenaFile = new File(getDataFolder() + File.separator + "arenas", id + ".yml");

			FileConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);
			config.set("Arena.enabled", false);
			config.set("Arena.id", id);
			config.set("Arena.signName", name);
			config.set("Teams.size", teamSize);
			config.set("Location.minPoint", "");
			config.set("Location.maxPoint", "");
			config.set("Location.spectate", "");
			config.set("Location.infoSign", "");
			config.set("Location.teamASign", "");
			config.set("Location.teamBSign", "");
			config.set("TeamA.spawns", "");
			config.set("TeamB.spawns", "");
			try {
				config.save(arenaFile);
			} catch (IOException e) {
				e.printStackTrace();
			}

			arenas.add(new Arena(this, config));
			return true;
		}
		return false;
	}

	/**
	 * Create a GameClass
	 * @param player to get inventory from
	 * @param id is name of class file
	 * @param name is the display name of class
	 * @return
	 */
	public boolean createClass(Player player, String id, String name) {
		if (getClassConfig(id) == null) {
			// Add to plugin config
			List<String> arenaList = getConfig().getStringList("Classes.list");
			arenaList.add(id);
			getConfig().set("Classes.list", arenaList);
			saveConfig();

			// Make config for arena
			File classFile = new File(getDataFolder() + File.separator + "classes", id + ".yml");

			FileConfiguration config = YamlConfiguration.loadConfiguration(classFile);
			config.set("Class.id", id);
			config.set("Class.name", name);
			config.set("Class.description", "");
			config.set("Class.material", "");
			config.set("Class.perkID", "");
			config.set("Class.cost", 0);

			for (int x = 0; x < 36; x++) {
				config.set("inventory.slot" + x + ".itemStack", player.getInventory().getContents()[x]);
			}

			for (int x = 0; x < 4; x++) {
				config.set("armor.slot" + x + ".itemStack", player.getInventory().getArmorContents()[x]);
			}

			List<String> effects = new ArrayList<String>();
			for (PotionEffect potionEffect : player.getActivePotionEffects()) {
				effects.add(potionEffect.getType() + ":" + potionEffect.getAmplifier());
			}

			config.set("Effects.list", effects);

			try {
				config.save(classFile);
			} catch (IOException e) {
				e.printStackTrace();
			}

			classes.add(new GameClass(config));
			return true;
		}
		return false;
	}

	// BEGIN ARENA METHODS

	/**
	 * Add player to ArenaPlayer list on join
	 * @param player is not null
	 */
	public void addArenaPlayer(Player player) {
		players.add(new ArenaPlayer(player));
	}

	/**
	 * Remove ArenaPlayer
	 * @param arenaPlayer is not null
	 */
	public void removeArenaPlayer(ArenaPlayer arenaPlayer) {
		players.remove(arenaPlayer);
	}

	/**
	 * Get ArenaPlayer
	 * @param player is not null
	 * @return ArenaPlayer
	 */
	public ArenaPlayer getArenaPlayer(Player player) {
		for (ArenaPlayer arenaPlayer : players) {
			if (arenaPlayer.getPlayer().equals(player)) {
				return arenaPlayer;
			}
		}
		return null;
	}

	/**
	 * Get a GameClass using its String id
	 * @param id is GameClass id
	 * @return Default GameClass if other is not found
	 */
	public GameClass getClass(String id) {
		for (GameClass gameClass : classes) {
			if (gameClass.getID().equals(id)) {
				return gameClass;
			}
		}
		return classes.get(0);
	}

	/**
	 * Get ArrayList of Arenas
	 * @return ArrayList of Arena
	 */
	public ArrayList<Arena> getArenas() { return arenas; }

	/**
	 * Get ArrayList of GameClass
	 * @return ArrayList of GameClass
	 */
	public ArrayList<GameClass> getGameClasses() { return classes; }
	// END ARENA METHODS
}
