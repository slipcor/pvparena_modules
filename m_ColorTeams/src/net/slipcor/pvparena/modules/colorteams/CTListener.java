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
import java.util.Set;

class CTListener implements Listener {
    private final Set<String> removals = new HashSet<>();

    @EventHandler
    public void onNameReceive(final AsyncPlayerReceiveNameTagEvent event) {
        final Player p = event.getNamedPlayer();

        if (p == null) {
            return;
        }

        final ArenaPlayer ap = ArenaPlayer.parsePlayer(p.getName());

        if (ap.getArena() == null) {
            if (removals.contains(ap.getName())) {
                event.setTag(ap.getName());
            }
            return;
        }

        if (ap.getArena().getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD)) {
            return;
        }

        boolean found = false;

        for (final ArenaModule mod : ap.getArena().getMods()) {
            if ("ColorTeams".equals(mod.getName())) {
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

        for (final ArenaTeam at : ap.getArena().getTeams()) {
            if (at.getTeamMembers().contains(ap)) {
                event.setTag(at.colorizePlayer(p));
                removals.add(ap.getName());
                return;
            }
        }
    }
}
