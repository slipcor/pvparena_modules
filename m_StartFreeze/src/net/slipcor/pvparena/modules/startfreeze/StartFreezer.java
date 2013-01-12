package net.slipcor.pvparena.modules.startfreeze;

import org.bukkit.scheduler.BukkitRunnable;

public class StartFreezer extends BukkitRunnable {
	
	final StartFreeze module;
	
	StartFreezer(StartFreeze mod) {
		module = mod;
	}
	
	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		module.runnable = null;
	}
}
