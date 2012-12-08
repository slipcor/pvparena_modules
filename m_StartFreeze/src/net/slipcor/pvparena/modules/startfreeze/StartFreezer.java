package net.slipcor.pvparena.modules.startfreeze;

public class StartFreezer implements Runnable {

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		StartFreeze.id = -1;
		StartFreeze.runnable = null;
	}
}
