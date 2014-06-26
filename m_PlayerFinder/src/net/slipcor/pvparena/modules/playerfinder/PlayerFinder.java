package net.slipcor.pvparena.modules.playerfinder;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PlayerFinder extends ArenaModule implements Listener {
    public PlayerFinder() {
        super("PlayerFinder");
    }

    boolean setup = false;

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    @Override
    public void parseStart() {
        if (!setup) {
            Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
            setup = true;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFind(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (ArenaPlayer.parsePlayer(player.getName()).getArena() == null) {
            debug.i("No arena!", player);
            return;
        }

        if (!ArenaPlayer.parsePlayer(player.getName()).getArena().equals(arena)) {
            debug.i("Wrong arena!", player);
            return;
        }

        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.COMPASS) {
            debug.i("No compass!", player);
            return;
        }

        int maxRadius = arena.getArenaConfig().getInt(CFG.MODULES_PLAYERFINDER_MAXRADIUS, 100);

        List<Entity> list = player.getNearbyEntities(maxRadius, maxRadius, maxRadius);
        Map<Double, Player> sortMap = new HashMap<Double, Player>();

        debug.i("ok!", player);

        for (Entity e : list) {
            if (e instanceof Player) {
                if (e == player) {
                    continue;
                }

                Player innerPlayer = (Player) e;

                if (ArenaPlayer.parsePlayer(innerPlayer.getName()).getStatus() != Status.FIGHT) {
                    continue;
                }

                debug.i(innerPlayer.getName(), player);
                sortMap.put(player.getLocation().distance(e.getLocation()), innerPlayer);

            }
        }

        if (sortMap.isEmpty()) {

            debug.i("noone there!", player);
            //TODO tell "noone there";
        }

        TreeMap<Double, Player> sortedMap = new TreeMap<Double, Player>(sortMap);

        if (event.getAction() == (Action.LEFT_CLICK_AIR)) {
            debug.i("left");
            for (Player otherPlayer : sortedMap.values()) {
                player.setCompassTarget(otherPlayer.getLocation());
                Arena.pmsg(player, Language.parse(MSG.MODULE_PLAYERFINDER_POINT, otherPlayer.getName()));
                break;
            }
        } else if (event.getAction() == (Action.RIGHT_CLICK_AIR)) {
            debug.i("right");
            for (double d : sortedMap.keySet()) {
                Arena.pmsg(player, Language.parse(MSG.MODULE_PLAYERFINDER_NEAR, String.valueOf((int) d)));
                break;
            }
        }

    }
}
