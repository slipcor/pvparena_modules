package net.slipcor.pvparena.modules.blockdissolve;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.runnables.ArenaRunnable;

public class CountdownRunner extends ArenaRunnable {
    private final MoveChecker checker;

    public CountdownRunner(Arena arena, MoveChecker mc, int seconds) {
        super(MSG.ARENA_STARTING_IN.getNode(), seconds, null, arena, false);
        checker = mc;
    }

    @Override
    protected void commit() {
        checker.active = true;
    }

    @Override
    protected void warn() {
        PVPArena.instance.getLogger().warning("CountdownRunner not scheduled yet!");
    }

}
