package net.slipcor.pvparena.modules.startfreeze;

import org.bukkit.scheduler.BukkitRunnable;

class StartFreezer extends BukkitRunnable {

    private final StartFreeze module;

    StartFreezer(StartFreeze mod) {
        module = mod;
    }

    /**
     * the run method, commit arena end
     */
    @Override
    public void run() {
        if (module != null) {
            module.runnable = null;
        }
    }
}
