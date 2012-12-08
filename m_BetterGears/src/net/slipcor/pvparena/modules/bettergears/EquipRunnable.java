package net.slipcor.pvparena.modules.bettergears;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;

public class EquipRunnable implements Runnable {

	final ArenaPlayer p;
	final BetterGears m;
	
	public EquipRunnable(ArenaPlayer ap, BetterGears mod) {
		p = ap;
		m = mod;
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, this, 10L);
	}

	@Override
	public void run() {
		m.handler.equip(p);
	}

}
