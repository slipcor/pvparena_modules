package net.slipcor.pvparena.modules.realspectate;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class RealSpectate extends ArenaModule {
    public RealSpectate() {
        super("RealSpectate");
    }

    private RealSpectateListener listener;

    private static final int priority = 2;

    @Override
    public String version() {
        return "v1.3.3.218";
    }

    @Override
    public PACheck checkJoin(final CommandSender sender,
                             final PACheck res, final boolean join) {
        if (join) {
            return res;
        }

        if (arena.getFighters().size() < 1) {
            res.setError(this, Language.parse(MSG.ERROR_NOPLAYERFOUND));
        }

        if (res.getPriority() < priority) {
            res.setPriority(this, priority);
        }
        return res;
    }

    @Override
    public void commitSpectate(final Player player) {
        debug.i("committing REAL spectate", player);
        final ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
        ap.setLocation(new PALocation(ap.get().getLocation()));

        ap.setArena(arena);
        ap.setStatus(Status.WATCH);

        if (ap.getState() == null) {

            final Arena arena = ap.getArena();
            ap.createState(player);
            ArenaPlayer.backupAndClearInventory(arena, player);
            ap.dump();
        }

        debug.i("switching:", player);
        getListener().switchPlayer(player, null, true);
    }

    @Override
    public void parseJoin(final CommandSender sender, final ArenaTeam team) {
        for (final SpectateWrapper sw : getListener().spectated_players.values()) {
            sw.update();
        }
    }


    @Override
    public void parseStart() {
        getListener();
    }

    @Override
    public void reset(final boolean force) {
        getListener();
        final Set<SpectateWrapper> list = new HashSet<>();
        for (final SpectateWrapper sw : getListener().spectated_players.values()) {
            list.add(sw);
        }

        for (final SpectateWrapper sw : list) {
            sw.stopHard();
        }
        getListener().spectated_players.clear();

    }

    @Override
    public void unload(final Player player) {
        final Set<SpectateWrapper> list = new HashSet<>();
        for (final SpectateWrapper sw : getListener().spectated_players.values()) {
            list.add(sw);
        }


        for (final Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(player);
        }

        for (final SpectateWrapper sw : list) {
            if (sw.hasSpectator(player)) {
                sw.removeSpectator(player);
                return;
            }
        }

        if (arena.getFighters().size() < 1) {
            final Set<SpectateWrapper> list2 = new HashSet<>();
            for (final SpectateWrapper sw : getListener().spectated_players.values()) {
                list2.add(sw);
            }

            for (final SpectateWrapper sw : list2) {
                sw.stopSpectating();
            }
            getListener().spectated_players.clear();
        }
    }

    RealSpectateListener getListener() {
        if (listener == null) {
            listener = new RealSpectateListener(this);
            Bukkit.getPluginManager().registerEvents(listener, PVPArena.instance);
        }
        return listener;
    }


}
