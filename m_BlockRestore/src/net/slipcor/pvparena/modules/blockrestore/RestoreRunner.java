package net.slipcor.pvparena.modules.blockrestore;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
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

import java.util.HashMap;
import java.util.Map;

class RestoreRunner implements Runnable {
    private final Map<Location, ItemStack[]> chests;
    private final HashMap<Location, ItemStack[]> furnaces;
    private final Map<Location, ItemStack[]> dispensers;
    private final Blocks blocks;
    private final Debug debug = new Debug(66);

    public RestoreRunner(final Blocks blocks, final HashMap<Location, ItemStack[]> chests,
                         final HashMap<Location, ItemStack[]> furnaces,
                         final HashMap<Location, ItemStack[]> dispensers) {
        debug.i("RestoreRunner contructor: " + blocks.getArena().getName());

        this.blocks = blocks;
        this.chests = chests;
        this.furnaces = furnaces;
        this.dispensers = dispensers;

        if (chests != null) {
            debug.i("chests: " + chests.size());
        }
        if (furnaces != null) {
            debug.i("furnaces: " + furnaces.size());
        }
        if (dispensers != null) {
            debug.i("dispensers: " + dispensers.size());
        }
    }

    @Override
    public void run() {
        PAA_Edit.activeEdits.put("server", blocks.getArena());
        final World world = Bukkit.getWorld(blocks.getArena().getWorld());
        for (final Location loc : chests.keySet()) {
            if (loc == null) {
                break;
            }
            try {
                debug.i("trying to restore chest: " + loc);
                final Block b = world.getBlockAt(loc);
                b.setType(Material.CHEST);
                final Inventory inv = ((Chest) b.getState())
                        .getInventory();
                inv.clear();
                int i = 0;
                for (final ItemStack is : chests.get(loc)) {
                    debug.i("restoring: " + StringParser.getStringFromItemStack(is));
                    inv.setItem(i++, is);
                }
                debug.i("success!");
                chests.remove(loc);
                Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
                        this, blocks.getArena().getArenaConfig().getInt(CFG.MODULES_BLOCKRESTORE_OFFSET) * 1L);
                return;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        for (final Location loc : dispensers.keySet()) {
            if (loc == null) {
                break;
            }
            try {
                debug.i("trying to restore dispenser: " + loc);

                final Inventory inv = ((Dispenser) world.getBlockAt(loc).getState())
                        .getInventory();
                inv.clear();
                for (final ItemStack is : dispensers.get(loc)) {
                    if (is != null) {
                        inv.addItem(is.clone());
                    }
                }
                debug.i("success!");
                dispensers.remove(loc);
                Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
                        this, blocks.getArena().getArenaConfig().getInt(CFG.MODULES_BLOCKRESTORE_OFFSET) * 1L);
                return;
            } catch (final Exception e) {
                //
            }
        }
        for (final Location loc : furnaces.keySet()) {
            if (loc == null) {
                break;
            }
            try {
                debug.i("trying to restore furnace: " + loc);
                ((Furnace) world.getBlockAt(loc).getState()).getInventory()
                        .setContents(RestoreContainer.cloneIS(furnaces.get(loc)));
                debug.i("success!");
                furnaces.remove(loc);
                Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
                        this, blocks.getArena().getArenaConfig().getInt(CFG.MODULES_BLOCKRESTORE_OFFSET) * 1L);
                return;
            } catch (final Exception e) {
                //
            }
        }
        PAA_Edit.activeEdits.remove("server");
    }

}
