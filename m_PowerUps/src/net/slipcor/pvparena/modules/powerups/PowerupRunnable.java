package net.slipcor.pvparena.modules.powerups;

import net.slipcor.pvparena.core.Debug;
import org.bukkit.Bukkit;

class PowerupRunnable implements Runnable {
    private final PowerupManager pum;
    private final Debug debug = new Debug(41);

    /**
     * construct a powerup spawn runnable
     *
     * @param pm the module instance
     */
    public PowerupRunnable(final PowerupManager pm) {
        pum = pm;
        debug.i("PowerupRunnable constructor");
    }

    /**
     * the run method, spawn a powerup
     */
    @Override
    public void run() {
        debug.i("PowerupRunnable commiting spawn");
        if (pum.getArena().isLocked()) {
            // deactivate the auto saving task
            Bukkit.getServer().getScheduler().cancelTask(pum.SPAWN_ID);
        } else {

            pum.calcPowerupSpawn();
        }
    }
}
