package factionsmanager.plugins.server.main;

import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;
import factionsmanager.plugins.server.commands.*;
import factionsmanager.plugins.server.listeners.EventsListener;
import factionsmanager.plugins.server.listeners.XPItemListener;
import factionsmanager.plugins.server.util.XPEnchantment;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import playermanager.plugins.server.guishop.GUIShopCommand;
import playermanager.plugins.server.main.PlayerManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Level;

public class FactionsManager extends JavaPlugin {

	public PlayerManager pM;
	public CombatTagApi combatTag;

	public boolean sendToLog = false;
	public String itemName = "AIR";
	public boolean useLevels = false;
	public String type = "Experience";
	public int normalAmount = 10;
	public int shiftAmount = 30;
	public int maxAmount = -1;
	public boolean showEnchant = true;

	public String amountColor = "&6";
	public String xpHolderColor = "&5";
	public String deposit = "&aYou have deposited {amount} {xptype}.";
	public String withdraw = "&3You have withdrawn {amount} {xptype}.";
	public String notEnough = "&cYou or the item does not have {amount} {xptype}.";
	public String max = "&cMax amount of {xptype} in this XP Item.";

	public boolean stopPlugin = false;

	public Enchantment xpEnch;
	
	public int xpPerSwordKill = 0;
	public int xpPerBowKill = 0;
	public int xpPer100BlocksPlaced = 0;
	public int xpPer100BlocksBroken = 0;

	public FileConfiguration pConfig;

	public void onEnable() {
		Plugin plugin = getServer().getPluginManager().getPlugin("PlayerManager");
		if (plugin == null) {
			onDisable();
		}
		pM = (PlayerManager)plugin;

		if(getServer().getPluginManager().getPlugin("CombatTag") != null){
			combatTag = new CombatTagApi((CombatTag)getServer().getPluginManager().getPlugin("CombatTag"));
		}

		// Load config
		configDefaults();
		loadConfig();
		loadPremiumFile();

		// Register events and command
		getServer().getPluginManager().registerEvents(new EventsListener(this), this);
		getServer().getPluginManager().registerEvents(new XPItemListener(this), this);
		getCommand("factionsmanager").setExecutor(new ReloadCommand(this));
		getCommand("factionhome").setExecutor(new FactionHomeCommand(this));
		getCommand("xrayban").setExecutor(new XrayBanCommand(this));
		getCommand("xray").setExecutor(new XrayCommand(this));
		getCommand("stats").setExecutor(new StatsCommand(this));

		// Create GUIShop, one line does everything else for you
		getCommand("xpshop").setExecutor(new GUIShopCommand(this, pM, false));
	}

	/**
	 * Copy config defaults
	 */
	private void configDefaults() {
		getConfig().addDefault("XP.xpPerSwordKill", 2);
		getConfig().addDefault("XP.xpPerBowKill", 3);
		getConfig().addDefault("XP.xpPer100BlocksBroken", 7);
		getConfig().addDefault("XP.xpPer100BlocksPlaced", 6);
		getConfig().addDefault("BackEnd.sendInteractionsToLog", true);
		getConfig().addDefault("Settings.itemName", "AIR");
		getConfig().addDefault("Settings.useLevels" , false);
		getConfig().addDefault("Settings.normalDepositAmount", 50);
		getConfig().addDefault("Settings.shiftDepositAmount", 300);
		getConfig().addDefault("Settings.maxAmount", -1);
		getConfig().addDefault("Settings.showEnchantWhenAmountOver0", true);
		getConfig().addDefault("Colors.itemNameAmount", "&6");
		getConfig().addDefault("Colors.itemName", "&5");
		getConfig().addDefault("Messages.deposit", "&aYou have deposited {amount} {xptype}.");
		getConfig().addDefault("Messages.withdraw", "&3You have withdrawn {amount} {xptype}.");
		getConfig().addDefault("Messages.notEnough", "&cYou or the item does not have {amount} {xptype}.");
		getConfig().addDefault("Messages.max", "&cMax amount of {xptype} in this XP Item.");

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
		xpPerSwordKill = getConfig().getInt("XP.xpPerSwordKill", 1);
		xpPerBowKill = getConfig().getInt("XP.xpPerBowKill", 2);
		xpPer100BlocksBroken = getConfig().getInt("XP.xpPer100BlocksBroken", 7);
		xpPer100BlocksPlaced = getConfig().getInt("XP.xpPer100BlocksPlaced", 6);
		sendToLog = getConfig().getBoolean("BackEnd.sendInteractionsToLog", false);
		itemName = getConfig().getString("Settings.itemName", "AIR");
		useLevels = getConfig().getBoolean("Settings.useLevels", false);
		if (useLevels) {
			type = "XP Levels";
		}
		normalAmount = getConfig().getInt("Settings.normalDepositAmount", 10);
		shiftAmount = getConfig().getInt("Settings.shiftDepositAmount", 30);
		maxAmount = getConfig().getInt("Settings.maxAmount", -1);
		showEnchant = getConfig().getBoolean("Settings.showEnchantWhenAmountOver0", true);
		amountColor = getConfig().getString("Colors.itemNameAmount", "&6");
		xpHolderColor = getConfig().getString("Colors.itemName", "&5");
		deposit = getConfig().getString("Messages.deposit", "&aYou have deposited {amount} {xptype}.");
		withdraw = getConfig().getString("Messages.withdraw", "&3You have withdrawn {amount} {xptype}.");
		notEnough = getConfig().getString("Messages.notEnough", "&cYou or the item does not have {amount} {xptype}.");
		max = getConfig().getString("Messages.max", "&cMax amount of {xptype} in this XP Item.");

		checkItem();
	}

	public void loadPremiumFile() {
		File file = new File(getDataFolder(), "premium.yml");
		pConfig = YamlConfiguration.loadConfiguration(file);
		pConfig.addDefault("PlayerUUIDGoesHereXXXX", "default");
		savePremiumFile();
	}

	public void savePremiumFile() {
		File file = new File(getDataFolder(), "premium.yml");
		try {
			pConfig.save(file);
		} catch (IOException e) {
		}
	}

	/**
	 * Reload XP Settings
	 */
	public void reloadXP() {
		xpPerSwordKill = getConfig().getInt("XP.xpPerSwordKill", 1);
		xpPerBowKill = getConfig().getInt("XP.xpPerBowKill", 2);
		xpPer100BlocksBroken = getConfig().getInt("XP.xpPer100BlocksBroken", 7);
		xpPer100BlocksPlaced = getConfig().getInt("XP.xpPer100BlocksPlaced", 6);
	}

	private void checkItem() {
		if (Material.getMaterial(itemName) == null || itemName.equals("AIR")) {
			stopPlugin = true;
			getLogger().info("XPItems disabled : Invaid item : /factionsmanager reload");
		}
		try{
			try {
				Field f = Enchantment.class.getDeclaredField("acceptingNew");
				f.setAccessible(true);
				f.set(null, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				xpEnch = new XPEnchantment(99);
				EnchantmentWrapper.registerEnchantment(xpEnch);
				getLogger().log(Level.INFO, "Enchantment Added!");
			} catch (IllegalArgumentException e){
				stopPlugin = true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void enableAntiXray(Player p) {
		if (!p.hasPermission("^Orebfuscator.deobfuscate")) {
			pM.addPermission(p, "^Orebfuscator.deobfuscate");
		}
	}
}
