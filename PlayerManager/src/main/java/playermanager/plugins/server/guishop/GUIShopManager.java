package playermanager.plugins.server.guishop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;
import playermanager.plugins.server.main.PlayerManager;

import java.util.ArrayList;

public class GUIShopManager implements Listener {

	public Plugin plugin;
	public PlayerManager pM;
	public ArrayList<GUIShop> guiShops = new ArrayList<GUIShop>();

	public GUIShopManager(Plugin pl, PlayerManager playerManager) {
		plugin = pl;
		pM = playerManager;
	}

	public void add(Player player) {
		guiShops.add(new GUIShop(this, player));
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInventoryClose(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		for (GUIShop shop : guiShops) {
			if (shop.isPlayersGUIShop((Player) e.getPlayer())) {
				guiShops.remove(shop);
				return;
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}

		Player p = (Player)e.getWhoClicked();
		for (GUIShop guiShop : guiShops) {
			if (guiShop.isPlayersGUIShop(p)) {
				e.setCancelled(true);
				guiShop.onInventoryClick(e);
				return;
			}
		}
	}
}
