package net.slipcor.pvparena.modules.items;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.SpawnManager;

public class ItemSpawnRunnable implements Runnable {

	private ItemStack[] items;
	private Items i;
	
	public ItemSpawnRunnable(Items i) {
		this.i = i;
		String sItems = i.getArena().getArenaConfig().getString(CFG.MODULES_ITEMS_ITEMS);

		if (sItems.equals("none")) {
			items = new ItemStack[0];
			return;
		}
		items = StringParser.getItemStacksFromString(sItems);
	}

	@Override
	public void run() {
		int i = -1;
		HashSet<PALocation> spawns = SpawnManager.getSpawns(this.i.getArena(), "item");
		for (PALocation loc : spawns) {
			if (i != -1) {
				i--;
			} else {
				i = (new Random()).nextInt(spawns.size());
			}
			if (i <= 0) {
				Bukkit.getWorld(this.i.getArena().getWorld()).dropItemNaturally(loc.toLocation(), items[(new Random()).nextInt(items.length)]);
				return;
			}
		}
	}

}
