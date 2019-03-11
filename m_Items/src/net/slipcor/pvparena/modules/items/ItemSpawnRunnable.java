package net.slipcor.pvparena.modules.items;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PASpawn;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.managers.SpawnManager;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.Set;

class ItemSpawnRunnable implements Runnable {

    private final ItemStack[] items;
    private final Set<PASpawn> spawns;
    private final Arena arena;

    public ItemSpawnRunnable(final Arena a) {
        arena = a;
        arena.getDebugger().i("ItemSpawnRunnable constructor");
        items = arena.getArenaConfig().getItems(CFG.MODULES_ITEMS_ITEMS);
        spawns = SpawnManager.getPASpawnsStartingWith(arena, "item");
    }

    @Override
    public void run() {
        arena.getDebugger().i("ItemSpawnRunnable running");
        int i = (new Random()).nextInt(spawns.size());
        for (final PASpawn loc : spawns) {
            if (--i <= 0) {
                loc.getLocation().toLocation().getWorld().dropItemNaturally(
                        loc.getLocation().toLocation(), items[(new Random()).nextInt(items.length)]);
                return;
            }
        }
    }

}
