package blockbattles.plugins.server.commands;

import blockbattles.plugins.server.main.BlockBattles;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class EnchantCommand implements CommandExecutor, Listener {

	private BlockBattles plugin;


	public EnchantCommand(BlockBattles pl) {
		plugin = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		if (args.length != 2) {
			return false;
		}

		Player player = (Player) sender;

		if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
			plugin.pM.sendMessage(player, "&cInvalid Item!");
			return true;
		}

		Enchantment enchantment = Enchantment.getByName(args[0]);
		int level = -1;
		try {
			level = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			level = 0;
		}

		if (enchantment == null || level <= 0) {
			plugin.pM.sendMessage(player, "&cInvalid Enchantment or level!");
			return true;
		}

		player.getItemInHand().addUnsafeEnchantment(enchantment, level);
		return true;
	}
}