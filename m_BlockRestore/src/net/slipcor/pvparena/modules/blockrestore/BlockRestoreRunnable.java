package net.slipcor.pvparena.modules.blockrestore;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

class BlockRestoreRunnable implements Runnable {
    private final Map<Location, ArenaBlock> removals;
    private final Arena arena;
    private final Debug debug = new Debug(67);
    private final Blocks module;

    public BlockRestoreRunnable(final Arena arena,
                                final Blocks module) {
        this.arena = arena;
        removals = getBlocks();
        this.module = module;
    }

    @Override
    public void run() {
        module.restoring = true;
        for (final Location l : removals.keySet()) {
            debug.i("location: " + l);
            removals.get(l).reset();
            removals.remove(l);
            Blocks.blocks.remove(l);
            Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
                    this, arena.getArenaConfig().getInt(CFG.MODULES_BLOCKRESTORE_OFFSET) * 1L);
            return;
        }
        module.restoring = false;
    }

    /**
     * get all blocks that have to be reset
     *
     * @return a map of location=>block to reset
     */
    private HashMap<Location, ArenaBlock> getBlocks() {
        final HashMap<Location, ArenaBlock> result = new HashMap<Location, ArenaBlock>();

        debug.i("reading all arenablocks");
        for (final Location l : Blocks.blocks.keySet()) {
            if (Blocks.blocks.get(l).arena.equals(arena.getName())
                    || "".equals(Blocks.blocks.get(l).arena)) {
                result.put(l, Blocks.blocks.get(l));
                debug.i(" - " + l);
            }
        }

        return result;
    }

    public void instantlyRestore() {
        for (final Location l : removals.keySet()) {
            debug.i("location: " + l);
            removals.get(l).reset();
            removals.remove(l);
            Blocks.blocks.remove(l);
        }
        removals.clear();
    }
}
