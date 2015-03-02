package blockbattles.plugins.server.util;

import blockbattles.plugins.server.game.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Web {

	private Arena arena;
	private Block block;

	private int id = -1;

	public Web(Arena a, Block b) {
		arena = a;
		block = b;
		runTask();
	}

	public Location getLocation() { return block.getLocation(); }

	public void cancelEarly() {
		block.setType(Material.AIR);
		arena.getPlugin().cancelTask(id);
	}

	private void runTask() {
		id = arena.getPlugin().getServer().getScheduler().runTaskLater(arena.getPlugin(), new Runnable() {
			@Override
			public void run() {
				block.setType(Material.AIR);
				arena.removeWeb(block.getLocation());
			}
		}, 1200L).getTaskId();
	}


}
