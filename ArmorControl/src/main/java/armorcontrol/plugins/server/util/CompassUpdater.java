package armorcontrol.plugins.server.util;

import armorcontrol.plugins.server.main.ArmorControl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CompassUpdater {

	private ArmorControl plugin;

	public CompassUpdater(ArmorControl pl) {
		plugin = pl;
		runTask();
	}

	private void runTask() {
		Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				if (plugin.getServer().getOnlinePlayers().length > 3) {
					for (Player p : plugin.getServer().getOnlinePlayers()) {
						if (plugin.juggernaut == null) {
							p.setCompassTarget(plugin.getRandomSpawn());
						} else {
							p.setCompassTarget(plugin.juggernaut.getLocation());
						}
					}
				}
			}
		}, 0L, 60L);
	}
}
