package net.slipcor.pvparena.modules.fallinganvils;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;

import java.util.Random;
import java.util.Set;

public class FallingAnvils extends ArenaModule {

    public FallingAnvils() {
        super("FallingAnvils");
    }

    int runnerID = -1;

    @Override
    public String version() {
        return "v1.3.2.93";
    }

    @Override
    public void reset(final boolean force) {
        if (runnerID != -1) {
            Bukkit.getScheduler().cancelTask(runnerID);
        }
    }

    @Override
    public void parseStart() {
        if (runnerID != -1) {
            Bukkit.getScheduler().cancelTask(runnerID);
        }
        long offset = (long) arena.getArenaConfig().getInt(Config.CFG.MODULES_FALLINGANVILS_SPAWNINTERVAL);
        final int count = arena.getArenaConfig().getInt(Config.CFG.MODULES_FALLINGANVILS_AMOUNTPERINTERVAL);
        final long life = (long) arena.getArenaConfig().getInt(Config.CFG.MODULES_FALLINGANVILS_LIFEINTERVAL);
        final Set<ArenaRegion> regions = arena.getRegionsByType(ArenaRegion.RegionType.BATTLE);
        final Random r = new Random();
        runnerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, new Runnable() {
            @Override
            public void run() {
                for (ArenaRegion region : regions) {
                    for (int i=0; i<count; i++) {
                        PABlockLocation loc = region.getShape().getCenter();
                        loc.setY(region.getShape().getMaximumLocation().getY());
                        int diffX = r.nextInt(loc.getX() - region.getShape().getMinimumLocation().getX());
                        int diffZ = r.nextInt(loc.getZ() - region.getShape().getMinimumLocation().getZ());

                        if (r.nextBoolean()) {
                            loc.setX(loc.getX() + diffX);
                        } else {
                            loc.setX(loc.getX() - diffX);
                        }

                        if (r.nextBoolean()) {
                            loc.setZ(loc.getZ() + diffZ);
                        } else {
                            loc.setZ(loc.getZ() - diffZ);
                        }

                        Location bloc = loc.toLocation();

                        final FallingBlock fb = bloc.getWorld().spawnFallingBlock(bloc, Material.ANVIL, (byte) 1);
                        fb.setFallDistance(100);
                        Bukkit.getScheduler().runTaskLater(PVPArena.instance, new Runnable() {
                            @Override
                            public void run() {
                                Block block = fb.getLocation().getBlock();
                                block.setType(Material.AIR, true);
                                fb.remove();
                            }
                        }, life);
                        fb.setHurtEntities(true);
                    }
                }
            }
        }, offset, offset);
    }
}

