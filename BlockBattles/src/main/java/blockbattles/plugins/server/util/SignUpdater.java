package blockbattles.plugins.server.util;

import blockbattles.plugins.server.game.Arena;
import blockbattles.plugins.server.main.BlockBattles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class SignUpdater {

	private BlockBattles plugin;

	private int id = 1;

	public SignUpdater(BlockBattles pl) {
		plugin = pl;

		runTask();
	}

	public void runTask() {
		id = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				for (Arena a : plugin.getArenas()) {
					if (a.getInfoSignLoc() != null) {
						Block b = a.getInfoSignLoc().getBlock();
						if (b != null && Utils.isSign(b.getType())) {
							Sign s = (Sign) b.getState();
							s.setLine(0, color(a.getInfoSign()[0]));
							s.setLine(1, color(a.getInfoSign()[1]));
							s.setLine(2, color(a.getInfoSign()[2]));
							s.setLine(3, color(a.getInfoSign()[3]));
							s.update();
						}
					}
					if (a.getTeamASignLoc() != null) {
						Block b = a.getTeamASignLoc().getBlock();
						if (b != null && Utils.isSign(b.getType())) {
							Sign s = (Sign) b.getState();
							s.setLine(0, color(a.getTeamASign()[0]));
							s.setLine(1, color(a.getTeamASign()[1]));
							s.setLine(2, color(a.getTeamASign()[2]));
							s.setLine(3, color(a.getTeamASign()[3]));
							s.update();
						}
					}
					if (a.getTeamBSignLoc() != null) {
						Block b = a.getTeamBSignLoc().getBlock();
						if (b != null && Utils.isSign(b.getType())) {
							Sign s = (Sign) b.getState();
							s.setLine(0, color(a.getTeamBSign()[0]));
							s.setLine(1, color(a.getTeamBSign()[1]));
							s.setLine(2, color(a.getTeamBSign()[2]));
							s.setLine(3, color(a.getTeamBSign()[3]));
							s.update();
						}
					}
				}
			}
		}, 0L, 60L).getTaskId();
	}

	public void endTask() { plugin.cancelTask(id); }

	private String color(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
}
