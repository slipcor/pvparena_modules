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
        for (final Map.Entry<Location, ArenaBlock> locationArenaBlockEntry : removals.entrySet()) {
            debug.i("location: " + locationArenaBlockEntry.getKey());
            locationArenaBlockEntry.getValue().reset();
            removals.remove(locationArenaBlockEntry.getKey());
            Blocks.blocks.remove(locationArenaBlockEntry.getKey());
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
        final HashMap<Location, ArenaBlock> result = new HashMap<>();

        debug.i("reading all arenablocks");
        for (final Location l : Blocks.blocks.keySet()) {
            if (Blocks.blocks.get(l).arena.equals(arena.getName())
                    || Blocks.blocks.get(l).arena != null && Blocks.blocks.get(l).arena.isEmpty()) {
                result.put(l, Blocks.blocks.get(l));
                debug.i(" - " + l);
            }
        }

        return result;
    }

    public void instantlyRestore() {
        for (final Map.Entry<Location, ArenaBlock> locationArenaBlockEntry : removals.entrySet()) {
            debug.i("location: " + locationArenaBlockEntry.getKey());
            locationArenaBlockEntry.getValue().reset();
            removals.remove(locationArenaBlockEntry.getKey());
            Blocks.blocks.remove(locationArenaBlockEntry.getKey());
        }
        removals.clear();
    }
}
