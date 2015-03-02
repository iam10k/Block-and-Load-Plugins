package escort.plugins.server.listeners;

import escort.plugins.server.main.Escort;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventsListener {

	private Escort plugin;

	public EventsListener(Escort pl) {
		plugin = pl;
	}


	// BEGIN PLAYER METHODS
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {

	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {

	}

	@EventHandler
	public void onKick(PlayerKickEvent e) {

	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {

	}
	// END PLAYER METHODS


	// BEGIN ENTITY METHODS
	@EventHandler
	public void onDamageByEntity(EntityDamageByEntityEvent e) {

	}
	// END ENTITY METHODS
}
