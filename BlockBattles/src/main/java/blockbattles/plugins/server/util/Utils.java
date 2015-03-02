package blockbattles.plugins.server.util;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

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
	 * Check if the material is a sign
	 * @param m is a material
	 * @return true if it is a sign
	 */
	public static boolean isSign(Material m) {
		switch (m) {
			case SIGN:
			case WALL_SIGN:
			case SIGN_POST:
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

	/**
	 * Give the player the lobby items
	 * @param player is not null
	 */
	public static void playerInLobby(Player player) {
		ItemStack is = new ItemStack(Material.BEDROCK, 1);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GOLD + "Select Class");
		is.setItemMeta(im);

		player.setFoodLevel(20);
		player.setHealth(player.getMaxHealth());
		player.setFireTicks(0);

		PlayerInventory inv = player.getInventory();
		inv.setHelmet(new ItemStack(Material.AIR, 1));
		inv.setChestplate(new ItemStack(Material.AIR, 1));
		inv.setLeggings(new ItemStack(Material.AIR, 1));
		inv.setBoots(new ItemStack(Material.AIR, 1));
		inv.clear();
		inv.setItem(0, is);

		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
	}

	public static boolean sameBlock(Location bLoc, Location loc) {
		if (bLoc == null || loc == null) { return false; }
		return (bLoc.getBlockX() == loc.getBlockX() && bLoc.getBlockY() == loc.getBlockY() && bLoc.getBlockZ() == loc.getBlockZ());
	}
}
