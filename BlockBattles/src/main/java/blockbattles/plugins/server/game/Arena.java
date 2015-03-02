package blockbattles.plugins.server.game;

import blockbattles.plugins.server.main.BlockBattles;
import blockbattles.plugins.server.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Arena {

	private BlockBattles plugin;
	private FileConfiguration config;

	// Team A
	private Team teamA;
	private int teamAWins = 0;
	private ArrayList<Location> teamASpawns = new ArrayList<Location>();

	// Team B
	private Team teamB;
	private int teamBWins = 0;
	private ArrayList<Location> teamBSpawns = new ArrayList<Location>();

	// Arena
	private Location arenaMin;
	private Location arenaMax;
	private Location spectate;

	// Sign Locations
	private Location infoSign;
	private Location teamASign;
	private Location teamBSign;

	// Game Stage
	private GameStage gameStage = GameStage.LOADING;

	// Game Clock
	RoundClock clock;

	// Blocks to remove list
	private ArrayList<Flower> flower = new ArrayList<Flower>();
	private ArrayList<Web> web = new ArrayList<Web>();
	private ArrayList<Block> landmines = new ArrayList<Block>();
	private ArrayList<CFour> c4list = new ArrayList<CFour>();

	public Arena(BlockBattles pl, FileConfiguration arenaConfig) {
		plugin = pl;
		config = arenaConfig;

		if (config.getBoolean("Arena.enabled", false)) {
			init();
		}
	}

	/**
	 * Load the game settings and put arena in waiting stage.
	 */
	private void init() {
		// Arena Locations
		arenaMin = Utils.convertStringToLocation(config.getString("Location.minPoint"), false);
		arenaMax = Utils.convertStringToLocation(config.getString("Location.maxPoint"), false);
		spectate = Utils.convertStringToLocation(config.getString("Location.spectate"), true);

		// Sign Locations
		infoSign = Utils.convertStringToLocation(config.getString("Location.infoSign"), false);
		teamASign = Utils.convertStringToLocation(config.getString("Location.teamASign"), false);
		teamBSign = Utils.convertStringToLocation(config.getString("Location.teamBSign"), false);

		teamA = new Team(this);
		teamB = new Team(this);

		initSpawns();

		gameStage = GameStage.WAITING;
	}

	public void closeArena() {

	}

	/**
	 * Load the spawns for each team.
	 */
	private void initSpawns() {
		for (String loc : config.getStringList("TeamA.spawns")) {
			teamASpawns.add(Utils.convertStringToLocation(loc, true));
		}
		for (String loc : config.getStringList("TeamB.spawns")) {
			teamBSpawns.add(Utils.convertStringToLocation(loc, true));
		}
	}

	/**
	 * Reload the arena after modifying the config
	 * @return false if arena cannot reload
	 */
	public boolean reloadArena() {
		if (teamA != null && (teamA.totalPlayers() > 0 || teamB.totalPlayers() > 0)) {
			return false;
		}

		init();

		return true;
	}

	// BEGIN MODIFY CONFIG METHODS
	private void saveConfig() {
		File arenaFile = new File(plugin.getDataFolder() + File.separator + "arenas", getID() + ".yml");
		try {
			config.save(arenaFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setArenaMin(Location loc) {
		config.set("Location.minPoint", Utils.convertLocationToString(loc, false, false));
		saveConfig();
	}

	public void setArenaMax(Location loc) {
		config.set("Location.maxPoint", Utils.convertLocationToString(loc, false, false));
		saveConfig();
	}

	public void setSpectate(Location loc) {
		config.set("Location.spectate", Utils.convertLocationToString(loc, false, true));
		saveConfig();
	}

	public void setInfoSign(Location loc) {
		config.set("Location.infoSign", Utils.convertLocationToString(loc, false, false));
		saveConfig();
	}

	public void setTeamASign(Location loc) {
		config.set("Location.teamASign", Utils.convertLocationToString(loc, false, false));
		saveConfig();
	}

	public void setTeamBSign(Location loc) {
		config.set("Location.teamBSign", Utils.convertLocationToString(loc, false, false));
		saveConfig();
	}

	public boolean addTeamASpawn(Location loc) {
		List<String> spawns = config.getStringList("TeamA.spawns");
		if (spawns.contains(Utils.convertLocationToString(loc, false, true))) {
			return false;
		}
		spawns.add(Utils.convertLocationToString(loc, false, true));
		config.set("TeamA.spawns", spawns);
		saveConfig();
		return true;
	}

	public boolean addTeamBSpawn(Location loc) {
		List<String> spawns = config.getStringList("TeamB.spawns");
		if (spawns.contains(Utils.convertLocationToString(loc, false, true))) {
			return false;
		}
		spawns.add(Utils.convertLocationToString(loc, false, true));
		config.set("TeamB.spawns", spawns);
		saveConfig();
		return true;
	}

	public void enable() {
		config.set("Arena.enabled", true);
		saveConfig();
	}
	// END MODIFY CONFIG METHODS


	// BEGIN ACCESS METHODS
	/**
	 * Get the BlockBattles Plugin
	 * @return BlockBattle class
	 */
	public BlockBattles getPlugin() { return plugin; }

	/**
	 * Get id that is the same as filename
	 * @return File ID
	 */
	public String getID() { return config.getString("Arena.id"); }

	/**
	 * Get the Arena's current stage
	 * @return GameStage of arena
	 */
	public GameStage getGameStage() { return gameStage; }

	/**
	 * Check if the Arena is enabled
	 * @return true if it is
	 */
	public boolean isEnabled() { return config.getBoolean("Arena.enabled", false); }

	/**
	 * Get max team size
	 * @return int of max team size
	 */
	public int getTeamSize() { return config.getInt("Teams.size", 4); }

	/**
	 * Get the arena's minimum coordinate X
	 * @return min location X
	 */
	public int getMinX() {
		if (arenaMin.getBlockX() < arenaMax.getBlockX()) { return arenaMin.getBlockX(); }
		return arenaMax.getBlockX();
	}

	/**
	 * Get the arena's minimum coordinate Y
	 * @return min location Y
	 */
	public int getMinY() {
		if (arenaMin.getBlockY() < arenaMax.getBlockY()) { return arenaMin.getBlockY(); }
		return arenaMax.getBlockY();
	}

	/**
	 * Get the arena's minimum coordinate Z
	 * @return min location Z
	 */
	public int getMinZ() {
		if (arenaMin.getBlockZ() < arenaMax.getBlockZ()) { return arenaMin.getBlockZ(); }
		return arenaMax.getBlockZ();
	}

	/**
	 * Get the arena's maximum coordinate X
	 * @return max location X
	 */
	public int getMaxX() {
		if (arenaMin.getBlockX() > arenaMax.getBlockX()) { return arenaMin.getBlockX(); }
		return arenaMax.getBlockX();
	}

	/**
	 * Get the arena's maximum coordinate Y
	 * @return max location Y
	 */
	public int getMaxY() {
		if (arenaMin.getBlockY() > arenaMax.getBlockY()) { return arenaMin.getBlockY(); }
		return arenaMax.getBlockY();
	}

	/**
	 * Get the arena's maximum coordinate Z
	 * @return max location Z
	 */
	public int getMaxZ() {
		if (arenaMin.getBlockZ() > arenaMax.getBlockZ()) { return arenaMin.getBlockZ(); }
		return arenaMax.getBlockZ();
	}

	/**
	 * Get the specate location
	 * @return Location
	 */
	public Location getSpectate() { return spectate; }

	public boolean inRegion(ArenaPlayer arenaPlayer) {
		Location loc = arenaPlayer.getPlayer().getLocation();
		if (loc.getBlockX() < getMinX() || loc.getBlockX() > getMaxX()) {
			return false;
		} else if (loc.getBlockY() < getMinY() || loc.getBlockY() > getMaxY()) {
			return false;
		} else if (loc.getBlockZ() < getMinZ() || loc.getBlockZ() > getMaxZ()) {
			return false;
		}
		return true;
	}

	/**
	 * Get the arena's info sign location
	 * @return Location of info sign
	 */
	public Location getInfoSignLoc() { return infoSign; }

	/**
	 * Get the arena's Team A sign location
	 * @return Location of Team A sign
	 */
	public Location getTeamASignLoc() { return teamASign; }

	/**
	 * Get the arena's Team B sign location
	 * @return Location of Team B sign
	 */
	public Location getTeamBSignLoc() { return teamBSign; }

	/**
	 * Check to see if the two ArenaPlayers are on the same team
	 * @param arenaPlayer is in this arena
	 * @param arenaPlayer2 is in this arena
	 * @return true if on same team
	 */
	public boolean onSameTeam(ArenaPlayer arenaPlayer, ArenaPlayer arenaPlayer2) {
		if (teamA.isPlayerOnTeam(arenaPlayer)) {
			return teamA.isPlayerOnTeam(arenaPlayer2);
		} else {
			return teamB.isPlayerOnTeam(arenaPlayer2);
		}
	}

	/**
	 * Get the name displayed on sign
	 * @return Lines to be put on sign
	 */
	public String[] getInfoSign() {
		String[] temp = new String[4];
		temp[0] = "";
		temp[1] = config.getString("Arena.signName", "Arena");
		temp[2] = gameStage.toString();
		temp[3] = "";
		return temp;
	}

	/**
	 * Get Team A info for a sign
	 * @return Lines to be put on sign
	 */
	public String[] getTeamASign() {
		String[] temp = new String[4];
		temp[0] = "&4Red Team";
		temp[1] = "&6" + teamA.totalPlayers() + " &0out of";
		temp[2] = "&0" + getTeamSize() + " players";
		temp[3] = "&a" + teamAWins + " wins";
		return temp;
	}

	/**
	 * Get Team B info for a sign
	 * @return Lines to be put on sign
	 */
	public String[] getTeamBSign() {
		String[] temp = new String[4];
		temp[0] = "&3Blue Team";
		temp[1] = "&6" + teamB.totalPlayers() + " &0out of";
		temp[2] = "&0" + getTeamSize() + " players";
		temp[3] = "&a" + teamBWins + " wins";
		return temp;
	}
	// END ACCESS METHODS


	// BEGIN PLAYER METHODS
	/**
	 * Add Player to the specified team
	 * @param arenaPlayer player to add
	 * @param team 0 - Team A : 1 - Team B
	 * @return true if added
	 */
	public boolean addPlayer(ArenaPlayer arenaPlayer, int team) {
		if (teamA.isPlayerOnTeam(arenaPlayer)) {
			teamA.removePlayer(arenaPlayer, arenaPlayer.getPlayer().getLocation());
			sendMessage(arenaPlayer, "  &cYou have left the arena!");
			return false;
		}

		if (teamB.isPlayerOnTeam(arenaPlayer)) {
			teamB.removePlayer(arenaPlayer, arenaPlayer.getPlayer().getLocation());
			sendMessage(arenaPlayer, "  &cYou have left the arena!");
			return false;
		}

		if (arenaPlayer.getArena() != null) {
			sendMessage(arenaPlayer, "  &cYou are already in an arena!");
			return false;
		}

		if (team == 0) {
			if (!teamA.canUseClass(arenaPlayer)) {
				sendMessage(arenaPlayer, "  &cThe class you have selected is already used by two players on Red Team. " +
						"Please select another class, team, or arena.");
				return false;
			}
			if (teamA.addPlayer(arenaPlayer)) {
				arenaPlayer.setArena(this);
				if (gameStage.equals(GameStage.WAITING)) {
					arenaPlayer.setStage(PlayerStage.WAITING);
					sendMessage(arenaPlayer, "  &aJoined Red Team!");
				} else {
					arenaPlayer.setStage(PlayerStage.SPECTATING);
					sendMessage(arenaPlayer, "  &aJoined Red Team!");
				}
			} else {
				sendMessage(arenaPlayer, "  &cRed Team is full!");
				return false;
			}
		} else {
			if (!teamB.canUseClass(arenaPlayer)) {
				sendMessage(arenaPlayer, "  &cThe class you have selected is already used by two players on Blue Team. " +
						"Please select another class, team, or arena.");
				return false;
			}
			if (teamB.addPlayer(arenaPlayer)) {
				arenaPlayer.setArena(this);
				if (gameStage.equals(GameStage.WAITING)) {
					arenaPlayer.setStage(PlayerStage.WAITING);
					sendMessage(arenaPlayer, "  &aJoined Blue Team!");
				} else {
					arenaPlayer.setStage(PlayerStage.SPECTATING);
					sendMessage(arenaPlayer, "  &aJoined Blue Team!");
				}
			} else {
				sendMessage(arenaPlayer, "  &cBlue Team is full!");
				return false;
			}
		}

		if (canStart()) {
			startRoundCountdown(1);
		}
		return true;
	}

	/**
	 * Remove player from arena
	 * @param arenaPlayer is in this arena
	 */
	public void removePlayer(ArenaPlayer arenaPlayer) {
		sendMessage("  " + getTeamColorWithName(arenaPlayer) + " &7has left the arena!");
		if (teamA.isPlayerOnTeam(arenaPlayer)) {
			teamA.removePlayer(arenaPlayer, plugin.lobby);
		} else if (teamB.isPlayerOnTeam(arenaPlayer)) {
			teamB.removePlayer(arenaPlayer, plugin.lobby);
		}

		if (canRoundEnd() || teamA.totalPlayers() == 0 || teamB.totalPlayers() == 0) {
			// Add win and send message
			if (teamA.allDead()) {
				teamBWins++;
				teamB.giveTeamWinRoundPoints();
				teamA.giveTeamLoseRoundPoints();
				sendMessage("  &3Blue Team &ahas won the round!");
			} else if (teamB.allDead()) {
				teamAWins++;
				teamA.giveTeamWinRoundPoints();
				teamB.giveTeamLoseRoundPoints();
				sendMessage("  &cRed Team &ahas won the round!");
			}
			// Check if there will not be another round
			if (teamAWins == 2 || teamBWins == 2 || teamA.totalPlayers() == 0 || teamB.totalPlayers() == 0) {
				endGame();
			} else { // Another round
				startRoundCountdown(teamAWins + teamBWins + 1);
			}
		}
	}
	// END PLAYER METHODS


	// BEGIN GAME METHODS
	/**
	 * Start the countdown for the next round.
	 * @param round int 1 to 3
	 */
	public void startRoundCountdown(int round) {
		if (round == 1) {
			gameStage = GameStage.ROUND1COUNTDOWN;
		} else if (round == 2) {
			teleportToSpectate();
			gameStage = GameStage.ROUND2COUNTDOWN;
		} else if (round == 3) {
			teleportToSpectate();
			gameStage = GameStage.ROUND3COUNTDOWN;
		}

		// Start countdown
		new RoundCountdown(this, round);

		// Remove flowers, web and mines
		for (int x = 0; x < flower.size(); x++) {
			flower.get(x).remove();
			flower.remove(x);
			x--;
		}
		for (int x = 0; x < web.size(); x++) {
			web.get(x).cancelEarly();
			web.remove(x);
			x--;
		}
		for (int x = 0; x < landmines.size(); x++) {
			landmines.get(x).setType(Material.AIR);
			landmines.remove(x);
			x--;
		}
	}

	/**
	 * Start the round either all the way of half way.
	 * @param round int 1 to 3
	 * @param halfway true if want to teleport to spawns
	 */
	public void startRound(int round, boolean halfway) {
		if (halfway) {
			teleportToSpawns();

			teamA.setStages(PlayerStage.WAITING);
			teamA.giveClasses();

			teamB.setStages(PlayerStage.WAITING);
			teamB.giveClasses();
		} else {
			if (round == 1) {
				gameStage = GameStage.ROUND1;
			} else if (round == 2) {
				gameStage = GameStage.ROUND2;
			} else if (round == 3) {
				gameStage = GameStage.ROUND3;
			}

			teamA.setStages(PlayerStage.BATTLE);

			teamB.setStages(PlayerStage.BATTLE);

			clock = new RoundClock(this, round);
		}
	}

	public void roundTimeUp() {
		if (teamB.totalAlive() > teamA.totalAlive()) {
			teamBWins++;
			teamB.giveTeamWinRoundPoints();
			teamA.giveTeamLoseRoundPoints();
			sendMessage("  &3Blue Team &ahas won the round!");
		} else if (teamA.totalAlive() > teamB.totalAlive()) {
			teamAWins++;
			teamA.giveTeamWinRoundPoints();
			teamB.giveTeamLoseRoundPoints();
			sendMessage("  &cRed Team &ahas won the round!");
		}

		// Check if there will not be another round
		if (teamAWins == 2 || teamBWins == 2) {
			endGame();
		} else { // Another round
			startRoundCountdown(teamAWins + teamBWins + 1);
		}
	}

	/**
	 * End game and reset arena
	 */
	public void endGame() {
		gameStage = GameStage.ENDING;
		if (teamAWins > teamBWins) {
			sendMessage("  &cRed Team &ahas won the game!");
			teamA.giveTeamWinGamePoints();
			teamB.giveTeamLoseGamePoints();
		} else if (teamBWins > teamAWins) {
			sendMessage("  &3Blue Team &ahas won the game!");
			teamB.giveTeamWinGamePoints();
			teamA.giveTeamLoseGamePoints();
		}

		teleportToLobby();

		// Remove flowers, web and mines, c4
		for (int x = 0; x < flower.size(); x=0) {
			flower.get(x).remove();
			flower.remove(x);
		}
		for (int x = 0; x < web.size(); x=0) {
			web.get(x).cancelEarly();
			web.remove(x);
		}
		for (int x = 0; x < landmines.size(); x=0) {
			landmines.get(x).setType(Material.AIR);
			landmines.remove(x);
		}
		for (int x = 0; x < c4list.size(); x=0) {
			c4list.get(x).remove();
			c4list.remove(x);
		}

		// Final resetting of arena
		teamAWins = 0;
		teamBWins = 0;

		gameStage = GameStage.WAITING;
	}

	/**
	 * Add flower/bush plant to this arena
	 * @param b is block
	 * @param arenaPlayer is player in game
	 */
	public void addFlowerPlaced(Block b, ArenaPlayer arenaPlayer) {
		flower.add(new Flower(arenaPlayer, b));
	}

	/**
	 * Add web to the arena
	 * @param b is valid block
	 */
	public void addWeb(Block b) {
		web.add(new Web(this, b));
	}

	public void addC4(ArenaPlayer arenaPlayer, Block b) {
		c4list.add(new CFour(this, arenaPlayer.getPlayer(), b));
	}

	public void removeC4(CFour c4) {
		c4list.remove(c4);
	}

	/**
	 * Remove web from arena
	 * @param loc is valid location
	 */
	public void removeWeb(Location loc) {
		for (Web w : web) {
			if (w.getLocation().equals(loc)) {
				web.remove(w);
				return;
			}
		}
	}

	/**
	 * Add a landmine to the arena
	 * @param b is valid block
	 */
	public void addLandMine(Block b) {
		landmines.add(b);
	}

	/**
	 * Get all the flowers in the arena
	 * @return arraylist of flowers
	 */
	public ArrayList<Flower> getFlowers() { return flower; }

	/**
	 * Get all the landmines in arena
	 * @return arraylist of blocks
	 */
	public ArrayList<Block> getLandmines() { return landmines; }

	/**
	 * Trigger C4 Event
	 * @param arenaPlayer used button
	 */
	public void triggerC4(ArenaPlayer arenaPlayer) {
		for (int x = 0; x < c4list.size(); x++) {
			if (c4list.get(x).isPlayersC4(arenaPlayer.getPlayer())) {
				c4list.get(x).detonate();
				x--;
			}
		}
	}
	// END GAME METHODS


	// BEGIN EVENT HANDLING METHODS
	public void onDeathByPlayerEvent(ArenaPlayer died, ArenaPlayer killer) {
		sendMessage(getTeamColorWithName(died) + " &7was killed by " + getTeamColorWithName(killer) +
						"(" + plugin.pM.getBBPlayer(killer.getPlayer()).getKillstreak() + " killstreak)");

		died.setStage(PlayerStage.SPECTATING);

		if (canRoundEnd() || teamA.totalPlayers() == 0 || teamB.totalPlayers() == 0) {
			// Add win and send message
			if (teamA.allDead()) {
				teamBWins++;
				teamB.giveTeamWinRoundPoints();
				teamA.giveTeamLoseRoundPoints();
				sendMessage("  &3Blue Team &ahas won the round!");
			} else if (teamB.allDead()) {
				teamAWins++;
				teamA.giveTeamWinRoundPoints();
				teamB.giveTeamLoseRoundPoints();
				sendMessage("  &cRed Team &ahas won the round!");
			}
			// Check if there will not be another round
			if (teamAWins == 2 || teamBWins == 2) {
				clock.cancelEarly();
				endGame();
			} else { // Another round
				clock.cancelEarly();
				startRoundCountdown(teamAWins + teamBWins + 1);
			}
		}
	}
	// END EVENT HANDLING METHODS


	// BEGIN MISC METHODS
	/**
	 * Can the round end?
	 * @return true if all players on one team are dead
	 */
	private boolean canRoundEnd() {
		return teamA.allDead() || teamB.allDead();
	}

	/**
	 * Can the game start?
	 * @return true if each team has over half max team size
	 */
	private boolean canStart() {
		return gameStage.equals(GameStage.WAITING) && teamA.totalPlayers() > getTeamSize()/2 && teamB.totalPlayers() > getTeamSize()/2;
	}

	/**
	 * Get the color code + username
	 * @param arenaPlayer is player
	 * @return Team Color and Player name
	 */
	private String getTeamColorWithName(ArenaPlayer arenaPlayer) {
		if (teamA.isPlayerOnTeam(arenaPlayer)) { return "&c" + arenaPlayer.getPlayer().getName(); }
		if (teamB.isPlayerOnTeam(arenaPlayer)) { return "&3" + arenaPlayer.getPlayer().getName(); }
		return "&7Player";
	}
	// END MISC METHODS


	// BEGIN TELEPORT METHODS
	/**
	 * Teleport both teams to their spawns.
	 */
	private void teleportToSpawns() {
		teamA.teleportTeamToSpawns(teamASpawns);
		teamB.teleportTeamToSpawns(teamBSpawns);
	}

	/**
	 * Teleport both teams to spectate
	 */
	private void teleportToSpectate() {
		teamA.teleportTeamToSpectate(spectate);
		teamB.teleportTeamToSpectate(spectate);
	}

	/**
	 * Teleport both teams to lobby
	 */
	private void teleportToLobby() {
		teamA.teleportTeamToLobby(plugin.lobby);
		teamB.teleportTeamToLobby(plugin.lobby);
	}
	// END TELEPORT METHODS


	// BEGIN SENDMESSAGE METHODS
	public void sendMessage(String msg) {
		teamA.sendMessage(msg);
		teamB.sendMessage(msg);
	}

	public void sendMessage(String[] msgs) {
		teamA.sendMessage(msgs);
		teamB.sendMessage(msgs);
	}

	public void sendMessage(ArenaPlayer arenaPlayer, String msg) {
		arenaPlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

	public void sendMessage(ArenaPlayer arenaPlayer, String[] msgs) {
		for (String msg : msgs) {
			arenaPlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}
	}
	// END SENDMESSAGE METHODS

}
