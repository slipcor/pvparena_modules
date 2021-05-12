package net.slipcor.pvparena.modules.blockrestore;

import net.slipcor.pvparena.core.Debug;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

class BlockRestoreRunnable extends BukkitRunnable {
    private final Map<Location, ArenaBlock> removals;
    private final Debug debug = new Debug(67);
    private final Blocks module;

    public BlockRestoreRunnable(final Blocks module, Map<Location, ArenaBlock> removals) {
        this.module = module;
        this.removals = removals;
    }

    @Override
    public void run() {
        if(this.removals.isEmpty()) {
            this.module.endRestoring();
            this.cancel();
        } else {
            Map.Entry<Location, ArenaBlock> locationArenaBlockEntry = this.removals.entrySet().iterator().next();
            this.debug.i("location: " + locationArenaBlockEntry.getKey());
            locationArenaBlockEntry.getValue().reset();
            this.module.removeBlock(locationArenaBlockEntry.getKey());
            this.removals.remove(locationArenaBlockEntry.getKey());
        }
    }
}
