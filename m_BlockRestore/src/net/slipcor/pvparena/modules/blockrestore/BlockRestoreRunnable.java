package net.slipcor.pvparena.modules.blockrestore;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

public class BlockRestoreRunnable implements Runnable {
	private HashMap<Location, ArenaBlock> removals;
	private Arena arena;
	private Debug db = new Debug(67);

	public BlockRestoreRunnable(Arena arena,
			HashMap<Location, ArenaBlock> blocks) {
		this.arena = arena;
		removals = getBlocks(arena);
	}

	@Override
	public void run() {
		arena.edit = true;
		for (Location l : removals.keySet()) {
			db.i("location: " + l.toString());
			removals.get(l).reset();
			removals.remove(l);
			Blocks.blocks.remove(l);
			Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
					this, arena.cfg.getInt("blockRestore.offset") * 1L);
			return;
		}
		arena.edit = false;
	}

	/**
	 * get all blocks that have to be reset (arena wise)
	 * 
	 * @param arena
	 *            the arena to check
	 * @return a map of location=>block to reset
	 */
	private HashMap<Location, ArenaBlock> getBlocks(Arena arena) {
		HashMap<Location, ArenaBlock> result = new HashMap<Location, ArenaBlock>();

		db.i("reading all arenablocks");
		for (Location l : Blocks.blocks.keySet()) {
			if (Blocks.blocks.get(l).arena.equals(arena.name)
					|| Blocks.blocks.get(l).arena.equals("")) {
				result.put(l, Blocks.blocks.get(l));
				db.i(" - " + l.toString());
			}
		}

		return result;
	}
}
