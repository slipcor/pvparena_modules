package net.slipcor.pvparena.modules.latelounge;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.command.PAAJoin;
import net.slipcor.pvparena.command.PAA_Command;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaModule;

public class LateLounge extends ArenaModule {
	public LateLounge() {
		super("LateLounge");
	}
	
	@Override
	public String version() {
		return "v0.8.4.2";
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
	public boolean checkJoin(Arena arena, Player player) {
		if (arena.cfg.getInt("ready.min") < 1 || !arena.cfg.getBoolean("latelounge.latelounge")) {
			return true;
		}
		HashSet<String> list = new HashSet<String>();
		
		if (players.get(arena) != null) {
			list = players.get(arena);
		}
		
		if (list.contains(player.getName())) {
			Arenas.tellPlayer(player, Language.parse("lang.lateloungewait"));
			return list.size() >= arena.cfg.getInt("ready.min");
		}
		
		if (arena.cfg.getInt("ready.min") > list.size() + 1) {
			// not enough players
			list.add(player.getName());
			players.put(arena, list);
			Player[] aPlayers = Bukkit.getOnlinePlayers();
			
			for (Player p : aPlayers) {
				if (p.equals(player)) {
					continue;
				}
				try {
					Arenas.tellPlayer(p, Language.parse("lateloungeannounce", arena.name, player.getName()));
				} catch (Exception e) {
					//
				}
			}
			Arenas.tellPlayer(player, Language.parse("lang.lateloungewait"));
			return false;
		} else if (arena.cfg.getInt("ready.min") == list.size() + 1) {
			// not enough players
			list.add(player.getName());
			players.put(arena, list);
			
			HashSet<String> removals = new HashSet<String>();
			
			for (String s : list) {
				Player p = Bukkit.getPlayerExact(s);
				if (p == null || !PVPArena.instance.getAmm().checkJoin(arena, player)) {
					removals.add(s);
					if (p != null) {
						Arenas.tellPlayer(p, Language.parse("lateloungerejoin"));
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
					PAA_Command command = new PAAJoin();
					command.commit(arena, p, null);
				}
				return true;
			}
			Arenas.tellPlayer(player, Language.parse("lang.lateloungewait"));
			return false;
		}
		
		return true;
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config, String type) {
		config.addDefault("latelounge.latelounge", Boolean.valueOf(false));

		config.options().copyDefaults(true);
	}

	@Override
	public void initLanguage(YamlConfiguration config) {
		config.addDefault("lang.lateloungewait", "Arena will be starting soon, please wait!");
		config.addDefault("lang.lateloungeannounce", "Arena %1% is starting! Player %2% wants to start. Join with /pa %1%");
		config.addDefault("lang.lateloungerejoin", "Ready check has caught you not being able to join. Rejoin when you can!");
		config.options().copyDefaults(true);
	}
	
	@Override
	public void reset(Arena arena, boolean force) {
		players.remove(arena);
	}
}
