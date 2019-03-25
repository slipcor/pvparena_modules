package net.slipcor.pvparena.modules.battlefieldguard;

import com.google.common.collect.ImmutableMap;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.api.PVPArenaAPI;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.EnumMap;

class BattleRunnable implements Runnable {
    private final Debug debug = new Debug(42);

    /**
     * construct a battle runnable
     */
    public BattleRunnable() {
        debug.i("BattleRunnable constructor");
    }

    /**
     * the run method, spawn a powerup
     */
    @Override
    public void run() {
        if (!Debug.override) {
            debug.i("BattleRunnable commiting");
        }
        try {
            for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
                final ArenaPlayer ap = ArenaPlayer.parsePlayer(p.getName());

                final String name = PVPArenaAPI.getArenaNameByLocation(p.getLocation());

                if (name == null || name.isEmpty()) {
                    continue; // not physically in an arena
                }

                if (PVPArena.hasAdminPerms(p)) {
                    continue;
                }

                if (!Debug.override) {
                    debug.i("arena pos: " + name, p);
                    debug.i("arena IN : " + ap.getArena(), p);
                }

                if (ap.getArena() == null || !ap.getArena().getName().equals(name)) {
                    if (ap.getArena() != null) {
                        if (ap.getArena().getArenaConfig().getBoolean(CFG.MODULES_BATTLEFIELDGUARD_ENTERDEATH)) {
                            ap.get().setLastDamageCause(
                                    new EntityDamageEvent(ap.get(), DamageCause.CUSTOM,
                                            1000.0));
                            ap.get().setHealth(0);
                            ap.get().damage(1000);
                        } else {
                            ap.getArena().playerLeave(p, CFG.TP_EXIT, false, false, true);
                        }
                        continue;
                    }

                    final Arena a = ArenaManager.getArenaByName(name);
                    if (a.getArenaConfig().getBoolean(CFG.MODULES_BATTLEFIELDGUARD_ENTERDEATH)) {
                        p.setLastDamageCause(new EntityDamageEvent(p, DamageCause.CUSTOM,1000.0));
                        p.setHealth(0);
                        p.damage(1000);
                    } else {
                        a.tpPlayerToCoordName(p, "exit");
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
