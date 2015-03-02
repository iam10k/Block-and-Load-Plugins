package escort.plugins.server.game;

import java.util.ArrayList;

public class Team {

	private Game game;
	private ArrayList<GamePlayer> teammates = new ArrayList<GamePlayer>(); // Teammates
	private ArrayList<Integer> teammatesLives = new ArrayList<Integer>(); // Teammates current lives
	private GamePlayer president = null;
	private boolean hasPresident = false;

	public Team(Game g, boolean president) {
		game = g;
		hasPresident = president;
	}

	// BEGIN ACCESS METHODS
	public Game getGame() { return game; }

	public GamePlayer getPresident() { return president; }

	public int totalPlayers() { return teammates.size(); }

	/**
	 * Check if the player is able to join the team
	 * @param gamePlayer is not null
	 * @return true if gamePlayer is not on team
	 */
	public boolean canJoinTeam(GamePlayer gamePlayer) {
		return getSlot(gamePlayer) == -1;
	}
	// END ACCESS METHODS


	// BEGIN PLAYER METHODS
	public boolean addPlayer(GamePlayer gamePlayer) {
		if (canJoinTeam(gamePlayer)) {
			teammates.add(gamePlayer);
			teammatesLives.add(3);

			// If game stage is in battle teleport to game
			if (game.getGameStage().equals(GameStage.GAMESTARTCOUNTDOWN)) {
				teleportPlayerToSpawnPoint(gamePlayer);
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean addPlayer(GamePlayer gamePlayer, int lives) {
		if (canJoinTeam(gamePlayer)) {
			teammates.add(gamePlayer);
			teammatesLives.add(lives);

			// If game stage is in battle teleport to game
			if (game.getGameStage().equals(GameStage.GAMESTARTCOUNTDOWN)) {
				teleportPlayerToSpawnPoint(gamePlayer);
			}
			return true;
		} else {
			return false;
		}
	}

	public void removePlayer(GamePlayer gamePlayer) {
		int slot = getSlot(gamePlayer);
		if (slot != -1) {
			teammates.remove(slot);
			teammatesLives.remove(slot);
			// Idk if I need anything else here
		}
	}

	public void pickRandomPresident() {
		int x = (int) (Math.random() * teammates.size());
		president = teammates.remove(x);
		teammatesLives.remove(x);
		// Send message to the game TODO: < that
	}

	public void kickPresident() {
		game.teleportPlayerToLobby(president);
		game.presidentKickedOut(president);
		president = null;
		pickRandomPresident();
	}

	public int getPlayerRemainingLives(GamePlayer gamePlayer) { return teammatesLives.get(getSlot(gamePlayer)); }
	// END PLAYER METHODS


	// BEGIN TELEPORT METHODS
	private void teleportPlayerToSpawnPoint(GamePlayer gamePlayer) {

	}
	// END TELEPORT METHODS


	// BEGIN HELPER METHODS
	private int getSlot(GamePlayer gamePlayer) {
		for (int x = 0; x < teammates.size(); x++) {
			if (teammates.get(x).equals(gamePlayer)) {
				return x;
			}
		}
		return -1;
	}
	// END HELPER METHODS

}
