package net.slipcor.pvparena.modules.chestfiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public class ChestFiller extends ArenaModule {
	public ChestFiller() {
		super("ChestFiller");
	}
	boolean setup = false;
	
	@Override
	public String version() {
		return "v0.10.3.15";
	}
	
	@Override
	public void parseStart() {
		if (!setup) {
			if (arena.getArenaConfig().getUnsafe("modules.chestfiller") == null) {
				arena.getArenaConfig().setManually("modules.chestfiller.cfitems", "1,2,3,4,5,6,7,8,9");
				arena.getArenaConfig().setManually("modules.chestfiller.cfmaxitems", 5);
				arena.getArenaConfig().setManually("modules.chestfiller.cfminitems", 0);
				arena.getArenaConfig().save();
			}
			if (arena.getArenaConfig().getUnsafe("modules.chestfiller.clear") == null) {
				arena.getArenaConfig().setManually("modules.chestfiller.clear", false);
				arena.getArenaConfig().save();
			}
			
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
		
		int max = Integer.parseInt(String.valueOf(arena.getArenaConfig().getUnsafe("modules.chestfiller.cfmaxitems")));
		int min = Integer.parseInt(String.valueOf(arena.getArenaConfig().getUnsafe("modules.chestfiller.cfminitems")));
		
		ItemStack[] stacks = StringParser.getItemStacksFromString(items);
		
		if (stacks.length < 1) {
			return;
		}
		
		for (ArenaRegionShape bf : arena.getRegionsByType(RegionType.BATTLE)) {
			World w = bf.getWorld();

			for (int x = bf.getMinimumLocation().getX(); x <= bf.getMaximumLocation().getX(); x++) {
				for (int y = bf.getMinimumLocation().getY(); y <= bf.getMaximumLocation().getY(); y++) {
					for (int z = bf.getMinimumLocation().getZ(); z <= bf.getMaximumLocation().getZ(); z++) {
			
						if (w.getBlockAt(x, y, z).getType() == Material.CHEST) {
							Chest c = (Chest) w.getBlockAt(x, y, z).getState();
							
							if (clear) {
								c.getBlockInventory().clear();
							}
							
							List<ItemStack> adding = new ArrayList<ItemStack>();
							
							Random r = (new Random());
							
							int count = r.nextInt(max-min)+min;
							
							
							int i = 0;
							
							while (i++ < count) {
								int d = r.nextInt();
								adding.add(stacks[d%stacks.length].clone());
							}
							
							for (ItemStack it : adding) {
								c.getInventory().addItem(it);
							}
							c.update();
						}
					}
				}
			}
		}
	}
}
