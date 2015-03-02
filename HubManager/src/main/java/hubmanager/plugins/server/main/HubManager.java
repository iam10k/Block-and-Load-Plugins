package hubmanager.plugins.server.main;

import hubmanager.plugins.server.commands.ParkourCommand;
import hubmanager.plugins.server.commands.ReloadCommand;
import hubmanager.plugins.server.listeners.EventsListener;
import hubmanager.plugins.server.util.Utils;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import playermanager.plugins.server.guishop.GUIShopCommand;
import playermanager.plugins.server.main.PlayerManager;

import java.util.ArrayList;

public class HubManager extends JavaPlugin {

	public PlayerManager pM;

	public PotionEffect speed;
	public PotionEffect vision;
	public Location spawn;
	public ArrayList<Location> headLocations = new ArrayList<Location>();

	public ArrayList<String> parkour = new ArrayList<String>();

	public void onEnable() {
		Plugin plugin = getServer().getPluginManager().getPlugin("PlayerManager");
		if (plugin == null) {
			onDisable();
		}
		pM = (PlayerManager)plugin;

		// Load config
		configDefaults();
		loadConfig();

		// Register events and command
		getServer().getPluginManager().registerEvents(new EventsListener(this), this);
		getCommand("hubmanager").setExecutor(new ReloadCommand(this));
		getCommand("parkour").setExecutor(new ParkourCommand(this));

		// Create GUIShop, one line does everything else for you
		getCommand("xpshop").setExecutor(new GUIShopCommand(this, pM, true));

		// Make time stay day
		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				getServer().getWorld("world").setTime(0);
			}
		}, 10L, 200L);
	}

	/**
	 * Copy config defaults
	 */
	private void configDefaults() {
		getConfig().addDefault("Spawn.Location", "");
		getConfig().addDefault("Potion.amp", 4);
		getConfig().addDefault("HeadLocations.list", "");
		getConfig().addDefault("Jump.velocity", 1.1);
		getConfig().addDefault("Jump.height", 1.0);
		for (int x = 0; x < 27; x++) {
			getConfig().addDefault("XPShop.slot" + x + ".material", "AIR");
		}
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	/**
	 * Load/Reload config
	 */
	public void loadConfig() {
		// Spawn Location
		if (!getConfig().getString("Spawn.Location").equals("")) {
			spawn = Utils.convertStringToLocation(getConfig().getString("Spawn.Location"), true);
		}

		// Potion Effects
		vision = new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 2, false);
		if (getConfig().getInt("Potion.amp", 0) != 0) {
			speed = new PotionEffect(PotionEffectType.SPEED, 1000000, getConfig().getInt("Potion.amp", 1), false);
		} else {
			speed = null;
		}

		// Mob head Locations
		headLocations.clear();
		for (String s : getConfig().getStringList("HeadLocations.list")) {
			Location loc = Utils.convertStringToLocation(s, false);
			if (loc != null) {
				headLocations.add(loc);
			}
		}
	}
}
