package net.slipcor.pvparena.modules.chestfiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class ChestFiller extends ArenaModule {
	public ChestFiller() {
		super("ChestFiller");
	}
	boolean setup = false;
	
	@Override
	public String version() {
		return "v1.0.2.146";
	}
	
	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("items: " + (String) arena.getArenaConfig().getUnsafe("modules.chestfiller.cfitems"));
		sender.sendMessage("max: " + (Integer) arena.getArenaConfig().getUnsafe("modules.chestfiller.cfmaxitems")
				+ " | " +
				"min: " + (Integer) arena.getArenaConfig().getUnsafe("modules.chestfiller.cfminitems"));
		
	}
	
	@Override
	public void parseStart() {
		if (!setup) {
			if (arena.getArenaConfig().getUnsafe("modules.chestfiller") == null) {
				arena.getArenaConfig().setManually("modules.chestfiller.cfitems", "1");
				arena.getArenaConfig().setManually("modules.chestfiller.cfmaxitems", 5);
				arena.getArenaConfig().setManually("modules.chestfiller.cfminitems", 0);
				arena.getArenaConfig().save();
			}
			if (arena.getArenaConfig().getUnsafe("modules.chestfiller.clear") == null) {
				arena.getArenaConfig().setManually("modules.chestfiller.clear", false);
				arena.getArenaConfig().save();
			}
			setup = true;
		}
		
		String items = null;
		try {
			items = (String) arena.getArenaConfig().getUnsafe("modules.chestfiller.cfitems");
		} catch (Exception e) {
			return;
		}
		
		boolean clear = false;
		try {
			clear = (Boolean) arena.getArenaConfig().getUnsafe("modules.chestfiller.clear");
		} catch (Exception e) {
			return;
		}
		
		int cmax = Integer.parseInt(String.valueOf(arena.getArenaConfig().getUnsafe("modules.chestfiller.cfmaxitems")));
		int cmin = Integer.parseInt(String.valueOf(arena.getArenaConfig().getUnsafe("modules.chestfiller.cfminitems")));
		
		ItemStack[] stacks = StringParser.getItemStacksFromString(items);
		
		if (stacks.length < 1) {
			return;
		}
		

		// ----------------------------------------
		
		if (arena.getArenaConfig().getStringList("inventories", new ArrayList<String>()).size() > 0) {

			List<String> tempList = arena.getArenaConfig()
					.getStringList("inventories", null);

			debug.i("reading inventories");

			for (String s : tempList) {
				Location loc = parseStringToLocation(s);

				fill(loc, clear, cmin, cmax, stacks);
			}

			return;
		}
		debug.i("NO inventories");
		
		int x;
		int y;
		int z;
		List<String> result = new ArrayList<String>();
		
		for (ArenaRegionShape bfRegion : arena.getRegionsByType(RegionType.BATTLE)) {
			PABlockLocation min = bfRegion.getMinimumLocation();
			PABlockLocation max = bfRegion.getMaximumLocation();

			debug.i("min: "+min.toString());
			debug.i("max: "+max.toString());

			World world = Bukkit.getWorld(max.getWorldName());


			if (bfRegion.getShape().equals(RegionShape.CUBOID)) {
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
			} else if (bfRegion.getShape().equals(RegionShape.SPHERIC)) {
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
		}
		
		
		arena.getArenaConfig().setManually("inventories", result);
		arena.getArenaConfig().save();

		// ----------------------------------------
		
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
	
	private void fill(Location loc, boolean clear, int max, int min, ItemStack[] stacks) {
		Chest c = (Chest) loc.getBlock().getState();
		
		if (clear) {
			c.getBlockInventory().clear();
		}
		
		List<ItemStack> adding = new ArrayList<ItemStack>();
		
		Random r = (new Random());
		
		int count = r.nextInt(max-min)+min;
		
		
		int i = 0;
		
		while (i++ < count) {
			int d = r.nextInt(stacks.length);
			adding.add(stacks[d].clone());
		}
		
		for (ItemStack it : adding) {
			c.getInventory().addItem(it);
		}
		c.update();
	}

	private Location saveBlock(World world, int x, int y, int z) {
		Block b = world.getBlockAt(x, y, z);
		if (b.getType() == Material.CHEST) {
			return b.getLocation();
		}
		return null;
	}

	private String parseLocationToString(Location loc) {
		return loc.getWorld().getName() + "," + loc.getBlockX() + ","
				+ loc.getBlockY() + "," + loc.getBlockZ();
	}
}
