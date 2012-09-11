package net.slipcor.pvparena.modules.powerups;

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

public class PowerupRunnable implements Runnable {
	private final Arena a;
	private final PowerupManager pum;
	private Debug db = new Debug(41);

	/**
	 * construct a powerup spawn runnable
	 * 
	 * @param a
	 *            the arena it's running in
	 */
	public PowerupRunnable(Arena a, PowerupManager pm) {
		this.a = a;
		pum = pm;
		db.i("PowerupRunnable constructor");
	}

	/**
	 * the run method, spawn a powerup
	 */
	@Override
	public void run() {
		db.i("PowerupRunnable commiting spawn");
		if (!a.isLocked()) {
			
			pum.calcPowerupSpawn(a);
		} else {
			// deactivate the auto saving task
			Bukkit.getServer().getScheduler().cancelTask(pum.SPAWN_ID);
		}
	}
}
