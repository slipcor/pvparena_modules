package net.slipcor.pvparena.modules.aftermatch;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

/**
 * custom runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to commit a powerup spawn in the
 * arena it is running in
 * 
 * @author slipcor
 * 
 * @version v0.7.0
 * 
 */

public class AfterRunnable implements Runnable {
	private final Arena a;
	private final AfterMatch pum;
	private Debug db = new Debug(41);

	/**
	 * construct a powerup spawn runnable
	 * 
	 * @param a
	 *            the arena it's running in
	 */
	public AfterRunnable(Arena a, AfterMatch pm) {
		this.a = a;
		pum = pm;
		db.i("AfterRunnable constructor");
	}

	/**
	 * the run method, spawn a powerup
	 */
	@Override
	public void run() {
		db.i("AfterRunnable commiting");
		if (!a.isLocked()) {
			
			pum.afterMatch(a);
		} else {
			// deactivate the auto saving task
			Bukkit.getServer().getScheduler().cancelTask(pum.runnables.get(a));
		}
	}
}
