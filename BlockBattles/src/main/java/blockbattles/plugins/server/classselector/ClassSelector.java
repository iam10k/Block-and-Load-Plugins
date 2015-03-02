package blockbattles.plugins.server.classselector;

import blockbattles.plugins.server.game.ArenaPlayer;
import blockbattles.plugins.server.game.GameClass;
import blockbattles.plugins.server.main.BlockBattles;
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
import org.bukkit.potion.PotionEffectType;
import playermanager.plugins.server.player.ServerPlayer;

import java.util.List;

public class ClassSelector implements InventoryHolder {

	private BlockBattles plugin;

	private Player playerWithGUI;
	private ServerPlayer serverPlayer;
	private Inventory inventory;

	public ClassSelector(BlockBattles pl, Player player) {
		plugin = pl;
		playerWithGUI = player;
		serverPlayer = plugin.pM.getServerPlayer(playerWithGUI);

		createInventory();
		showInventory();
	}

	private void createInventory() {
		ItemStack[] items = new ItemStack[27];
		for (int x = 0; x < 27; x++) {
			if (plugin.getGameClasses().size() > x) {
				// Get the GameClass
				GameClass gameClass = plugin.getGameClasses().get(x);

				// Get material type
				Material material = gameClass.getMaterial();
				// Get amount of the item
				int amount = 1;

				// Check if material is valid and not air
				if (material == null || material.equals(Material.AIR)) {
					continue;
				}

				// Create the itemstack
				ItemStack is = new ItemStack(material, amount);

				// Get name
				String name = gameClass.getName();
				// Get lore
				List<String> lore = gameClass.getDescription();
				lore.add("");

				// Add last line, cost or purchased
				if (gameClass.getCost() == 0) {
					lore.add("&aDEFAULT");
				} else if (serverPlayer.getBoughtPerks().contains(gameClass.getPerk())) {
					lore.add("&aUNLOCKED");
				} else {
					lore.add("&cLOCKED");
					lore.add("&3/xpshop &fto purchase");
				}
				// Format lore colors
				for (int z = 0; z < lore.size(); z++) {
					lore.set(z, ChatColor.translateAlternateColorCodes('&', lore.get(z)));
				}

				// Set itemmeta
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
				im.setLore(lore);

				// Set item in items array
				is.setItemMeta(im);
				items[x] = is;
			}
		}
		inventory = Bukkit.createInventory(this, 27, "Class Selector");
		inventory.setContents(items);
	}

	public void showInventory() {
		playerWithGUI.openInventory(inventory);
	}

	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player)e.getWhoClicked();
		if (!e.getInventory().equals(p.getInventory())) {
			if (e.getClick().equals(ClickType.LEFT)) {
				if (e.getCurrentItem() != null && !e.getCurrentItem().getType().equals(Material.AIR)) {
					if (plugin.getGameClasses().get(e.getSlot()).getCost() == 0 ||
							serverPlayer.getBoughtPerks().contains(plugin.getGameClasses().get(e.getSlot()).getPerk())) {
						plugin.getArenaPlayer(playerWithGUI).setPClass(plugin.getGameClasses().get(e.getSlot()).getID());
						plugin.pM.sendMessage(playerWithGUI, plugin.getGameClasses().get(e.getSlot()).getName() + " class selected!");
						playerWithGUI.closeInventory();
					} else {
						plugin.pM.sendMessage(playerWithGUI, "&cThat class is locked. To unlock type /xpshop");
						playerWithGUI.closeInventory();
					}
				}
			}
		}
	}

	public boolean isPlayersGUIShop(Player player) {
		return playerWithGUI.getName().equals(player.getName());
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}
}
