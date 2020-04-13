package net.slipcor.pvparena.modules.blockrestore;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.util.BlockIterator;

import java.util.*;

public class Blocks extends ArenaModule implements Listener {

    public Blocks() {
        super("BlockRestore");
    }

    @Override
    public String version() {
        return getClass().getPackage().getImplementationVersion();
    }

    private boolean listening;

    public static final Map<Location, ArenaBlock> blocks = new HashMap<>();
    //public static HashMap<Location, String[]> signs = new HashMap<Location, String[]>();

    private static final Map<ArenaRegion, RestoreContainer> containers = new HashMap<>();

    private static final Debug debug = new Debug(24);

    boolean restoring;

    @Override
    public boolean checkCommand(final String s) {
        return "blockrestore".equals(s) || "!br".equals(s);
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("blockrestore");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!br");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"hard"});
        result.define(new String[]{"restorechests"});
        result.define(new String[]{"clearinv"});
        result.define(new String[]{"offset"});
        return result;
    }

    @Override
    public PACheck checkJoin(final CommandSender sender,
                             final PACheck res, final boolean join) {
        if (restoring) {
            res.setError(this, "restoring");
        }
        return res;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        // !br hard
        // !br restorechests
        // !br clearinv
        // !br offset X

        if (!PVPArena.hasAdminPerms(sender)
                && !PVPArena.hasCreatePerms(sender, arena)) {
            Arena.pmsg(sender,
                    Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2, 3})) {
            return;
        }

        if (args[1].startsWith("clearinv")) {

            arena.getArenaConfig().setManually("inventories", null);
            arena.getArenaConfig().save();
            Arena.pmsg(sender, Language.parse(MSG.MODULE_BLOCKRESTORE_CLEARINVDONE));
            return;
        }

        if ("hard".equals(args[1]) || "restorechests".equals(args[1]) || "restoreblocks".equals(args[1])) {
            final CFG c;
            if ("hard".equals(args[1])) {
                c = CFG.MODULES_BLOCKRESTORE_HARD;
            } else if (args[1].contains("blocks")) {
                c = CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS;
            } else {
                c = CFG.MODULES_BLOCKRESTORE_RESTORECHESTS;
            }
            final boolean b = arena.getArenaConfig().getBoolean(c);

            arena.getArenaConfig().set(c, !b);
            arena.getArenaConfig().save();
            arena.msg(sender, Language.parse(MSG.SET_DONE, c.getNode(), String.valueOf(!b)));

            return;
        }

        if ("offset".equals(args[1])) {
            if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{3})) {
                return;
            }

            final int i;
            try {
                i = Integer.parseInt(args[2]);
            } catch (final Exception e) {
                arena.msg(sender,
                        Language.parse(MSG.ERROR_NOT_NUMERIC, args[2]));
                return;
            }

            arena.getArenaConfig().set(CFG.MODULES_BLOCKRESTORE_OFFSET, i);
            arena.getArenaConfig().save();
            arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_BLOCKRESTORE_OFFSET.getNode(), String.valueOf(i)));
        }
    }

    @Override
    public void displayInfo(final CommandSender player) {
        player.sendMessage(StringParser.colorVar("hard", arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD))
                + " | " + StringParser.colorVar("blocks", arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS))
                + " | " + StringParser.colorVar("chests", arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTORECHESTS))
                + " | offset " + arena.getArenaConfig().getInt(CFG.MODULES_BLOCKRESTORE_OFFSET));
    }

    @Override
    public boolean needsBattleRegion() {
        return true;
    }

    @Override
    public void onEntityExplode(final EntityExplodeEvent event) {
        if (!arena.isLocked() &&
                !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
                        && arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
            for (final Block b : event.blockList()) {
                saveBlock(b);
            }
        }
    }


    @Override
    public void onBlockBreak(final Block block) {
        debug.i("block break in blockRestore");
        if (arena == null || arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
                || !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
            debug.i(arena + " || blockRestore.hard: " + arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD));
            return;
        }
        if (!arena.isLocked()) {
            saveBlock(block);
        }
        debug.i("!arena.isLocked() " + !arena.isLocked());
    }

    @Override
    public void onBlockPiston(final Block block) {
        if (!arena.isLocked()
                && !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
                && arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
            saveBlock(block);
        }
    }

    @Override
    public void onBlockPlace(final Block block, final Material mat) {
        if (!arena.isLocked()
                && !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
                && arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
            saveBlock(block, mat);
        }
    }

    @Override
    public void reset(final boolean force) {
        resetBlocks();
        restoreChests();
    }

    /**
     * reset all blocks belonging to an arena
     */
    private void resetBlocks() {
        if (arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
            debug.i("resetting blocks");
            try {
                Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new BlockRestoreRunnable(arena, this));
            } catch (final IllegalPluginAccessException e) {
                new BlockRestoreRunnable(arena, this).instantlyRestore();
            }
        }

    }


    /**
     * restore chests, if wanted and possible
     */
    void restoreChests() {
        debug.i("resetting chests");
        final Set<ArenaRegion> bfs = arena.getRegionsByType(RegionType.BATTLE);

        if (bfs.size() < 1) {
            debug.i("no battlefield region, skipping restoreChests");
            return;
        }

        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTORECHESTS)) {
            debug.i("not restoring chests, skipping restoreChests");
            return;
        }

        for (final ArenaRegion bfRegion : bfs) {
            debug.i("resetting arena: " + bfRegion.getRegionName());

            if (containers.get(bfRegion) != null) {
                debug.i("container not null!");
                containers.get(bfRegion).restoreChests();
            }

        }
    }

    /**
     * save a block to be restored (block destroy)
     *
     * @param block the block to save
     */
    private void saveBlock(final Block block) {
        debug.i("save block at " + block.getLocation());
        if (!blocks.containsKey(block.getLocation())) {
            blocks.put(block.getLocation(), new ArenaBlock(block));
        }
    }

    /**
     * save a block to be restored (block place)
     *
     * @param block the block to save
     * @param type  the material to override
     */
    private void saveBlock(final Block block, final Material type) {
        debug.i("save block at " + block.getLocation());
        debug.i(" - type: " + type);
        if (!blocks.containsKey(block.getLocation())) {
            blocks.put(block.getLocation(), new ArenaBlock(block, type));
        }
    }

    /**
     * save arena chest, if wanted and possible
     */
    void saveChests() {
        final Set<ArenaRegion> bfs = arena.getRegionsByType(RegionType.BATTLE);

        if (bfs.size() < 1) {
            debug.i("no battlefield region, skipping saveChests");
            return;
        }

        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTORECHESTS)) {
            debug.i("not restoring chests, skipping saveChests");
            return;
        }

        for (final ArenaRegion bfRegion : bfs) {
            if (containers.containsKey(bfRegion)) {
                containers.get(bfRegion).saveChests();
            }
        }
    }

    @Override
    public void parseStart() {
        if (!listening) {
            Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
            listening = true;
        }
        final Set<ArenaRegion> bfs = arena.getRegionsByType(RegionType.BATTLE);

        if (bfs.size() < 1) {
            debug.i("no battlefield region, skipping restoreChests");
            return;
        }

        for (final ArenaRegion region : bfs) {
            saveRegion(region);
        }

        for (final ArenaRegion r : arena.getRegions()) {
            if (r.getRegionName().startsWith("restore")) {
                saveRegion(r);
            }
        }
    }

    private void saveRegion(final ArenaRegion region) {
        if (region == null) {
            return;
        }

        containers.put(region, new RestoreContainer(this, region));

        saveChests();

        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS) ||
                !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD) &&
                        !region.getRegionName().startsWith("restore")) {
            return;
        }

        final PABlockLocation min = region.getShape().getMinimumLocation();
        final PABlockLocation max = region.getShape().getMaximumLocation();

        final World world = Bukkit.getWorld(min.getWorldName());

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    saveBlock(world.getBlockAt(x, y, z));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockRedstone(final BlockRedstoneEvent event) {
        if (event.getNewCurrent() > event.getOldCurrent()) {
            for (final ArenaRegion shape : arena.getRegionsByType(RegionType.BATTLE)) {
                if (shape.getShape().contains(new PABlockLocation(event.getBlock().getLocation()))) {
                    if (event.getBlock().getType() == Material.TNT) {
                        saveBlock(event.getBlock());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void projectileHit(final ProjectileHitEvent event) {
        for (final ArenaRegion shape : arena.getRegionsByType(RegionType.BATTLE)) {
            if (shape.getShape().contains(new PABlockLocation(event.getEntity().getLocation()))) {
                if (event.getEntityType() == EntityType.ARROW) {
                    final Arrow arrow = (Arrow) event.getEntity();
                    if (arrow.getFireTicks() > 0) {
                        final BlockIterator bi = new BlockIterator(arrow.getWorld(), arrow.getLocation().toVector(), arrow.getVelocity(), 0, 2);
                        while (bi.hasNext()) {
                            final Block block = bi.next();
                            if (block.getType() == Material.TNT) {
                                saveBlock(block);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        if (event.getBucket() != Material.LAVA_BUCKET && event.getBucket() != Material.WATER_BUCKET) {
            return;
        }
        Block toCheck = event.getBlockClicked().getRelative(event.getBlockFace());
        for (final ArenaRegion shape : arena.getRegionsByType(RegionType.BATTLE)) {
            if (shape.getShape().contains(new PABlockLocation(toCheck.getLocation()))) {
                saveBlock(toCheck);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(final PlayerBucketFillEvent event) {
        if (event.getBucket() != Material.BUCKET) {
            return;
        }
        Block toCheck = event.getBlockClicked().getRelative(event.getBlockFace());
        for (final ArenaRegion shape : arena.getRegionsByType(RegionType.BATTLE)) {
            if (shape.getShape().contains(new PABlockLocation(toCheck.getLocation()))) {
                saveBlock(toCheck);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block blk = event.getToBlock();
        if (blk.getType() == Material.WATER || blk.getType() == Material.LAVA) {
            Location loc = blk.getLocation();
            for (final ArenaRegion shape : arena.getRegionsByType(RegionType.BATTLE)) {
                if (shape.getShape().contains(new PABlockLocation(loc))) {
                    saveBlock(loc.getBlock());
                }
            }

        }
    }
}
