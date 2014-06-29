package net.slipcor.pvparena.modules.autosneak;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.events.*;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.ArrayList;
import java.util.Set;

/**
 * AutoSneak module
 *
 * Thanks to CVDH for providing your code!
 */
public class AutoSneak extends ArenaModule implements Listener {

    public AutoSneak() {
        super("autosneak");
    }

    @Override
    public String version() {
        return "v1.3.0.496";
    }

    private final ArrayList<String> sneaker = new ArrayList<String>();

    @Override
    public void configParse(final YamlConfiguration config) {
        Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
    }

    @Override
    public void reset(final boolean force) {
        sneaker.clear();
    }

    @EventHandler
    public void onStart(final PAStartEvent e) {
        if (!e.getArena().equals(arena)) {
            return;
        }
        final Set<String> players = e.getArena().getPlayedPlayers();
        for (String player : players) {
            doSneak(player);
        }
    }

    @EventHandler
    public void onDeath(final PADeathEvent e) {
        if (!e.getArena().equals(arena)) {
            return;
        }
        if (e.isRespawning()) {
            doSneak(e.getPlayer().getName());
        }
    }

    @EventHandler
    public void OnLeave(final PALeaveEvent e) {
        if (!e.getArena().equals(arena)) {
            return;
        }
        if (sneaker.contains(e.getPlayer().getName())) {
            stopSneak(e.getPlayer().getName());
        }
    }

    @EventHandler
    public void OnExit(final PAExitEvent e) {
        if (!e.getArena().equals(arena)) {
            return;
        }
        if (sneaker.contains(e.getPlayer().getName())) {
            stopSneak(e.getPlayer().getName());
        }
    }

    @EventHandler
    public void onSneak(final PlayerToggleSneakEvent e) {
        if (sneaker.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
            e.getPlayer().setSneaking(true);
        }
    }

    @EventHandler
    public void OnEnd(final PAEndEvent e) {
        if (!e.getArena().equals(arena)) {
            return;
        }
        for (String p : e.getArena().getPlayedPlayers()) {
            if (sneaker.contains(p)) {
                stopSneak(p);
            }
        }
    }

    protected void doSneak(final String player) {
        Bukkit.getPlayer(player).setSneaking(true);
        sneaker.add(player);
    }

    protected void stopSneak(final String player) {
        Bukkit.getPlayer(player).setSneaking(false);
        sneaker.remove(player);
    }
}

