package net.slipcor.pvparena.modules.playerfinder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class PlayerFinder extends ArenaModule implements Listener {
	public PlayerFinder() {
		super("PlayerFinder");
	}
	
	boolean setup = false;
	
	@Override
	public String version() {
		return "v1.0.1.56";
	}
	
	@Override
	public void onThisLoad() {
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
		
		if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.COMPASS) {
			debug.i("No compass!", player);
			return;
		}

		List<Entity> list = player.getNearbyEntities(100, 100, 100);
		Map<Double, Player> sortMap = new HashMap<Double, Player>();

		debug.i("ok!", player);
		
		for (Entity e : list) {
			if (e instanceof Player) {
				if (e == player) {
					continue;
				}
				debug.i(((Player) e).getName(), player);
				sortMap.put(player.getLocation().distance(e.getLocation()), (Player) e);
				
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
