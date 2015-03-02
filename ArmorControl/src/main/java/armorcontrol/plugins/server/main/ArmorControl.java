package armorcontrol.plugins.server.main;

import armorcontrol.plugins.server.commands.ArmorControlCommand;
import armorcontrol.plugins.server.commands.StatsCommand;
import armorcontrol.plugins.server.listeners.Events;
import armorcontrol.plugins.server.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import playermanager.plugins.server.guishop.GUIShopCommand;
import playermanager.plugins.server.main.PlayerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ArmorControl extends JavaPlugin {

    public PlayerManager pM;

	public List<String> items;
	public int xpPerKill = 0;
	public int xpPerKillJuggernaut = 0;
	public int xpPer20SecAsJuggernaut = 0;
	public int xpObtainingArmorPiece = 0;

	public List<String> stringLocationsOfChests = new ArrayList<String>();
	public ArrayList<Location> chests = new ArrayList<Location>();

	public List<String> playersInEditor = new ArrayList<String>();

	public Player juggernaut = null;
	private int juggernautCount = 0;
	private int juggernautTaskID = 1;

	public List<String> stringLocationsOfSpawns = new ArrayList<String>();
	public ArrayList<Location> spawns = new ArrayList<Location>();

	public ArrayList<LandMine> landMines = new ArrayList<LandMine>();
	public ArrayList<RepairBlocks> repairBlocks = new ArrayList<RepairBlocks>();

	public boolean prepareToStop = false;
	public boolean useLayerRegen = false;

    public void onEnable() {
    	Plugin plugin = getServer().getPluginManager().getPlugin("PlayerManager");
        if (plugin == null) {
            onDisable();
        }
		pM = (PlayerManager)plugin;

		// Config & Settings
		configDefaults();
		loadConfig();
		
		// Set up map and compass updater
		loadChests();
		if (chests.size() > 0) {
			clearChestsAndMap();
			fillChestsWithItems();
			spawnInDiamondArmor();
		}
		loadSpawns();
		new CompassUpdater(this);

		// Listener and Commands
		getServer().getPluginManager().registerEvents(new Events(this), this);
		getCommand("armorcontrol").setExecutor(new ArmorControlCommand(this));
		getCommand("stats").setExecutor(new StatsCommand(this));

		// Create GUIShop, one line does everything else for you
		getCommand("xpshop").setExecutor(new GUIShopCommand(this, pM, false));
    }

	public void onDisable() {
		for (LandMine landMine : landMines) {
			landMine.remove();
		}
	}

	/**
	 * Copy config defaults
	 */
	private void configDefaults() {
		getConfig().addDefault("ListOfItems", new String[]{"DIAMOND_SWORD:0(1)"});
		getConfig().addDefault("LocationsOfChests", stringLocationsOfChests);
		getConfig().addDefault("LocationsOfSpawns" , stringLocationsOfSpawns);
		getConfig().addDefault("XP.perKill", 0);
		getConfig().addDefault("XP.perKillJuggernaut", 0);
		getConfig().addDefault("XP.per20SecAsJuggernaut", 0);
		getConfig().addDefault("XP.obtainDiamondArmor", 0);
		getConfig().addDefault("Game.layerRegen", false);
		for (int x = 0; x < 27; x++) {
			getConfig().addDefault("XPShop.slot" + x + ".material", "AIR");
		}
		getConfig().options().copyDefaults(true);
		saveConfig();
		// How items will need to be in the listofitems in config
		// itemname:damagevalue(ammount)
	}

	/**
	 * Load/Reload config
	 */
	public void loadConfig() {
		reloadConfig();
		items = getConfig().getStringList("ListOfItems");
		stringLocationsOfChests = getConfig().getStringList("LocationsOfChests");
		stringLocationsOfSpawns = getConfig().getStringList("LocationsOfSpawns");
		xpPerKill = getConfig().getInt("XP.perKill");
		xpPerKillJuggernaut = getConfig().getInt("XP.perKillJuggernaut");
		xpPer20SecAsJuggernaut = getConfig().getInt("XP.per20SecAsJuggernaut");
		xpObtainingArmorPiece = getConfig().getInt("XP.obtainDiamondArmor");
		useLayerRegen = getConfig().getBoolean("Game.layerRegen", false);
	}

	/**
	 * Load Chests into Array
	 */
	private void loadChests() {
		for (String location : stringLocationsOfChests) {
			Location loc = Utils.convertStringToLocation(location, true);
			if (loc.getBlock().getType().equals(Material.CHEST)) {
				chests.add(loc);
			}
		}
	}

	/**
	 * Load Spawns into Array
	 */
	private void loadSpawns() {
		for (String location : stringLocationsOfSpawns) {
			Location loc = Utils.convertStringToLocation(location, true);
			spawns.add(loc);
		}
	}

	/**
	 * Get a random spawn location
	 * @return random spawn location
	 */
	public Location getRandomSpawn() {
		if (spawns.size() > 0) {
			return spawns.get((int) Math.floor(spawns.size() * Math.random()));
		} else {
			return getServer().getWorld("world").getSpawnLocation();
		}
	}

	/**
	 * Cancel a task with id
	 * @param id is int of a repeating task
	 */
	public void cancelTask(int id) { getServer().getScheduler().cancelTask(id); }

	/**
	 * Use when the Juggernaut dies
	 */
	public void clearJuggernaut() {
		cancelTask(juggernautTaskID);
		pM.getACPlayer(juggernaut).loseArmor();
		juggernaut = null;
	}

	/**
	 * Use when a player gets all the armor
	 * @param player
	 */
	public void newJuggernaut(Player player) {
		juggernaut = player;
		juggernautCount = 0;
		pM.getACPlayer(juggernaut).obtainArmor();
		juggernautTaskID = getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				if (juggernautCount > 0) {
					broadcast("  &a" + juggernaut.getName() + " &7has been &3Juggernaut &7for " +
							Utils.convertPlaytimeSeconds(juggernautCount));
				}
				pM.getServerPlayer(juggernaut).addCredits(xpPer20SecAsJuggernaut);
				juggernautCount += 20;
			}
		}, 0L, 400L).getTaskId();
		broadcast("  &a" + player.getName() + " &7is now the &3Juggernaut&7!");

	}

	/**
	 * Empty all chests on the map to prevent armor dupping upon restart
	 */
	private void clearChestsAndMap() {
		// Loop through each chest and clear inventory
		for (Location loc : chests) {
			if (loc.getBlock() != null && loc.getBlock().getType().equals(Material.CHEST)) {
				Chest c = (Chest)loc.getBlock().getState();
				Inventory cInv = c.getBlockInventory();

				// Set each contents to null
				ItemStack[] contents = cInv.getContents();
				for (int x = 0; x < contents.length; x++) {
					contents[x] = null;
				}
				cInv.setContents(contents);
				c.update(true, false);
			}
		}

		for (Entity entity : getServer().getWorld("world").getEntities()) {
			entity.remove();
		}

	}

	/**
	 * Add all the items to a random chest
	 */
	private void fillChestsWithItems() {
		for (String item : items) {

			Location loc;
			Chest c = null;
			Inventory cInv = null;

			// Make item stack
			String itemName = item.substring(0, item.indexOf(':'));
			int amount = Integer.parseInt(item.substring(item.indexOf('(') + 1, item.indexOf(')')));
			Short damageValue = Short.parseShort(item.substring(item.indexOf(':') + 1, item.indexOf('(')));

			// Create Material and if it is not null put it in chest
			Material m = Material.getMaterial(itemName);
			if (m == null) {
				getLogger().log(Level.INFO, "While filling chests, item (" + itemName + ") was not found as a valid item!");
				continue;
			}

			// Create ItemStack
			ItemStack is = new ItemStack(m, amount, damageValue);

			// Pick different chest if it has that item already
			while (cInv == null || cInv.contains(Material.getMaterial(itemName))) {
				loc = chests.get((int) Math.floor(chests.size() * Math.random()));
				if (loc.getBlock() != null && loc.getBlock().getType().equals(Material.CHEST)) {
					c = (Chest)loc.getBlock().getState();
					cInv = c.getBlockInventory();
				}
			}

			// Pick random spot in chest and add it
			ItemStack[] contents = cInv.getContents();
			contents[Utils.randomChestSlot(contents)] = is;
			cInv.setContents(contents);
			c.update(true, false);
		}

		// Start chest fill every three seconds after
		getServer().getScheduler().runTaskTimer(this, new ChestFiller(this), 2400L, 60L);
	}

	/**
	 * Spawns in the diamond armor to a random chest
	 */
	private void spawnInDiamondArmor() {
		Location loc;
		Chest c = null;
		Inventory cInv = null;

		int dhChest;
		int dcChest;
		int dlChest;
		int dbChest;

		// Make item stacks
		ItemStack dh = new ItemStack(Material.DIAMOND_HELMET, 1);
		ItemStack dc = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		ItemStack dl = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
		ItemStack db = new ItemStack(Material.DIAMOND_BOOTS, 1);

		// Add diamond helmet
		// First pick random chest
		dhChest = (int) Math.floor(chests.size() * Math.random());
		loc = chests.get(dhChest);
		while (loc.getBlock() == null && !loc.getBlock().getType().equals(Material.CHEST)) {
			dhChest = (int) Math.floor(chests.size() * Math.random());
			loc = chests.get(dhChest);
		}
		c = (Chest)loc.getBlock().getState();
		cInv = c.getBlockInventory();

		// Pick random spot in chest and add it
		ItemStack[] contents = cInv.getContents();
		contents[Utils.randomChestSlot(contents)] = dh;
		cInv.setContents(contents);
		c.update(true, false);

		// Clear for next armor piece
		cInv = null;
		loc = null;
		c = null;
		contents = null;

		// Add diamond chestplate
		// First pick random chest
		dcChest = (int) Math.floor(chests.size() * Math.random());
		loc = chests.get(dcChest);
		while (loc.getBlock() == null || !loc.getBlock().getType().equals(Material.CHEST)
				|| dhChest == dcChest) {
			dcChest = (int) Math.floor(chests.size() * Math.random());
			loc = chests.get(dcChest);
		}
		c = (Chest)loc.getBlock().getState();
		cInv = c.getBlockInventory();

		// Pick random spot in chest and add it
		contents = cInv.getContents();
		contents[Utils.randomChestSlot(contents)] = dc;
		cInv.setContents(contents);
		c.update(true, false);

		// Clear for next armor piece
		cInv = null;
		loc = null;
		c = null;
		contents = null;

		// Add diamond leggings
		// First pick random chest
		dlChest = (int) Math.floor(chests.size() * Math.random());
		loc = chests.get(dlChest);
		while (loc.getBlock() == null || !loc.getBlock().getType().equals(Material.CHEST)
				|| dhChest == dlChest || dcChest == dlChest) {
			dlChest = (int) Math.floor(chests.size() * Math.random());
			loc = chests.get(dlChest);
		}
		c = (Chest)loc.getBlock().getState();
		cInv = c.getBlockInventory();

		// Pick random spot in chest and add it
		contents = cInv.getContents();
		contents[Utils.randomChestSlot(contents)] = dl;
		cInv.setContents(contents);
		c.update(true, false);

		// Clear for next armor piece
		cInv = null;
		loc = null;
		c = null;

		// Add diamond boots
		// First pick random chest
		dbChest = (int) Math.floor(chests.size() * Math.random());
		loc = chests.get(dbChest);
		while (loc.getBlock() == null || !loc.getBlock().getType().equals(Material.CHEST)
				|| dhChest == dbChest || dcChest == dbChest || dlChest == dbChest) {
			dbChest = (int) Math.floor(chests.size() * Math.random());
			loc = chests.get(dbChest);
		}
		c = (Chest)loc.getBlock().getState();
		cInv = c.getBlockInventory();

		// Pick random spot in chest and add it
		contents = cInv.getContents();
		contents[Utils.randomChestSlot(contents)] = db;
		cInv.setContents(contents);
		c.update(true, false);
	}

	/**
	 * Respawn a piece of diamond armor
	 * @param m is Diamond Armor
	 */
	public void respawnArmor(Material m) {
		if (!Utils.isArmor(m) || Utils.armorType(m) != 1) {
			return;
		}
		if (Utils.armorSlot(m) == 4) {
			if (checkForNumberOfHelmet() > 1) {
				return;
			}
		} else if (Utils.armorSlot(m) == 3) {
			if (checkForNumberOfChestplate() > 1) {
				return;
			}
		} else if (Utils.armorSlot(m) == 2) {
			if (checkForNumberOfLeggings() > 1) {
				return;
			}
		} else if (Utils.armorSlot(m) == 1) {
			if (checkForNumberOfBoots() > 1) {
				return;
			}
		}

		// Create itemstack
		ItemStack is = new ItemStack(m, 1);

		// First pick random chest
		int chest = (int) Math.floor(chests.size() * Math.random());
		Location loc = chests.get(chest);
		while (loc.getBlock() == null && !loc.getBlock().getType().equals(Material.CHEST)) {
			chest = (int) Math.floor(chests.size() * Math.random());
			loc = chests.get(chest);
		}
		Chest c = (Chest)loc.getBlock().getState();
		Inventory cInv = c.getBlockInventory();

		// Pick random spot in chest and add it
		ItemStack[] contents = cInv.getContents();
		int invSlot = (int)Math.floor(contents.length * Math.random());
		while (contents[invSlot] != null) {
			invSlot = (int)Math.floor(contents.length * Math.random());
		}
		contents[invSlot] = is;
		cInv.setContents(contents);
		c.update(true, false);
	}

	/**
	 * Check for total amount of armor on map
	 * @return total amount
	 */
	public int checkForNumberOfHelmet() {
		int dh = 0;

		for (Player player : getServer().getOnlinePlayers()) {
			PlayerInventory inv = player.getInventory();
			if (Utils.armorType(inv.getHelmet().getType()) == 1) {
				dh++;
			}
		}

		for (Location loc : chests) {
			if (loc.getBlock() != null && loc.getBlock().getType().equals(Material.CHEST)) {
				Chest c = (Chest)loc.getBlock().getState();
				Inventory inv = c.getInventory();
				if (inv.contains(Material.DIAMOND_HELMET)) {
					dh++;
				}
			}
		}

		for (Entity entity : getServer().getWorld("world").getEntities()) {
			if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
				Item i = (Item) entity;
				if (i.getItemStack().getType().equals(Material.DIAMOND_HELMET)) {
					dh++;
				}
			}
		}

		return dh;
	}

	/**
	 * Check for total amount of armor on map
	 * @return total amount
	 */
	public int checkForNumberOfChestplate() {
		int dc = 0;

		for (Player player : getServer().getOnlinePlayers()) {
			PlayerInventory inv = player.getInventory();
			if (Utils.armorType(inv.getChestplate().getType()) == 1) {
				dc++;
			}
		}

		for (Location loc : chests) {
			if (loc.getBlock() != null && loc.getBlock().getType().equals(Material.CHEST)) {
				Chest c = (Chest)loc.getBlock().getState();
				Inventory inv = c.getInventory();
				if (inv.contains(Material.DIAMOND_CHESTPLATE)) {
					dc++;
				}
			}
		}

		for (Entity entity : getServer().getWorld("world").getEntities()) {
			if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
				Item i = (Item) entity;
				if (i.getItemStack().getType().equals(Material.DIAMOND_CHESTPLATE)) {
					dc++;
				}
			}
		}

		return dc;
	}

	/**
	 * Check for total amount of armor on map
	 * @return total amount
	 */
	public int checkForNumberOfLeggings() {
		int dl = 0;

		for (Player player : getServer().getOnlinePlayers()) {
			PlayerInventory inv = player.getInventory();
			if (Utils.armorType(inv.getLeggings().getType()) == 1) {
				dl++;
			}
		}

		for (Location loc : chests) {
			if (loc.getBlock() != null && loc.getBlock().getType().equals(Material.CHEST)) {
				Chest c = (Chest)loc.getBlock().getState();
				Inventory inv = c.getInventory();
				if (inv.contains(Material.DIAMOND_LEGGINGS)) {
					dl++;
				}
			}
		}

		for (Entity entity : getServer().getWorld("world").getEntities()) {
			if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
				Item i = (Item) entity;
				if (i.getItemStack().getType().equals(Material.DIAMOND_LEGGINGS)) {
					dl++;
				}
			}
		}

		return dl;
	}

	/**
	 * Check for total amount of armor on map
	 * @return total amount
	 */
	public int checkForNumberOfBoots() {
		int db = 0;

		for (Player player : getServer().getOnlinePlayers()) {
			PlayerInventory inv = player.getInventory();
			if (Utils.armorType(inv.getBoots().getType()) == 1) {
				db++;
			}
		}

		for (Location loc : chests) {
			if (loc.getBlock() != null && loc.getBlock().getType().equals(Material.CHEST)) {
				Chest c = (Chest)loc.getBlock().getState();
				Inventory inv = c.getInventory();
				if (inv.contains(Material.DIAMOND_BOOTS)) {
					db++;
				}
			}
		}

		for (Entity entity : getServer().getWorld("world").getEntities()) {
			if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
				Item i = (Item) entity;
				if (i.getItemStack().getType().equals(Material.DIAMOND_BOOTS)) {
					db++;
				}
			}
		}

		return db;
	}

	/**
	 * Check if there may be duped armor
	 * @return true if there is
	 */
	public boolean checkForDupedArmor() {
		int dh = 0;
		int dc = 0;
		int dl = 0;
		int db = 0;

		for (Player player : getServer().getOnlinePlayers()) {
			PlayerInventory inv = player.getInventory();
			if (Utils.armorType(inv.getHelmet().getType()) == 1) {
				dh++;
			}
			if (Utils.armorType(inv.getChestplate().getType()) == 1) {
				dc++;
			}
			if (Utils.armorType(inv.getLeggings().getType()) == 1) {
				dl++;
			}
			if (Utils.armorType(inv.getBoots().getType()) == 1) {
				db++;
			}
		}

		for (Location loc : chests) {
			if (loc.getBlock() != null && loc.getBlock().getType().equals(Material.CHEST)) {
				Chest c = (Chest)loc.getBlock().getState();
				Inventory inv = c.getInventory();
				if (inv.contains(Material.DIAMOND_HELMET)) {
					dh++;
				}
				if (inv.contains(Material.DIAMOND_CHESTPLATE)) {
					dc++;
				}
				if (inv.contains(Material.DIAMOND_LEGGINGS)) {
					dl++;
				}
				if (inv.contains(Material.DIAMOND_BOOTS)) {
					db++;
				}
			}
		}

		for (Entity entity : getServer().getWorld("world").getEntities()) {
			if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
				Item i = (Item) entity;
				if (i.getItemStack().getType().equals(Material.DIAMOND_HELMET)) {
					dh++;
				} else if (i.getItemStack().getType().equals(Material.DIAMOND_CHESTPLATE)) {
					dc++;
				} else if (i.getItemStack().getType().equals(Material.DIAMOND_LEGGINGS)) {
					dl++;
				} else if (i.getItemStack().getType().equals(Material.DIAMOND_BOOTS)) {
					db++;
				}
			}
		}

		return dh > 1 || dc > 1 || dl > 1 || db > 1;
	}

	/**
	 * Broadcast a message to all players, ChatColor.translate is done for you
	 * @param msg string
	 */
	public void broadcast(String msg) {
		getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

}
