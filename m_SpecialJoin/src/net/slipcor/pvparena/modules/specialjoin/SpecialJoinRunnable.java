package net.slipcor.pvparena.modules.specialjoin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;
import net.slipcor.pvparena.managers.ArenaManager;

public class SpecialJoinRunnable implements Runnable {
	
	private final SpecialJoin module;
	
	public SpecialJoinRunnable(SpecialJoin specialJoin) {
		module = specialJoin;
	}

	@Override
	public void run() {
		for (Arena a : ArenaManager.getArenas()) {
			if (!module.isActive(a)) {
				continue;
			}
			for (ArenaRegionShape ars : a.getRegionsByType(RegionType.JOIN)) {
				try {
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						if (ars.contains(new PABlockLocation(p.getLocation()))) {
							PAG_Join j = new PAG_Join();
							if (a == null) {			
								return;
							}
							j.commit(a, p, new String[0]);
						}
					}
				} catch (Exception e) {
					
				}
			}
		}
	}

}
