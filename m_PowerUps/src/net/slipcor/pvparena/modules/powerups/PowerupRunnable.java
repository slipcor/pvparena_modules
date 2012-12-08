package net.slipcor.pvparena.modules.powerups;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.core.Debug;

public class PowerupRunnable implements Runnable {
	private final PowerupManager pum;
	private Debug db = new Debug(41);

	/**
	 * construct a powerup spawn runnable
	 * 
	 * @param a
	 *            the arena it's running in
	 */
	public PowerupRunnable(PowerupManager pm) {
		pum = pm;
		db.i("PowerupRunnable constructor");
	}

	/**
	 * the run method, spawn a powerup
	 */
	@Override
	public void run() {
		db.i("PowerupRunnable commiting spawn");
		if (!pum.getArena().isLocked()) {
			
			pum.calcPowerupSpawn();
		} else {
			// deactivate the auto saving task
			Bukkit.getServer().getScheduler().cancelTask(pum.SPAWN_ID);
		}
	}
}
