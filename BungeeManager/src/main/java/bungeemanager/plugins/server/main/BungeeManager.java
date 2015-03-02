package bungeemanager.plugins.server.main;

import bungeemanager.plugins.server.commands.ReloadCommand;
import bungeemanager.plugins.server.listener.EventListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;


public class BungeeManager extends Plugin {

	public String motd = "";

	public void onEnable() {

		// Load config
		loadConfig();

		// Register command & Listener
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new ReloadCommand(this));
		ProxyServer.getInstance().getPluginManager().registerListener(this, new EventListener(this));
	}

	/**
	 * Load/Reload config
	 */
	public void loadConfig() {
		File file = new File(getDataFolder() + File.separator + "ping.txt");
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(file));
			motd = fileReader.readLine();
		} catch (IOException e) {
			motd = "&5<&7---- &aBlock and Load Network &7----&5>";
		}
		if (motd == null || motd.equals("")) {
			motd = "&5<&7---- &aBlock and Load Network &7----&5>";
		}
	}
}
