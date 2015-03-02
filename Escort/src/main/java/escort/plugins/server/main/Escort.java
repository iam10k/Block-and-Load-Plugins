package escort.plugins.server.main;

import escort.plugins.server.game.Game;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import playermanager.plugins.server.main.PlayerManager;

public class Escort extends JavaPlugin {

	public PlayerManager pM;

	public Location lobby;

	private Game game = new Game(this);

	public void onEnable() {
		Plugin plugin = getServer().getPluginManager().getPlugin("PlayerManager");
		if (plugin == null) {
			onDisable();
		}
		pM = (PlayerManager)plugin;

		// Config & Settings
		configDefaults();
		loadConfig();

		/*
		Lobby will load by default (it is named world), players join team and vote for map in the lobby
		Then once the game starts it will do getServer().createWorld("the one they voted for").
		Teleport all the players to that world, to play the game. Once they are died out of the game or game over
		World will be unloaded after the game is over. Make sure to link permissions world to other maps
		 */
	}

	/**
	 * Copy config defaults
	 */
	private void configDefaults() {
		getConfig().addDefault("XP.perSwordKill", 0);
		getConfig().addDefault("XP.perBowKill", 0);
		getConfig().addDefault("XP.winGame", 0);
		getConfig().addDefault("XP.loseGame", 0);
		for (int x = 0; x < 27; x++) {
			getConfig().addDefault("XPShop.slot" + x + ".material", "AIR");
		}
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	/**
	 * Load/Reload config
	 */
	private void loadConfig() {
		reloadConfig();

	}

	/**
	 * Cancel a task with id
	 * @param id is int of a repeating task
	 */
	public void cancelTask(int id) { getServer().getScheduler().cancelTask(id); }

}
