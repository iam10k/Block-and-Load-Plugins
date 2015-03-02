package bungeemanager.plugins.server.listener;

import bungeemanager.plugins.server.main.BungeeManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class EventListener implements Listener {

	private BungeeManager plugin;

	public EventListener(BungeeManager pl) {
		plugin = pl;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(ProxyPingEvent e) {
		e.getResponse().setDescription(ChatColor.translateAlternateColorCodes('&', plugin.motd));
	}
}
