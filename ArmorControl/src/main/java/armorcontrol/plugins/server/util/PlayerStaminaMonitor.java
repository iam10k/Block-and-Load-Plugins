package armorcontrol.plugins.server.util;

import armorcontrol.plugins.server.main.ArmorControl;
import org.bukkit.entity.Player;

public class PlayerStaminaMonitor implements Runnable {

	private ArmorControl plugin;
	private Player p;
	private int level = 20;
	private int count = 0;
	private boolean canSprint = true;

	public PlayerStaminaMonitor(ArmorControl pl, Player player) {
		plugin = pl;
		p = player;
		plugin.getServer().getScheduler().runTaskTimer(plugin, this, 0L, 1L);
	}

	public Player getPlayer() { return p; }


	public void run() {
		count++;
		if (p.isSprinting()) {
			if (level < 2) {
				p.setSprinting(false);
			} else {
				if (count % 20 == 0) {
					level--;
					p.setLevel(level);
				}
			}
		} else {
			if (level < 20) {
				if (count % 20 == 0) {
					p.setSprinting(true);
					level++;
					p.setLevel(level);
					p.setSprinting(false);
				}
			}
		}
		if (count == 20) {
			count = 0;
		}
	}

	public void died() {
		level = 20;
	}

}
