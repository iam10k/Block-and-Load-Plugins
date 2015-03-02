package blockbattles.plugins.server.enchantments;

import blockbattles.plugins.server.game.ArenaPlayer;
import blockbattles.plugins.server.game.PlayerStage;
import blockbattles.plugins.server.main.BlockBattles;
import com.rit.sucy.CustomEnchantment;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import playermanager.plugins.server.player.BBPlayer;

public class DamageEnchantment extends CustomEnchantment {

	private BlockBattles plugin;

	public DamageEnchantment(BlockBattles pl) {
		super("Damage");
		plugin = pl;
		max = 1000;
	}

	@Override
	public void applyEffect(LivingEntity user, LivingEntity target, int enchantLevel, EntityDamageByEntityEvent e) {
		// Damage change damage done
		e.setDamage(e.getDamage() + enchantLevel);

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
			e.setDamage(1);
			if (Math.random() >= .5) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0));
			} else {
				player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
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
			player.getInventory().setHelmet(new ItemStack(Material.AIR));
			player.getInventory().setChestplate(new ItemStack (Material.AIR));
			player.getInventory().setLeggings(new ItemStack (Material.AIR));
			player.getInventory().setBoots(new ItemStack (Material.AIR));
			player.teleport(damaged.getArena().getSpectate());
			damaged.getArena().onDeathByPlayerEvent(damaged, damager);
		}
	}


}
