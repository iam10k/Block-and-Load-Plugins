package blockbattles.plugins.server.classselector;

import blockbattles.plugins.server.main.BlockBattles;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public class ClassSelectorManager implements Listener {

	public BlockBattles plugin;
	public ArrayList<ClassSelector> classSelectors = new ArrayList<ClassSelector>();

	public ClassSelectorManager(BlockBattles pl) {
		plugin = pl;
	}

	public void add(Player player) {
		classSelectors.add(new ClassSelector(plugin, player));
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInventoryClose(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		for (ClassSelector classSelector : classSelectors) {
			if (classSelector.isPlayersGUIShop((Player) e.getPlayer())) {
				classSelectors.remove(classSelector);
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
		for (ClassSelector classSelector : classSelectors) {
			if (classSelector.isPlayersGUIShop(p)) {
				e.setCancelled(true);
				classSelector.onInventoryClick(e);
				return;
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			if (e.getItem() != null && e.getItem().getType().equals(Material.BEDROCK)) {
				e.setCancelled(true);
				classSelectors.add(new ClassSelector(plugin, e.getPlayer()));
			}
		}
	}
}
