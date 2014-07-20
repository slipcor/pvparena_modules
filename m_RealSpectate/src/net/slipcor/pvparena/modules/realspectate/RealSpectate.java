package net.slipcor.pvparena.modules.realspectate;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAJoinEvent;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class RealSpectate extends ArenaModule {
    public RealSpectate() {
        super("RealSpectate");
    }

    RealSpectateListener listener = null;

    int priority = 2;

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    @Override
    public PACheck checkJoin(CommandSender sender,
                             PACheck res, boolean join) {
        if (join)
            return res;

        if (arena.getFighters().size() < 1) {
            res.setError(this, Language.parse(MSG.ERROR_NOPLAYERFOUND));
        }

        if (res.getPriority() < priority) {
            res.setPriority(this, priority);
        }
        return res;
    }

    @Override
    public void commitSpectate(Player player) {
        debug.i("committing REAL spectate", player);
        ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
        ap.setLocation(new PALocation(ap.get().getLocation()));


        if (ap.getState() == null) {

            final Arena arena = ap.getArena();

            final PAJoinEvent event = new PAJoinEvent(arena, player, false);
            Bukkit.getPluginManager().callEvent(event);

            ap.createState(player);
            ArenaPlayer.backupAndClearInventory(arena, player);
            ap.dump();


            if (ap.getArenaTeam() != null && ap.getArenaClass() == null) {
                final String autoClass = arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);
                if (autoClass != null && !autoClass.equals("none") && arena.getClass(autoClass) != null) {
                    arena.chooseClass(player, null, autoClass);
                }
                if (autoClass == null) {
                    arena.msg(player, Language.parse(MSG.ERROR_CLASS_NOT_FOUND, "autoClass"));
                    return;
                }
            }
        }

        ap.setArena(arena);
        ap.setStatus(Status.WATCH);
        debug.i("switching:", player);
        getListener().switchPlayer(player, null, true);
    }

    @Override
    public void parseJoin(CommandSender sender, ArenaTeam team) {
        for (SpectateWrapper sw : getListener().spectated_players.values()) {
            sw.update();
        }
    }


    @Override
    public void parseStart() {
        getListener();
    }

    @Override
    public void reset(boolean force) {
        getListener();
        HashSet<SpectateWrapper> list = new HashSet<SpectateWrapper>();
        for (SpectateWrapper sw : getListener().spectated_players.values()) {
            list.add(sw);
        }

        for (SpectateWrapper sw : list) {
            sw.stopHard();
        }
        getListener().spectated_players.clear();

    }

    @Override
    public void unload(Player player) {
        HashSet<SpectateWrapper> list = new HashSet<SpectateWrapper>();
        for (SpectateWrapper sw : getListener().spectated_players.values()) {
            list.add(sw);
        }


        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(player);
        }

        for (SpectateWrapper sw : list) {
            if (sw.hasSpectator(player)) {
                sw.removeSpectator(player);
                return;
            }
        }

        if (arena.getFighters().size() < 1) {
            HashSet<SpectateWrapper> list2 = new HashSet<SpectateWrapper>();
            for (SpectateWrapper sw : getListener().spectated_players.values()) {
                list2.add(sw);
            }

            for (SpectateWrapper sw : list2) {
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
