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

    private String arena;
    private final PABlockLocation location;
    private final BlockData blockData;
    private final String[] lines;

    /**
     * create an arena block instance (blockdestroy)
     *
     * @param block the block to copy
     */
    public ArenaBlock(final Block block) {
        this.location = new PABlockLocation(block.getLocation());
        this.blockData = block.getBlockData();

        this.debug.i("creating arena block:");
        this.debug.i("loc: " + this.location + "; mat: " + this.blockData.getMaterial().name());

        try {
            this.arena = ArenaManager.getArenaByRegionLocation(this.location).getName();
        } catch (final Exception e) {
            this.arena = "";
        }
        if (block.getState() instanceof Sign) {
            this.lines = ((Sign) block.getState()).getLines();
        } else {
            this.lines = null;
        }
    }

    /**
     * create an arena block instance (blockplace)
     *
     * @param block the block to copy
     * @param type  the Material to override (the Material before placing)
     */
    public ArenaBlock(final Block block, final Material type) {
        this.location = new PABlockLocation(block.getLocation());
        try {
            this.arena = ArenaManager.getArenaByRegionLocation(this.location).getName();
        } catch (final Exception e) {
            this.arena = "";
        }
        this.blockData = Bukkit.createBlockData(type);
        this.lines = null;

        this.debug.i("creating arena block:");
        this.debug.i("loc: " + this.location + "; mat: " + this.blockData.getMaterial());

    }

    /**
     * reset an arena block
     */
    public void reset() {
        final Block b = this.location.toLocation().getBlock();

        b.setBlockData(this.blockData);

        if (this.lines != null) {
            int i = 0;
            for (final String s : this.lines) {
                if (s != null) {
                    try {
                        ((Sign) b.getState()).setLine(i, s);
                    } catch (final Exception e) {
                        PVPArena.instance.getLogger().warning(
                                "tried to reset sign at location "
                                        + this.location);
                    }
                }
                i++;
            }
        }
    }

    public String getArena() {
        return this.arena;
    }
}
