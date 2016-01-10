package net.slipcor.pvparena.modules.spectate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class SpectateListener implements Listener {
    private final Set<Player> spectators = new HashSet<>();

    public void removeSpectator(final Player spectator) {
        spectators.remove(spectator);
    }

    public void stop() {
        final Collection<Player> removals = new HashSet<>();
        removals.addAll(spectators);
        for (final Player p : removals) {
            Bukkit.getServer().dispatchCommand(p, "pa leave");
        }
        spectators.clear();
    }
}
