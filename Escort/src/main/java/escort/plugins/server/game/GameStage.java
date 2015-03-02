package escort.plugins.server.game;

public enum GameStage {
	LOBBY("&aWaiting..."),
	GAMESTARTCOUNTDOWN("Beginning"),
	ROUND1("&4Round 1"),
	ROUND2("&4Round 2"),
	ENDINGCOUNTDOWN("&6Ending...");

	private String string;

	private GameStage(String s) {
		string = s;
	}

	public String toString() {
		return string;
	}
}
