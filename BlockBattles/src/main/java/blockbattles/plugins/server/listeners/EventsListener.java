package blockbattles.plugins.server.listeners;

import blockbattles.plugins.server.game.Arena;
import blockbattles.plugins.server.game.ArenaPlayer;
import blockbattles.plugins.server.game.GameStage;
import blockbattles.plugins.server.game.PlayerStage;
import blockbattles.plugins.server.main.BlockBattles;
import blockbattles.plugins.server.util.Flower;
import blockbattles.plugins.server.util.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import playermanager.plugins.server.player.BBPlayer;

import java.util.ArrayList;
import java.util.List;


public class EventsListener implements Listener {

	private BlockBattles plugin;

	private ArrayList<Player> shotDart = new ArrayList<Player>();
	private ArrayList<Player> leftMage = new ArrayList<Player>();
	private ArrayList<Player> rightMage = new ArrayList<Player>();

	public EventsListener(BlockBattles pl) {
		plugin = pl;
	}

	// BEGIN PLAYER EVENTS
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		plugin.addArenaPlayer(e.getPlayer());

		Utils.playerInLobby(e.getPlayer());

		e.getPlayer().teleport(plugin.lobby);
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		ArenaPlayer arenaPlayer = plugin.getArenaPlayer(e.getPlayer());

		if (arenaPlayer.getArena() != null) {
			arenaPlayer.getArena().removePlayer(arenaPlayer);
		}

		plugin.removeArenaPlayer(arenaPlayer);
	}

	@EventHandler
	public void onKick(PlayerKickEvent e) {
		ArenaPlayer arenaPlayer = plugin.getArenaPlayer(e.getPlayer());

		if (arenaPlayer.getArena() != null) {
			arenaPlayer.getArena().removePlayer(arenaPlayer);
		}

		plugin.removeArenaPlayer(arenaPlayer);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		ArenaPlayer arenaPlayer = plugin.getArenaPlayer(e.getPlayer());

		if (arenaPlayer.getArena() != null) {
			if (arenaPlayer.getArena().getGameStage().equals(GameStage.ROUND1COUNTDOWN)) {
				e.setTo(e.getFrom());
			} else if (arenaPlayer.getArena().getGameStage().equals(GameStage.ROUND2COUNTDOWN)) {
				e.setTo(e.getFrom());
			} else if (arenaPlayer.getArena().getGameStage().equals(GameStage.ROUND3COUNTDOWN)) {
				e.setTo(e.getFrom());
			} else {
				// Check for flowers
				for (Flower flower : arenaPlayer.getArena().getFlowers()) {
					if (flower.getLocation().distance(e.getTo()) < 2) {
						if (flower.getType() == 4) {
							if (!arenaPlayer.getArena().onSameTeam(arenaPlayer, flower.getArenaPlayer())) {
								e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
								e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 3));
							}
						} else {
							if (arenaPlayer.getArena().onSameTeam(arenaPlayer, flower.getArenaPlayer())) {
								e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 1));
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		ArenaPlayer arenaPlayer = plugin.getArenaPlayer(e.getPlayer());

		if (arenaPlayer.getStage().equals(PlayerStage.LOBBY) || arenaPlayer.getStage().equals(PlayerStage.WAITING)) {
			if (!arenaPlayer.isEditing()) {
				e.setCancelled(true);
			}
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				Block b = e.getClickedBlock();
				if (Utils.isSign(b.getType())) {
					for (Arena a : plugin.getArenas()) {
						if (Utils.sameBlock(b.getLocation(), a.getTeamASignLoc())) {
							a.addPlayer(arenaPlayer, 0);
						} else if (Utils.sameBlock(b.getLocation(), a.getTeamBSignLoc())) {
							a.addPlayer(arenaPlayer, 1);
						}
					}
				}
			}
			if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				if (e.getItem() != null && e.getItem().getType().equals(Material.BEDROCK)) {
					e.setCancelled(false);
				}
			}
		} else {
			if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				if (arenaPlayer.isEditing()) {
					e.setCancelled(false);
					return;
				}

				// Detonate C4
				if (e.getItem() != null && e.getItem().getType().equals(Material.STONE_BUTTON)) {
					e.setCancelled(true);
					if (arenaPlayer.getArena() != null) {
						arenaPlayer.getArena().triggerC4(arenaPlayer);
					}
				}

				// Stick Custom Effects
				if (e.getItem() != null && e.getItem().getType().equals(Material.STICK)) {
					final Player player = e.getPlayer();
					if (!shotDart.contains(player)) {
						player.launchProjectile(Arrow.class, player.getLocation().getDirection().multiply(2.5));
						shotDart.add(player);
						plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
							@Override
							public void run() {
								shotDart.remove(player);
							}
						}, 40L);
					}
				}

				// Blaze Rod Custom Effects
				if (e.getItem() != null && e.getItem().getType().equals(Material.BLAZE_ROD)) {
					final Player player = e.getPlayer();
					if (!leftMage.contains(player)) {
						player.launchProjectile(Snowball.class);
						leftMage.add(player);
						plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
							@Override
							public void run() {
								leftMage.remove(player);
							}
						}, 40L);
					}
				}
			}

			if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if (!arenaPlayer.isEditing() && !arenaPlayer.getStage().equals(PlayerStage.BATTLE)) {
					e.setCancelled(true);
					return;
				}

				// Blaze Rod Custom Effects
				if (e.getItem() != null && e.getItem().getType().equals(Material.BLAZE_ROD)) {
					final Player player = e.getPlayer();
					if (!rightMage.contains(player)) {
						player.launchProjectile(Fireball.class);
						rightMage.add(player);
						plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
							@Override
							public void run() {
								rightMage.remove(player);
							}
						}, 280L);
					}
				}

				// Allow flint and steel
				if (e.getItem() != null && e.getItem().getType().equals(Material.FLINT_AND_STEEL)) {
					e.setCancelled(false);
					return;
				}

			}

			// Pressure plate check
			if (e.getAction().equals(Action.PHYSICAL) && Utils.isPressurePlate(e.getClickedBlock().getType())) {
				if (arenaPlayer.getArena().getLandmines().contains(e.getClickedBlock())) {
					arenaPlayer.getArena().getLandmines().remove(e.getClickedBlock());
				}
				if (Utils.pressurePlateLevel(e.getClickedBlock().getType()) == 3) {
					e.getClickedBlock().setType(Material.AIR);
					e.getClickedBlock().getWorld().createExplosion(e.getClickedBlock().getLocation(), 2F, false);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (e.getItemDrop().getItemStack().getType().equals(Material.BEDROCK)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		ArenaPlayer arenaPlayer = plugin.getArenaPlayer(e.getPlayer());

		if (arenaPlayer.getArena() == null) {
			e.getPlayer().teleport(plugin.lobby);
		}

		if (arenaPlayer.getArena() != null) {
			if (arenaPlayer.getArena().getGameStage().equals(GameStage.WAITING)) {
				e.getPlayer().teleport(plugin.lobby);
			} else if (arenaPlayer.getArena().getGameStage().equals(GameStage.ENDING)) {
				e.getPlayer().teleport(plugin.lobby);
			} else {
				e.getPlayer().teleport(arenaPlayer.getArena().getSpectate());
			}
		}
	}
	// END PLAYER EVENTS


	// BEGIN ENTITY EVENTS
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		// Player Damaged
		Player player = (Player) e.getEntity();
		ArenaPlayer damaged = plugin.getArenaPlayer(player);

		// Check if player can take damage
		if (damaged.getArena() == null || damaged.getStage().equals(PlayerStage.SPECTATING)
				|| damaged.getStage().equals(PlayerStage.LOBBY) || damaged.getStage().equals(PlayerStage.WAITING)) {
			e.setCancelled(true);
			return;
		}

		if (!e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE) &&
				!e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
			if (player.getHealth() - e.getDamage() < 0.5) {
				e.setCancelled(true);
				e.setDamage(0);
				player.setHealth(0.5);
				return;
			}
		}

		// Player who hit the player
		Player damagePlayer = null;
		if (e.getDamager() instanceof Player) {
			damagePlayer = (Player) e.getDamager();
		}
		if (e.getDamager() instanceof Arrow) {
			Projectile arrow = (Arrow) e.getDamager();
			damagePlayer = (Player) arrow.getShooter();
		}
		if (e.getDamager() instanceof Snowball) {
			Projectile snowball = (Snowball) e.getDamager();
			damagePlayer = (Player) snowball.getShooter();
		}
		if (e.getDamager() instanceof Fireball) {
			Projectile fireball = (Fireball) e.getDamager();
			damagePlayer = (Player) fireball.getShooter();
		}
		if (damagePlayer == null) {
			e.setCancelled(true);
			return;
		}
		ArenaPlayer damager = plugin.getArenaPlayer(damagePlayer);


		// Cancel Damage if needed
		if (damaged.getArena() == null || damager.getArena() == null) {
			e.setCancelled(true);
			return;
		} else if (!damaged.getArena().equals(damager.getArena())) {
			e.setCancelled(true);
			return;
		} else if (damaged.getArena().onSameTeam(damaged, damager)) {
			e.setCancelled(true);
			return;
		}

		if (!damaged.getStage().equals(PlayerStage.BATTLE)) {
			e.setCancelled(true);
			return;
		}

		// If it was a snowball or fireball then do effects
		if (damager.getPClass().equalsIgnoreCase("mage")) {
			if (e.getDamager() instanceof Snowball) {
				player.setFireTicks(50);
			}
			if (e.getDamager() instanceof Fireball) {
				player.getWorld().createExplosion(player.getLocation(), 2F, false);
			}
		}

		// If it was a copycat try to give copycat armor
		if (damager.getPClass().equalsIgnoreCase("copycat")) {
			int slot = (int) (Math.random() * 4);
			if (slot == 0) {
				if (player.getInventory().getHelmet() != null) {
					damagePlayer.getInventory().setHelmet(player.getInventory().getHelmet());
				}
			} else if (slot == 1) {
				if (player.getInventory().getChestplate() != null) {
					damagePlayer.getInventory().setChestplate(player.getInventory().getChestplate());
				}
			} else if (slot == 2) {
				if (player.getInventory().getLeggings() != null) {
					damagePlayer.getInventory().setLeggings(player.getInventory().getLeggings());
				}
			} else if (slot == 3) {
				if (player.getInventory().getBoots() != null) {
					damagePlayer.getInventory().setBoots(player.getInventory().getBoots());
				}
			}
		}

		// If it was a dart only do one heart and add posion
		if (damager.getPClass().equalsIgnoreCase("witchdoctor")) {
			if (e.getDamager() instanceof Arrow) {
				e.setDamage(1);
				if (Math.random() >= .5) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0));
				} else {
					player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
				}
			}
		}

		if (player.getHealth() - e.getDamage() < 0.5) {
			e.setDamage(0);
			e.setCancelled(true);

			BBPlayer damagedBB = plugin.pM.getBBPlayer(player);
			BBPlayer damagerBB = plugin.pM.getBBPlayer(damager.getPlayer());

			// Add dead player death
			damagedBB.addDeath();

			// Add killer credits
			if (e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
				damagerBB.addBowKill();
				damagerBB.addCredits(plugin.xpPerBowKill);
			} else {
				damagerBB.addSwordKill();
				damagerBB.addCredits(plugin.xpPerSwordKill);
			}

			player.setHealth(20);
			player.getInventory().clear();
			player.getInventory().setHelmet(new ItemStack (Material.AIR));
			player.getInventory().setChestplate(new ItemStack (Material.AIR));
			player.getInventory().setLeggings(new ItemStack (Material.AIR));
			player.getInventory().setBoots(new ItemStack (Material.AIR));
			player.teleport(damaged.getArena().getSpectate());
			damaged.getArena().onDeathByPlayerEvent(damaged, damager);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		Player damaged = (Player) e.getEntity();

		if (!e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE) &&
				!e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
			if (damaged.getHealth() - e.getDamage() < 0.5) {
				e.setCancelled(true);
				e.setDamage(0);
				damaged.setHealth(0.5);
			}
		}
	}

	@EventHandler
	public void onEntityDamageBlock(EntityDamageByBlockEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		Player damaged = (Player) e.getEntity();

		if (!e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE) &&
				!e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
			if (damaged.getHealth() - e.getDamage() < 0.5) {
				e.setCancelled(true);
				e.setDamage(0);
				damaged.setHealth(0.5);
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
	public void onEntityExplosion(EntityExplodeEvent e) {
		List<Block> list = e.blockList();
		for (int x = 0; x < list.size(); x++) {
			list.remove(x);
			x--;
		}
	}
	// END ENTITY EVENTS


	// BEGIN BLOCK EVENTS
	@EventHandler
	public void onPlaceBlock(BlockPlaceEvent e) {
		ArenaPlayer arenaPlayer = plugin.getArenaPlayer(e.getPlayer());
		// Check if player is editing
		if (arenaPlayer.getArena() == null || arenaPlayer.getStage().equals(PlayerStage.LOBBY) ||
				arenaPlayer.getStage().equals(PlayerStage.SPECTATING)) {
			e.setCancelled(true);
			if (e.getPlayer().hasPermission("blockbattles.admin") || arenaPlayer.isEditing()) {
				e.setCancelled(false);
			}
			return;
		}

		// Cancel no matter what then check for un cancel
		e.setCancelled(true);

		// Web

		if (e.getBlock().getType().equals(Material.WEB)) {
			e.setCancelled(false);
			arenaPlayer.getArena().addWeb(e.getBlock());
		}

		// Flower
		if (e.getBlock().getType().equals(Material.DOUBLE_PLANT)) {
			e.setCancelled(false);
			arenaPlayer.getArena().addFlowerPlaced(e.getBlock(), arenaPlayer);
		}

		// Land mines
		if (Utils.isPressurePlate(e.getBlock().getType())) {
			e.setCancelled(false);
			arenaPlayer.getArena().addLandMine(e.getBlock());
		}

		// C4
		if (e.getBlock().getType().equals(Material.STAINED_GLASS)) {
			e.setCancelled(false);
			arenaPlayer.getArena().addC4(arenaPlayer, e.getBlock());
		}

		// Flint and steel
		if (e.getItemInHand() != null && e.getItemInHand().getType().equals(Material.FLINT_AND_STEEL)) {
			e.setCancelled(false);
		}
	}

	@EventHandler
	public void onBreakBlock(BlockBreakEvent e) {
		ArenaPlayer arenaPlayer = plugin.getArenaPlayer(e.getPlayer());
		e.setCancelled(true);
		if (arenaPlayer.isEditing()) {
			e.setCancelled(false);
		}
	}
	// END BLOCK EVENTS


	// BEGIN MISC EVENTS
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		e.setCancelled(true);
	}
	// END MISC EVENTS
}