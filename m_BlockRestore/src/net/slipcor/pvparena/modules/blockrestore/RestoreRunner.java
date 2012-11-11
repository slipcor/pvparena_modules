package net.slipcor.pvparena.modules.blockrestore;

import java.util.HashMap;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.StringParser;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RestoreRunner implements Runnable {
	private Arena arena;
	HashMap<Location, ItemStack[]> chests;
	HashMap<Location, ItemStack[]> furnaces;
	HashMap<Location, ItemStack[]> dispensers;
	private Debug db = new Debug(66);
	
	public RestoreRunner(Arena arena, HashMap<Location, ItemStack[]> chests,
			HashMap<Location, ItemStack[]> furnaces,
			HashMap<Location, ItemStack[]> dispensers) {
		db.i("RestoreRunner contructor: " + arena.getName());
		
		this.arena = arena;
		this.chests = chests;
		this.furnaces = furnaces;
		this.dispensers = dispensers;

		if (chests != null) {
			db.i("chests: " + chests.size());
		}
		if (furnaces != null) {
			db.i("furnaces: " + furnaces.size());
		}
		if (dispensers != null) {
			db.i("dispensers: " + dispensers.size());
		}
	}

	@Override
	public void run() {
		PAA_Edit.activeEdits.put("server", arena);
		World world = Bukkit.getWorld(arena.getWorld());
		for (Location loc : chests.keySet()) {
			if (loc == null) {
				break;
			}
			try {
				db.i("trying to restore chest: " + loc.toString());
				Block b = world.getBlockAt(loc);
				b.setType(Material.CHEST);
				Inventory inv = ((Chest) b.getState())
						.getInventory();
				inv.clear();
				int i = 0;
				for (ItemStack is : chests.get(loc)) {
					db.i("restoring: " + StringParser.getStringFromItemStack(is));
					inv.setItem(i++,is);
				}
				db.i("success!");
				chests.remove(loc);
				Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
						this, arena.getArenaConfig().getInt(CFG.MODULES_BLOCKRESTORE_OFFSET) * 1L);
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (Location loc : dispensers.keySet()) {
			if (loc == null) {
				break;
			}
			try {
				db.i("trying to restore dispenser: " + loc.toString());

				Inventory inv = ((Dispenser) world.getBlockAt(loc).getState())
						.getInventory();
				inv.clear();
				for (ItemStack is : dispensers.get(loc)) {
					if (is != null) {
						inv.addItem(is.clone());
					}
				}
				db.i("success!");
				dispensers.remove(loc);
				Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
						this, arena.getArenaConfig().getInt(CFG.MODULES_BLOCKRESTORE_OFFSET) * 1L);
				return;
			} catch (Exception e) {
				//
			}
		}
		for (Location loc : furnaces.keySet()) {
			if (loc == null) {
				break;
			}
			try {
				db.i("trying to restore furnace: " + loc.toString());
				((Furnace) world.getBlockAt(loc).getState()).getInventory()
						.setContents(RestoreContainer.cloneIS(furnaces.get(loc)));
				db.i("success!");
				furnaces.remove(loc);
				Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
						this, arena.getArenaConfig().getInt(CFG.MODULES_BLOCKRESTORE_OFFSET) * 1L);
				return;
			} catch (Exception e) {
				//
			}
		}
		PAA_Edit.activeEdits.remove("server");
	}

}
