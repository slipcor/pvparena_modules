package net.slipcor.pvparena.modules.walls;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.runnables.ArenaRunnable;

public class WallsRunner extends ArenaRunnable {
	
	final Walls module;
	
	public WallsRunner(final Walls module, final Arena arena, final int seconds) {
		super(MSG.TIMER_WALLS.getNode(), seconds, null, arena, false);
		this.module = module;
	}

	@Override
	protected void commit() {
		
		if (module != null){
			module.removeWalls();
			module.runnable = null;
		}
	}

	@Override
	protected void warn() {
		PVPArena.instance.getLogger().warning("WallsRunner not scheduled yet!");
	}
}
