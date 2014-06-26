package net.slipcor.pvparena.modules.aftermatch;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.runnables.ArenaRunnable;

public class AfterRunnable extends ArenaRunnable {
    private final AfterMatch pum;
    private Debug debug = new Debug(41);

    public AfterRunnable(AfterMatch pm, int i) {
        super(MSG.MODULE_AFTERMATCH_STARTINGIN.getNode(), i, null, pm.getArena(), false);
        pum = pm;
        debug.i("AfterRunnable constructor");
    }

    @Override
    protected void commit() {
        debug.i("AfterRunnable commiting");
        if (!pum.getArena().isLocked()) {

            pum.afterMatch();
        }
    }

    @Override
    protected void warn() {
        PVPArena.instance.getLogger().warning("AfterRunnable not scheduled yet!");
    }
}
