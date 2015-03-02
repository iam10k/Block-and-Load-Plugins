package blockbattles.plugins.server.util;

import blockbattles.plugins.server.game.Arena;
import org.bukkit.Bukkit;

public class RoundClock {

	private Arena arena;
	int round;
	int current = 0;

	private int id = 1;

	public RoundClock(Arena a, int currentRound) {
		arena = a;
		round = currentRound;

		runTask();
	}

	public void runTask() {
		id = Bukkit.getScheduler().runTaskTimer(arena.getPlugin(), new Runnable() {
			@Override
			public void run() {
				if (current <= 920 && current >= 0) {
					if (current == 920) {
						arena.roundTimeUp();
					} else if (current == 900) {
						arena.sendMessage("  &aRound Over!");
					} else if (current == 300) {
						arena.sendMessage("  &a10 Minutes remaining in round " + round + ".");
					} else if (current == 600) {
						arena.sendMessage("  &a5 Minutes remaining in round " + round + ".");
					} else if (current == 840) {
						arena.sendMessage("  &a1 Minute remaining in round " + round + ".");
					}
				} else {
					arena.getPlugin().cancelTask(id);
				}
				current += 10;
			}
		}, 0L, 200L).getTaskId();
	}

	public void cancelEarly() { arena.getPlugin().cancelTask(id); }
}
