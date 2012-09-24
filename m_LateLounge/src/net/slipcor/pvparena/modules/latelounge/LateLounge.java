package net.slipcor.pvparena.modules.latelounge;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.loadables.ArenaModule;

public class LateLounge extends ArenaModule {
	public LateLounge() {
		super("LateLounge");
	}
	
	int priority = 3; 
	
	@Override
	public String version() {
		return "v0.9.0.0";
	}
	
	private static HashMap<Arena, HashSet<String>> players = new HashMap<Arena, HashSet<String>>();

	@Override
	public void addSettings(HashMap<String, String> types) {
		types.put("latelounge.latelounge", "boolean");
	}
	
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
	public PACheckResult checkJoin(Arena arena, CommandSender sender,
			PACheckResult res, boolean b) {
		if (res.hasError() || res.getPriority() > priority) {
			return res;
		}

		/*
		Player player = (Player) sender;
		
		
		if (arena.getArenaConfig().getInt("ready.min") < 1 || !arena.getArenaConfig().getBoolean("latelounge.latelounge")) {
			return res;
		}
		HashSet<String> list = new HashSet<String>();
		
		if (players.get(arena) != null) {
			list = players.get(arena);
		}
		
		if (list.contains(player.getName())) {
			if (list.size() < arena.getArenaConfig().getInt("ready.min")) {
				res.setError(Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
			}
			return res;
		}
		
		if (arena.getArenaConfig().getInt("ready.min") > list.size() + 1) {
			// not enough players
			list.add(player.getName());
			players.put(arena, list);
			Player[] aPlayers = Bukkit.getOnlinePlayers();
			
			for (Player p : aPlayers) {
				if (p.equals(player)) {
					continue;
				}
				try {
					ArenaManager.tellPlayer(p, Language.parse(MSG.MODULE_LATELOUNGE_ANNOUNCE, arena.getName(), player.getName()));
				} catch (Exception e) {
					//
				}
			}
			res.setError(Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
		} else if (arena.getArenaConfig().getInt("ready.min") == list.size() + 1) {
			// not enough players
			list.add(player.getName());
			players.put(arena, list);
			
			HashSet<String> removals = new HashSet<String>();
			
			for (String s : list) {
				Player p = Bukkit.getPlayerExact(s);
				if (p == null || !PVPArena.instance.getAmm().checkJoin(arena, player)) {
					removals.add(s);
					if (p != null) {
						ArenaManager.tellPlayer(p, Language.parse(MSG.MODULE_LATELOUNGE_REJOIN));
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
					command.commit(arena, p, null);
				}
				return res;
			}
			res.setError(Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
		}
		*/
		res.setPriority(priority);
		res.setModName(getName());
		return res;
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		config.addDefault("latelounge.latelounge", Boolean.valueOf(false));
		config.options().copyDefaults(true);
	}
	
	@Override
	public void reset(Arena arena, boolean force) {
		players.remove(arena);
	}
}
