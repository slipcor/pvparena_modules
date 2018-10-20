package net.slipcor.pvparena.modules.items;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Items extends ArenaModule {
    private int id = -1;

    public Items() {
        super("Items");
    }

    @Override
    public String version() {
        return "v1.13.2";
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage("interval: " + arena.getArenaConfig().getInt(CFG.MODULES_ITEMS_INTERVAL) +
                "items: " + arena.getArenaConfig().getString(CFG.MODULES_ITEMS_ITEMS));
    }

    @Override
    public boolean hasSpawn(final String s) {
        return s.toLowerCase().startsWith("item");
    }

    @Override
    public void reset(final boolean force) {
        Bukkit.getScheduler().cancelTask(id);
        id = -1;
    }

    @Override
    public void parseStart() {
        if (arena.getArenaConfig().getInt(CFG.MODULES_ITEMS_INTERVAL) > 0) {
            Bukkit.getScheduler().cancelTask(id);
            id = Bukkit.getScheduler()
                    .scheduleSyncRepeatingTask(
                            PVPArena.instance,
                            new ItemSpawnRunnable(arena),
                            arena.getArenaConfig().getInt(
                                    CFG.MODULES_ITEMS_INTERVAL) * 20L,
                            arena.getArenaConfig().getInt(
                                    CFG.MODULES_ITEMS_INTERVAL) * 20L);
        }
    }
}
