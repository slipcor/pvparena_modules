package net.slipcor.pvparena.modules.blockrestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.regions.CuboidRegion;
import net.slipcor.pvparena.regions.SphericRegion;

public class RestoreContainer {
	private Blocks blocks;
	private ArenaRegion bfRegion;

	private HashMap<Location, ItemStack[]> chests = new HashMap<Location, ItemStack[]>();
	private HashMap<Location, ItemStack[]> furnaces = new HashMap<Location, ItemStack[]>();
	private HashMap<Location, ItemStack[]> dispensers = new HashMap<Location, ItemStack[]>();

	public RestoreContainer(Blocks b, ArenaRegion r) {
		blocks = b;
		bfRegion = r;
	}

	private static Debug debug = new Debug(55);

	protected void restoreChests() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
				new RestoreRunner(blocks, chests, furnaces, dispensers));
	}

	protected static ItemStack[] cloneIS(ItemStack[] contents) {
		ItemStack[] result = new ItemStack[contents.length];

		for (int i = 0; i < result.length; i++) {
			if (contents[i] == null) {
				continue;
			}
			ItemStack is = contents[i];
			result[i] = new ItemStack(is.getType(), is.getAmount(),
					is.getDurability());
			result[i].setData(is.getData());

			for (Enchantment ench : is.getEnchantments().keySet()) {
				result[i].addUnsafeEnchantment(ench,
						is.getEnchantments().get(ench));
			}
		}
		debug.i(StringParser.getStringFromItemStacks(result));

		return result;
	}

	public void saveChests() {

		if (blocks.getArena().getArenaConfig().getStringList("inventories", new ArrayList<String>()).size() > 0) {

			List<String> tempList = blocks.getArena().getArenaConfig()
					.getStringList("inventories", null);

			debug.i("reading inventories");

			for (String s : tempList) {
				Location loc = parseStringToLocation(s);

				saveBlock(loc.getWorld(), loc.getBlockX(), loc.getBlockY(),
						loc.getBlockZ());
			}

			return;
		}
		debug.i("NO inventories");

		chests.clear();
		furnaces.clear();
		dispensers.clear();
		int x;
		int y;
		int z;
		
		PABlockLocation min = bfRegion.getShape().getMinimumLocation();
		PABlockLocation max = bfRegion.getShape().getMaximumLocation();

		debug.i("min: "+min.toString());
		debug.i("max: "+max.toString());

		World world = Bukkit.getWorld(max.getWorldName());

		List<String> result = new ArrayList<String>();

		if (bfRegion.getShape() instanceof CuboidRegion) {
			debug.i("cube!");

			for (x = min.getX(); x <= max.getX(); x++) {
				for (y = min.getY(); y <= max.getY(); y++) {
					for (z = min.getZ(); z <= max.getZ(); z++) {
						Location loc = saveBlock(world, x, y, z);
						if (loc == null) {
							continue;
						}
						debug.i("loc not null: " + loc.toString());
						result.add(parseLocationToString(loc));
					}
				}
			}
		} else if (bfRegion.getShape() instanceof SphericRegion) {
			debug.i("sphere!");
			for (x = min.getX(); x <= max.getX(); x++) {
				for (y = min.getY(); y <= max.getY(); y++) {
					for (z = min.getZ(); z <= max.getZ(); z++) {
						Location loc = saveBlock(world, x, y, z);
						if (loc == null) {
							continue;
						}
						debug.i("loc not null: " + loc.toString());
						result.add(parseLocationToString(loc));
					}
				}
			}
		}
		blocks.getArena().getArenaConfig().setManually("inventories", result);
		blocks.getArena().getArenaConfig().save();
	}

	private Location saveBlock(World world, int x, int y, int z) {
		Block b = world.getBlockAt(x, y, z);
		if (b.getType() == Material.CHEST) {
			Chest c = (Chest) b.getState();

			chests.put(b.getLocation(), cloneIS(c.getInventory().getContents()));
			return b.getLocation();
		} else if (b.getType() == Material.FURNACE) {
			Furnace c = (Furnace) b.getState();

			furnaces.put(b.getLocation(), cloneIS(c.getInventory()
					.getContents()));
			return b.getLocation();
		} else if (b.getType() == Material.DISPENSER) {
			Dispenser c = (Dispenser) b.getState();

			dispensers.put(b.getLocation(), cloneIS(c.getInventory()
					.getContents()));
			return b.getLocation();
		}
		return null;
	}

	private Location parseStringToLocation(String loc) {
		// world,x,y,z
		String[] args = loc.split(",");

		World world = Bukkit.getWorld(args[0]);
		int x = Integer.parseInt(args[1]);
		int y = Integer.parseInt(args[2]);
		int z = Integer.parseInt(args[3]);

		return new Location(world, x, y, z);
	}

	private String parseLocationToString(Location loc) {
		return loc.getWorld().getName() + "," + loc.getBlockX() + ","
				+ loc.getBlockY() + "," + loc.getBlockZ();
	}
}
