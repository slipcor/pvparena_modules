package net.slipcor.pvparena.modules.specialjoin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class SpecialJoin extends ArenaModule implements Listener {
	public SpecialJoin() {
		super("SpecialJoin");
	}
	static HashMap<PABlockLocation, Arena> places = new HashMap<PABlockLocation, Arena>();
	static HashMap<String, Arena> selections = new HashMap<String, Arena>();
	
	@Override
	public String version() {
		return "v0.10.0.0";
	}
	
	@Override
	public boolean checkCommand(String s) {
		return s.toLowerCase().equals("setjoin");
	}
	
	@Override
	public void configParse(YamlConfiguration config) {
		List<String> res;
		
		try {
			res = config.getStringList("modules.specialjoin.places");
			for (String s : res) {
				places.put(Config.parseBlockLocation(s), arena);
			}
		} catch (Exception e) {
			
		}
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			arena.msg(sender,
					Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}
		
		// /pa [arenaname] setjoin
		
		if (selections.containsKey(sender.getName())) {
			// remove & announce
			selections.remove(sender.getName());
			arena.msg(sender,
					Language.parse(MSG.MODULE_SPECIALJOIN_STOP));
		} else {
			// add & announce
			selections.put(sender.getName(), arena);
			arena.msg(sender,
					Language.parse(MSG.MODULE_SPECIALJOIN_START));
		}
	}
	
	@Override
	public void parseEnable() {
		Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
	}
	
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
			if (a == null || !a.equals(arena)) {			
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
			
			if (!a.equals(arena)) {
				return;
			}
			
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
		if (a == null || !a.equals(arena)) {			
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
