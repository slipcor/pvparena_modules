package net.slipcor.pvparena.modules.battlefieldguard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.slipcor.pvparena.api.PVPArenaAPI;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.managers.ArenaManager;

public class BattleRunnable implements Runnable {
	private Debug db = new Debug(42);

	/**
	 * construct a powerup spawn runnable
	 * 
	 * @param a
	 *            the arena it's running in
	 */
	public BattleRunnable() {
		db.i("BattleRunnable constructor");
	}

	/**
	 * the run method, spawn a powerup
	 */
	@Override
	public void run() {
		if (!Debug.override)
			db.i("BattleRunnable commiting");
		try {
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				ArenaPlayer ap = ArenaPlayer.parsePlayer(p.getName());
				
				String name = PVPArenaAPI.getArenaNameByLocation(p.getLocation());
				
				if (p.hasPermission("pvparena.admin")) {
					continue;
				}

				if (!Debug.override)
				db.i("arena: " + String.valueOf(name));
				
				if (name == null || name.equals("")) {
					continue; // not physically in an arena
				}
				
				if (ap.getArena() == null || !ap.getArena().getName().equals(name)) {
					
					if (ap.getArena() != null) {
						if (ap.getArena().getArenaConfig().getBoolean(CFG.MODULES_BATTLEFIELDGUARD_ENTERDEATH)) {
							ap.get().setLastDamageCause(new EntityDamageEvent(ap.get(), DamageCause.CUSTOM, 1000));
							ap.get().setHealth(0);
							ap.get().damage(1000);
						} else {
							ap.getArena().playerLeave(p, CFG.TP_EXIT, false);
						}
						continue;
					}
					
					Arena a = ArenaManager.getArenaByName(name);
					if (a.getArenaConfig().getBoolean(CFG.MODULES_BATTLEFIELDGUARD_ENTERDEATH)) {
						p.setHealth(0);
						p.damage(1000);
					} else {
						a.tpPlayerToCoordName(p, "exit");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
