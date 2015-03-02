package blockbattles.plugins.server.game;

import blockbattles.plugins.server.main.BlockBattles;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameClass {

	private FileConfiguration config;

	private ItemStack[] inventory = new ItemStack[36];
	private ItemStack[] armor = new ItemStack[4];

	private String id;

	public GameClass(FileConfiguration cfg) {
		config = cfg;
		initClass();
	}

	/**
	 * Load the GameClass inventory
	 */
	private void initClass() {
		id = config.getString("Class.id");

		for (int x = 0; x < 36; x++) {
			inventory[x] = config.getItemStack("inventory.slot" + x + ".itemStack");
		}

		for (int x = 0; x < 4; x++) {
			armor[x] = config.getItemStack("armor.slot" + x + ".itemStack");
		}
	}

	/**
	 * Get the class ID
	 * @return string of GameClass id
	 */
	public String getID() {
		return id;
	}

	/**
	 * Get the display name of the class
	 * @return Item Name
	 */
	public String getName() { return config.getString("Class.name"); }

	/**
	 * Get the description (lore) of GameClass
	 * @return List<String> ready to be set as lore
	 */
	public List<String> getDescription() { return config.getStringList("Class.description"); }

	/**
	 * Get the Material to be displayed in class selection
	 * @return Material if found, PAPER if not
	 */
	public Material getMaterial() {
		Material m = Material.getMaterial(config.getString("Class.material", "PAPER"));
		if (m != null) {
			return m;
		} else {
			return Material.PAPER;
		}
	}

	public String getPerk() { return config.getString("Class.perkID"); }

	public int getCost() { return config.getInt("Class.cost"); }

	private ArrayList<PotionEffect> getEffects() {
		ArrayList<PotionEffect> temp = new ArrayList<PotionEffect>();
		for (String potion : config.getStringList("Effects.list")) {
			if (PotionEffectType.getByName(potion.substring(0, potion.indexOf(":"))) != null) {
				int amp = Integer.parseInt(potion.substring(potion.indexOf(":")));
				temp.add(new PotionEffect(PotionEffectType.getByName(potion), 1000000, amp - 1));
			}
		}
		return temp;
	}

	/**
	 * Give this class to the player
	 * @param player is not null
	 */
	public void give(Player player) {
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}

		player.getInventory().setContents(inventory);
		player.getInventory().setArmorContents(armor);

		for (PotionEffect effect : getEffects()) {
			player.addPotionEffect(effect);
		}

	}

	// BEGIN MODIFY CONFIG METHODS
	private void saveConfig(BlockBattles plugin) {
		File arenaFile = new File(plugin.getDataFolder() + File.separator + "classes", getID() + ".yml");
		try {
			config.save(arenaFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setMaterial(BlockBattles plugin, String material) {
		String m = Material.getMaterial(material).name();
		config.set("Class.material", m);
		saveConfig(plugin);
	}

	public void setCost(BlockBattles plugin, int cost) {
		config.set("Class.cost", cost);
		saveConfig(plugin);
	}

	public void setPerkID(BlockBattles plugin, String id) {
		config.set("Class.perkID", id);
		saveConfig(plugin);
	}
	// END MODIFY CONFIG METHODS
}
