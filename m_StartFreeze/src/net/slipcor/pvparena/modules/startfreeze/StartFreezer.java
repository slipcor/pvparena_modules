package net.slipcor.pvparena.modules.startfreeze;

import net.slipcor.pvparena.arena.Arena;

public class StartFreezer implements Runnable {
	private final Arena a;

	public StartFreezer(Arena a) {
		this.a = a;
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		StartFreeze.ids.remove(a);
		StartFreeze.runnables.remove(a);
	}
}
