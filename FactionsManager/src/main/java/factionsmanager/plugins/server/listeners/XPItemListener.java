package factionsmanager.plugins.server.listeners;

import factionsmanager.plugins.server.main.FactionsManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class XPItemListener implements Listener {

	private FactionsManager plugin;
	private String i1;
	private String i2;
	private String[] chars = {"0","1","2","3","4","5","6","7","8","9",
							"a","b","c","d","f","g","h","i","j","k","l","m",
							"n","o","p","r","s","t","u","v","w","x","y","z"};

	private ArrayList<String> playersXPItems = new ArrayList<String>();

	public XPItemListener(FactionsManager pl) {
		plugin = pl;
		i1 = color("&aL &5: &aDeposit");
		i2 = color("&cR &5: &cWithdraw");
		addRecipe();
	}

	@EventHandler
	public void onLeftClick(final PlayerInteractEvent e) { //Deposit Method
		//Safe Guard
		if (plugin.stopPlugin) {
			return;
		}

		//Keep code neat
		Player pL = e.getPlayer();

		if (!pL.hasPermission("xpitem.deposit")) {
			return;
		}

		//Check if using right item
		if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		if (e.hasItem() && e.getItem().getType() != Material.getMaterial(plugin.itemName)) {
			return;
		}
		if (pL.getItemInHand().getType() != Material.getMaterial(plugin.itemName)) {
			return;
		}
		if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.FENCE)) {
			return;
		}


		//EXP or Levels
		if (plugin.useLevels) { //Using Levels

			//Used to keep track of amount changing
			int amount = 0;

			//Sneaking or not
			if (pL.isSneaking()) {
				amount = plugin.shiftAmount;
			} else {
				amount = plugin.normalAmount;
			}

			//Check if player has suitable amount
			if (pL.getLevel() < amount) {
				sendError(3, pL, amount);
				return;
			}

			//Create Default Lore
			List<String> lore = new ArrayList<String>();
			lore.add(color(plugin.amountColor + "0 " + plugin.xpHolderColor + plugin.type));
			lore.add(i1);
			lore.add(i2);
			lore.add(genRandID());

			//Load Items ItemMeta
			ItemMeta iM = e.getItem().getItemMeta();

			//Check if item needs name and lore and if yes set default data
			if (iM.getLore() == null || iM.getLore().size() == 0) {
				iM.setLore(lore);
				iM.setDisplayName(color(plugin.amountColor + "0 " + plugin.xpHolderColor + plugin.type));
				pL.getInventory().getItemInHand().setItemMeta(iM);
			}

			//Load current levels
			String temp = ChatColor.stripColor(iM.getLore().get(0).substring(0, iM.getLore().get(0).indexOf(" ")));
			int xp = Integer.parseInt(temp);

			//Check if it won't go over max
			if (xp + amount > plugin.maxAmount && plugin.maxAmount > 0) {
				sendError(4, pL, plugin.maxAmount);
				return;
			}

			//Update amount
			xp += amount;

			//Update Lore and ItemMeta
			lore.set(0, color(plugin.amountColor + xp + " " + plugin.xpHolderColor + plugin.type));
			lore.set(3, genRandID());
			iM.setLore(lore);
			//if (plugin.showEnchant) {
			//	iM.addEnchant(plugin.xpEnch, 0, true);
			//}
			iM.setDisplayName(color(plugin.amountColor + xp + " " + plugin.xpHolderColor + plugin.type));

			//Prevent Duping XP
			pL.getInventory().getItemInHand().setAmount(1);

			//Update Item in Player Inventory
			pL.getInventory().getItemInHand().setItemMeta(iM);

			//Log if needed
			if (plugin.sendToLog) {
				plugin.getLogger().info(pL.getName() + " has added " + amount + " to a XP Holder.");
			}

			//Take XP from player && Deposit msg
			pL.setLevel(pL.getLevel() - amount);
			sendError(1, pL, amount);

		} else { //Using EXP

			//Used to keep track of amount changing
			int amount = 0;

			//Sneaking or not
			if (pL.isSneaking()) {
				amount = plugin.shiftAmount;
			} else {
				amount = plugin.normalAmount;
			}

			//Check if player has suitable amount
			if (pL.getTotalExperience() < amount) {
				sendError(3, pL, amount);
				return;
			}

			//Create Default Lore
			List<String> lore = new ArrayList<String>();
			lore.add(color(plugin.amountColor + "0 " + plugin.xpHolderColor + plugin.type));
			lore.add(i1);
			lore.add(i2);
			lore.add(genRandID());

			//Load Items ItemMeta
			ItemMeta iM = e.getItem().getItemMeta();

			//Check if item needs name and lore and if yes set default data
			if (iM.getLore() == null || iM.getLore().size() == 0) {
				iM.setLore(lore);
				iM.setDisplayName(color(plugin.amountColor + "0 " + plugin.xpHolderColor + plugin.type));
				pL.getInventory().getItemInHand().setItemMeta(iM);
			}

			//Load current levels
			String temp = ChatColor.stripColor(iM.getLore().get(0).substring(0, iM.getLore().get(0).indexOf(" ")));
			int xp = Integer.parseInt(temp);

			//Check if it won't go over max
			if (xp + amount > plugin.maxAmount && plugin.maxAmount > 0) {
				sendError(4, pL, plugin.maxAmount);
				return;
			}

			//Update amount
			xp += amount;

			//Update Lore and ItemMeta
			lore.set(0, color(plugin.amountColor + xp + " " + plugin.xpHolderColor + plugin.type));
			lore.set(3, genRandID());
			iM.setLore(lore);
			//if (plugin.showEnchant) {
				//iM.addEnchant(plugin.xpEnch, 5, true);
			//}
			iM.setDisplayName(color(plugin.amountColor + xp + " " + plugin.xpHolderColor + plugin.type));

			//Prevent Duping XP
			pL.getInventory().getItemInHand().setAmount(1);

			//Update Item in Player Inventory
			pL.getInventory().getItemInHand().setItemMeta(iM);

			//Log if needed
			if (plugin.sendToLog) {
				plugin.getLogger().info(pL.getName() + " has added " + amount + " to a XP Holder.");
			}

			//Take EXP from player && Deposit msg
			int playerXP = pL.getTotalExperience();
			pL.setLevel(0);
			pL.setTotalExperience(0);
			pL.setExp(0);
			pL.giveExp(playerXP - amount);
			sendError(1, pL, amount);
		}
	}

	@EventHandler
	public void onRightClick(final PlayerInteractEvent e) { //Withdraw Method
		//Safe Guard
		if (plugin.stopPlugin) {
			return;
		}

		//Check if using right item
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (e.hasItem() && e.getItem().getType() != Material.getMaterial(plugin.itemName)) {
			return;
		}
		if (e.getPlayer().getItemInHand().getType() != Material.getMaterial(plugin.itemName)) {
			return;
		}
		if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.FENCE)) {
			return;
		}

		if (!e.getPlayer().hasPermission("xpitem.withdraw")) {
			return;
		}

		//Make sure it is cancelled
		e.setCancelled(true);

		//Prevent XP Duping
		if (playersXPItems.contains(e.getPlayer().getName())) {
			return;
		}

		//Add player to using XPItem
		playersXPItems.add(e.getPlayer().getName());

		//Delay incase of enderpearl
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {

				//Can't cancel it enough
				e.setCancelled(true);

				//Use to clean up code
				Player pL = e.getPlayer();

				//EXP or Levels
				if (plugin.useLevels) { //Using Levels

					//Used to keep track of amount changing
					int amount = 0;

					//Sneaking or not
					if (pL.isSneaking()) {
						amount = plugin.shiftAmount;
					} else {
						amount = plugin.normalAmount;
					}

					//Create Default Lore
					List<String> lore = new ArrayList<String>();
					lore.add(color(plugin.amountColor + "0 " + plugin.xpHolderColor + plugin.type));
					lore.add(i1);
					lore.add(i2);
					lore.add(genRandID());

					//Load Items ItemMeta
					ItemMeta iM = e.getItem().getItemMeta();

					//Check if item needs name and lore and if yes set default data
					if (iM.getLore() == null || iM.getLore().size() == 0) {
						iM.setLore(lore);
						iM.setDisplayName(color(plugin.amountColor + "0 " + plugin.xpHolderColor + plugin.type));
						pL.getInventory().getItemInHand().setItemMeta(iM);
					}

					//Load current levels
					String temp = ChatColor.stripColor(iM.getLore().get(0).substring(0, iM.getLore().get(0).indexOf(" ")));
					int xp = Integer.parseInt(temp);

					//Check if item holds enough XP
					if (xp < amount) {
						sendError(3, pL, amount);
						//Done using XPItem
						playersXPItems.remove(pL.getName());
						return;
					}

					//Update amount
					xp -= amount;

					//Update Lore and ItemMeta
					//if (xp == 0) {
					//	if (plugin.showEnchant) {
					//		iM.removeEnchant(Enchantment.getByName("Experience"));
					//	}
					//}
					lore.set(0, color(plugin.amountColor + xp + " " + plugin.xpHolderColor + plugin.type));
					lore.set(3, genRandID());
					iM.setLore(lore);
					iM.setDisplayName(color(plugin.amountColor + xp + " " + plugin.xpHolderColor + plugin.type));

					//Prevent Duping XP
					pL.getInventory().getItemInHand().setAmount(1);

					//Update Item in Player Inventory
					pL.getInventory().getItemInHand().setItemMeta(iM);

					//Log if needed
					if (plugin.sendToLog) {
						plugin.getLogger().info(pL.getName() + " has withdrawn " + amount + " from a XP Holder.");
					}

					//Give XP to player && Withdraw msg
					pL.setLevel(pL.getLevel() + amount);
					sendError(2, pL, amount);

					//Done using XPItem
					playersXPItems.remove(pL.getName());

				} else { //Using EXP

					//Used to keep track of amount changing
					int amount = 0;

					//Sneaking or not
					if (pL.isSneaking()) {
						amount = plugin.shiftAmount;
					} else {
						amount = plugin.normalAmount;
					}

					//Create Default Lore
					List<String> lore = new ArrayList<String>();
					lore.add(color(plugin.amountColor + "0 " + plugin.xpHolderColor + plugin.type));
					lore.add(i1);
					lore.add(i2);
					lore.add(genRandID());

					//Load Items ItemMeta
					ItemMeta iM = e.getItem().getItemMeta();

					//Check if item needs name and lore and if yes set default data
					if (iM.getLore() == null || iM.getLore().size() == 0) {
						iM.setLore(lore);
						iM.setDisplayName(color(plugin.amountColor + "0 " + plugin.xpHolderColor + plugin.type));
						pL.getInventory().getItemInHand().setItemMeta(iM);
					}

					//Load current levels
					String temp = ChatColor.stripColor(iM.getLore().get(0).substring(0, iM.getLore().get(0).indexOf(" ")));
					int xp = Integer.parseInt(temp);

					//Check if item holds enough XP
					if (xp < amount) {
						sendError(3, pL, amount);
						//Done using XPItem
						playersXPItems.remove(pL.getName());
						return;
					}

					//Update amount
					xp -= amount;

					//Update Lore and ItemMeta
					//if (xp == 0) {
					//	if (plugin.showEnchant) {
					//		iM.removeEnchant(Enchantment.getByName("Experience"));
					//	}
					//}
					lore.set(0, color(plugin.amountColor + xp + " " + plugin.xpHolderColor + plugin.type));
					lore.set(3, genRandID());
					iM.setLore(lore);
					iM.setDisplayName(color(plugin.amountColor + xp + " " + plugin.xpHolderColor + plugin.type));

					//Prevent Duping XP
					pL.getInventory().getItemInHand().setAmount(1);

					//Update Item in Player Inventory
					pL.getInventory().getItemInHand().setItemMeta(iM);

					//Log if needed
					if (plugin.sendToLog) {
						plugin.getLogger().info(pL.getName() + " has withdrawn " + amount + " from a XP Holder.");
					}

					//Give EXP to player && Withdraw msg
					int playerXP = pL.getTotalExperience();
					pL.setLevel(0);
					pL.setTotalExperience(0);
					pL.setExp(0);
					pL.giveExp(playerXP + amount);
					sendError(2, pL, amount);

					// Done using XPItem
					playersXPItems.remove(pL.getName());
				}

				//Can't Cancel enough
				e.setCancelled(true);
			}
		}, 1);
	}

	@EventHandler
	public void onEnchant(EnchantItemEvent e) {
		Player player = e.getEnchanter();

		int newLevel = player.getLevel() - e.getExpLevelCost();
		int playerExperience = getTotalExpToLevel(newLevel);

		if (playerExperience < 0) {
			playerExperience = 0;
		}

		e.setExpLevelCost(0);

		player.setTotalExperience(0);
		player.setLevel(0);
		player.setExp(0);

		player.giveExp(playerExperience);
	}

	private void addRecipe() {
		ItemStack is = new ItemStack(Material.EXP_BOTTLE, 1);
		//Create Default Lore
		List<String> lore = new ArrayList<String>();
		lore.add(color(plugin.amountColor + "0 " + plugin.xpHolderColor + plugin.type));
		lore.add(i1);
		lore.add(i2);
		lore.add(genRandID());

		//Load Items ItemMeta
		ItemMeta iM = is.getItemMeta();
		iM.setLore(lore);
		iM.setDisplayName(color(plugin.amountColor + "0 " + plugin.xpHolderColor + plugin.type));

		//Add itemMeta
		is.setItemMeta(iM);

		//Add Recipe
		ShapedRecipe g = new ShapedRecipe(is);
		g.shape("ABA", "BCB", "DBD");
		g.setIngredient('A', Material.DIAMOND);
		g.setIngredient('B', Material.GLASS);
		g.setIngredient('C', Material.CHEST);
		g.setIngredient('D', Material.BOOKSHELF);
		plugin.getServer().addRecipe(g);
	}

	private String color(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	private void sendError(int error, Player pL, int amount) {
		String temp = "";
		if (error == 1) {
			temp = plugin.deposit;
		} else if (error == 2) {
			temp = plugin.withdraw;
		} else if (error == 3) {
			temp = plugin.notEnough;
		} else if (error == 4) {
			temp = plugin.max;
		} else if (error < 1 && error > 4) {
			return;
		}
		if (temp.length() < 1) {
			return;
		}
		temp = temp.replace("{amount}", "" + amount);
		temp = temp.replace("{xptype}", plugin.type);
		pL.sendMessage(color(temp));
	}

	private String genRandID() {
		String id = "";
		int length = 10;
		Random gen = new Random();
		if (gen.nextInt(2) == 0) {
			length += gen.nextInt(2);
		} else {
			length -= gen.nextInt(2);
		}
		for (int x = 0; x < length; x++) {
			String temp = chars[gen.nextInt(chars.length)];
			while (id.contains(temp)) {
				temp = chars[gen.nextInt(chars.length)];
			}
			id += temp;
		}
		return id;
	}

	public int getTotalExpToLevel(int level){
		if (level < 16){
			return 17*level;
		} else if (level < 31){
			return (int) (1.5*level*level -29.5*level+360 );
		} else {
			return (int) (3.5*level*level-151.5*level+2220);
		}
	}
}