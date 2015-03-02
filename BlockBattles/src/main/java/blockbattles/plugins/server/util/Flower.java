package blockbattles.plugins.server.util;

import blockbattles.plugins.server.game.ArenaPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Flower {

	private ArenaPlayer arenaPlayer;
	private Block block;
	private int type = 0;

	public Flower(ArenaPlayer player, Block b) {
		arenaPlayer = player;
		block = b;
		type = block.getData();
	}

	public ArenaPlayer getArenaPlayer() { return arenaPlayer; }

	public Block getBlock() { return block; }

	public Location getLocation() { return block.getLocation(); }

	/**
	 *
	 * @return 4-Rose Bush 5-Peony
	 */
	public int getType() { return type; }

	public void remove() {
		block.setType(Material.AIR);
	}
}
