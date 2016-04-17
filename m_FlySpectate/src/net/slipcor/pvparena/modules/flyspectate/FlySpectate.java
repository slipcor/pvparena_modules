package net.slipcor.pvparena.modules.flyspectate;


import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.commands.PAG_Leave;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.events.PAJoinEvent;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlySpectate extends ArenaModule {
    public FlySpectate() {
        super("FlySpectate");
    }

    private RealSpectateListener listener;

    private static final int priority = 3;

    @Override
    public String version() {
        return "v1.3.2.103";
    }

    @Override
    public PACheck checkJoin(final CommandSender sender,
                             final PACheck res, final boolean join) {
        if (join && (arena.getArenaConfig().getBoolean(CFG.PERMS_JOININBATTLE) || !arena.isFightInProgress())) {
            return res;
        }

        if (arena.getFighters().size() < 1) {
            res.setError(this, Language.parse(MSG.ERROR_NOPLAYERFOUND));
        }

        if (res.getPriority() < priority || join && res.hasError()) {
            res.setPriority(this, priority);
        }
        return res;
    }

    RealSpectateListener getListener() {
        if (listener == null) {
            listener = new RealSpectateListener(this);
            Bukkit.getPluginManager().registerEvents(listener, PVPArena.instance);
        }
        return listener;
    }

    @Override
    public void commitJoin(final Player player, final ArenaTeam team) {
        class RunLater implements Runnable {

            @Override
            public void run() {
                commitSpectate(player);
            }

        }
        Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 3L);
    }

    @Override
    public void commitSpectate(final Player player) {
        debug.i("committing FLY spectate", player);

        final PAJoinEvent event = new PAJoinEvent(arena, player, true);
        Bukkit.getPluginManager().callEvent(event);
        final ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
        if (event.isCancelled() || arena.equals(ap.getArena())) {
            if (arena.equals(ap.getArena())) {
                arena.msg(player, Language.parse(MSG.ERROR_ARENA_ALREADY_PART_OF, arena.getName()));
            } else {
                debug.i("joining spectate cancelled by eventhandler");
            }
            return;
        }

        ap.setLocation(new PALocation(ap.get().getLocation()));

        ap.debugPrint();

        ap.setArena(arena);
        ap.setStatus(Status.WATCH);
        ap.setTeleporting(true);
        debug.i("switching:", player);
        getListener().hidePlayerLater(player);

        if (ap.getState() == null) {

            final Arena arena = ap.getArena();

            ap.createState(player);
            ArenaPlayer.backupAndClearInventory(arena, player);
            ap.dump();

        } else {
            new PAG_Leave().commit(arena, player, new String[0]);
            return;
        }


        final long delay = arena.getArenaConfig().getBoolean(CFG.PERMS_FLY) ? 6L : 5L;
        class RunLater implements Runnable {

            @Override
            public void run() {
                arena.tpPlayerToCoordName(player, "spectator");
                player.setGameMode(GameMode.CREATIVE);
                player.setAllowFlight(true);
                player.setFlying(true);
                arena.msg(player, Language.parse(MSG.NOTICE_WELCOME_SPECTATOR));
                ap.setTeleporting(false);
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new RunLater(), delay);
    }

    @Override
    public void parseJoin(final CommandSender sender, final ArenaTeam team) {
        getListener().hideAllSpectatorsLater();
    }

    @Override
    public void reset(final boolean force) {
        if (listener != null) {
            listener.stop();
        }
    }

    @Override
    public void unload(final Player player) {
        for (final Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(player);
        }

        listener.removeSpectator(player);

        player.setAllowFlight(false);
        player.setFlying(false);
    }
}
