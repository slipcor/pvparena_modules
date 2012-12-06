package net.slipcor.pvparena.modules.aftermatch;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.runnables.ArenaRunnable;

public class AfterRunnable extends ArenaRunnable {
	private final AfterMatch pum;
	private Debug db = new Debug(41);

	/**
	 * construct a powerup spawn runnable
	 * 
	 * @param a
	 *            the arena it's running in
	 */
	public AfterRunnable(AfterMatch pm, int i) {
		super(MSG.MODULE_AFTERMATCH_STARTINGIN.getNode(), i, null, pm.getArena(), false);
		pum = pm;
		db.i("AfterRunnable constructor");
	}

	@Override
	protected void commit() {
		db.i("AfterRunnable commiting");
		if (!pum.getArena().isLocked()) {
			
			pum.afterMatch();
		}
	}

	@Override
	protected void warn() {
		PVPArena.instance.getLogger().warning("AfterRunnable not scheduled yet!");
	}
}
