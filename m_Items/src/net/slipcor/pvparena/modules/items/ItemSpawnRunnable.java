package net.slipcor.pvparena.modules.items;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PASpawn;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.SpawnManager;

public class ItemSpawnRunnable implements Runnable {

	private ItemStack[] items;
	private final Set<PASpawn> spawns;
	private final Arena arena;
	
	public ItemSpawnRunnable(Arena a, Items i) {
		arena = a;
		arena.getDebugger().i("ItemSpawnRunnable constructor");
		String sItems = arena.getArenaConfig().getString(CFG.MODULES_ITEMS_ITEMS);

		if (sItems.equals("none")) {
			items = new ItemStack[0];
			spawns = new HashSet<PASpawn>();
			return;
		}
		items = StringParser.getItemStacksFromString(sItems);
		spawns = SpawnManager.getPASpawnsStartingWith(arena, "item");
	}

	@Override
	public void run() {
		arena.getDebugger().i("ItemSpawnRunnable running");
		int i = (new Random()).nextInt(spawns.size());
		for (PASpawn loc : spawns) {
			if (--i <= 0) {
				loc.getLocation().toLocation().getWorld().dropItemNaturally(
						loc.getLocation().toLocation(), items[(new Random()).nextInt(items.length)]);
				return;
			}
		}
	}

}
