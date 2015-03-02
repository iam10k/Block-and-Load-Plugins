package hubmanager.plugins.server.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.BlockFace;

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

	public static BlockFace getDirection(Location base, Location player) {
		int x = player.getBlockX() - base.getBlockX();
		int z =  player.getBlockZ() - base.getBlockZ();

		double theta = Math.atan2(x, z);

		theta += Math.PI;

		double angle = Math.toDegrees(theta);

		if (angle < 0) {
			angle += 360;
		}

		if (angle >= 342 && angle < 18) {
			return BlockFace.NORTH;
		} else if (angle >= 18 && angle < 36) {
			return BlockFace.NORTH_NORTH_WEST;
		} else if (angle >= 36 && angle < 54) {
			return BlockFace.NORTH_WEST;
		} else if (angle >= 54 && angle < 72) {
			return BlockFace.WEST_NORTH_WEST;
		} else if (angle >= 72 && angle < 108) {
			return BlockFace.WEST;
		} else if (angle >= 108 && angle < 126) {
			return BlockFace.WEST_SOUTH_WEST;
		} else if (angle >= 126 && angle < 144) {
			return BlockFace.SOUTH_WEST;
		} else if (angle >= 144 && angle < 162) {
			return BlockFace.SOUTH_SOUTH_WEST;
		} else if (angle >= 162 && angle < 198) {
			return BlockFace.SOUTH;
		} else if (angle >= 198 && angle < 216) {
			return BlockFace.SOUTH_SOUTH_EAST;
		} else if (angle >= 216 && angle < 234) {
			return BlockFace.SOUTH_EAST;
		} else if (angle >= 234 && angle < 252) {
			return BlockFace.EAST_SOUTH_EAST;
		} else if (angle >= 252 && angle < 288) {
			return BlockFace.EAST;
		} else if (angle >= 288 && angle < 306) {
			return BlockFace.EAST_NORTH_EAST;
		} else if (angle >= 306 && angle < 324) {
			return BlockFace.NORTH_EAST;
		} else if (angle >= 324 && angle < 342) {
			return BlockFace.NORTH_NORTH_EAST;
		} else {
			return BlockFace.NORTH;
		}
	}
}
