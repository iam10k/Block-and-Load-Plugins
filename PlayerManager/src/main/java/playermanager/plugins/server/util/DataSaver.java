package playermanager.plugins.server.util;

import org.bukkit.entity.Player;
import playermanager.plugins.server.main.PlayerManager;

import java.util.logging.Level;

public class DataSaver implements Runnable {

	private PlayerManager plugin;

	public DataSaver(PlayerManager pl) {
		plugin = pl;
	}

	public void run() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			plugin.savePlayer(player);
		}

		plugin.getLogger().log(Level.INFO, "Saving all player data!");
	}
}
