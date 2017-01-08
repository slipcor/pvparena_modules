package net.slipcor.pvparena.modules.blockdissolve;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

class MoveChecker implements Listener {
    private final Debug debug = new Debug(42);
    private final ItemStack[] materials;
    private final Arena arena;
    private final Map<Block, Runnable> map = new HashMap<>();
    private final int delay;
    private final int startSeconds;
    boolean active;
    double offset;

    public MoveChecker(final Arena arena, final String definition, final int delay) {
        materials = StringParser.getItemStacksFromString(definition);
        debug.i("BattleRunnable constructor");
        this.arena = arena;
        Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
        this.delay = delay;
        startSeconds = arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_STARTSECONDS);
        offset = arena.getArenaConfig().getDouble(CFG.MODULES_BLOCKDISSOLVE_CALCOFFSET);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(final PlayerMoveEvent event) {

        if (active && arena.isFightInProgress()) {
            final ArenaPlayer player = ArenaPlayer.parsePlayer(event.getPlayer().getName());
            if (arena != player.getArena()) {
                return;
            }

            if (arena.getPlayedSeconds() > startSeconds && player.getStatus() == Status.FIGHT) {


                checkBlock(event.getPlayer().getLocation().clone().subtract(0, 1, 0));
            }
        }
    }

    private void checkBlock(final Location location) {

        final double x = location.getX() * 10 % 10 / 10;
        final double z = location.getZ() * 10 % 10 / 10;

        if (x < offset) {
            checkBlock(location.clone().add(-1, 0, 0).getBlock());
        } else if (x > (1-offset)) {
            checkBlock(location.clone().add(1, 0, 0).getBlock());
        }

        if (z < offset) {
            checkBlock(location.clone().add(0, 0, -1).getBlock());
        } else if (z > 0.666) {
            checkBlock(location.clone().add(0, 0, 1).getBlock());
        }

        if (x < offset && z < offset) {
            checkBlock(location.clone().add(-1, 0, -1).getBlock());
        } else if (x <offset && z > (1-offset)) {
            checkBlock(location.clone().add(-1, 0, 1).getBlock());
        } else if (x > (1-offset) && z < offset) {
            checkBlock(location.clone().add(1, 0, -1).getBlock());
        } else if (x > (1-offset) && z > (1-offset)) {
            checkBlock(location.clone().add(1, 0, 1).getBlock());
        }

        checkBlock(location.getBlock());
    }

    private void checkBlock(final Block block) {
        final Material mat = block.getType();

        for (final ItemStack stack : materials) {
            if (mat == stack.getType()) {
                access(block, false);
                return;
            }
        }
    }

    private synchronized void access(final Block block, final boolean remove) {
        if (block == null && remove) {
            map.clear();
            active = false;
            return;
        }

        if (map.containsKey(block)) {
            return;
        }
        if (remove) {
            map.remove(block);
        } else {
            map.put(block, new RunLater(block));
        }
    }

    public void startTask() {
        class RunLater2 implements Runnable {
            @Override
            public void run() {
                if (active) {
                    for (ArenaPlayer ap : arena.getFighters()) {
                        if (ap.getStatus() == Status.FIGHT) {
                            checkBlock(ap.get().getLocation().clone().subtract(0, 1, 0));
                        }
                    }
                }
            }

        }

        Bukkit.getScheduler().runTaskTimer(PVPArena.instance, new RunLater2(), 20L, 20L);
    }

    class RunLater implements Runnable {
        final Block block;

        RunLater(final Block b) {
            block = b;
            Bukkit.getScheduler().runTaskLater(PVPArena.instance, this, delay);
        }

        @Override
        public void run() {
            access(block, true);
            block.setType(Material.AIR);
        }

    }

    public void clear() {
        access(null, true);
        active = false;
    }

    public void start() {
        new CountdownRunner(arena, this, startSeconds);
    }
}
