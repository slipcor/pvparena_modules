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
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.material.Attachable;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.util.BlockIterator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Blocks extends ArenaModule implements Listener {

    public Blocks() {
        super("BlockRestore");
    }

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    private boolean listening = false;

    public static HashMap<Location, ArenaBlock> blocks = new HashMap<Location, ArenaBlock>();
    //public static HashMap<Location, String[]> signs = new HashMap<Location, String[]>();

    private static HashMap<ArenaRegion, RestoreContainer> containers = new HashMap<ArenaRegion, RestoreContainer>();

    private static Debug debug = new Debug(24);

    protected boolean restoring = false;

    private void checkBlock(Block b, BlockFace bf) {
        if (b.getType().equals(Material.LADDER) ||
                b.getType().equals(Material.STONE_BUTTON) ||
                b.getType().equals(Material.LEVER) ||
                b.getType().equals(Material.WALL_SIGN)) {
            Attachable a = (Attachable) b.getState().getData();
            if (a.getAttachedFace().equals(bf)) {
                saveBlock(b);
            }
        }
    }

    @Override
    public boolean checkCommand(String s) {
        return (s.equals("blockrestore") || s.equals("!br"));
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("blockrestore");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!br");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"hard"});
        result.define(new String[]{"restorechests"});
        result.define(new String[]{"clearinv"});
        result.define(new String[]{"offset"});
        return result;
    }

    @Override
    public PACheck checkJoin(CommandSender sender,
                             PACheck res, boolean join) {
        if (restoring) {
            res.setError(this, "restoring");
        }
        return res;
    }

    @Override
    public void commitCommand(CommandSender sender, String[] args) {
        // !br hard
        // !br restorechests
        // !br clearinv
        // !br offset X

        if (!PVPArena.hasAdminPerms(sender)
                && !(PVPArena.hasCreatePerms(sender, arena))) {
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

        if (args[1].equals("hard") || args[1].equals("restorechests") || args[1].equals("restoreblocks")) {
            CFG c;
            if (args[1].equals("hard")) {
                c = CFG.MODULES_BLOCKRESTORE_HARD;
            } else if (args[1].contains("blocks")) {
                c = CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS;
            } else {
                c = CFG.MODULES_BLOCKRESTORE_RESTORECHESTS;
            }
            boolean b = arena.getArenaConfig().getBoolean(c);

            arena.getArenaConfig().set(c, !b);
            arena.getArenaConfig().save();
            arena.msg(sender, Language.parse(MSG.SET_DONE, c.getNode(), String.valueOf(!b)));

            return;
        }

        if (args[1].equals("offset")) {
            if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{3})) {
                return;
            }

            int i;
            try {
                i = Integer.parseInt(args[2]);
            } catch (Exception e) {
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
    public void displayInfo(CommandSender player) {
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
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!arena.isLocked() &&
                (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
                        && arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS))) {
            for (Block b : event.blockList()) {
                saveBlock(b);
            }
        }
    }


    @Override
    public void onBlockBreak(Block block) {
        debug.i("block break in blockRestore");
        if (arena == null || arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
                || !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
            debug.i(arena + " || blockRestore.hard: " + arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD));
            return;
        }
        if (!arena.isLocked()) {

            checkBlock(block.getRelative(BlockFace.NORTH), BlockFace.SOUTH);
            checkBlock(block.getRelative(BlockFace.SOUTH), BlockFace.NORTH);
            checkBlock(block.getRelative(BlockFace.EAST), BlockFace.WEST);
            checkBlock(block.getRelative(BlockFace.WEST), BlockFace.EAST);

            saveBlock(block);
        }
        debug.i("!arena.isLocked() " + !arena.isLocked());
    }

    @Override
    public void onBlockPiston(Block block) {
        if (!arena.isLocked()
                && !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
                && arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
            saveBlock(block);
        }
    }

    @Override
    public void onBlockPlace(Block block, Material mat) {
        if (!arena.isLocked()
                && !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
                && arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
            saveBlock(block, mat);
        }
    }

    @Override
    public void reset(boolean force) {
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
                Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new BlockRestoreRunnable(arena, blocks, this));
            } catch (IllegalPluginAccessException e) {
                (new BlockRestoreRunnable(arena, blocks, this)).instantlyRestore();
            }
        }

    }


    /**
     * restore chests, if wanted and possible
     */
    public void restoreChests() {
        debug.i("resetting chests");
        Set<ArenaRegion> bfs = arena.getRegionsByType(RegionType.BATTLE);

        if (bfs.size() < 1) {
            debug.i("no battlefield region, skipping restoreChests");
            return;
        }

        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTORECHESTS)) {
            debug.i("not restoring chests, skipping restoreChests");
            return;
        }

        for (ArenaRegion bfRegion : bfs) {
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
    private void saveBlock(Block block) {
        debug.i("save block at " + block.getLocation().toString());
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
    private void saveBlock(Block block, Material type) {
        debug.i("save block at " + block.getLocation().toString());
        debug.i(" - type: " + type.toString());
        if (!blocks.containsKey(block.getLocation())) {
            blocks.put(block.getLocation(), new ArenaBlock(block, type));
        }
    }

    /**
     * save arena chest, if wanted and possible
     */
    public void saveChests() {
        Set<ArenaRegion> bfs = arena.getRegionsByType(RegionType.BATTLE);

        if (bfs.size() < 1) {
            debug.i("no battlefield region, skipping saveChests");
            return;
        }

        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTORECHESTS)) {
            debug.i("not restoring chests, skipping saveChests");
            return;
        }

        for (ArenaRegion bfRegion : bfs) {
            containers.get(bfRegion).saveChests();
        }
    }

    @Override
    public void parseStart() {
        if (!listening) {
            Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
            listening = true;
        }
        Set<ArenaRegion> bfs = arena.getRegionsByType(RegionType.BATTLE);

        if (bfs.size() < 1) {
            debug.i("no battlefield region, skipping restoreChests");
            return;
        }

        for (ArenaRegion region : bfs) {
            saveRegion(region);
        }

        for (ArenaRegion r : arena.getRegions()) {
            if (r.getRegionName().startsWith("restore")) {
                saveRegion(r);
            }
        }
    }

    private void saveRegion(ArenaRegion region) {
        if (region == null) {
            return;
        }

        containers.put(region, new RestoreContainer(this, region));

        saveChests();

        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS) ||
                (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD) &&
                        !region.getRegionName().startsWith("restore"))) {
            return;
        }

        PABlockLocation min = region.getShape().getMinimumLocation();
        PABlockLocation max = region.getShape().getMaximumLocation();

        World world = Bukkit.getWorld(min.getWorldName());

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    saveBlock(world.getBlockAt(x, y, z));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockRedstone(BlockRedstoneEvent event) {
        if (event.getNewCurrent() > event.getOldCurrent()) {
            for (ArenaRegion shape : arena.getRegionsByType(RegionType.BATTLE)) {
                if (shape.getShape().contains(new PABlockLocation(event.getBlock().getLocation()))) {
                    if (event.getBlock().getType() == Material.TNT) {
                        saveBlock(event.getBlock());
                        System.out.print("got you!");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void projectileHit(ProjectileHitEvent event) {
        for (ArenaRegion shape : arena.getRegionsByType(RegionType.BATTLE)) {
            if (shape.getShape().contains(new PABlockLocation(event.getEntity().getLocation()))) {
                if (event.getEntityType() == EntityType.ARROW) {
                    Arrow arrow = (Arrow) event.getEntity();
                    if (arrow.getFireTicks() > 0) {
                        BlockIterator bi = new BlockIterator(arrow.getWorld(), arrow.getLocation().toVector(), arrow.getVelocity(), 0, 2);
                        while (bi.hasNext()) {
                            Block block = bi.next();
                            if (block.getType() == Material.TNT) {
                                saveBlock(block);
                                System.out.print("got you!");
                            }
                        }
                    }
                }
            }
        }
    }
}
