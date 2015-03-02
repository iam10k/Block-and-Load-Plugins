package armorcontrol.plugins.server.util;

import armorcontrol.plugins.server.main.ArmorControl;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import playermanager.plugins.server.player.ACPlayer;
import playermanager.plugins.server.player.ServerPlayer;

public class Utils {

	/**
	 * Convert a string into a location.
	 * @param strLocation - The location as a string.
	 * @param containsYawAndPitch - The location contains a yaw and a pitch.
	 * @return A location with data from the string.
	 */
	public static Location convertStringToLocation(String strLocation, boolean containsYawAndPitch) {
		if (strLocation == null) return null;
		try {
			if (strLocation.contains(" ")) {
				String[] locSplit = strLocation.split(" ");
				World w = Bukkit.getWorld(locSplit[0]);
				if (w == null) {
					w = Bukkit.createWorld(WorldCreator.name(locSplit[0]));
				}
				double x = Double.parseDouble(locSplit[1]), y = Double.parseDouble(locSplit[2]), z = Double.parseDouble(locSplit[3]);
				float yaw = 0F, pitch = 0F;
				if (containsYawAndPitch) {
					yaw = Float.parseFloat(locSplit[4]);
					pitch = Float.parseFloat(locSplit[5]);
				}
				return new Location(w, x, y, z, yaw, pitch);
			}
		} catch (Exception ex) {
		}
		return null;
	}

	/**
	 * Convert a location into a string.
	 * @param location - The string in the location format: {world} {x} {y} {z} [{yaw} {pitch}]
	 * @param roundedValues - If the x, y and z values should be rounded.
	 * @param containsYawAndPitch - The location contains a yaw and a pitch.
	 * @return A string with the data of a location.
	 */
	public static String convertLocationToString(Location location, boolean roundedValues, boolean containsYawAndPitch) {
		if (location == null) {
			return "world " + 0D + " " + 50D + " " + 0D + (containsYawAndPitch ? " " + 0F + " " + 0F : "");
		}
		String strLoc = (location.getWorld() != null ? location.getWorld().getName() : "world") + " ";
		if (roundedValues)  {
			strLoc += (double) ((int) location.getX()) + " " + (double) ((int) location.getY()) + " " + (double) ((int) location.getZ());
		} else {
			strLoc += location.getX() + " " + location.getY() + " " + location.getZ();
		}
		if (containsYawAndPitch) {
			strLoc += " " + location.getYaw() + " " + location.getPitch();
		}
		return strLoc;
	}

	/**
	 * Check if material is tool or weapon
	 * @param m is a material
	 * @return true if m is tool or armor
	 */
	public static boolean isTool(Material m) {
		switch (m) {
			case WOOD_AXE:
			case WOOD_PICKAXE:
			case WOOD_SPADE:
			case WOOD_SWORD:
			case STONE_AXE:
			case STONE_PICKAXE:
			case STONE_SPADE:
			case STONE_SWORD:
			case IRON_AXE:
			case IRON_PICKAXE:
			case IRON_SPADE:
			case IRON_SWORD:
			case GOLD_AXE:
			case GOLD_PICKAXE:
			case GOLD_SPADE:
			case GOLD_SWORD:
			case DIAMOND_AXE:
			case DIAMOND_PICKAXE:
			case DIAMOND_SPADE:
			case DIAMOND_SWORD:
			case BOW:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Check if material is armor
	 * @param m is a material
	 * @return true if m is armor
	 */
	public static boolean isArmor(Material m) {
		switch (m) {
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
			case CHAINMAIL_HELMET:
			case CHAINMAIL_CHESTPLATE:
			case CHAINMAIL_LEGGINGS:
			case CHAINMAIL_BOOTS:
			case IRON_HELMET:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_BOOTS:
			case GOLD_HELMET:
			case GOLD_CHESTPLATE:
			case GOLD_LEGGINGS:
			case GOLD_BOOTS:
			case DIAMOND_HELMET:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Check if material is a sword
	 * @param m is a material
	 * @return true if m is sword
	 */
	public static boolean isSword(Material m) {
		switch (m) {
			case WOOD_SWORD:
			case STONE_SWORD:
			case IRON_SWORD:
			case GOLD_SWORD:
			case DIAMOND_SWORD:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Check if material is food
	 * @param m is a material
	 * @return true if m is food
	 */
	public static boolean isFood(Material m) {
		switch (m) {
			case BREAD:
			case CARROT_ITEM:
			case BAKED_POTATO:
			case POTATO_ITEM:
			case POISONOUS_POTATO:
			case GOLDEN_CARROT:
			case PUMPKIN_PIE:
			case COOKIE:
			case MELON:
			case MUSHROOM_SOUP:
			case RAW_CHICKEN:
			case COOKED_CHICKEN:
			case RAW_BEEF:
			case COOKED_BEEF:
			case RAW_FISH:
			case COOKED_FISH:
			case PORK:
			case GRILLED_PORK:
			case GOLDEN_APPLE:
			case ROTTEN_FLESH:
			case SPIDER_EYE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Check if material is allowed to be on map
	 * @param m is a material
	 * @return true if m is allowed
	 */
	public static boolean isOther(Material m) {
		switch (m) {
			case OBSIDIAN:
			case TNT:
			case SNOW_BALL:
			case WOOD_PLATE:
			case STONE_PLATE:
			case IRON_PLATE:
			case GOLD_PLATE:
			case ARROW:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Check if material is a pressure plate
	 * @param m is a material
	 * @return true if m is a pressure plate
	 */
	public static boolean isPressurePlate(Material m) {
		switch (m) {
			case WOOD_PLATE:
			case STONE_PLATE:
			case IRON_PLATE:
			case GOLD_PLATE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Check if material is a pressure plate
	 * @param m is a material
	 * @return 1-Wood 2-Stone 3-Iron 4-Gold 5-Other
	 */
	public static int pressurePlateLevel(Material m) {
		switch (m) {
			case WOOD_PLATE:
				return 1;
			case STONE_PLATE:
				return 2;
			case IRON_PLATE:
				return 3;
			case GOLD_PLATE:
				return 4;
			default:
				return 5;
		}
	}

	public static boolean isSpawnable(Material m) {
		return (isOther(m) || isTool(m) || isArmor(m) || isFood(m)) &&
				armorType(m) != 5 && !m.equals(Material.GOLD_SWORD);
	}

	public static boolean isDropable(Material m) {
		return (isOther(m) || isArmor(m) || isFood(m)) && !isTool(m) &&
				armorType(m) != 5 && !m.equals(Material.GOLD_SWORD);
	}

	/**
	 * Get tool type such as diamond or gold
	 * @param m is tool
	 * @return 1-Diamond 2-Iron 3-Stone 4-Gold 5-Wood 6-Other
	 */
	public static int toolType(Material m) {
		switch (m) {
			case DIAMOND_AXE:
			case DIAMOND_HOE:
			case DIAMOND_PICKAXE:
			case DIAMOND_SPADE:
			case DIAMOND_SWORD:
				return 1;
			case IRON_AXE:
			case IRON_HOE:
			case IRON_PICKAXE:
			case IRON_SPADE:
			case IRON_SWORD:
				return 2;
			case STONE_AXE:
			case STONE_HOE:
			case STONE_PICKAXE:
			case STONE_SPADE:
			case STONE_SWORD:
				return 3;
			case GOLD_AXE:
			case GOLD_HOE:
			case GOLD_PICKAXE:
			case GOLD_SPADE:
			case GOLD_SWORD:
				return 4;
			case WOOD_AXE:
			case WOOD_HOE:
			case WOOD_PICKAXE:
			case WOOD_SPADE:
			case WOOD_SWORD:
				return 5;
			default:
				return 6;
		}
	}

	/**
	 * Get armor type such as diamond or gold
	 * @param m is armor
	 * @return 1-Diamond 2-Iron 3-Chainmail 3-Gold 5-Leather 6-Other
	 */
	public static int armorType(Material m) {
		switch (m) {
			case DIAMOND_BOOTS:
			case DIAMOND_LEGGINGS:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_HELMET:
				return 1;
			case IRON_HELMET:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_BOOTS:
				return 2;
			case CHAINMAIL_HELMET:
			case CHAINMAIL_CHESTPLATE:
			case CHAINMAIL_LEGGINGS:
			case CHAINMAIL_BOOTS:
				return 3;
			case GOLD_HELMET:
			case GOLD_CHESTPLATE:
			case GOLD_LEGGINGS:
			case GOLD_BOOTS:
				return 4;
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
				return 5;
			default:
				return 6;
		}
	}

	/**
	 * Get the piece of armor
	 * @param m is armor
	 * @return 1-Boots 2-Leggings 3-Chestplate 4-Helmet 5-Other
	 */
	public static int armorSlot(Material m) {
		switch (m) {
			case DIAMOND_BOOTS:
			case IRON_BOOTS:
			case GOLD_BOOTS:
			case LEATHER_BOOTS:
			case CHAINMAIL_BOOTS:
				return 1;
			case DIAMOND_LEGGINGS:
			case GOLD_LEGGINGS:
			case IRON_LEGGINGS:
			case CHAINMAIL_LEGGINGS:
			case LEATHER_LEGGINGS:
				return 2;
			case DIAMOND_CHESTPLATE:
			case GOLD_CHESTPLATE:
			case IRON_CHESTPLATE:
			case LEATHER_CHESTPLATE:
			case CHAINMAIL_CHESTPLATE:
				return 3;
			case CHAINMAIL_HELMET:
			case DIAMOND_HELMET:
			case GOLD_HELMET:
			case LEATHER_HELMET:
			case IRON_HELMET:
				return 4;
			default:
				return 5;
		}
	}

	/**
	 * Select random slot in chest that is empty
	 * @param contents is a chest inventory
	 * @return int of empty slot in inventory
	 */
	public static int randomChestSlot(ItemStack[] contents) {
		int invSlot = (int)Math.floor(contents.length * Math.random());
		while (contents[invSlot] != null) {
			invSlot = (int)Math.floor(contents.length * Math.random());
		}
		return invSlot;
	}

	/**
	 * Convert time to playtime
	 * @param time is seconds
	 * @return Time converted to string
	 */
	public static String convertPlaytimeSeconds(int time) {
		String s = "";
		int years = time / 31560000;
		if (years == 1) { s += years + " Year  "; }
		else if (years > 1) { s += years + " Years  "; }

		int days = (time - (years * 31560000)) / 86400;
		if (days == 1) { s += days + " Day  "; }
		else if (days > 1) { s += days + " Days  "; }

		int hours = (time - (years * 31560000) - (days * 86400)) / 3600;
		if (hours == 1) { s += hours + " Hour  "; }
		else if (hours > 1) { s += hours + " Hours  "; }

		int minutes = (time - (years * 31560000) - (days * 86400) - (hours * 3600)) / 60;
		if (minutes == 1) { s += minutes + " Minute  "; }
		else if (minutes > 1) { s += minutes + " Minutes  "; }

		int seconds = (time - (years * 31560000) - (days * 86400) - (hours * 3600) - (minutes * 60));
		if (seconds == 1) { s += seconds + " Second"; }
		else if (seconds > 1) { s += seconds + " Seconds"; }
		return s;
	}

	public static void updateArmorInventory(Player p, ArmorControl plugin) {
		PlayerInventory inv = p.getInventory();
		ItemStack[] contents = inv.getContents();
		ACPlayer acPlayer = plugin.pM.getACPlayer(p);

		for (int x = 0; x < contents.length; x++) {
			ItemStack is = contents[x];
			if(is == null) {
				continue;
			}

			Material m = is.getType();

			if (Utils.isArmor(m)) {
				// Remove from inventory even if it will not be put on
				contents[x] = null;

				// Get armor type string
				String armorMaterial = "";
				if (Utils.armorType(m) == 1) { armorMaterial = "&bDiamond"; }
				if (Utils.armorType(m) == 2) { armorMaterial = "&8Iron"; }
				if (Utils.armorType(m) == 3) { armorMaterial = "&7Chainmail"; }
				if (Utils.armorType(m) == 4) { armorMaterial = "&6Gold"; }
				if (Utils.armorType(m) == 5) { armorMaterial = "&fLeather"; }

				// Determine if player needs the armor put on
				if (Utils.armorSlot(m) == 4) {
					int h = Utils.armorType(inv.getHelmet().getType());
					if (h > Utils.armorType(m)) {
						inv.setHelmet(is);
						plugin.pM.sendMessage(p, "  " + armorMaterial + " Helmet auto equipped.");
						if (Utils.armorType(m) == 1) {
							acPlayer.addCredits(plugin.xpObtainingArmorPiece);
						}
					}
				} else if (Utils.armorSlot(m) == 3) {
					int c = Utils.armorType(inv.getChestplate().getType());
					if (c > Utils.armorType(m)) {
						inv.setChestplate(is);
						plugin.pM.sendMessage(p, "  " + armorMaterial + " Chestplate auto equipped.");
						if (Utils.armorType(m) == 1) {
							acPlayer.addCredits(plugin.xpObtainingArmorPiece);
						}
					}
				} else if (Utils.armorSlot(m) == 2) {
					int l = Utils.armorType(inv.getLeggings().getType());
					if (l > Utils.armorType(m)) {
						inv.setLeggings(is);
						plugin.pM.sendMessage(p, "  " + armorMaterial + " Leggings auto equipped.");
						if (Utils.armorType(m) == 1) {
							acPlayer.addCredits(plugin.xpObtainingArmorPiece);
						}
					}
				} else if (Utils.armorSlot(m) == 1) {
					int b = Utils.armorType(inv.getBoots().getType());
					if (b > Utils.armorType(m)) {
						inv.setBoots(is);
						plugin.pM.sendMessage(p, "  " + armorMaterial + " Boots auto equipped.");
						if (Utils.armorType(m) == 1) {
							acPlayer.addCredits(plugin.xpObtainingArmorPiece);
						}
					}
				}
			}
		}

		// Check if player is the juggernaut now
		if (plugin.juggernaut == null) {
			if (inv.getHelmet() != null && Utils.armorType(inv.getHelmet().getType()) == 1) {
				if (inv.getChestplate() != null && Utils.armorType(inv.getChestplate().getType()) == 1) {
					if (inv.getLeggings() != null && Utils.armorType(inv.getLeggings().getType()) == 1) {
						if (inv.getBoots() != null && Utils.armorType(inv.getBoots().getType()) == 1) {
							plugin.newJuggernaut(p);
						}
					}
				}
			}
		}

		inv.setContents(contents);
	}

	public static void updateInventory(Player p, ArmorControl plugin) {
		PlayerInventory inv = p.getInventory();
		ItemStack[] contents = inv.getContents();
		ItemStack[] newContents = new ItemStack[contents.length];

		for (int x = 0; x < contents.length; x++) {
			ItemStack is = contents[x];
			if(is == null) {
				continue;
			}

			Material m = is.getType();

			// Add compass and sword by default
			if (x == 8) {
				newContents[8] = is;
				continue;
			} else if (x == 0) {
				newContents[0] = is;
				continue;
			}

			// Check if sword needs to be updated
			if (Utils.isSword(m)) {
				// Get sword type string
				String toolMaterial = "";
				if (Utils.toolType(m) == 1) { toolMaterial = "&bDiamond"; }
				if (Utils.toolType(m) == 2) { toolMaterial = "&8Iron"; }
				if (Utils.toolType(m) == 3) { toolMaterial = "&7Stone"; }
				if (Utils.toolType(m) == 4) { toolMaterial = "&6Gold"; }
				if (Utils.toolType(m) == 5) { toolMaterial = "&fWood"; }

				// Determine if player needs the sword put on
				int s = Utils.toolType(contents[0].getType());
				if (s > Utils.toolType(m)) {
					// Place in new inventory
					newContents[0] = is;
					plugin.pM.sendMessage(p, "  " + toolMaterial + " Sword auto equipped.");
				}
				continue;
			}

			/*// Check Bow
			if (m.equals(Material.BOW)) {
				if (newContents[2] == null || !newContents[2].getType().equals(Material.BOW)) {
					// Place in new inventory
					newContents[2] = is;
				}
			}*/

			// Check Rotten Flesh
			if (m.equals(Material.ROTTEN_FLESH)) {
				if (newContents[1] != null && newContents[1].getType().equals(Material.ROTTEN_FLESH)) {
					ItemStack flesh = newContents[1];
					if (flesh.getAmount() + is.getAmount() > 32) {
						flesh.setAmount(32);
						newContents[1] = flesh;
					} else {
						flesh.setAmount(flesh.getAmount() + is.getAmount());
						newContents[1] = flesh;
					}
				} else {
					newContents[1] = is;
					if (is.getAmount() > 32) {
						newContents[1].setAmount(32);
					}
				}
			}

			// Check Snow Balls
			if (m.equals(Material.SNOW_BALL)) {
				if (newContents[2] != null && newContents[2].getType().equals(Material.SNOW_BALL)) {
					ItemStack snowBalls = newContents[2];
					if (snowBalls.getAmount() + is.getAmount() > 64) {
						snowBalls.setAmount(64);
						newContents[2] = snowBalls;
					} else {
						snowBalls.setAmount(snowBalls.getAmount() + is.getAmount());
						newContents[2] = snowBalls;
					}
				} else {
					newContents[2] = is;
				}
			}

			// Check TNT
			if (m.equals(Material.TNT)) {
				if (newContents[3] != null && newContents[3].getType().equals(Material.TNT)) {
					ItemStack tnt = newContents[3];
					if (tnt.getAmount() + is.getAmount() > 64) {
						tnt.setAmount(64);
						newContents[3] = tnt;
					} else {
						tnt.setAmount(tnt.getAmount() + is.getAmount());
						newContents[3] = tnt;
					}
				} else {
					newContents[3] = is;
				}
			}

			// Check Obsidian
			if (m.equals(Material.OBSIDIAN)) {
				if (newContents[4] != null && newContents[4].getType().equals(Material.OBSIDIAN)) {
					ItemStack obsisian = newContents[4];
					if (obsisian.getAmount() + is.getAmount() > 64) {
						obsisian.setAmount(64);
						newContents[4] = obsisian;
					} else {
						obsisian.setAmount(obsisian.getAmount() + is.getAmount());
						newContents[4] = obsisian;
					}
				} else {
					newContents[4] = is;
				}
			}

			// Check Wood Plate
			if (m.equals(Material.STONE_PLATE)) {
				if (newContents[5] != null && newContents[5].getType().equals(Material.STONE_PLATE)) {
					ItemStack arrow = newContents[5];
					if (arrow.getAmount() + is.getAmount() > 64) {
						arrow.setAmount(64);
						newContents[5] = arrow;
					} else {
						arrow.setAmount(arrow.getAmount() + is.getAmount());
						newContents[5] = arrow;
					}
				} else {
					newContents[5] = is;
				}
			}

			// Check Wood Plate
			if (m.equals(Material.IRON_PLATE)) {
				if (newContents[6] != null && newContents[6].getType().equals(Material.IRON_PLATE)) {
					ItemStack arrow = newContents[6];
					if (arrow.getAmount() + is.getAmount() > 64) {
						arrow.setAmount(64);
						newContents[6] = arrow;
					} else {
						arrow.setAmount(arrow.getAmount() + is.getAmount());
						newContents[6] = arrow;
					}
				} else {
					newContents[6] = is;
				}
			}

			// Check Wood Plate
			if (m.equals(Material.GOLD_PLATE)) {
				if (newContents[7] != null && newContents[7].getType().equals(Material.GOLD_PLATE)) {
					ItemStack arrow = newContents[7];
					if (arrow.getAmount() + is.getAmount() > 64) {
						arrow.setAmount(64);
						newContents[7] = arrow;
					} else {
						arrow.setAmount(arrow.getAmount() + is.getAmount());
						newContents[7] = arrow;
					}
				} else {
					newContents[7] = is;
				}
			}
		}

		inv.setContents(newContents);
	}

	public static void playerJoin(Player p, ArmorControl plugin) {
		PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setItem(8, new ItemStack(Material.COMPASS, 1));
		inv.setItem(0, new ItemStack(Material.GOLD_SWORD, 1));
		inv.setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
		inv.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
		inv.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS, 1));
		inv.setBoots(new ItemStack(Material.LEATHER_BOOTS, 1));
		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
		p.setFireTicks(0);
		p.setTotalExperience(0);
		p.setLevel(20);

		ServerPlayer serverPlayer = plugin.pM.getServerPlayer(p);
		for (String perk : plugin.getConfig().getStringList("Perks.list")) {
			if (serverPlayer.getBoughtPerks().contains(perk)) {
				int slot = plugin.getConfig().getInt("Perks." + perk + ".slot");
				if (plugin.getConfig().contains("XPShop.slot" + slot)) {
					// Get material type
					String material = plugin.getConfig().getString("XPShop.slot" + slot + ".material");
					// Get amount of the item
					int amount = plugin.getConfig().getInt("XPShop.slot" + slot + ".amount");

					// Check if material is valid and not air
					if (Material.getMaterial(material) == null || Material.getMaterial(material).equals(Material.AIR)) {
						continue;
					}
					// Create the itemstack and add to inventory
					ItemStack is = new ItemStack(Material.getMaterial(material), amount);
					inv.addItem(is);

					// If bow add arrow too
					if (Material.getMaterial(material).equals(Material.BOW)) {
						inv.addItem(new ItemStack(Material.ARROW, 16));
					}
				}
			}
		}

		updateInventory(p, plugin);
	}

	public static void teleportPlayerUp(Player player) {
		Location orgLoc = player.getLocation();
		for (int y = orgLoc.getBlockY() + 1; y < 100; y++) {
			Block block1 = orgLoc.getWorld().getBlockAt(orgLoc.getBlockX(), y, orgLoc.getBlockZ());
			Block block2 = orgLoc.getWorld().getBlockAt(orgLoc.getBlockX(), y + 1, orgLoc.getBlockZ());
			if (block1 != null &&block1.getType().equals(Material.AIR)) {
				if (block2 != null && block2.getType().equals(Material.AIR)) {
					player.teleport(block1.getLocation());
					return;
				}
			}
		}
	}
}
