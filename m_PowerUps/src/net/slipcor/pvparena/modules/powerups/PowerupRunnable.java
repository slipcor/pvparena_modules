package net.slipcor.pvparena.modules.powerups;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.core.Debug;

public class PowerupRunnable implements Runnable {
	private final PowerupManager pum;
	private Debug debug = new Debug(41);

	/**
	 * construct a powerup spawn runnable
	 * 
	 * @param a
	 *            the arena it's running in
	 */
	public PowerupRunnable(PowerupManager pm) {
		pum = pm;
		debug.i("PowerupRunnable constructor");
	}

	/**
	 * the run method, spawn a powerup
	 */
	@Override
	public void run() {
		debug.i("PowerupRunnable commiting spawn");
		if (!pum.getArena().isLocked()) {
			
			pum.calcPowerupSpawn();
		} else {
			// deactivate the auto saving task
			Bukkit.getServer().getScheduler().cancelTask(pum.SPAWN_ID);
		}
	}
}
