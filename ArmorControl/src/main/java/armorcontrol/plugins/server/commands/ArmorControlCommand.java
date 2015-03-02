package armorcontrol.plugins.server.commands;

import armorcontrol.plugins.server.main.ArmorControl;
import armorcontrol.plugins.server.util.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ArmorControlCommand implements CommandExecutor, Listener {

	private ArmorControl plugin;


	public ArmorControlCommand(ArmorControl pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("refresh")) {
				if (sender.hasPermission("armorcontrol.admin")) {
					Player refreshPlayer = sender.getServer().getPlayer(args[1]);
					if (refreshPlayer.getInventory().getHelmet().getType().equals(Material.LEATHER_HELMET)) {
						if (refreshPlayer.getInventory().getChestplate().getType().equals(Material.LEATHER_CHESTPLATE)) {
							if (refreshPlayer.getInventory().getLeggings().getType().equals(Material.LEATHER_LEGGINGS)) {
								if (refreshPlayer.getInventory().getBoots().getType().equals(Material.LEATHER_BOOTS)) {
									if (refreshPlayer != null) {
										Utils.playerJoin(refreshPlayer, plugin);
									}
								}
							}
						}
					}
					return true;
				}
				return true;
			}
			return false;
		}

		if (!(sender instanceof Player)) {
			return false;
		}

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("editmode")) {
				if (sender.hasPermission("armorcontrol.edit")) {

					String name = sender.getName();

					if (plugin.playersInEditor.contains(name)) {
						plugin.pM.sendMessage(sender, "&7[&3EDITMODE&7] &cEditing mode disabled.");
						plugin.playersInEditor.remove(name);
					} else {
						plugin.pM.sendMessage(sender, "&7[&3EDITMODE&7] &aEditing mode enabled.");
						plugin.playersInEditor.add(name);
					}
					return true;
				}
				return true;
			} if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("armorcontrol.reload")) {
					plugin.loadConfig();
					plugin.pM.sendMessage(sender, "&aReloaded ArmorControl.");
					return true;
				}
				return true;
			} else if (args[0].equalsIgnoreCase("freeze")) {
				if (sender.hasPermission("armorcontrol.admin")) {
					plugin.prepareToStop = !plugin.prepareToStop;
					if (plugin.prepareToStop) {
						plugin.pM.sendMessage(sender, "&7[&3HALT&7] &aGameplay has paused.");
					} else {
						plugin.pM.sendMessage(sender, "&7[&3HALT&7] &aGameplay has resumed.");
					}
					return true;
				}
				return true;
			} else if (args[0].equalsIgnoreCase("checkfordupe")) {
				if (sender.hasPermission("armorcontrol.admin")) {
					if (plugin.checkForDupedArmor()) {
						plugin.pM.sendMessage(sender, "&cFound duped armor!");
					} else {
						plugin.pM.sendMessage(sender, "&aNo duped armor!");
					}
					return true;
				}
				return true;
			}
			return false;
		}
		return false;
	}
}
