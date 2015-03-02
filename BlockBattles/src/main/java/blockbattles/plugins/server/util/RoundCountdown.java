package blockbattles.plugins.server.util;

import blockbattles.plugins.server.game.Arena;
import org.bukkit.Bukkit;

public class RoundCountdown {

	private Arena arena;
	int round;
	int current = 20;

	private int id = 1;

	public RoundCountdown(Arena a, int nextRound) {
		arena = a;
		round = nextRound;
		if (round == 1) {
			current = 30;
		}

		runTask();
	}

	public void runTask() {
		id = Bukkit.getScheduler().runTaskTimer(arena.getPlugin(), new Runnable() {
			@Override
			public void run() {
				if (current >= 0) {
					if (current == 0) {
						arena.sendMessage("  &aRound " + round + " beginning!");
						arena.startRound(round, false);
					} else if (current == 30) {
						arena.sendMessage("  &a30 Seconds till round " + round + " begins!");
					} else if (current == 20) {
						arena.sendMessage("  &a20 Seconds till round " + round + " begins!");
					} else if (current == 10) {
						arena.sendMessage("  &a10 Seconds till round " + round + " begins!");
						arena.startRound(round, true);
					} else if (current <= 5) {
						arena.sendMessage("  &a" + current + " Seconds till round " + round + " begins!");
					}
					current--;
				} else {
					arena.getPlugin().cancelTask(id);
				}

			}
		}, 0L, 20L).getTaskId();
	}

}
