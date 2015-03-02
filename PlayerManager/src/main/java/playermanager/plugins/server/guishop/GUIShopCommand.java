package playermanager.plugins.server.guishop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import playermanager.plugins.server.main.PlayerManager;

import java.util.List;

public class GUIShopCommand implements CommandExecutor, Listener {

	private Plugin plugin;
	private PlayerManager pM;
	private GUIShopManager shopManager;
	private boolean usePerms = false;


	public GUIShopCommand(Plugin pl, PlayerManager playerManager, boolean usePermissions) {
		plugin = pl;
		pM = playerManager;
		usePerms = usePermissions;
		shopManager = new GUIShopManager(pl, pM);
		plugin.getServer().getPluginManager().registerEvents(shopManager, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		Player p = (Player) sender;
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("saveshop")) {
				if (sender.hasPermission("guishop.create")) {
					plugin.reloadConfig();
					ItemStack[] contents = p.getInventory().getContents();
					for (int x = 9, z = 0; x < contents.length; x++, z++) {
						ItemStack is = contents[x];
						if (is != null && !is.getType().equals(Material.AIR)) {
							plugin.getConfig().set("XPShop.slot" + z + ".itemstack", is);
							plugin.getConfig().set("XPShop.slot" + z + ".material" , is.getType().name());
							plugin.getConfig().set("XPShop.slot" + z + ".amount" , is.getAmount());
							plugin.getConfig().set("XPShop.slot" + z + ".name", is.getItemMeta().getDisplayName());
							if (!plugin.getConfig().contains("XPShop.slot" + z + ".cost")) {
								plugin.getConfig().set("XPShop.slot" + z + ".cost", 0);
							}
							if (!plugin.getConfig().contains("XPShop.slot" + z + ".perkID")) {
								plugin.getConfig().set("XPShop.slot" + z + ".perkID", "XXXX");
							}
							plugin.saveConfig();
						}
					}
					pM.sendMessage(p, "&aGUIShop created successfully!");
					return true;
				}
				return true;
			} else if (args[0].equalsIgnoreCase("getshop")) {
				if (sender.hasPermission("guishop.getshop")) {
					ItemStack[] contents = p.getInventory().getContents();
					plugin.reloadConfig();
					for (int x = 9, z = 0; x < contents.length; x++, z++) {
						if (plugin.getConfig().contains("XPShop.slot" + z + ".itemstack")) {
							ItemStack is = plugin.getConfig().getItemStack("XPShop.slot" + z + ".itemstack");
							contents[x] = is;
						} else {
							String material = plugin.getConfig().getString("XPShop.slot" + z + ".material");
							// Get amount of the item
							int amount = plugin.getConfig().getInt("XPShop.slot" + z + ".amount");

							// Check if material is valid and not air
							if (Material.getMaterial(material) == null || Material.getMaterial(material).equals(Material.AIR)) {
								contents[x] = new ItemStack(Material.AIR, 1);
							} else if (Material.getMaterial(material).equals(Material.EXP_BOTTLE)) {
								contents[x] = new ItemStack(Material.EXP_BOTTLE, 1);
							} else if (Material.getMaterial(material).equals(Material.PAPER)) {
								contents[x] = new ItemStack(Material.PAPER, 1);
							} else {
								ItemStack is = new ItemStack(Material.getMaterial(material), amount);
								String name = plugin.getConfig().getString("XPShop.slot" + z + ".name");

								// Get lore
								List<String> lore = plugin.getConfig().getStringList("XPShop.slot" + z + ".lore");
								// Format lore colors
								for (int y = 0; y < lore.size(); y++) {
									lore.set(y, ChatColor.translateAlternateColorCodes('&', lore.get(y)));
								}

								ItemMeta im = is.getItemMeta();
								im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
								im.setLore(lore);
								is.setItemMeta(im);

								contents[x] = is;
							}
						}
					}
					p.getInventory().setContents(contents);
					pM.sendMessage(p, "&aGUIShop retrieved successfully!");
					return true;
				}
				return true;
			}
			return false;
		} else if (args.length == 0) {
			if (usePerms) {
				if (!sender.hasPermission("playermanager.xpshop")) {
					pM.sendMessage(sender, "&cXPShop is a donator only perk here.");
					return true;
				}
			}
			shopManager.add(p);
			return true;
		}
		return false;
	}
}
