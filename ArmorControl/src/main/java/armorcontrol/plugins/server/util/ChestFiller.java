package armorcontrol.plugins.server.util;

import armorcontrol.plugins.server.main.ArmorControl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class ChestFiller implements Runnable {

	private ArmorControl plugin;
	int x = 0;

	public ChestFiller (ArmorControl pl) {
		plugin = pl;
	}

	@Override
	public void run() {
		if (plugin.getServer().getOnlinePlayers().length > 3) {
			String item = plugin.items.get(x);
			Location loc;
			Chest c = null;
			Inventory cInv = null;

			// Make item stack
			String itemName = item.substring(0, item.indexOf(':'));
			int amount = Integer.parseInt(item.substring(item.indexOf('(') + 1, item.indexOf(')')));
			Short damageValue = Short.parseShort(item.substring(item.indexOf(':') + 1, item.indexOf('(')));

			// Create Material and if it is not null put it in chest
			Material m = Material.getMaterial(itemName);
			if (m != null) {
				// Create ItemStack
				ItemStack is = new ItemStack(m, amount, damageValue);

				// Pick different chest if it has that item already
				int count = 0;
				while (cInv == null || cInv.contains(m)) {
					loc = plugin.chests.get((int) Math.floor(plugin.chests.size() * Math.random()));
					count++;
					if (loc.getBlock() != null && loc.getBlock().getType().equals(Material.CHEST)) {
						c = (Chest)loc.getBlock().getState();
						cInv = c.getBlockInventory();
					}
					if (count == plugin.chests.size()) {
						x++;
						if (x == plugin.items.size()) {
							x = 0;
						}
						return;
					}
				}

				// Pick random spot in chest and add it
				ItemStack[] contents = cInv.getContents();
				contents[Utils.randomChestSlot(contents)] = is;
				cInv.setContents(contents);

				c.update(true, false);

				// Update world time
				if (x % 4 == 0) {
					c.getWorld().setTime(0);
				}
			} else {
				plugin.getLogger().log(Level.INFO, "While filling chests, item (" + itemName + ") was not found as a valid item!");
			}

			// Increment the count
			x++;
			if (x == plugin.items.size()) {
				x = 0;
			}
		}
	}
}
