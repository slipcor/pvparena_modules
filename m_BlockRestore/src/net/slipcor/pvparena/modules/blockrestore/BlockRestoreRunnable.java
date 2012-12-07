package net.slipcor.pvparena.modules.blockrestore;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Config.CFG;

public class BlockRestoreRunnable implements Runnable {
	private HashMap<Location, ArenaBlock> removals;
	private Arena arena;
	private Debug db = new Debug(67);

	public BlockRestoreRunnable(Arena arena,
			HashMap<Location, ArenaBlock> blocks) {
		this.arena = arena;
		removals = getBlocks();
	}

	@Override
	public void run() {
		PAA_Edit.activeEdits.put("server", arena);
		for (Location l : removals.keySet()) {
			db.i("location: " + l.toString());
			removals.get(l).reset();
			removals.remove(l);
			Blocks.blocks.remove(l);
			Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
					this, arena.getArenaConfig().getInt(CFG.MODULES_BLOCKRESTORE_OFFSET) * 1L);
			return;
		}
		PAA_Edit.activeEdits.remove("server");
	}

	/**
	 * get all blocks that have to be reset (arena wise)
	 * 
	 * @param arena
	 *            the arena to check
	 * @return a map of location=>block to reset
	 */
	private HashMap<Location, ArenaBlock> getBlocks() {
		HashMap<Location, ArenaBlock> result = new HashMap<Location, ArenaBlock>();

		db.i("reading all arenablocks");
		for (Location l : Blocks.blocks.keySet()) {
			if (Blocks.blocks.get(l).arena.equals(arena.getName())
					|| Blocks.blocks.get(l).arena.equals("")) {
				result.put(l, Blocks.blocks.get(l));
				db.i(" - " + l.toString());
			}
		}

		return result;
	}
}
