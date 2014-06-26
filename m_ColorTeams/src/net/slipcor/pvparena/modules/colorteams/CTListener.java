package net.slipcor.pvparena.modules.colorteams;

import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

import java.util.HashSet;

public class CTListener implements Listener {
    HashSet<String> removals = new HashSet<String>();

    @EventHandler
    public void onNameReceive(AsyncPlayerReceiveNameTagEvent event) {
        Player p = event.getNamedPlayer();

        if (p == null) {
            return;
        }

        ArenaPlayer ap = ArenaPlayer.parsePlayer(p.getName());

        if (ap == null || ap.getArena() == null) {
            if (removals.contains(ap.getName())) {
                event.setTag(ap.getName());
            }
            return;
        }

        if (ap.getArena().getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD)) {
            return;
        }

        boolean found = false;

        for (ArenaModule mod : ap.getArena().getMods()) {
            if (mod.getName().equals("ColorTeams")) {
                found = true;
                break;
            }
        }

        if (!found) {
            return;
        }

        if (ap.getArena().getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_HIDENAME)) {
            event.setTag(" ");
            return;
        }

        for (ArenaTeam at : ap.getArena().getTeams()) {
            if (at.getTeamMembers().contains(ap)) {
                event.setTag(at.colorizePlayer(p));
                removals.add(ap.getName());
                return;
            }
        }
    }
}
