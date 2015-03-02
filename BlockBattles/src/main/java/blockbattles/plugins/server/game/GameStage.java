package blockbattles.plugins.server.game;

public enum GameStage {
	LOADING("&7Loading..."),
	WAITING("&aWaiting..."),
	ROUND1COUNTDOWN("&4Round 1"),
	ROUND1("&4Round 1"),
	ROUND2COUNTDOWN("&4Round 2"),
	ROUND2("&4Round 2"),
	ROUND3COUNTDOWN("&4Round 3"),
	ROUND3("&4Round 3"),
	ENDING("&6Ending...");

	private String string;

	private GameStage(String s) {
		string = s;
	}

	public String toString() {
		return string;
	}
}
