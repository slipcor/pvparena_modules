package net.slipcor.pvparena.modules.announcements;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Set;

public final class Announcement {
    private static final Debug debug = new Debug(7);

    public enum type {
        JOIN, ADVERT, START, END, WINNER, LOSER, PRIZE, CUSTOM
    }

    private Announcement() {

    }

    /**
     * Announce a message to the public
     *
     * @param a       the arena from where the announcement should come
     * @param t       the type of announcement
     * @param message the message to announce
     */
    static void announce(final Arena a, final type t, final String message) {
        if (!sendCheck(a, t)) {
            return; // do not send the announcement type
        }
        debug.i("announce [" + a.getName() + "] type: " + t.name() + " : "
                + message);

        if (AnnouncementManager.announced.contains(a.getName()) && (t == type.ADVERT)) {
            debug.i("skip because we did!");
            return;
        } else if (t == type.ADVERT) {
            AnnouncementManager.announced.add(a.getName());
        }

        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (a.hasPlayer(p) || ArenaPlayer.parsePlayer(p.getName()).isIgnoringAnnouncements()) {
                continue;
            }
            send(a, p, message.replace(
                    ChatColor.WHITE.toString(),
                    ChatColor.valueOf(
                            a.getArenaConfig().getString(
                                    CFG.MODULES_ANNOUNCEMENTS_COLOR))
                            .toString()));
        }
    }

    /**
     * check the arena for the announcement tyoe
     *
     * @param a the arena to check
     * @param t the announcement type to check
     * @return true if the arena is configured to send this announcement type,
     * false otherwise
     */
    private static boolean sendCheck(final Arena a, final type t) {
        final CFG cfg = CFG.valueOf("MODULES_ANNOUNCEMENTS_" + t.name());
        return a.getArenaConfig().getBoolean(cfg);
    }

    /**
     * send an announcement to a player
     *
     * @param a       the arena sending the announcement
     * @param p       the player to send the message
     * @param message the message to send
     */
    private static void send(final Arena a, final Player p, final String message) {
        if (a.getArenaConfig().getInt(CFG.MODULES_ANNOUNCEMENTS_RADIUS) > 0) {
            final Set<ArenaRegion> bfs = a
                    .getRegionsByType(RegionType.BATTLE);
            for (final ArenaRegion ars : bfs) {
                if (ars.getShape().tooFarAway(
                        a.getArenaConfig().getInt(
                                CFG.MODULES_ANNOUNCEMENTS_RADIUS),
                        p.getLocation())) {
                    return; // too far away: out (checks world!)
                }
            }
        }
        a.msg(p,
                ChatColor.valueOf(a.getArenaConfig().getString(
                        CFG.MODULES_ANNOUNCEMENTS_COLOR)) + message);
    }

}
