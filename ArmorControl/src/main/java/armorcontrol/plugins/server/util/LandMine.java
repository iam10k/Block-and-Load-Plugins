package armorcontrol.plugins.server.util;

import armorcontrol.plugins.server.main.ArmorControl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class LandMine {

	private Block block;
	private ArmorControl plugin;
	private int taskID = -1;

	public LandMine(ArmorControl pl, Block b) {
		plugin = pl;
		block = b;
		taskID = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				removeTime();
			}
		}, 2400).getTaskId();
	}

	public Location getLocation() { return block.getLocation(); }

	public void remove() {
		if (Utils.isPressurePlate(block.getType())) {
			block.setType(Material.AIR);
		}
		plugin.cancelTask(taskID);
	}

	public void removeTime() {
		if (Utils.isPressurePlate(block.getType())) {
			block.setType(Material.AIR);
		}
		plugin.landMines.remove(this);
	}
}
