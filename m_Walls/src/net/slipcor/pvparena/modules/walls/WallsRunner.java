package net.slipcor.pvparena.modules.walls;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.runnables.ArenaRunnable;

public class WallsRunner extends ArenaRunnable {

    private final Walls module;

    public WallsRunner(final Walls module, final Arena arena, final int seconds) {
        super(MSG.TIMER_WALLS.getNode(), seconds, null, arena, false);
        this.module = module;
    }

    @Override
    protected void commit() {

        if (module != null) {
            module.removeWalls();
            module.runnable = null;
        }
    }

    @Override
    protected void spam() {
        super.spam();
        if (arena.getArenaConfig().getBoolean(Config.CFG.MODULES_WALLS_SCOREBOARDCOUNTDOWN)) {
            arena.addCustomScoreBoardEntry(module, Language.parse(arena, MSG.MODULE_WALLS_FALLINGIN), 99);
            arena.addCustomScoreBoardEntry(module, seconds + " " + Language.parse(arena, MSG.TIME_SECONDS), 98);
            arena.addCustomScoreBoardEntry(module, "--------------", 97);
        }
    }

    @Override
    protected void warn() {
        PVPArena.instance.getLogger().warning("WallsRunner not scheduled yet!");
    }
}
