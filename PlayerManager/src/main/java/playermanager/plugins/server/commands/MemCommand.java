package playermanager.plugins.server.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import playermanager.plugins.server.main.PlayerManager;

public class MemCommand implements CommandExecutor, Listener {

    private PlayerManager plugin;

    public MemCommand(PlayerManager pl) {
        plugin = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("playermanager.mem")) {
			int max = getMaxRam();
			int total = getTotalRam();
			int used = getUsedRam();
			int free = getFreeRam();
			plugin.sendMessage(sender, "&7- Memory Stats");
			plugin.sendMessage(sender, "&7    Max memory for server: &c" +  max + " &7MB");
			plugin.sendMessage(sender, "&7    Total allocated memory: &c" + total + " &7MB (" + total * 100 / max + "%)");
			plugin.sendMessage(sender, "&7    Free allocated memory: &c" + free + " &7MB (" + free * 100 / total + "%)");
			plugin.sendMessage(sender, "&7    Used allocated memory: &c" + used + " &7MB (" + used * 100 / total + "%)");
			return true;
        }
        return false;
    }

	public final int getFreeRam() {
		Runtime runtime = Runtime.getRuntime();
		return Math.round((float)(runtime.freeMemory() / 1048576L));
	}

	public final int getMaxRam() {
		Runtime runtime = Runtime.getRuntime();
		return Math.round((float)(runtime.maxMemory() / 1048576L));
	}

	public final int getUsedRam() {
		return getTotalRam() - getFreeRam();
	}

	public final int getTotalRam() {
		Runtime runtime = Runtime.getRuntime();
		return Math.round((float)(runtime.totalMemory() / 1048576L));
	}
}
