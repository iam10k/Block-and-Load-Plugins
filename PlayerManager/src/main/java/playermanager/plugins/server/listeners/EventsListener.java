package playermanager.plugins.server.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;
import playermanager.plugins.server.util.BlockChecker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class EventsListener implements Listener {

    private PlayerManager plugin;
	private BlockChecker checker;

    public EventsListener(PlayerManager pl) {
		plugin = pl;
		checker = new BlockChecker(plugin);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
		ServerPlayer serverPlayer = plugin.getServerPlayer(p.getName());
		if (serverPlayer != null) {
			if (plugin.kickIfBanned(serverPlayer) == 1) {
				e.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + "Banned from this game." +
						"Try the hacking only server.");
				e.setResult(PlayerLoginEvent.Result.KICK_BANNED);
				e.setKickMessage(ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + "Banned from this game. Try the hacking only server.");
			} else if (plugin.kickIfBanned(serverPlayer) == 2) {
				e.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + "Banned from the Network!");
				e.setResult(PlayerLoginEvent.Result.KICK_BANNED);
				e.setKickMessage(ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + "Banned from the Network!");
			}

			if (plugin.getServer().getOnlinePlayers().length >= plugin.getServer().getMaxPlayers()) {
				if (serverPlayer.getPlayerRank().equals("default")) {
					e.disallow(PlayerLoginEvent.Result.KICK_FULL, ChatColor.GREEN + "Purchase a premium rank to play on full servers! " +
							"www.blockandload.us/shop");
					e.setResult(PlayerLoginEvent.Result.KICK_FULL);
					e.setKickMessage(ChatColor.GREEN + "Purchase a premium rank to play on full servers! " +
							"www.blockandload.us/shop");
				} else {
					e.setResult(PlayerLoginEvent.Result.ALLOWED);
				}
			}
		}
    }

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		ServerPlayer serverPlayer;

		serverPlayer = plugin.addPlayer(p);
		plugin.setGroups(p, serverPlayer.getRanks());

		serverPlayer.addIP(p.getAddress().getAddress().getHostAddress());

		if (!p.hasPermission("playermanager.ignoreip")) {
			String playerIP = p.getAddress().getAddress().getHostAddress();
			checkIPContainsPlayer(playerIP, p.getName());
		}

		String name = plugin.getRankPrefix(p, serverPlayer) + plugin.getRankSuffix(p, serverPlayer) + serverPlayer.getNick();
		p.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		if (!plugin.getRankPrefix(p, serverPlayer).equals("")) {
			String color = "";
			if (plugin.getRankSuffix(p, serverPlayer) != null && !plugin.getRankSuffix(p, serverPlayer).equals("")) {
				color = plugin.getRankSuffix(p, serverPlayer);
			}
			if ((color + serverPlayer.getNick()).length() > 16) {
				p.setPlayerListName(ChatColor.translateAlternateColorCodes('&', (color + serverPlayer.getNick()).substring(0, 16)));
			} else {
				p.setPlayerListName(ChatColor.translateAlternateColorCodes('&', color + serverPlayer.getNick()));
			}
		}

		String[] temp = new String[5];
		temp[0] = "&6---------------------------------------------";
		temp[1] = "&3Block and Load Network &7stat tracking";
		temp[2] = "     &7enabled for: &6" + p.getDisplayName();
		temp[3] = "&3/player &7to view stats";
		temp[4] = "&6---------------------------------------------";
		plugin.sendMessage(p, temp);

		if (plugin.server.equals("factions")) {
			plugin.enableAntiXray(e.getPlayer());
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		e.setQuitMessage("");

		final Player p = e.getPlayer();
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				plugin.removePlayer(p);
			}
		}, 4L);
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		// Split the command
		String[] args = e.getMessage().split(" ");
		// Get rid of the first / if it exists
		String cmd = args[0].replace("/", "");
		if (args.length > 1) {
			if(cmd.equalsIgnoreCase("op")) {
				// Cancel it if needed, return if not needed
				for (String s : plugin.getConfig().getStringList("AutoDeOp.playersAllowedOp")) {
					if (s.equalsIgnoreCase(args[1])) {
						return;
					}
				}
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onConsoleCMD(ServerCommandEvent e){
		// Split the command
		String[] args = e.getCommand().split(" ");
		// Get rid of the first / if it exists
		String cmd = args[0].replace("/", "");
		if (args.length > 1) {
			if(cmd.equalsIgnoreCase("op")) {
				// Cancel it if needed, return if not needed
				for (String s : plugin.getConfig().getStringList("AutoDeOp.playersAllowedOp")) {
					if (s.equalsIgnoreCase(args[1])) {
						return;
					}
				}
				e.setCommand("");
			}
		}
	}

    private void checkIPContainsPlayer(String IP, String player) {
        // Get data from the database using username
        String query = "SELECT players FROM ips WHERE ip='" + IP + "'";
        String players = "";
        try {
            ResultSet rs = plugin.querySQL(query);
            if (rs.next()) {
                players = rs.getString("players");
            } else {
                query = "INSERT INTO ips (ip, players) VALUES ('" + IP + "', '" + player + "')";
				plugin.updateSQL(query);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
		plugin.closeConnection();

        if (!players.toLowerCase().contains(player.toLowerCase())) {
            players += "," + player;
            query = "UPDATE ips SET " +
                    "players='" + players + "'" +
                    " WHERE ip='" + IP + "'";
			plugin.updateSQL(query);
        }
    }

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.isCancelled() || e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		Player pl = e.getPlayer();
		if (pl.hasPermission("playermanager.freecambypassblock")) {
			return;
		}

		Block b = e.getClickedBlock();

		if(plugin.getInteractBlocks().contains(b.getType())){
			if(!checker.canSee(e.getPlayer(), e.getClickedBlock())){
				plugin.getLogger().log(Level.INFO, pl.getName() + " may be trying to freecam!");
				e.setCancelled(true);
				for (Player player : plugin.getServer().getOnlinePlayers()) {
					if (player.hasPermission("playermanager.notify")) {
						plugin.sendMessage(player, "&7[&cFREECAM&7] &b" + pl.getName() + " may be freecamming!");
					}
				}
			}
		}
	}
}
