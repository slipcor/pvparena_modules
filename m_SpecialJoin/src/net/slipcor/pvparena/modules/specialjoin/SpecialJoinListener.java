package net.slipcor.pvparena.modules.specialjoin;

import java.util.ArrayList;
import java.util.HashMap;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpecialJoinListener implements Listener {
	/*
	 * PlayerInteract: 
	 *     ButtonJoin
	 *       preset like flags
	 *       'now hit blue join'
	 * Runnable
	 *     Check JoinRegions
	 * 
	 * 
	 */
	
	public static HashMap<PABlockLocation, Arena> places = new HashMap<PABlockLocation, Arena>();
	public static HashMap<String, Arena> selections = new HashMap<String, Arena>();
	
	@EventHandler
	public void onSpecialJoin(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		if (event.getAction().equals(Action.PHYSICAL)) {
			
			// Join via pressure plate
			
			if (event.getPlayer() == null) {
				return;
			}
			PABlockLocation loc = new PABlockLocation(event.getPlayer().getLocation());
			
			Arena a = places.get(loc);
			PAG_Join j = new PAG_Join();
			if (a == null) {			
				return;
			}
			j.commit(a, event.getPlayer(), new String[0]);
			return;
		}
		
		if (!event.hasBlock()) {
			return;
		}
		
		if (selections.containsKey(event.getPlayer().getName())) {
			Arena a = selections.get(event.getPlayer().getName());
			
			Material mat = event.getClickedBlock().getType();
			String place = null;
			
			if (mat == Material.STONE_PLATE || mat == Material.WOOD_PLATE) {
				place = mat.name();
			} else if (mat == Material.STONE_BUTTON || /*mat == Material.BUTTON || */mat == Material.LEVER) {
				place = mat.name();
			} else if (mat == Material.SIGN || mat == Material.SIGN_POST || mat == Material.WALL_SIGN) {
				place = mat.name();
			} else {
				return;
			}
			places.put(new PABlockLocation(event.getClickedBlock().getLocation()), a);
			selections.remove(event.getPlayer().getName());
			a.msg(event.getPlayer(),
					Language.parse(MSG.MODULE_SPECIALJOIN_DONE, place));

			update(a);
			return;
		}
		
		PABlockLocation loc = new PABlockLocation(event.getClickedBlock().getLocation());
		
		Arena a = places.get(loc);

		PAG_Join j = new PAG_Join();
		if (a == null) {			
			return;
		}
		
		Material mat = event.getClickedBlock().getType();
		
		if (mat == Material.STONE_BUTTON || /*mat == Material.BUTTON || */mat == Material.LEVER) {
			j.commit(a, event.getPlayer(), new String[0]);
		} else if (mat == Material.SIGN || mat == Material.SIGN_POST || mat == Material.WALL_SIGN) {
			Sign s = (Sign) event.getClickedBlock().getState();
			String[] arr = new String[1];
			arr[0] = s.getLine(2); // third line
			j.commit(a, event.getPlayer(), arr);
		}
	}

	private void update(Arena a) {
		ArrayList<String> locs = new ArrayList<String>();
		for (PABlockLocation l : places.keySet()) {
			if (places.get(l).equals(a)) {
				locs.add(Config.parseToString(l));
			}
		}
		a.getArenaConfig().setManually("modules.specialjoin.places", locs);
		a.getArenaConfig().save();
	}
}
