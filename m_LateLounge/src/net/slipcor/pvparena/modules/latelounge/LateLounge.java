package net.slipcor.pvparena.modules.latelounge;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.PAA__Command;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class LateLounge extends ArenaModule {
	public LateLounge() {
		super("LateLounge");
	}
	
	int priority = 3; 
	
	@Override
	public String version() {
		return "v0.9.3.8";
	}
	
	private static HashMap<Arena, HashSet<String>> players = new HashMap<Arena, HashSet<String>>();
	
	/**
	 * hook into a player trying to join the arena
	 * 
	 * @param arena
	 *            the arena the player wants to join
	 * @param player
	 *            the trying player
	 * @return false if a player should not be granted permission
	 */
	@Override
	public PACheck checkJoin(Arena arena, CommandSender sender,
			PACheck res, boolean b) {
		if (res.hasError() || res.getPriority() > priority) {
			return res;
		}
		
		HashSet<String> list = new HashSet<String>();
		
		if (players.get(arena) != null) {
			list = players.get(arena);
		}
		
		Player player = (Player) sender;
		
		if (list.contains(player.getName())) {
			if (list.size() < arena.getArenaConfig().getInt(CFG.READY_MINPLAYERS)) {
				res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
				return res;
			}
		}
		
		if (arena.getArenaConfig().getInt(CFG.READY_MINPLAYERS) > list.size() + 1) {
			// not enough players
			list.add(player.getName());
			players.put(arena, list);
			Player[] aPlayers = Bukkit.getOnlinePlayers();
			
			for (Player p : aPlayers) {
				if (p.equals(player)) {
					continue;
				}
				try {
					Arena.pmsg(p, Language.parse(MSG.MODULE_LATELOUNGE_ANNOUNCE, arena.getName(), player.getName()));
				} catch (Exception e) {
					//
				}
			}
			res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
			return res;
		} else if (arena.getArenaConfig().getInt(CFG.READY_MINPLAYERS) == list.size() + 1) {
			// not enough players
			list.add(player.getName());
			players.put(arena, list);
			
			HashSet<String> removals = new HashSet<String>();
			
			for (String s : list) {
				Player p = Bukkit.getPlayerExact(s);
				
				boolean removeMe = false;
				
				if (p != null) {
					for (ArenaModule mod : PVPArena.instance.getAmm().getModules()) {
						if (mod.isActive(arena) && !mod.getName().equals(getName())) {
							if (mod.checkJoin(arena, p, new PACheck(), true).hasError()) {
								removeMe = true;
								break;
							}
						}
					}
				}
				
				if (p == null || removeMe) {
					removals.add(s);
					if (p != null) {
						res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_REJOIN));
					}
				}
			}

			if (removals.size() > 0) {
				for (String s : removals) {
					list.remove(s);
				}
				players.put(arena, list);
			} else {
				for (String s : list) {
					Player p = Bukkit.getPlayerExact(s);
					PAA__Command command = new PAG_Join();
					command.commit(arena, p, new String[0]);
				}
				return res;
			}
			res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
		}
		return res;
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return arena.getArenaConfig().getBoolean(CFG.MODULES_LATELOUNGE_ACTIVE);
	}
	
	@Override
	public void reset(Arena arena, boolean force) {
		players.remove(arena);
	}
}
