package net.slipcor.pvparena.modules.blockrestore;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;

class ArenaBlock {
    private final Debug debug = new Debug(9);

    public String arena;
    private final PABlockLocation location;
    private final BlockData blockData;
    private final String[] lines;

    /**
     * create an arena block instance (blockdestroy)
     *
     * @param block the block to copy
     */
    public ArenaBlock(final Block block) {
        location = new PABlockLocation(block.getLocation());
        blockData = block.getBlockData();

        debug.i("creating arena block:");
        debug.i("loc: " + location + "; mat: " + blockData.getMaterial());

        try {
            arena = ArenaManager.getArenaByRegionLocation(location).getName();
        } catch (final Exception e) {
            arena = "";
        }
        if (block.getState() instanceof Sign) {
            lines = ((Sign) block.getState()).getLines();
        } else {
            lines = null;
        }
    }

    /**
     * create an arena block instance (blockplace)
     *
     * @param block the block to copy
     * @param type  the Material to override (the Material before placing)
     */
    public ArenaBlock(final Block block, final Material type) {
        location = new PABlockLocation(block.getLocation());
        try {
            arena = ArenaManager.getArenaByRegionLocation(location).getName();
        } catch (final Exception e) {
            arena = "";
        }
        blockData = Bukkit.createBlockData(type);
        lines = null;

        debug.i("creating arena block:");
        debug.i("loc: " + location + "; mat: " + blockData.getMaterial());

    }

    /**
     * reset an arena block
     */
    public void reset() {
        final Block b = location.toLocation().getBlock();

        b.setBlockData(blockData);

        if (lines != null) {
            int i = 0;
            for (final String s : lines) {
                if (s != null) {
                    try {
                        ((Sign) b.getState()).setLine(i, s);
                    } catch (final Exception e) {
                        PVPArena.instance.getLogger().warning(
                                "tried to reset sign at location "
                                        + location);
                    }
                }
                i++;
            }
        }
    }
}
