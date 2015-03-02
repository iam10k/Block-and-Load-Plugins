package factionsmanager.plugins.server.listeners;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import factionsmanager.plugins.server.main.FactionsManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import playermanager.plugins.server.player.FACPlayer;
import playermanager.plugins.server.player.ServerPlayer;

public class EventsListener implements Listener {

	private FactionsManager plugin;

	public EventsListener(FactionsManager pl) {
		plugin = pl;

		// Add Custom Recipes
		//addRecipes();
	}

	// BEGIN PLAYER EVENTS
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		ServerPlayer serverPlayer = plugin.pM.getServerPlayer(e.getPlayer());

		String playerRank = serverPlayer.getPlayerRank();

		if (!playerRank.equals("default") && plugin.pConfig != null) {
			checkForMoney(e.getPlayer(), playerRank);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		// Hide quit message
		e.setQuitMessage("");
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (plugin.combatTag.isNPC(e.getEntity())) {
			return;
		}

		// Add death to player who died
		FACPlayer died = plugin.pM.getFACPlayer(e.getEntity());
		died.addDeath();

		// Add kill to the killer of dead player
		if (e.getEntity().getKiller() != null) {
			FACPlayer killer = plugin.pM.getFACPlayer(e.getEntity().getKiller());
			if (e.getEntity().getLastDamageCause()!= null &&
					e.getEntity().getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
				killer.addBowKill();
				killer.addCredits(plugin.xpPerBowKill);
			} else {
				killer.addSwordKill();
				killer.addCredits(plugin.xpPerSwordKill);
			}

			// Set death message
			e.setDeathMessage(ChatColor.translateAlternateColorCodes('&',
				"&c" + e.getEntity().getName() + " &7was killed by &a" +
					e.getEntity().getKiller().getName() + "(" + killer.getKillstreak() + " killstreak)"));
		} else {
			// Send to player who died & clear death message
			String msg = e.getDeathMessage();
			plugin.pM.sendMessage(e.getEntity(), "&7" + msg);
			e.setDeathMessage("");
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.ENDER_CHEST)) {
				if (!e.getPlayer().hasPermission("factionsmanager.enderchest")) {
					e.setCancelled(true);
				}
			}
		}
	}
	// END PLAYER EVENTS


	// BEGIN ENTITY EVENTS
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player && e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
			RegionManager manager = getWorldGuard().getRegionManager(e.getEntity().getLocation().getWorld());
			ApplicableRegionSet set = manager.getApplicableRegions(e.getEntity().getLocation());
			for (ProtectedRegion each : set) {
				if (each.getId().startsWith("nofall")) {
					e.setCancelled(true);
				}
			}
		}
	}
	// END ENTITY EVENTS


	// BEGIN BLOCK EVENTS
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (!e.isCancelled()) {
			if (e.getBlockPlaced().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
				if (e.getBlockPlaced().getLocation().getBlockY() > 126) {
					e.setCancelled(true);
					plugin.pM.sendMessage(e.getPlayer(), "&cThe top of the Nether is disabled.");
					return;
				}
			}

			FACPlayer facPlayer = plugin.pM.getFACPlayer(e.getPlayer());
			if (hardBlock(e.getBlock().getType())) {
				facPlayer.addBlockPlaced();
				if (facPlayer.getBlocksPlaced() % 100 == 0) {
					plugin.pM.sendMessage(e.getPlayer(), "  &6" + facPlayer.getBlocksPlaced() + " blocks placed!");
					facPlayer.addCredits(plugin.xpPer100BlocksPlaced);
				}
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent e) {
		if (!e.isCancelled()) {
			FACPlayer facPlayer = plugin.pM.getFACPlayer(e.getPlayer());
			if (facPlayer.miningBanned()) {
				if (hardBlock(e.getBlock().getType())) {
					e.setCancelled(true);
					plugin.pM.sendMessage(e.getPlayer(), "&cYou are not allowed to mine for a week.");
					return;
				}
			}
			if (hardBlock(e.getBlock().getType())) {
				facPlayer.addBlockBroken();
				if (facPlayer.getBlocksBroken() % 100 == 0) {
					plugin.pM.sendMessage(e.getPlayer(), "  &6" + facPlayer.getBlocksBroken() + " blocks broken!");
					facPlayer.addCredits(plugin.xpPer100BlocksBroken);
				}
			}
			if (e.getBlock().getType().equals(Material.STONE)) {
				facPlayer.addStone();
			} else if (e.getBlock().getType().equals(Material.COAL_ORE)) {
				facPlayer.addCoal();
			} else if (e.getBlock().getType().equals(Material.LAPIS_ORE)) {
				facPlayer.addLapis();
			} else if (e.getBlock().getType().equals(Material.REDSTONE_ORE)) {
				facPlayer.adddRedstone();
			} else if (e.getBlock().getType().equals(Material.GLOWING_REDSTONE_ORE)) {
				facPlayer.adddRedstone();
			} else if (e.getBlock().getType().equals(Material.IRON_ORE)) {
				facPlayer.addIron();
			} else if (e.getBlock().getType().equals(Material.GOLD_ORE)) {
				facPlayer.addGold();
			} else if (e.getBlock().getType().equals(Material.EMERALD_ORE)) {
				facPlayer.addEmerald();
			} else if (e.getBlock().getType().equals(Material.DIAMOND_ORE)) {
				facPlayer.addDiamond();
			}

			if (facPlayer.enableOrebfusactor()) {
				plugin.enableAntiXray(e.getPlayer());
			}
		}
	}

	@EventHandler
	public void onBlockFromTo(BlockFromToEvent e) {
		if (e.getBlock().getType().equals(Material.LAVA) || e.getBlock().getType().equals(Material.STATIONARY_LAVA)) {
			e.setCancelled(true);
		}
	}
	// END BLOCK EVENTS


	// BEGIN CRAFT ITEM EVENTS
	/*@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent e) {
		int dCount = 0;
		int iCount = 0;
		int gCount = 0;
		for(ItemStack is : e.getInventory().getMatrix()) {
			if (is != null && !is.getType().equals(Material.AIR)) {
				if (is.getType().equals(Material.DIAMOND)) {
					dCount++;
				}
				if (is.getType().equals(Material.IRON_INGOT)) {
					iCount++;
				}
				if (is.getType().equals(Material.GOLD_INGOT)) {
					gCount++;
				}
			}
		}

		if (dCount > 3 || iCount > 3 || gCount > 3) {
			if (isArmor(e.getRecipe().getResult().getType())) {
				e.getInventory().setResult(new ItemStack(Material.AIR));
				for (HumanEntity viewer : e.getViewers()) {
					if (viewer instanceof Player) {
						plugin.pM.sendMessage(((Player) viewer), "&cHaving trouble crafting armor? " +
								"&aYou will need to use Iron, Gold, or Diamond Blocks to make armor.");
					}
				}
			}
		}
	}

	@EventHandler
	public void onCraft(CraftItemEvent e) {
		int dCount = 0;
		int iCount = 0;
		int gCount = 0;
		for(ItemStack is : e.getInventory().getMatrix()) {
			if (is != null && !is.getType().equals(Material.AIR)) {
				if (is.getType().equals(Material.DIAMOND)) {
					dCount++;
				}
				if (is.getType().equals(Material.IRON_INGOT)) {
					iCount++;
				}
				if (is.getType().equals(Material.GOLD_INGOT)) {
					gCount++;
				}
			}
		}

		if (dCount > 3 || iCount > 3 || gCount > 3) {
			if (isArmor(e.getRecipe().getResult().getType())) {
				e.getInventory().setResult(new ItemStack(Material.AIR));
				if (e.getWhoClicked() instanceof Player) {
					plugin.pM.sendMessage(((Player) e.getWhoClicked()), "&cHaving trouble crafting armor? " +
							"&aYou will need to use Iron, Gold, or Diamond Blocks to make armor.");
				}
			}
		}
	}*/
	// END CRAFT ITEM EVENTS


	private WorldGuardPlugin getWorldGuard() {
		Plugin wgplugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(wgplugin instanceof WorldGuardPlugin)) {
			return null; // Maybe you want throw an exception instead
		}

		return (WorldGuardPlugin) wgplugin;
	}

	private boolean hardBlock(Material m) {
		switch (m) {
			case OBSIDIAN:
			case ANVIL:
			case COAL_BLOCK:
			case DIAMOND_BLOCK:
			case IRON_BLOCK:
			case REDSTONE_BLOCK:
			case ENCHANTMENT_TABLE:
			case IRON_FENCE:
			case IRON_DOOR:
			case MOB_SPAWNER:
			case WEB:
			case DISPENSER:
			case DROPPER:
			case FURNACE:
			case BEACON:
			case GOLD_BLOCK:
			case COAL_ORE:
			case DIAMOND_ORE:
			case EMERALD_ORE:
			case ENDER_STONE:
			case GOLD_ORE:
			case HOPPER:
			case IRON_ORE:
			case LAPIS_BLOCK:
			case LAPIS_ORE:
			case QUARTZ_ORE:
			case REDSTONE_ORE:
			case GLOWING_REDSTONE_ORE:
			case TRAP_DOOR:
			case WOOD_DOOR:
			case ENDER_CHEST:
			case CHEST:
			case WORKBENCH:
			case BRICK_STAIRS:
			case BRICK:
			case CAULDRON:
			case COBBLESTONE:
			case COBBLE_WALL:
			case COBBLESTONE_STAIRS:
			case FENCE:
			case FENCE_GATE:
			case JUKEBOX:
			case MOSSY_COBBLESTONE:
			case NETHER_BRICK:
			case NETHER_FENCE:
			case NETHER_BRICK_STAIRS:
			case STEP:
			case LOG:
			case LOG_2:
			case WOOD:
			case WOOD_STEP:
			case BOOKSHELF:
			case STONE:
			case SMOOTH_BRICK:
			case SMOOTH_STAIRS:
			case HARD_CLAY:
			case STAINED_CLAY:
			case JACK_O_LANTERN:
			case MELON_BLOCK:
			case SKULL:
			case PUMPKIN:
			case SIGN:
			case WALL_SIGN:
			case SIGN_POST:
			case QUARTZ_BLOCK:
			case NOTE_BLOCK:
			case QUARTZ_STAIRS:
			case SANDSTONE:
			case SANDSTONE_STAIRS:
			case WOOL:
				return true;
			default:
				return false;
		}
	}

	private void checkForMoney(Player player, String rank) {
		if (plugin.pConfig.contains(player.getUniqueId().toString())) {
			if (!plugin.pConfig.getString(player.getUniqueId().toString()).equals(rank)) {
				plugin.pConfig.set(player.getUniqueId().toString(), rank);
				plugin.savePremiumFile();
				giveMoney(player, rank);
			}
		} else {
			plugin.pConfig.set(player.getUniqueId().toString(), rank);
			plugin.savePremiumFile();
			giveMoney(player, rank);
		}
	}

	public void giveMoney(Player player, String rank) {
		if (rank.equals("bronze")) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"econ add " + player.getName() + " 7500");
		} else if (rank.equals("gold")) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"econ add " + player.getName() + " 10000");
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"addcredits " + player.getName() + " 100");
		} else if (rank.equals("sapphire")) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"econ add " + player.getName() + " 15000");
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"addcredits " + player.getName() + " 150");
		} else if (rank.equals("emerald")) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"econ add " + player.getName() + " 20000");
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"addcredits " + player.getName() + " 200");
		} else if (rank.equals("ruby")) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"econ add " + player.getName() + " 25000");
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"addcredits " + player.getName() + " 250");
		} else if (rank.equals("diamond")) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"econ add " + player.getName() + " 30000");
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"addcredits " + player.getName() + " 300");
		} else if (rank.equals("pinnacle")) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"econ add " + player.getName() + " 35000");
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
					"addcredits " + player.getName() + " 400");
		}
	}

	/**
	 * Check if material is armor
	 * @param m is a material
	 * @return true if m is armor
	 */
	/*private boolean isArmor(Material m) {
		switch (m) {
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
			case CHAINMAIL_HELMET:
			case CHAINMAIL_CHESTPLATE:
			case CHAINMAIL_LEGGINGS:
			case CHAINMAIL_BOOTS:
			case IRON_HELMET:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_BOOTS:
			case GOLD_HELMET:
			case GOLD_CHESTPLATE:
			case GOLD_LEGGINGS:
			case GOLD_BOOTS:
			case DIAMOND_HELMET:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
				return true;
			default:
				return false;
		}
	}

	private void addRecipes() {
		Material[] armor = new Material[]{ Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS,
				Material.DIAMOND_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS,
				Material.IRON_BOOTS, Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS,
				Material.GOLD_BOOTS };
		// ADD RECIPES
		ItemStack is = null;

		for (Material m : armor) {
			is = new ItemStack(m, 1);
			if (armorSlot(m) == 1) {
				ShapedRecipe b = new ShapedRecipe(is);
				b.shape("   ", "I I", "I I");
				b.setIngredient('I', getMaterial(m));
				plugin.getServer().addRecipe(b);

				ShapedRecipe b2 = new ShapedRecipe(is);
				b2.shape("I I", "I I", "   ");
				b2.setIngredient('I', getMaterial(m));
				plugin.getServer().addRecipe(b2);
			}

			if (armorSlot(m) == 2) {
				ShapedRecipe l = new ShapedRecipe(is);
				l.shape("III", "I I", "I I");
				l.setIngredient('I', getMaterial(m));
				plugin.getServer().addRecipe(l);
			}

			if (armorSlot(m) == 3) {
				ShapedRecipe c = new ShapedRecipe(is);
				c.shape("I I", "III", "III");
				c.setIngredient('I', getMaterial(m));
				plugin.getServer().addRecipe(c);
			}

			if (armorSlot(m) == 4) {
				ShapedRecipe h = new ShapedRecipe(is);
				h.shape("   ", "III", "I I");
				h.setIngredient('I', getMaterial(m));
				plugin.getServer().addRecipe(h);

				ShapedRecipe h2 = new ShapedRecipe(is);
				h2.shape("III", "I I", "   ");
				h2.setIngredient('I', getMaterial(m));
				plugin.getServer().addRecipe(h2);
			}
		}

	}

	private Material getMaterial(Material m) {
		if (armorType(m) == 1) { return Material.DIAMOND_BLOCK; }
		if (armorType(m) == 2) { return Material.IRON_BLOCK; }
		if (armorType(m) == 4) { return Material.GOLD_BLOCK; }
		return null;
	}*/

	/**
	 * Get armor type such as diamond or gold
	 * @param m is armor
	 * @return 1-Diamond 2-Iron 3-Chainmail 4-Gold 5-Leather 6-Other
	 */
	/*private int armorType(Material m) {
		switch (m) {
			case DIAMOND_BOOTS:
			case DIAMOND_LEGGINGS:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_HELMET:
				return 1;
			case IRON_HELMET:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_BOOTS:
				return 2;
			case CHAINMAIL_HELMET:
			case CHAINMAIL_CHESTPLATE:
			case CHAINMAIL_LEGGINGS:
			case CHAINMAIL_BOOTS:
				return 3;
			case GOLD_HELMET:
			case GOLD_CHESTPLATE:
			case GOLD_LEGGINGS:
			case GOLD_BOOTS:
				return 4;
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
				return 5;
			default:
				return 6;
		}
	}*/

	/**
	 * Get the piece of armor
	 * @param m is armor
	 * @return 1-Boots 2-Leggings 3-Chestplate 4-Helmet 5-Other
	 */
	/*public static int armorSlot(Material m) {
		switch (m) {
			case DIAMOND_BOOTS:
			case IRON_BOOTS:
			case GOLD_BOOTS:
			case LEATHER_BOOTS:
			case CHAINMAIL_BOOTS:
				return 1;
			case DIAMOND_LEGGINGS:
			case GOLD_LEGGINGS:
			case IRON_LEGGINGS:
			case CHAINMAIL_LEGGINGS:
			case LEATHER_LEGGINGS:
				return 2;
			case DIAMOND_CHESTPLATE:
			case GOLD_CHESTPLATE:
			case IRON_CHESTPLATE:
			case LEATHER_CHESTPLATE:
			case CHAINMAIL_CHESTPLATE:
				return 3;
			case CHAINMAIL_HELMET:
			case DIAMOND_HELMET:
			case GOLD_HELMET:
			case LEATHER_HELMET:
			case IRON_HELMET:
				return 4;
			default:
				return 5;
		}
	}*/
}
