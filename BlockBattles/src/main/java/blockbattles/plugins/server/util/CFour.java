package blockbattles.plugins.server.util;

import blockbattles.plugins.server.game.Arena;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class CFour {

	private Arena arena;
	private Player player;
	private Block block;

	public CFour(Arena a, Player pl, Block b) {
		arena = a;
		player = pl;
		block = b;
	}

	public boolean isPlayersC4(Player p) {
		return player.equals(p);
	}

	public void detonate() {
		block.setType(Material.AIR);
		block.getWorld().createExplosion(block.getLocation(), 3F, false);
		arena.removeC4(this);
	}

	public void remove() {
		block.setType(Material.AIR);
		arena.removeC4(this);
	}
}
