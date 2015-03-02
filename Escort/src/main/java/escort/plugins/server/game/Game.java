package escort.plugins.server.game;

import escort.plugins.server.main.Escort;
import escort.plugins.server.utils.CountDown;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

public class Game {

	private Escort plugin;

	// World loaded
	private String mapLoaded = "";

	// Teams
	private Team defenders;
	private Team escorters;

	// Players who died out of game
	private ArrayList<String> died = new ArrayList<String>();
	private ArrayList<String> previousPlayers = new ArrayList<String>(); // Formated like EnkyHD,team 0 or 1,lives 0-3

	// Votes to kick president
	int votesToKickPresident = 0;

	// Map Config, when = null means map is not loaded yet and game has not started
	private FileConfiguration config = null;

	// Game Stage
	private GameStage gameStage = GameStage.LOBBY;

	public Game(Escort pl) {
		defenders = new Team(this, false);
		escorters = new Team(this, false);
	}

	// BEGIN INSTANCE ACCESS METHODS
	public Escort getPlugin() { return plugin; }

	public String getMapLoaded() { return mapLoaded; }

	public boolean hasGameStarted() { return config != null; }

	public GameStage getGameStage() { return gameStage; }

	public void addVoteToKick() {
		if (hasGameStarted()) {
			if (++votesToKickPresident > (double)escorters.totalPlayers() * .6) {
				escorters.kickPresident();
			}
		}
	}
	// END INSTANCE ACCESS METHODS


	// BEGIN PLAYER MANAGEMENT METHODS
	/**
	 * Add Player to the specified team
	 * @param gamePlayer player to add
	 * @param team 0 - Defenders : 1 - Escorters
	 * @return true if added
	 */
	public boolean addPlayer(GamePlayer gamePlayer, int team) {
		// Game is in round 2 so player cannot join
		if (!gameStage.equals(GameStage.ROUND1) && !gameStage.equals(GameStage.LOBBY) && !gameStage.equals(GameStage.GAMESTARTCOUNTDOWN)) {
			plugin.pM.sendMessage(gamePlayer.getPlayer(), "&cThis game is in round two. Please try another game.");
			return false;
		}

		// Player already had died out of game
		if (died.contains(gamePlayer.getName())) {
			plugin.pM.sendMessage(gamePlayer.getPlayer(), "&cYou have already lost all of your lives in this game.");
			return false;
		}

		// Player may have played before
		if (hasBeenInGame(gamePlayer.getPlayer().getName())) {
			if (getTeamOfPlayerWhoLeft(gamePlayer.getName()) != team) {
				plugin.pM.sendMessage(gamePlayer.getPlayer(), "&cYou cannot join this team you were on the other team.");
				return false;
			}

			if (team == 0) {
				defenders.addPlayer(gamePlayer, getLivesOfPlayerWhoLeft(gamePlayer.getName()));
				removePlayerWhoJoinedBack(gamePlayer.getName());
			} else {
				escorters.addPlayer(gamePlayer, getLivesOfPlayerWhoLeft(gamePlayer.getName()));
				removePlayerWhoJoinedBack(gamePlayer.getName());
			}
		}

		// Try to add the player to the team selected
		if (team == 0) {
			if (plugin.pM.getServerPlayer(gamePlayer.getPlayer()).getPlayerRank().equals("default")) {
				if (canJoinTeam(0)) {
					if (escorters.canJoinTeam(gamePlayer)) {
						if (defenders.addPlayer(gamePlayer)) {
							plugin.pM.sendMessage(gamePlayer.getPlayer(), "&aJoined the Defending team.");
						} else {
							plugin.pM.sendMessage(gamePlayer.getPlayer(), "&cAlready on the Defending team.");
							return false;
						}
					} else {
						plugin.pM.sendMessage(gamePlayer.getPlayer(), "&cAlready on the Escorting team.");
						return false;
					}
				} else {
					plugin.pM.sendMessage(gamePlayer.getPlayer(), "&cThe Defending team is full. Purchase Premium to join full teams.");
					return false;
				}
			} else {
				defenders.addPlayer(gamePlayer);
			}
		} else {
			if (plugin.pM.getServerPlayer(gamePlayer.getPlayer()).getPlayerRank().equals("default")) {
				if (canJoinTeam(1)) {
					if (defenders.canJoinTeam(gamePlayer)) {
						if (escorters.addPlayer(gamePlayer)) {
							plugin.pM.sendMessage(gamePlayer.getPlayer(), "&aJoined the Escorting team.");
						} else {
							plugin.pM.sendMessage(gamePlayer.getPlayer(), "&cAlready on the Escorting team.");
							return false;
						}
					} else {
						plugin.pM.sendMessage(gamePlayer.getPlayer(), "&cAlready on the Defending team.");
						return false;
					}
				} else {
					plugin.pM.sendMessage(gamePlayer.getPlayer(), "&cThe Escorting team is full. Purchase Premium to join full teams.");
					return false;
				}
			} else {
				escorters.addPlayer(gamePlayer);
			}
		}

		if (!hasGameStarted() && canStart()) {
			startGameCountdown();
		}
		return true;
	}

	public void playerDiedOut(GamePlayer gamePlayer) { // Called from death event probably in this class
		if (getTeamOfPlayerInGame(gamePlayer) == 0) {
			defenders.removePlayer(gamePlayer);
		} else if (getTeamOfPlayerInGame(gamePlayer) == 1) {
			escorters.removePlayer(gamePlayer);
		} else {
			return;
		}
		teleportPlayerToLobby(gamePlayer);
		died.add(gamePlayer.getName());

		// Send message
	}

	public void playerQuitOut(GamePlayer gamePlayer) { // Called from listener
		if (getTeamOfPlayerInGame(gamePlayer) == 0) {
			defenders.removePlayer(gamePlayer);
		} else if (getTeamOfPlayerInGame(gamePlayer) == 1) {
			escorters.removePlayer(gamePlayer);
		} else {
			return;
		}
		teleportPlayerToLobby(gamePlayer);
		previousPlayers.add(gamePlayer.getName() + "," + getTeamOfPlayerInGame(gamePlayer) + "," + getLivesOfPlayerInGame(gamePlayer));
		// Send message
	}

	public void playerKickOut(GamePlayer gamePlayer) { // Called from listener
		if (getTeamOfPlayerInGame(gamePlayer) == 0) {
			defenders.removePlayer(gamePlayer);
		} else if (getTeamOfPlayerInGame(gamePlayer) == 1) {
			escorters.removePlayer(gamePlayer);
		} else {
			return;
		}
		teleportPlayerToLobby(gamePlayer);
		previousPlayers.add(gamePlayer.getName() + "," + getTeamOfPlayerInGame(gamePlayer) + "," + getLivesOfPlayerInGame(gamePlayer));
		// Send message
	}

	public void presidentKickedOut(GamePlayer gamePlayer) {
		died.add(gamePlayer.getName());
	}
	// END PLAYER MANAGEMENT METHODS


	// BEGIN GAME METHODS
	private void startGameCountdown() {
		// Set stage and start count down to starting
		gameStage = GameStage.GAMESTARTCOUNTDOWN;
		new CountDown(this, true);
	}

	private void endGameCountdown() {
		// Set stage and start count down to ending
		gameStage = GameStage.ENDINGCOUNTDOWN;
		new CountDown(this, false);
	}

	private void startGame() {

	}

	private void endGame() {

	}
	// END GAME METHODS


	// BEGIN MISC METHODS
	// END MISC METHODS


	// BEGIN TELEPORT METHODS
	public void teleportPlayerToLobby(GamePlayer gamePlayer) {
		gamePlayer.getPlayer().teleport(plugin.lobby);
		gamePlayer.setState(PlayerState.LOBBY);
	}
	// END TELEPORT METHODS


	// BEGIN CHECKING METHODS
	private boolean canJoinTeam(int team) {
		if (totalPlayers() > 4) {
			if (team == 0) {
				if (defenders.totalPlayers() > escorters.totalPlayers()) {
					if (defenders.totalPlayers() - 4 > escorters.totalPlayers()) {
						return false;
					}
				}
				return true;
			} else {
				if (escorters.totalPlayers() > defenders.totalPlayers()) {
					if (escorters.totalPlayers() - 4 > defenders.totalPlayers()) {
						return false;
					}
				}
				return true;
			}
		} else {
			return true;
		}

	}

	private boolean canStart() {
		return defenders.totalPlayers() + escorters.totalPlayers() == 30;
	}

	private boolean hasBeenInGame(String name) {
		for (String s : previousPlayers) {
			if (s.startsWith(name)) {
				return true;
			}
		}
		return false;
	}

	private int getTeamOfPlayerWhoLeft(String name) {
		for (String s : previousPlayers) {
			if (s.startsWith(name)) {
				int team = 0;
				try {
				team = Integer.parseInt(s.substring(s.indexOf(",") + 1, s.indexOf(",") + 2));
				} catch (NumberFormatException e) {
					return 0;
				}
				return team;
			}
		}
		return 0;
	}

	private int getLivesOfPlayerWhoLeft(String name) {
		for (String s : previousPlayers) {
			if (s.startsWith(name)) {
				int lives = 0;
				try {
					lives = Integer.parseInt(s.substring(s.indexOf(",") + 3));
				} catch (NumberFormatException e) {
					return 0;
				}
				return lives;
			}
		}
		return 0;
	}

	private int getLivesOfPlayerInGame(GamePlayer gamePlayer) {
		if (!defenders.canJoinTeam(gamePlayer)) {
			return defenders.getPlayerRemainingLives(gamePlayer);
		} else if (!escorters.canJoinTeam(gamePlayer)) {
			return escorters.getPlayerRemainingLives(gamePlayer);
		}
		return 0;
	}

	private int getTeamOfPlayerInGame(GamePlayer gamePlayer) {
		if (!defenders.canJoinTeam(gamePlayer)) {
			return 0;
		} else if (!escorters.canJoinTeam(gamePlayer)) {
			return 1;
		}
		return 0;
	}

	private Team getTeamOfPlayer(GamePlayer gamePlayer) {
		if (!defenders.canJoinTeam(gamePlayer)) {
			return defenders;
		} else if (!escorters.canJoinTeam(gamePlayer)) {
			return escorters;
		}
		return null;
	}

	private void removePlayerWhoJoinedBack(String name) {
		for (int x = 0; x < previousPlayers.size(); x++) {
			if (previousPlayers.get(x).startsWith(name)) {
				previousPlayers.remove(x);
			}
		}
	}

	private int totalPlayers() { return defenders.totalPlayers() + escorters.totalPlayers(); }
	// END CHECKING METHODS
}
