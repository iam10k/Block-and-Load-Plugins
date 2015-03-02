package playermanager.plugins.server.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Openable;
import org.bukkit.util.BlockIterator;
import playermanager.plugins.server.main.PlayerManager;

public class BlockChecker {


	private PlayerManager plugin;

	public BlockChecker(PlayerManager pl) {
		plugin = pl;
	}

    public boolean isFacing(Player pl, Block b)  {
        return isFacing(pl.getLocation(), b);
    }

    public boolean isFacing(Location loc, Block b) {
		Block c;
		BlockIterator bIt = new BlockIterator(loc, 0, 5);
		while (bIt.hasNext()) {
			c = bIt.next();
			if (c.equals(b)) {
				return true; //Success
			}
			if (plugin.getTransparentBlocks().contains(c.getType())) {
				continue;
			} else {
				if ((b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) && (getChestNextTo(b) == c)) {
					return true;
				}
				if (!isDoor(c.getType())) {
					break;
				}

				Block bottomDoor = c;
				if (isTopHalf(c.getLocation())) {
					bottomDoor = loc.getWorld().getBlockAt(c.getLocation().getBlockX(),
							c.getLocation().getBlockY()-1, c.getLocation().getBlockY());
				}

				Openable door = (Openable) bottomDoor.getState().getData();
				if (door.isOpen()) {
					break;
				}
			}
        }
        return false;
    }

    public boolean canSee(Player p, Block b) {
        Location pLoc = p.getLocation().clone();
        Location bLoc = b.getLocation().clone();


        pLoc.add(0.0D, 1.62D, 0.0D);
        if (isFacing(pLoc, b)) {
            return true;
        }
        Location corner = lookAt(pLoc, bLoc);
        if (isFacing(corner, b)) {
            return true;
        }
        bLoc = b.getLocation().clone().add(1.0D, 0.0D, 0.0D);
        corner = lookAt(pLoc, bLoc);
        if (isFacing(corner, b)) {
            return true;
        }
        bLoc = b.getLocation().clone().add(0.0D, 1.0D, 0.0D);
        corner = lookAt(pLoc, bLoc);
        if (isFacing(corner, b)) {
            return true;
        }
        bLoc = b.getLocation().clone().add(0.0D, 0.0D, 1.0D);
        corner = lookAt(pLoc, bLoc);
        if (isFacing(corner, b)) {
            return true;
        }
        bLoc = b.getLocation().clone().add(1.0D, 1.0D, 0.0D);
        corner = lookAt(pLoc, bLoc);
        if (isFacing(corner, b)) {
            return true;
        }
        bLoc = b.getLocation().clone().add(1.0D, 0.0D, 1.0D);
        corner = lookAt(pLoc, bLoc);
        if (isFacing(corner, b)) {
            return true;
        }
        bLoc = b.getLocation().clone().add(0.0D, 1.0D, 1.0D);
        corner = lookAt(pLoc, bLoc);
        if (isFacing(corner, b)) {
            return true;
        }
        bLoc = b.getLocation().clone().add(1.0D, 1.0D, 1.0D);
        corner = lookAt(pLoc, bLoc);
        if (isFacing(corner, b)) {
            return true;
        }
        return false;
    }

    public Location lookAt(Location loc, Location lookat) {
        loc = loc.clone();


        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();
        if (dx != 0.0D) {
            if (dx < 0.0D) {
                loc.setYaw(4.712389F);
            } else {
                loc.setYaw(1.570796F);
            }
            loc.setYaw(loc.getYaw() - (float)Math.atan(dz / dx));
        } else if (dz < 0.0D) {
            loc.setYaw(3.141593F);
        }
        double dxz = Math.sqrt(Math.pow(dx, 2.0D) + Math.pow(dz, 2.0D));

        float pitch = (float)-Math.atan(dy / dxz);



        loc.setYaw(-loc.getYaw() * 180.0F / 3.141593F + 360.0F);

        loc.setPitch(pitch * 180.0F / 3.141593F);

        return loc;
    }

    private Block getChestNextTo(Block b) {
        Block[] c = new Block[4];
        c[0] = b.getLocation().add(1.0D, 0.0D, 0.0D).getBlock();
        c[1] = b.getLocation().add(-1.0D, 0.0D, 0.0D).getBlock();
        c[2] = b.getLocation().add(0.0D, 0.0D, 1.0D).getBlock();
        c[3] = b.getLocation().add(0.0D, 0.0D, -1.0D).getBlock();
        for (Block d : c) {
            if (d.getType().equals(Material.CHEST) || d.getType().equals(Material.TRAPPED_CHEST)) {
                return d;
            }
        }
        return null;
    }

	private boolean isTopHalf(Location loc) {
		Block b = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY()-1, loc.getBlockY());
		if (b != null && isDoor(b.getType())) {
			return true;
		}
		return false;
	}

	private boolean isDoor(Material m) {
		switch (m) {
			case IRON_DOOR:
			case IRON_DOOR_BLOCK:
			case WOODEN_DOOR:
			case WOOD_DOOR:
				return true;
			default:
				return false;
		}
	}
}

