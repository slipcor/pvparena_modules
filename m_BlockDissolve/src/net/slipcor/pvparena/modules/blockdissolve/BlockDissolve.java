package net.slipcor.pvparena.modules.blockdissolve;

import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class BlockDissolve extends ArenaModule {

    private boolean setup;
    private MoveChecker checker;

    public BlockDissolve() {
        super("BlockDissolve");
    }

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    @Override
    public void configParse(final YamlConfiguration config) {
        if (setup) {
            return;
        }
        if (checker == null) {
            checker = new MoveChecker(arena, arena.getArenaConfig().getString(CFG.MODULES_BLOCKDISSOLVE_MATERIALS),
                    arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_TICKS));
        }
        setup = true;
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage("ticks: " + arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_TICKS));
        sender.sendMessage("materials: " + arena.getArenaConfig().getString(CFG.MODULES_BLOCKDISSOLVE_MATERIALS));
    }

    @Override
    public void parseStart() {
        checker.start();
    }

    public void reset(final boolean force) {
        checker.clear();
    }
}
