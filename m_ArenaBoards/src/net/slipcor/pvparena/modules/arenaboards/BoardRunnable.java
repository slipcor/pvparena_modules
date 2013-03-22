package net.slipcor.pvparena.modules.arenaboards;

import net.slipcor.pvparena.core.Debug;

public class BoardRunnable implements Runnable {
	protected ArenaBoardManager abm;
	private Debug debug = new Debug(38);

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public BoardRunnable(ArenaBoardManager m) {
		abm = m;
		debug.i("BoardRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		debug.i("BoardRunnable commiting");
		if (abm == null) {
			if (ArenaBoardManager.globalBoard != null) {
				ArenaBoardManager.globalBoard.update(); 
			}
		} else {
			for (ArenaBoard ab : abm.boards.values()) {
				ab.update();
			}
		}
	}
}
