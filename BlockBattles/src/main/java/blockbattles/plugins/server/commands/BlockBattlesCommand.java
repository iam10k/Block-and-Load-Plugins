package blockbattles.plugins.server.commands;

import blockbattles.plugins.server.game.Arena;
import blockbattles.plugins.server.game.ArenaPlayer;
import blockbattles.plugins.server.game.GameClass;
import blockbattles.plugins.server.main.BlockBattles;
import blockbattles.plugins.server.util.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class BlockBattlesCommand implements CommandExecutor, Listener {

	private BlockBattles plugin;


	public BlockBattlesCommand(BlockBattles pl) {
		plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("blockbattles.editmode")) {
			return true;
		}

		if (!(sender instanceof Player)) {
			return false;
		}

		Player player = (Player) sender;

		ArenaPlayer arenaPlayer = plugin.getArenaPlayer((Player) sender);

		if (args.length == 0) {
			String[] temp = new String[8];
			temp[0] = "&3/blockbattles editarena [id]";
			temp[1] = "&3/blockbattles set <lobby|spectate|minpoint|maxpoint|infosign|teamasign|teambsign|enable>";
			temp[2] = "&3/blockbattles addspawn <a|b>";
			temp[3] = "&3/blockbattles reloadarena <id>";
			temp[4] = "&3/blockbattles createclass <id> <name>";
			temp[5] = "&3/blockbattles editclass <id> <material|cost|perkid|name> <value>";
			temp[6] = "&3/blockbattles createarena <id> <name> <teamSize>";
			temp[7] = "&3/blockbattles list <arenas|classes>";

			plugin.pM.sendMessage(player, temp);
			return true;
		}

		if (args.length == 1) {

			// Enable edit or disable edit /blockbattles editarena
			if (args[0].equalsIgnoreCase("editarena")) {
				if (!arenaPlayer.isEditing()) {
					for (Arena a : plugin.getArenas()) {
						if (a.isEnabled()) {
							if (a.inRegion(arenaPlayer)) {
								arenaPlayer.setEditing(true);
								arenaPlayer.setArena(a);
								plugin.pM.sendMessage(player, "&aNow editing " + a.getID() + ".");
								return true;
							}
						}
					}
					plugin.pM.sendMessage(player, "&cYou are not in an arena region.");
					return true;
				} else {
					plugin.pM.sendMessage(player, "&cNo longer editing " + arenaPlayer.getArena().getID() + ".");
					arenaPlayer.setEditing(false);
					arenaPlayer.setArena(null);
					return true;
				}
			}
		}

		if (args.length == 2) {

			// Set commands /blockbattles set [lobby|spectate|minpoint|maxpoint|infosign|teamasign|teambsign]
			if (args[0].equalsIgnoreCase("set")) {

				// Set Lobby Location /blockbattles set lobby
				if (args[1].equalsIgnoreCase("lobby")) {
					plugin.getConfig().set("Lobby.location", Utils.convertLocationToString(player.getLocation(), false, true));
					plugin.saveConfig();
					plugin.lobby = player.getLocation();
					plugin.pM.sendMessage(player, "&aLobby location set!");
					return true;
				} else {

					// Cannot use these commands if not editing an arena
					if (!arenaPlayer.isEditing() && arenaPlayer.getArena() != null) {
						plugin.pM.sendMessage(player, "&cYou are not editing an arena.");
						return true;
					}

					// Set Spectate Location /blockbattles set spectate
					if (args[1].equalsIgnoreCase("spectate")) {
						arenaPlayer.getArena().setSpectate(player.getLocation());
						plugin.pM.sendMessage(player, "&aSet spectate location.");
						return true;
					}

					// Set Min Point Location /blockbattles set minpoint
					if (args[1].equalsIgnoreCase("minpoint")) {
						arenaPlayer.getArena().setArenaMin(player.getLocation());
						plugin.pM.sendMessage(player, "&aSet minimum region location.");
						return true;
					}

					// Set Max Point Location /blockbattles set maxpoint
					if (args[1].equalsIgnoreCase("maxpoint")) {
						arenaPlayer.getArena().setArenaMax(player.getLocation());
						plugin.pM.sendMessage(player, "&aSet maximum region location.");
						return true;
					}

					// Set Info Sign Location /blockbattles set infosign
					if (args[1].equalsIgnoreCase("infosign")) {
						arenaPlayer.getArena().setInfoSign(player.getEyeLocation());
						plugin.pM.sendMessage(player, "&aSet info sign location.");
						return true;
					}

					// Set Team A Sign Location /blockbattles set teamasign
					if (args[1].equalsIgnoreCase("teamasign")) {
						arenaPlayer.getArena().setTeamASign(player.getEyeLocation());
						plugin.pM.sendMessage(player, "&aSet team A sign location.");
						return true;
					}

					// Set Team B Sign Location /blockbattles set teamBsign
					if (args[1].equalsIgnoreCase("teambsign")) {
						arenaPlayer.getArena().setTeamBSign(player.getEyeLocation());
						plugin.pM.sendMessage(player, "&aSet team B sign location.");
						return true;
					}

					// Enable arena /blockbattles set enable
					if (args[1].equalsIgnoreCase("enable")) {
						arenaPlayer.getArena().enable();
						plugin.pM.sendMessage(player, "&aArena enabled. Reload arena to make usable.");
						return true;
					}
				}
			}

			// Add Spawn to Team /blockbattles addspawn <a|b>
			if (args[0].equalsIgnoreCase("addspawn")) {

				// Cannot use these commands if not editing an arena
				if (!arenaPlayer.isEditing() && arenaPlayer.getArena() != null) {
					plugin.pM.sendMessage(player, "&cYou are not editing an arena.");
					return true;
				}

				// Add Spawn to Team A /blockbattles addspawn a
				if (args[1].equalsIgnoreCase("a")) {
					if (arenaPlayer.getArena().addTeamASpawn(player.getLocation())) {
						plugin.pM.sendMessage(player, "&aAdded team A spawn location.");
						return true;
					}
					plugin.pM.sendMessage(player, "&cSpawn already added.");
					return true;
				}

				// Add Spawn to Team B /blockbattles addspawn b
				if (args[1].equalsIgnoreCase("b")) {
					if (arenaPlayer.getArena().addTeamBSpawn(player.getLocation())) {
						plugin.pM.sendMessage(player, "&aAdded team B spawn location.");
						return true;
					}
					plugin.pM.sendMessage(player, "&cSpawn already added.");
					return true;
				}
			}

			// Select the arena to edit that has no region /blockbattles editarena <id>
			if (args[0].equalsIgnoreCase("editarena")) {
				if (!arenaPlayer.isEditing()) {
					for (Arena a : plugin.getArenas()) {
						if (a != null && a.getID().equalsIgnoreCase(args[1])) {
							arenaPlayer.setEditing(true);
							arenaPlayer.setArena(a);
							plugin.pM.sendMessage(player, "&aNow editing " + a.getID() + ".");
							return true;
						}
					}
					plugin.pM.sendMessage(player, "&cArena " + args[1] + " not found.");
					return true;
				}
			}

			// Reload arena /blockbattles reloadarena <id>
			if (args[0].equalsIgnoreCase("reloadarena")) {
				for (Arena a : plugin.getArenas()) {
					if (a.getID().equalsIgnoreCase(args[1])) {
						a.reloadArena();
						plugin.pM.sendMessage(player, "&a" + a.getID() + " reloaded.");
						return true;
					}
				}
				plugin.pM.sendMessage(player, "&cArena " + args[1] + " not found.");
				return true;
			}

			// List classes or arenas
			if (args[0].equalsIgnoreCase("list")) {
				if (args[1].equalsIgnoreCase("arenas")) {
					String temp = "";
					for (Arena a : plugin.getArenas()) {
						temp += a.getID() + ",";
					}
					plugin.pM.sendMessage(player, "&aArenas: " + temp);
					return true;
				} else if (args[1].equalsIgnoreCase("classes")) {
					String temp = "";
					for (GameClass gameClass : plugin.getGameClasses()) {
						temp += gameClass.getID() + ",";
					}
					plugin.pM.sendMessage(player, "&aClasses: " + temp);
					return true;
				} else {
					plugin.pM.sendMessage(player, "&3/blockbattles list <arenas|classes>");
				}
			}
		}

		if (args.length == 3) {

			// Create Class Command /blockbattles createclass <id> <name>
			if (args[0].equalsIgnoreCase("createclass")) {
				if (plugin.createClass(player, args[1].toLowerCase(), args[2])) {
					plugin.pM.sendMessage(player, "&aClass created!");
					return true;
				} else {
					plugin.pM.sendMessage(player, "&cClass already exists!");
					return true;
				}
			}
		}

		if (args.length == 4) {

			// Create Class Command /blockbattles editclass <id> <setting> <value>
			if (args[0].equalsIgnoreCase("editclass")) {
				if (plugin.getClass(args[1].toLowerCase()) != null) {

					// Change class material /blockbattles editclass <id> material <value>
					if (args[2].equalsIgnoreCase("material")) {
						if (Material.getMaterial(args[3]) != null) {
							plugin.getClass(args[1].toLowerCase()).setMaterial(plugin, args[3]);
							plugin.pM.sendMessage(player, "&aMaterial changed!");
							return true;
						} else {
							plugin.pM.sendMessage(player, "&cInvalid material!");
							return true;
						}
					}

					// Change class material /blockbattles editclass <id> cost <value>
					if (args[2].equalsIgnoreCase("cost")) {
						int cost = 0;
						try {
							cost = Integer.parseInt(args[3]);
						} catch (NumberFormatException e) {
							plugin.pM.sendMessage(player, "&cInvalid cost!");
							return true;
						}
						plugin.getClass(args[1].toLowerCase()).setCost(plugin, cost);
						plugin.pM.sendMessage(player, "&aCost changed!");
						return true;
					}

					// Change class material /blockbattles editclass <id> perkid <value>
					if (args[2].equalsIgnoreCase("perkid")) {
						plugin.getClass(args[1].toLowerCase()).setPerkID(plugin, args[3].toUpperCase());
						plugin.pM.sendMessage(player, "&aPerkID changed!");
						return true;
					}

					// Change class display name /blockbattles editclass <id> name <value>
					if (args[2].equalsIgnoreCase("name")) {
						plugin.getClass(args[1].toLowerCase()).setPerkID(plugin, args[3].replace("_", " "));
						plugin.pM.sendMessage(player, "&aName changed!");
						return true;
					}
				} else {
					plugin.pM.sendMessage(player, "&cClass not found!");
					return true;
				}
			}

			// Create Arena Command /blockbattles createarena <id> <name> <teamsize>
			if (args[0].equalsIgnoreCase("createarena")) {
				int size;
				try {
					size = Integer.parseInt(args[3]);
				} catch (NumberFormatException e) {
					size = 4;
				}
				if (plugin.createArena(args[1].toLowerCase(), args[2].replace("_", " "), size)) {
					plugin.pM.sendMessage(player, "&aArena created!");
					return true;
				} else {
					plugin.pM.sendMessage(player, "&cArena already exists!");
					return true;
				}
			}
		}
		return false;
	}
}