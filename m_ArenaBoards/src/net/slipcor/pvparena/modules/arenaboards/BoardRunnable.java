package net.slipcor.pvparena.modules.arenaboards;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

/**
 * arena ending runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to end the arena it is running in
 * 
 * @author slipcor
 * 
 * @version v0.8.10
 * 
 */

public class BoardRunnable implements Runnable {
	protected static ArenaBoardManager abm;
	private final Arena a;
	private Debug db = new Debug(38);

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public BoardRunnable(Arena a) {
		this.a = a;
		db.i("BoardRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("BoardRunnable commiting");
		if (a == null) {
			if (ArenaBoardManager.globalBoard != null) {
				ArenaBoardManager.globalBoard.update(); 
			}
		} else {
			for (ArenaBoard ab : abm.boards.values()) {
				if (ab.arena.name.equals(a.name)) {
					ab.update();
				}
			}
		}
	}
}
