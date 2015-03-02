package armorcontrol.plugins.server.util;

import armorcontrol.plugins.server.main.ArmorControl;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class PrimedObsidian {

	private ArmorControl plugin;
	private Block b;
	public int time = 0;
	public int id = 1;

	public PrimedObsidian(ArmorControl pl, Block block) {
		plugin = pl;
		b = block;
		runTask();
	}

	private void runTask() {
		id = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				if (time == 0) {
					b.setType(Material.AIR);
				} else if (time == 1) {
					b.setType(Material.OBSIDIAN);
				} else if (time == 2) {
					b.setType(Material.AIR);
				} else if (time == 3) {
					b.setType(Material.OBSIDIAN);
				} else if (time == 4) {
					b.setType(Material.AIR);
					b.getWorld().createExplosion(b.getLocation(), 7F, false);
				} else if (time == 5) {
					plugin.cancelTask(id);
				}
				time++;
			}
		}, 8L, 10L).getTaskId();
	}
}
