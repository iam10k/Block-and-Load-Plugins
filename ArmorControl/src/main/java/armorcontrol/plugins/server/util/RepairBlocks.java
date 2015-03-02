package armorcontrol.plugins.server.util;

import armorcontrol.plugins.server.main.ArmorControl;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class RepairBlocks {

	private ArmorControl plugin;
	private List<Location> l = new ArrayList<Location>();
	private List<Material> m = new ArrayList<Material>();
	private List<Byte> t = new ArrayList<Byte>();
	private Location location;
	private boolean started = false;

	private int id = -1;

	public RepairBlocks(ArmorControl pl, List<Block> blocks, Location loc) {
		plugin = pl;
		location = loc;
		for (Block block : blocks) {
			l.add(block.getLocation());
			m.add(block.getType());
			t.add(block.getData());
		}

		if (l.size() > 0) {
			reOrderLowestYFirst();
		}

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				runTask();
			}
		}, 160L);
	}

	public void addBlocks(List<Block> blocks) {
		for (Block block : blocks) {
			l.add(block.getLocation());
			m.add(block.getType());
			t.add(block.getData());
		}

		if (l.size() > 0) {
			reOrderLowestYFirst();
		}
	}

	private void reOrderLowestYFirst() {
		ArrayList<Location> tempL = new ArrayList<Location>();
		ArrayList<Material> tempM = new ArrayList<Material>();
		ArrayList<Byte> tempT = new ArrayList<Byte>();

		tempL.add(l.remove(0));
		tempM.add(m.remove(0));
		tempT.add(t.remove(0));

		for (int a = 0; a < l.size(); a++) {
			int nextPos = l.get(a).getBlockY();
			for (int b = 0; b < tempL.size(); b++) {
				int pos = tempL.get(b).getBlockY();
				if (nextPos < pos) {
					tempL.add(b, l.get(a));
					tempM.add(b, m.get(a));
					tempT.add(b, t.get(a));
					break;
				}
				if (b+1 == tempL.size()) {
					tempL.add(l.get(a));
					tempM.add(m.get(a));
					tempT.add(t.get(a));
					break;
				}
			}
		}

		l = tempL;
		m = tempM;
		t = tempT;
	}

	public void runTask() {
		id = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				started = true;
				if (!plugin.useLayerRegen) {
					if (l.size() > 0) {
						l.get(0).getBlock().setType(m.get(0));
						l.get(0).getBlock().setData(t.get(0));
						l.get(0).getWorld().playSound(l.get(0), getSound(m.get(0)), 1F, 0F);
						l.remove(0);
						m.remove(0);
						t.remove(0);
					} else {
						plugin.cancelTask(id);
					}
				} else {
					if (l.size() > 0) {
						int y = l.get(0).getBlockY();
						for (int x = 0; x < l.size(); x++) {
							if (l.get(x).getBlockY() == y) {
								l.get(0).getBlock().setType(m.get(0));
								l.get(0).getBlock().setData(t.get(0));
								l.get(0).getWorld().playSound(l.get(0), getSound(m.get(0)), .5F, 0F);
								l.remove(0);
								m.remove(0);
								t.remove(0);
								x--;
							} else {
								return;
							}
						}
					} else {
						plugin.cancelTask(id);
						remove();
					}
				}
			}
		}, 0L, getTime()).getTaskId();
	}

	private long getTime() {
		if (!plugin.useLayerRegen) {
			if (l.size() > 300) {
				return (long) 1200/l.size();
			} else if (l.size() > 60) {
				return (long) 900/l.size();
			} else if (l.size() > 0) {
				return (long) 300/l.size();
			} else {
				return 5L;
			}
		} else {
			return 140L;
		}
	}

	private Sound getSound(Material m) {
		switch (m) {
			case GRASS:
			case DIRT:
			case MYCEL:
				return Sound.DIG_GRASS;
			case GRAVEL:
				return Sound.DIG_GRAVEL;
			case SAND:
			case SOUL_SAND:
			case CLAY:
			case STAINED_CLAY:
				return Sound.DIG_SAND;
			case SNOW:
			case SNOW_BLOCK:
				return Sound.DIG_SNOW;
			case LOG:
			case LOG_2:
			case WOOD:
				return Sound.DIG_WOOD;
			case WOOL:
				return Sound.DIG_WOOL;
			case GLASS:
			case STAINED_GLASS:
			case STAINED_GLASS_PANE:
				return Sound.GLASS;
			default:
				return Sound.DIG_STONE;
		}
	}

	private void remove() {
		plugin.repairBlocks.remove(this);
	}

	public boolean within5Blocks(Location loc) {
		return loc.distanceSquared(location) <= 25;
	}

	public boolean isStarted() { return started; }

}
