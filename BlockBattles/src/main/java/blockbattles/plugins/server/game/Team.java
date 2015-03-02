package blockbattles.plugins.server.game;

import blockbattles.plugins.server.main.BlockBattles;
import blockbattles.plugins.server.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import java.util.ArrayList;

public class Team {

	private Arena arena;

	private ArrayList<ArenaPlayer> teammates = new ArrayList<ArenaPlayer>();

	public Team(Arena a) {
		arena = a;
	}

	// BEGIN TEAM MEMBER METHODS
	/**
	 * Check if the team is not full.
	 * @return true if the team is full
	 */
	private boolean isTeamFull() {
		return !(teammates.size() < arena.getTeamSize());
	}

	/**
	 * Get the total players on the team
	 * @return total players
	 */
	public int totalPlayers() { return teammates.size(); }

	/**
	 * Check if the player can use the selected class
	 * @param arenaPlayer is not on team
	 * @return true if they can use that class
	 */
	public boolean canUseClass(ArenaPlayer arenaPlayer) {
		if (totalPlayers() >= 2) {
			int withClass = 0;
			for (ArenaPlayer aPlayer : teammates) {
				if (aPlayer.getPClass().equals(arenaPlayer.getPClass())) {
					withClass++;
				}
			}
			if (withClass > 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Add player to team
	 * @param arenaPlayer is not on team
	 * @return true if added to team
	 */
	public boolean addPlayer(ArenaPlayer arenaPlayer) {
		if (!isTeamFull()) {
			teammates.add(arenaPlayer);
			return true;
		}
		return false;
	}

	/**
	 * Remove player from the Team
	 * @param arenaPlayer is on the team
	 * @param location is a location
	 */
	public void removePlayer(ArenaPlayer arenaPlayer, Location location) {
		arenaPlayer.getPlayer().teleport(location);
		arenaPlayer.setStage(PlayerStage.LOBBY);
		arenaPlayer.setArena(null);
		Utils.playerInLobby(arenaPlayer.getPlayer());
		teammates.remove(arenaPlayer);
	}
	/**
	 * Check if the player is on this team
	 * @param aPlayer is in the arena
	 * @return true if player is on team
	 */
	public boolean isPlayerOnTeam(ArenaPlayer aPlayer) {
		for (ArenaPlayer arenaPlayer : teammates) {
			if (arenaPlayer.equals(aPlayer)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if all players are dead
	 * @return true if all players are dead
	 */
	public boolean allDead() {
		for (ArenaPlayer arenaPlayer : teammates) {
			if (!arenaPlayer.getStage().equals(PlayerStage.SPECTATING)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get how many players are still alive
	 * @return how many players are still alive
	 */
	public int totalAlive() {
		int x = 0;
		for (ArenaPlayer arenaPlayer : teammates) {
			if (arenaPlayer.getStage().equals(PlayerStage.BATTLE)) {
				x++;
			}
		}
		return x;
	}

	/**
	 * Give teammates B&L XP for game win
	 */
	public void giveTeamWinGamePoints() {
		BlockBattles plugin = arena.getPlugin();
		for (ArenaPlayer arenaPlayer : teammates) {
			plugin.pM.getBBPlayer(arenaPlayer.getPlayer()).addCredits(plugin.xpWinGame);
		}
	}

	/**
	 * Give teammates B&L XP for round win
	 */
	public void giveTeamWinRoundPoints() {
		BlockBattles plugin = arena.getPlugin();
		for (ArenaPlayer arenaPlayer : teammates) {
			plugin.pM.getBBPlayer(arenaPlayer.getPlayer()).addCredits(plugin.xpWinRound);
		}
	}

	/**
	 * Give teammates B&L XP for game loss
	 */
	public void giveTeamLoseGamePoints() {
		BlockBattles plugin = arena.getPlugin();
		for (ArenaPlayer arenaPlayer : teammates) {
			plugin.pM.getBBPlayer(arenaPlayer.getPlayer()).addCredits(plugin.xpLoseGame);
		}
	}

	/**
	 * Give teammates B&L XP for round loss
	 */
	public void giveTeamLoseRoundPoints() {
		BlockBattles plugin = arena.getPlugin();
		for (ArenaPlayer arenaPlayer : teammates) {
			plugin.pM.getBBPlayer(arenaPlayer.getPlayer()).addCredits(plugin.xpLoseRound);
		}
	}
	// END TEAM MEMBERS METHODS


	// BEGIN GAME PLAY METHODS
	/**
	 * Teleport all players on the Team to a spawn location
	 * @param spawns is an ArrayList of Locations
	 */
	public void teleportTeamToSpawns(ArrayList<Location> spawns) {
		int x = 0;
		for (ArenaPlayer arenaPlayer : teammates) {
			arenaPlayer.getPlayer().teleport(spawns.get(x));
			arenaPlayer.setStage(PlayerStage.BATTLE);
			x++;
			if (x == spawns.size()) {
				x = 0;
			}
		}
	}

	/**
	 * Teleport all players on the Team to spectate
	 * @param location is a location
	 */
	public void teleportTeamToSpectate(Location location) {
		for (ArenaPlayer arenaPlayer : teammates) {
			arenaPlayer.getPlayer().teleport(location);
			arenaPlayer.setStage(PlayerStage.SPECTATING);
		}
	}

	/**
	 * Teleport all players on the Team to lobby and set new stage
	 * @param location is a location
	 */
	public void teleportTeamToLobby(Location location) {
		for (int x = 0; x < teammates.size(); x++) {
			ArenaPlayer arenaPlayer = teammates.get(x);
			if (!arenaPlayer.getPlayer().isDead()) {
				arenaPlayer.getPlayer().teleport(location);
			}
			arenaPlayer.setStage(PlayerStage.LOBBY);
			arenaPlayer.setArena(null);
			Utils.playerInLobby(arenaPlayer.getPlayer());
			teammates.remove(arenaPlayer);
			x--;
		}
	}

	/**
	 * Set every players stage.
	 * @param stage is PlayerStage
	 */
	public void setStages(PlayerStage stage) {
		for (ArenaPlayer arenaPlayer : teammates) {
			arenaPlayer.setStage(stage);
		}
	}

	/**
	 * Give the players their class for the round.
	 */
	public void giveClasses() {
		for (ArenaPlayer arenaPlayer : teammates) {
			arenaPlayer.getPlayer().getInventory().clear();
			arenaPlayer.getPlayer().setHealth(arenaPlayer.getPlayer().getMaxHealth());
			arena.getPlugin().getClass(arenaPlayer.getPClass()).give(arenaPlayer.getPlayer());
		}
	}
	// END GAME PLAY METHODS


	// BEGIN SENDMESSAGE METHODS
	public void sendMessage(String msg) {
		for (ArenaPlayer arenaPlayer : teammates) {
			arenaPlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}
	}

	public void sendMessage(String[] msgs) {
		for (ArenaPlayer arenaPlayer : teammates) {
			for (String msg : msgs) {
				arenaPlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			}
		}
	}
	// END SENDMESSAGE METHODS

}
