package hubmanager.plugins.server.listeners;

import hubmanager.plugins.server.main.HubManager;
import hubmanager.plugins.server.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class EventsListener implements Listener {

	private HubManager plugin;

	private ArrayList<String> playersWhoCanDoubleJump = new ArrayList<String>();


	public EventsListener(HubManager pl) {
		plugin = pl;
	}

	// BEGIN PLAYER EVENTS
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		p.teleport(plugin.spawn);
		for(PotionEffect potionEffect : p.getActivePotionEffects()) {
			p.removePotionEffect(potionEffect.getType());
		}
		p.addPotionEffect(plugin.vision, true);
		if (plugin.speed != null) {
			p.addPotionEffect(plugin.speed, true);
		}

		if (!p.hasPermission("hubmanager.noclearinventory")) {
			p.setGameMode(GameMode.SURVIVAL);
			p.getInventory().clear();
		}
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		if (plugin.parkour.contains(e.getPlayer().getName())) {
			plugin.parkour.remove(e.getPlayer().getName());
		}

		if (playersWhoCanDoubleJump.contains(e.getPlayer().getName())) {
			playersWhoCanDoubleJump.remove(e.getPlayer().getName());
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (e.getTo().getBlockY() < 15) {
			p.teleport(plugin.spawn);
		}

		for (Location headLoc : plugin.headLocations) {
			if (headLoc.getBlock().getType().equals(Material.SKULL)) {
				if (headLoc.distanceSquared(p.getLocation()) <= 100) {
					Block sameY = p.getWorld().getBlockAt(p.getLocation().getBlockX(),
							headLoc.getBlockY(), p.getLocation().getBlockZ());
					if (headLoc.getBlockX() != sameY.getLocation().getBlockX() || headLoc.getBlockZ() != sameY.getLocation().getBlockZ()) {
						Skull skull = (Skull) headLoc.getBlock().getState();
						if (headLoc.getBlock().getFace(sameY.getLocation().getBlock()) != skull.getRotation()) {
							skull.setRotation(Utils.getDirection(headLoc, p.getLocation()));
							skull.update();
						}
					}
				}
			}
		}

		// When the player lands on ground
		if (p.getGameMode() != GameMode.CREATIVE &&
				!p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR) &&
				!plugin.parkour.contains(p.getName())) {
			playersWhoCanDoubleJump.add(p.getName());
			p.setAllowFlight(true);
		}
		// If player changes gamemode make them not able to jump
		if (p.getGameMode() == GameMode.CREATIVE && playersWhoCanDoubleJump.contains(p.getName())) {
			playersWhoCanDoubleJump.remove(e.getPlayer().getName());
			p.setAllowFlight(true);
		}
	}

	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
		if (!e.isCancelled()) {
			double velocity = plugin.getConfig().getDouble("Jump.velocity");
			double height = plugin.getConfig().getDouble("Jump.height");
			Player p = e.getPlayer();
			Location loc = p.getLocation();
			if (p.getGameMode() != GameMode.CREATIVE && playersWhoCanDoubleJump.contains(p.getName()) &&
					!plugin.parkour.contains(p.getName())) {
				playersWhoCanDoubleJump.remove(p.getName());
				e.setCancelled(true);
				p.setAllowFlight(false);
				p.setFlying(false);
				p.setVelocity(loc.getDirection().multiply(velocity).setY(height));
			}
		}
	}
	// END PLAYER EVENTS


	// BEGIN BLOCK EVENTS
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if (e.getPlayer().hasPermission("hubmanager.add")) {
			if (e.getBlockPlaced().getType().equals(Material.SKULL)) {
				String loc = Utils.convertLocationToString(e.getBlockPlaced().getLocation(), true, false);
				plugin.headLocations.add(e.getBlockPlaced().getLocation());
				List<String> list = plugin.getConfig().getStringList("HeadLocations.list");
				list.add(loc);
				plugin.getConfig().set("HeadLocations.list", list);
				plugin.saveConfig();
				plugin.pM.sendMessage(e.getPlayer(), "&aHead Location added!");
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (e.getBlock().getType().equals(Material.SKULL)) {
			String loc = Utils.convertLocationToString(e.getBlock().getLocation(), true, false);
			if (plugin.getConfig().getStringList("HeadLocations.list").contains(loc)) {
				plugin.headLocations.remove(e.getBlock().getLocation());
				List<String> list = plugin.getConfig().getStringList("HeadLocations.list");
				list.remove(loc);
				plugin.getConfig().set("HeadLocations.list", list);
				plugin.saveConfig();
				plugin.pM.sendMessage(e.getPlayer(), "&aHead Location removed!");
			}
		}
	}
	// END BLOCK EVENTS


	// BEGIN MISC EVENTS
	@EventHandler
	public void weatherChange(WeatherChangeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onSign(SignChangeEvent e) {
		for (int x = 0; x < e.getLines().length; x++) {
			e.setLine(x, ChatColor.translateAlternateColorCodes('&', e.getLine(x)));
		}
	}
	// END MISC EVENTS
}
