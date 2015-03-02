package escort.plugins.server.utils;

import escort.plugins.server.game.Game;

public class CountDown {

	private Game game;
	private boolean start;
	private int count = 0;

	public CountDown(Game g, boolean startCountdown) {
		game = g;
		start = startCountdown;
	}

	private void start() {

	}
}
