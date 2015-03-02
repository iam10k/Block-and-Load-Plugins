package playermanager.plugins.server.guishop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import playermanager.plugins.server.player.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIShop implements InventoryHolder {

	private GUIShopManager manager;

	private Inventory inventory;
	private Player playerWithGUI;
	private ServerPlayer serverPlayer;

	public GUIShop(GUIShopManager guiShopManager, Player player) {
		manager = guiShopManager;
		playerWithGUI = player;
		serverPlayer = manager.pM.getServerPlayer(playerWithGUI);
		createInventory();
		showInventory();
	}

	private void createInventory() {
		manager.plugin.reloadConfig();
		ItemStack[] items = new ItemStack[27];
		for (int x = 0; x < 27; x++) {
			if (manager.plugin.getConfig().contains("XPShop.slot" + x)) {
				// Get material type
				String material = manager.plugin.getConfig().getString("XPShop.slot" + x + ".material");
				// Get amount of the item
				int amount = manager.plugin.getConfig().getInt("XPShop.slot" + x + ".amount");

				// Check if material is valid and not air
				if (Material.getMaterial(material) == null || Material.getMaterial(material).equals(Material.AIR)) {
					continue;
				}

				// If item is exp bottle or paper use the default template
				if (Material.getMaterial(material).equals(Material.EXP_BOTTLE)) {
					// Create the itemstack
					ItemStack is = new ItemStack(Material.getMaterial(material), amount);

					// Set itemmeta, name
					ItemMeta im = is.getItemMeta();
					im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6" +
									manager.pM.format(serverPlayer.getCredits()) + " &aB&&aL XP"));

					// Set item in items array
					is.setItemMeta(im);
					items[x] = is;

					continue;
				}
				if (Material.getMaterial(material).equals(Material.PAPER)) {
					// Create the itemstack
					ItemStack is = new ItemStack(Material.getMaterial(material), amount);

					// Create lore
					List<String> lore = new ArrayList<String>();
					lore.add("&aXP Bottle will tell your B&&aL XP.");
					lore.add("&fHover to view more info.");
					lore.add("");
					lore.add("&3Shift click to purchase.");
					lore.add("");
					lore.add("&9Visit blockandload.us/shop");
					lore.add("&9to purchase B&9L XP.");
					// Format lore colors
					for (int z = 0; z < lore.size(); z++) {
						lore.set(z, ChatColor.translateAlternateColorCodes('&', lore.get(z)));
					}

					// Set itemmeta, name
					ItemMeta im = is.getItemMeta();
					im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6GUIShop Guide"));
					im.setLore(lore);

					// Set item in items array
					is.setItemMeta(im);
					items[x] = is;

					continue;
				}

				// Create the itemstack
				ItemStack is = manager.plugin.getConfig().getItemStack("XPShop.slot" + x + ".itemstack").clone();

				// Get lore
				List<String> lore = is.getItemMeta().getLore();

				if (lore == null) {
					lore = new ArrayList<String>();
				}

				// Add last line, cost or purchased
				if (serverPlayer.getBoughtPerks().contains(manager.plugin.getConfig().getString("XPShop.slot" + x + ".perkID"))) {
					lore.add("&aPURCHASED");
				} else {
					lore.add("&a" + manager.pM.format(manager.plugin.getConfig().getInt("XPShop.slot" + x + ".cost")) + " B&&aL XP");
				}
				// Format lore colors
				for (int z = 0; z < lore.size(); z++) {
					lore.set(z, ChatColor.translateAlternateColorCodes('&', lore.get(z)));
				}

				// Set itemmeta
				ItemMeta im = is.getItemMeta();
				im.setLore(lore);

				// Set item in items array
				is.setItemMeta(im);
				items[x] = is;
			}
		}
		inventory = Bukkit.createInventory(this, 27, "XP Shop");
		inventory.setContents(items);
		manager.plugin.reloadConfig();
	}

	public void showInventory() {
		playerWithGUI.openInventory(inventory);
	}

	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player)e.getWhoClicked();
		if (!e.getInventory().equals(p.getInventory())) {
			if (e.getClick().equals(ClickType.SHIFT_LEFT)) {
				if (e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(Material.AIR)) {
					purchaseItem(e.getSlot());
				}
			}
		}
	}

	private void purchaseItem(int slot) {
		// Get ID and cost
		String perkID = manager.plugin.getConfig().getString("XPShop.slot" + slot + ".perkID");
		int cost = manager.plugin.getConfig().getInt("XPShop.slot" + slot + ".cost");

		// Check if player can buy the perk
		if (!serverPlayer.getBoughtPerks().contains(perkID)) {
			if (serverPlayer.getCredits() >= cost) {
				// Prevents errors if perk doesn't exist
				if (manager.plugin.getConfig().contains("Perks." + perkID + ".name")) {
					// Remove credits, add Perk, and execute Purchase commands
					serverPlayer.removeCredits(cost);
					if (manager.plugin.getConfig().getBoolean("Perks."+ perkID + ".permanent", true)) {
						serverPlayer.addBoughtPerk(perkID);
					}
					executePerkCommands(perkID);

					manager.pM.sendMessage(playerWithGUI, "  &aPerk successfully purchased!");

					// Update the inventory
					createInventory();
					showInventory();
				}
			} else {
				manager.pM.sendMessage(playerWithGUI, "  &cNot enough XP to purchase!");
			}
		} else {
			manager.pM.sendMessage(playerWithGUI, "  &cYou already have that perk!");
		}
	}

	private void executePerkCommands(String perkID) {
		// Get name and commands
		String perkName = manager.plugin.getConfig().getString("Perks." + perkID + ".name");
		List<String> cmdsToExecute = manager.plugin.getConfig().getStringList("Perks." + perkID + ".purchaseCommands");

		// Execute perk commands
		for (String cmd : cmdsToExecute) {
			manager.plugin.getServer().dispatchCommand(
					manager.plugin.getServer().getConsoleSender(), cmd.replace("%PLAYER%", playerWithGUI.getName()));
		}
		manager.pM.sendMessage(playerWithGUI, "  &a" + perkName + " purchased successfully.");
	}

	public boolean isPlayersGUIShop(Player player) {
		return playerWithGUI.getName().equals(player.getName());
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}
}
