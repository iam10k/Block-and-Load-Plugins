package playermanager.plugins.server.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;
import playermanager.plugins.server.player.ServerPlayer;

public class PlayerCommand implements CommandExecutor, Listener {

	private PlayerManager plugin;


	public PlayerCommand(PlayerManager pl) {
		this.plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		ServerPlayer serverPlayer = plugin.getServerPlayer((Player) sender);

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("badges")) {
				String[] msgs = new String[3];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  &cFeature coming soon! &3"; // Connect to database and get badges. Show 10 at a time
				msgs[2] = "&6---------------------------------------------";
				plugin.sendMessage(sender, msgs);

				return true;
			} else if (args[0].equalsIgnoreCase("info")) {
				String[] msgs = new String[7];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  Date Joined: &3" + serverPlayer.getDateJoined();
				msgs[2] = "&7  B&&7L XP: &3" + plugin.format(serverPlayer.getCredits());
				msgs[3] = "&7  XP Multiplier: &3" + serverPlayer.getMultiplier();
				msgs[4] = "&7  Votes: &3" + serverPlayer.getTotalVotes();
				msgs[5] = "&7  Muted: &3" + serverPlayer.getMuteState();
				msgs[6] = "&6---------------------------------------------";
				plugin.sendMessage(sender, msgs);

				return true;
			} else {
				String[] msgs = new String[5];
				msgs[0] = "&6---------------------------------------------";
				msgs[1] = "&7  /player info &3- to display your stats";
				msgs[2] = "&7  /player badges &3- to display your badges";
				msgs[3] = "&7  /stats &3- for gamemode specific stats";
				msgs[4] = "&6---------------------------------------------";
				plugin.sendMessage(sender, msgs);

				return true;
			}
		} else {
			String[] msgs = new String[5];
			msgs[0] = "&6---------------------------------------------";
			msgs[1] = "&7  /player info &3- to display your stats";
			msgs[2] = "&7  /player badges &3- to display your badges";
			msgs[3] = "&7  /stats &3- for gamemode specific stats";
			msgs[4] = "&6---------------------------------------------";
			plugin.sendMessage(sender, msgs);

			return true;
		}
	}
}
