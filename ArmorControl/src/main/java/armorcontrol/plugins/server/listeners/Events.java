package armorcontrol.plugins.server.listeners;

import armorcontrol.plugins.server.main.ArmorControl;
import armorcontrol.plugins.server.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import playermanager.plugins.server.player.ACPlayer;

import java.util.ArrayList;
import java.util.List;

public class Events implements Listener {

	private ArmorControl plugin;

	private ArrayList<PlayerStaminaMonitor> stamina = new ArrayList<PlayerStaminaMonitor>();

	public Events(ArmorControl pl) {
		plugin = pl;
	}

	// BEGIN PLAYER EVENTS
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Action a = e.getAction();
		Block b = e.getClickedBlock();

		// Edit mode check
		if (a.equals(Action.RIGHT_CLICK_BLOCK) && b != null) {
			if (b.getType().equals(Material.CHEST) && plugin.playersInEditor.contains(p.getName())) {
				// Cancel event
				e.setCancelled(true);

				// Create location to be added
				Location l = b.getLocation();

				// Check if location is already added, stop if it is
				for (String s : plugin.stringLocationsOfChests) {
					if (s.equals(Utils.convertLocationToString(l, true, true))) {
						plugin.pM.sendMessage(p, "&7[&3EDITMODE&7] &cChest removed successfully!");
						plugin.stringLocationsOfChests.remove(s);
						plugin.getConfig().set("LocationsOfChests", plugin.stringLocationsOfChests);
						plugin.saveConfig();
						plugin.chests.remove(l);
						return;
					}
				}

				// Add the location the the arraylist of chests
				plugin.stringLocationsOfChests.add(Utils.convertLocationToString(l, true, true));
				plugin.getConfig().set("LocationsOfChests", plugin.stringLocationsOfChests);
				plugin.saveConfig();
				plugin.chests.add(l);

				plugin.pM.sendMessage(p, "&7[&3EDITMODE&7] &aChest added successfully!");
			}
		}
		// Edit mode check
		if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)) {
			if (plugin.playersInEditor.contains(p.getName()) && p.getItemInHand().getType().equals(Material.STICK)) {
				// Cancel event
				e.setCancelled(true);

				// Create location to be added
				Location l = p.getLocation();

				// Check if location is already added, stop if it is
				for (String s : plugin.stringLocationsOfSpawns) {
					if (s.equals(Utils.convertLocationToString(l, true, true))) {
						plugin.pM.sendMessage(p, "&7[&3EDITMODE&7] &cSpawn location is already added!");
						return;
					}
				}

				// Add the location the the arraylist of chests
				plugin.stringLocationsOfSpawns.add(Utils.convertLocationToString(l, true, true));
				plugin.getConfig().set("LocationsOfSpawns", plugin.stringLocationsOfSpawns);
				plugin.saveConfig();
				plugin.spawns.add(l);

				plugin.pM.sendMessage(p, "&7[&3EDITMODE&7] &aSpawn location added successfully!");
			}
		}
		// Insta eat check
		if (a.equals(Action.LEFT_CLICK_AIR) || a.equals(Action.LEFT_CLICK_BLOCK)) {
			if (e.hasItem() && e.getItem().getType().equals(Material.ROTTEN_FLESH)) {
				if (p.getHealth() < p.getMaxHealth()) {
					PlayerInventory inv = p.getInventory();
					ItemStack is = e.getItem();
					if (is.getAmount() - 1 == 0) {
						inv.remove(is);
					} else {
						is.setAmount(is.getAmount() - 1);
						inv.setItem(inv.getHeldItemSlot(), is);
					}

					double healAmount = 2.0;
					if (plugin.juggernaut != null && plugin.juggernaut.getName().equalsIgnoreCase(p.getName())) {
						healAmount = Math.random() * 2;
					}

					if (p.getHealth() + healAmount >= p.getMaxHealth()) {
						p.setHealth(p.getMaxHealth());
					} else {
						p.setHealth(p.getHealth() + healAmount);
					}
				}
			}
		}
		// Pressure plate check
		if (a.equals(Action.PHYSICAL) && Utils.isPressurePlate(e.getClickedBlock().getType())) {
			if (Utils.pressurePlateLevel(e.getClickedBlock().getType()) == 4) {
				e.getClickedBlock().setType(Material.AIR);
				e.getClickedBlock().getWorld().createExplosion(e.getClickedBlock().getLocation(), 5F, false);
			} else if (Utils.pressurePlateLevel(e.getClickedBlock().getType()) == 3) {
				e.getClickedBlock().setType(Material.AIR);
				e.getClickedBlock().getWorld().createExplosion(e.getClickedBlock().getLocation(), 4F, false);
			} else if (Utils.pressurePlateLevel(e.getClickedBlock().getType()) == 2) {
				e.getClickedBlock().setType(Material.AIR);
				e.getClickedBlock().getWorld().createExplosion(e.getClickedBlock().getLocation(), 3F, false);
			} else if (Utils.pressurePlateLevel(e.getClickedBlock().getType()) == 1) {
				e.getClickedBlock().setType(Material.AIR);
				e.getClickedBlock().getWorld().createExplosion(e.getClickedBlock().getLocation(), 2F, false);
			}
			for (LandMine landMine : plugin.landMines) {
				if (landMine.getLocation().equals(e.getClickedBlock().getLocation())) {
					landMine.remove();
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		// Add death to player who died
		ACPlayer died = plugin.pM.getACPlayer(e.getEntity());
		died.addDeath();

		// Make player keep Level
		e.setKeepLevel(true);

		for (PlayerStaminaMonitor monitor : stamina) {
			if (monitor.getPlayer().equals(e.getEntity())) {
				monitor.died();
			}
		}

		// Add kill to the killer of dead player
		if (e.getEntity().getKiller() != null) {
			ACPlayer killer = plugin.pM.getACPlayer(e.getEntity().getKiller());
			killer.addKill();

			// Check if the juggernaut was killed or not
			if (plugin.juggernaut != null && e.getEntity().getName().equals(plugin.juggernaut.getName())) {
				killer.addCredits(plugin.xpPerKillJuggernaut);

				// Set death message
				e.setDeathMessage(ChatColor.translateAlternateColorCodes('&',
						"&7The &3Juggernaut &7was killed by &a" +
								e.getEntity().getKiller().getName() + "(" + killer.getKillstreak() + " killstreak)"));
			} else {
				killer.addCredits(plugin.xpPerKill);

				// Set death message
				e.setDeathMessage(ChatColor.translateAlternateColorCodes('&',
						"&c" + e.getEntity().getName() + " &7was killed by &a" +
								e.getEntity().getKiller().getName() + "(" + killer.getKillstreak() + " killstreak)"));
			}
		} else {
			// Check if the juggernaut was killed or not
			if (plugin.juggernaut != null && e.getEntity().getName().equals(plugin.juggernaut.getName())) {
				// Set death message
				e.setDeathMessage(ChatColor.translateAlternateColorCodes('&',
						"&7The &3Juggernaut &7was killed by &aan explosion"));
			} else {
				// Set death message
				e.setDeathMessage(ChatColor.translateAlternateColorCodes('&',
						"&c" + e.getEntity().getName() + " &7was killed by &aan explosion"));
			}
		}

		// If player was juggernaut clear juggernaut
		if (plugin.juggernaut != null && e.getEntity().getName().equals(plugin.juggernaut.getName())) {
			plugin.clearJuggernaut();
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
		final Player p = e.getPlayer();

		// Check inventory for new items
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				Utils.updateArmorInventory(p, plugin);
				Utils.updateInventory(p, plugin);
			}
		}, 2L);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (e.getItemDrop().getType().equals(EntityType.DROPPED_ITEM)) {
			if (!Utils.isDropable(e.getItemDrop().getItemStack().getType())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		// Hide quit message
		e.setQuitMessage("");

		// Check if player had any armor
		PlayerInventory inv = e.getPlayer().getInventory();
		if (inv.getHelmet() != null && Utils.armorType(inv.getHelmet().getType()) == 1) {
			plugin.respawnArmor(Material.DIAMOND_HELMET);
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', "  &5A piece of diamond armor will respawn in a random chest."));
		}
		if (inv.getChestplate() != null && Utils.armorType(inv.getChestplate().getType()) == 1) {
			plugin.respawnArmor(Material.DIAMOND_CHESTPLATE);
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', "  &5A piece of diamond armor will respawn in a random chest."));
		}
		if (inv.getLeggings() != null && Utils.armorType(inv.getLeggings().getType()) == 1) {
			plugin.respawnArmor(Material.DIAMOND_LEGGINGS);
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', "  &5A piece of diamond armor will respawn in a random chest."));
		}
		if (inv.getBoots() != null && Utils.armorType(inv.getBoots().getType()) == 1) {
			plugin.respawnArmor(Material.DIAMOND_BOOTS);
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', "  &5A piece of diamond armor will respawn in a random chest."));
		}
		if (plugin.juggernaut != null && plugin.juggernaut.equals(e.getPlayer())) {
			plugin.clearJuggernaut();
			e.setQuitMessage(ChatColor.translateAlternateColorCodes('&', "  &7The &3Juggernaut &7has left the game. " +
					"&5Diamond Armor &7will respawn in random chests."));
		}

		for (PlayerStaminaMonitor monitor : stamina) {
			if (monitor.getPlayer().equals(e.getPlayer())) {
				stamina.remove(monitor);
				return;
			}
		}

		if (plugin.getServer().getOnlinePlayers().length <= 4) {
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					if (plugin.getServer().getOnlinePlayers().length < 4) {
						for (Player player : plugin.getServer().getOnlinePlayers()) {
							player.kickPlayer(ChatColor.RED + "4 Players are required to play sorry.");
						}
					}
				}
			}, 1000L);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerKick(PlayerKickEvent e) {
		// Hide quit message
		e.setLeaveMessage("");

		// Check if player had any armor
		PlayerInventory inv = e.getPlayer().getInventory();
		if (inv.getHelmet() != null && Utils.armorType(inv.getHelmet().getType()) == 1) {
			plugin.respawnArmor(Material.DIAMOND_HELMET);
			e.setLeaveMessage(ChatColor.translateAlternateColorCodes('&', "  &5A piece of diamond armor will respawn in a random chest."));
		}
		if (inv.getChestplate() != null && Utils.armorType(inv.getChestplate().getType()) == 1) {
			plugin.respawnArmor(Material.DIAMOND_CHESTPLATE);
			e.setLeaveMessage(ChatColor.translateAlternateColorCodes('&', "  &5A piece of diamond armor will respawn in a random chest."));
		}
		if (inv.getLeggings() != null && Utils.armorType(inv.getLeggings().getType()) == 1) {
			plugin.respawnArmor(Material.DIAMOND_LEGGINGS);
			e.setLeaveMessage(ChatColor.translateAlternateColorCodes('&', "  &5A piece of diamond armor will respawn in a random chest."));
		}
		if (inv.getBoots() != null && Utils.armorType(inv.getBoots().getType()) == 1) {
			plugin.respawnArmor(Material.DIAMOND_BOOTS);
			e.setLeaveMessage(ChatColor.translateAlternateColorCodes('&', "  &5A piece of diamond armor will respawn in a random chest."));
		}
		if (plugin.juggernaut != null && plugin.juggernaut.equals(e.getPlayer())) {
			e.setLeaveMessage("  &7The &3Juggernaut &7has left the game. &5Diamond Armor &7will respawn in random chests.");
			plugin.clearJuggernaut();
		}

		for (PlayerStaminaMonitor monitor : stamina) {
			if (monitor.getPlayer().equals(e.getPlayer())) {
				stamina.remove(monitor);
				return;
			}
		}

		if (plugin.getServer().getOnlinePlayers().length < 4) {
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					if (plugin.getServer().getOnlinePlayers().length < 4) {
						for (Player player : plugin.getServer().getOnlinePlayers()) {
							player.kickPlayer(ChatColor.RED + "4 Players are required to play sorry.");
						}
					}
				}
			}, 1000L);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		// Add to stamina monitor
		stamina.add(new PlayerStaminaMonitor(plugin, p));

		// Teleport to spawn and give start items
		p.teleport(plugin.getRandomSpawn());
		Utils.playerJoin(p, plugin);

		String[] msgs = new String[19];
		msgs[0] = "&7---- &3ArmorControl &7----";
		msgs[1] = "&3How to play:";
		msgs[2] = " &7- &aFind chests around the map and try to obtain" +
				" the full set of &bDiamond Armor&a.";
		msgs[3] = " &7- &aOnce a player has the full set of &bDiamond" +
				" Armor &athey will become the &3Juggernaut";
		msgs[4] = " &7- &aOnce a player has the full set of &bDiamond" +
				" Armor &athey will become the &3Juggernaut";
		msgs[5] = "";
		msgs[6] = "&3Items:";
		msgs[7] = " &7- Snowballs: &aCreates a small explosion.";
		msgs[8] = " &7- TNT: &aAuto ignites when placed.";
		msgs[9] = " &7- Obsidian: &aCreates a large explosion.";
		msgs[10] = " &7- Pressure Plates: &aUsed as mines.";
		msgs[11] = " &7- Rotten Flesh: &aLeft Click heals 1 heart.";
		msgs[12] = " &7- XP Bar: &aSprint stamina.";
		msgs[13] = "";
		msgs[14] = "&3B&&3L XP:";
		msgs[15] = " &7- Kill: &a" + plugin.xpPerKill;
		msgs[16] = " &7- Kill Juggernaut: &a" + plugin.xpPerKillJuggernaut;
		msgs[17] = " &7- Obtain Diamond Armor: &a" + plugin.xpObtainingArmorPiece;
		msgs[18] = " &7- Being Juggernaut: &a" + plugin.xpPer20SecAsJuggernaut;
		plugin.pM.sendMessage(p, msgs);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		e.setRespawnLocation(plugin.getRandomSpawn());

		Player p = e.getPlayer();
		Utils.playerJoin(p, plugin);
		p.setLevel(20);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (plugin.getServer().getOnlinePlayers().length < 4 || plugin.prepareToStop) {
			if (!e.getPlayer().hasPermission("armorcontrol.admin")) {
				if (Math.random() * 4 <= 1) {
					plugin.pM.sendMessage(e.getPlayer(), "  &cSorry, but there needs to be at least 4 players to play.");
				}
				e.setTo(e.getFrom());
			}
		}
	}
	// END PLAYER EVENTS


	// BEGIN INVENTORY EVENTS
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		// Check if it was a chest and it is a player (should always be though)
		if (e.getInventory().getType().equals(InventoryType.CHEST) && e.getPlayer() instanceof Player) {
			final Player p = (Player) e.getPlayer();

			// Check inventory for new items
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					Utils.updateArmorInventory(p, plugin);
					Utils.updateInventory(p, plugin);
				}
			}, 2L);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			if (e.getInventory() instanceof PlayerInventory) {
				if (e.getWhoClicked().hasPermission("armorcontrol.admin")) {
					if (e.getCurrentItem() != null) {
						if (Utils.isDropable(e.getCurrentItem().getType()) || Utils.isDropable(e.getCursor().getType())) {
							e.setCancelled(true);
							return;
						} else {
							if (plugin.playersInEditor.contains(e.getWhoClicked().getName())) {
								e.setCancelled(false);
								return;
							}
						}
					}
				}
			}
			if (plugin.playersInEditor.contains(e.getWhoClicked().getName())) {
				e.setCancelled(false);
			}
		}

		if (e.getSlotType().equals(InventoryType.SlotType.ARMOR)) {
			e.setCancelled(true);
		} else if (e.getSlotType().equals(InventoryType.SlotType.QUICKBAR)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (!(e.getInventory() instanceof PlayerInventory)) {
			if (e.getPlayer() instanceof Player) {
				ACPlayer acPlayer = plugin.pM.getACPlayer((Player) e.getPlayer());
				acPlayer.addChestOpened();
			}
		}
	}
	// END INVENTORY EVENTS


	// BEGIN BLOCK EVENTS
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (!plugin.playersInEditor.contains(e.getPlayer().getName())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlaceBlock(BlockPlaceEvent e) {
		Block b = e.getBlockPlaced();

		if (!plugin.prepareToStop) {
			// Obsidian Bombs
			if (b.getType().equals(Material.OBSIDIAN)) {
				new PrimedObsidian(plugin, b);
			}

			// Auto Prime TNT
			else if (b.getType().equals(Material.TNT)) {
				new PrimedTNT(plugin, b);
			}

			// Pressure Plates
			else if (Utils.isPressurePlate(b.getType())) {
				plugin.landMines.add(new LandMine(plugin, b));
			}
		} else {
			if (!plugin.playersInEditor.contains(e.getPlayer().getName())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onLeafDecay(LeavesDecayEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onPhysics(BlockPhysicsEvent e) {
		if (e.getBlock() != null) {
			if (e.getBlock().getType().equals(Material.SAND) || e.getBlock().getType().equals(Material.GRAVEL)) {
				e.setCancelled(true);
			}
		}
	}
	// END BLOCK EVENTS


	// BEGIN ENTITY EVENTS
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		// Repair tools & armor when taking damage
		if (e.getEntity() instanceof Player) {
			if (!e.getEntity().isDead()) {
				PlayerInventory inv = ((Player) e.getEntity()).getInventory();
				inv.getHelmet().setDurability((short)-100);
				inv.getChestplate().setDurability((short)-100);
				inv.getLeggings().setDurability((short)-100);
				inv.getBoots().setDurability((short)-100);
				inv.getItem(0).setDurability((short)-100);
			}
		}
		// Repair tools & armor when attacking
		if (e.getDamager() instanceof Player) {
			if (!e.getDamager().isDead()) {
				PlayerInventory inv = ((Player) e.getDamager()).getInventory();
				inv.getHelmet().setDurability((short)-100);
				inv.getChestplate().setDurability((short)-100);
				inv.getLeggings().setDurability((short)-100);
				inv.getBoots().setDurability((short)-100);
				inv.getItem(0).setDurability((short)-100);
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		// Prevent dropped items from exploding
		if (e.getEntityType().equals(EntityType.DROPPED_ITEM)) {
			e.setCancelled(true);
		}

		// Repair tools & armor when taking damage
		if (e.getEntity() instanceof Player) {
			// Cancel fall damage
			if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
				e.setCancelled(true);
			}

			// Cancel Drown Damage
			if (e.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) {
				e.setCancelled(true);
			}

			// Move to top on suffocation
			if (e.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION)) {
				e.setCancelled(true);
				Utils.teleportPlayerUp(((Player) e.getEntity()).getPlayer());
			}

			final Player p = ((Player) e.getEntity()).getPlayer();
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					if (!p.isDead()) {
						PlayerInventory inv = p.getInventory();
						inv.getHelmet().setDurability((short)-100);
						inv.getChestplate().setDurability((short)-100);
						inv.getLeggings().setDurability((short)-100);
						inv.getBoots().setDurability((short)-100);
						inv.getItem(0).setDurability((short)-100);
					}
				}
			}, 1L);
		}
	}

	@EventHandler
	public void onEntityExplosion(EntityExplodeEvent e) {
		List<Block> list = e.blockList();
		for (int x = 0; x < list.size(); x++) {
			if (list.get(x).getType().equals(Material.CHEST) ||
					Utils.isOther(list.get(x).getType()) || list.get(x).getType().equals(Material.AIR) ||
					list.get(x).getType().equals(Material.GLASS)) {
				list.remove(x);
				x--;
			}
		}
		for (RepairBlocks rb : plugin.repairBlocks) {
			if (!rb.isStarted() && rb.within5Blocks(e.getLocation())) {
				rb.addBlocks(list);
				return;
			}
		}
		plugin.repairBlocks.add(new RepairBlocks(plugin, e.blockList(), e.getLocation()));
	}

	@EventHandler
	public void onProjectileLand(ProjectileHitEvent e) {
		if (!plugin.prepareToStop) {
			if (e.getEntityType().equals(EntityType.SNOWBALL)) {
				Entity entity = e.getEntity();
				entity.getLocation().getWorld().createExplosion(entity.getLocation(), 3F, false);
			}
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		e.setDroppedExp(0);
	}

	@EventHandler
	public void onEntityFoodLevel(FoodLevelChangeEvent e) {
		if (e.getEntityType().equals(EntityType.PLAYER)) {
			e.setFoodLevel(25);
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityGainHealth(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) {
				Player p = ((Player) e.getEntity()).getPlayer();
				if (plugin.juggernaut != null && plugin.juggernaut.getName().equals(p.getName())) {
					e.setCancelled(true);
				}
			}
		}
	}
	// END ENTITY EVENTS


	// BEGIN ITEM EVENTS
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent e) {
		if (!Utils.isSpawnable(e.getEntity().getItemStack().getType())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemDespawn(ItemDespawnEvent e) {
		Material m = e.getEntity().getItemStack().getType();
		Location loc = e.getLocation();
		if (Utils.isArmor(m)) {
			if (Utils.armorType(m) == 1) {
				e.setCancelled(true);
				plugin.broadcast("  &5Piece of Diamond Armor located at X:&6" + loc.getBlockX() + " &5Z:&6" + loc.getBlockZ());
			}
		}
	}
	// END ITEM EVENTS


	// BEGIN MISC EVENTS
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		e.setCancelled(true);
	}
	// END MISC EVENTS
}
