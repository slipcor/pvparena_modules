package net.slipcor.pvparena.modules.blockdissolve;

import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class BlockDissolve extends ArenaModule {

    private boolean setup;
    private MoveChecker checker;

    public BlockDissolve() {
        super("BlockDissolve");
    }

    @Override
    public String version() {
        return "v1.13.5";
    }

    @Override
    public void configParse(final YamlConfiguration config) {
        if (setup) {
            return;
        }
        if (checker == null) {
            checker = new MoveChecker(arena, arena.getArenaConfig().getItems(CFG.MODULES_BLOCKDISSOLVE_MATERIALS),
                    arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_TICKS));
        }
        setup = true;
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage("ticks: " + arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_TICKS));
        StringBuilder materials = new StringBuilder("materials: ");
        ItemStack[] items = arena.getArenaConfig().getItems(CFG.MODULES_BLOCKDISSOLVE_MATERIALS);
        for(ItemStack item : items) {
            materials.append(item.getType().name());
        }
        sender.sendMessage(materials.toString());
    }

    @Override
    public void parseStart() {
        if (checker == null) {
            checker = new MoveChecker(arena, arena.getArenaConfig().getItems(CFG.MODULES_BLOCKDISSOLVE_MATERIALS),
                    arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_TICKS));
        }
        checker.start();
    }

    @Override
    public void reset(final boolean force) {
        if (checker != null) {
            checker.clear();
        }
    }
}
