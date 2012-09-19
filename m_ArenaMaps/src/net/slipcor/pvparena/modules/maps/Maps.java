package net.slipcor.pvparena.modules.maps;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.goals.GoalFlags;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;

public class Maps extends ArenaModule {
	private static HashMap<Arena, HashSet<String>> mappings = new HashMap<Arena, HashSet<String>>();
	private static HashMap<Arena, HashSet<MapItem>> items = new HashMap<Arena, HashSet<MapItem>>();
	
	public Maps() {
		super("ArenaMaps");
	}
	
	@Override
	public String version() {
		return "v0.8.4.0";
	}
	
	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		config.addDefault("maps.playerPosition", Boolean.valueOf(false));
		config.addDefault("maps.showSpawns", Boolean.valueOf(true));
		config.addDefault("maps.showPlayers", Boolean.valueOf(true));
		config.addDefault("maps.showLives", Boolean.valueOf(true));
		config.options().copyDefaults(true);
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new MapListener(), PVPArena.instance);
	}

	public static HashSet<MapItem> getItems(Arena arena) {
		return items.get(arena) != null ? items.get(arena) : new HashSet<MapItem>();
	}
	
	@Override
	public void parseJoin(Arena arena, CommandSender sender, ArenaTeam team) {
		Player player = (Player) sender;
		HashSet<String> maps = new HashSet<String>();
		if (mappings.get(arena) == null) {
			maps = new HashSet<String>();
			prepareSpawnLocations(arena);
		} else {
			maps = mappings.get(arena);
		}
		
		maps.add(player.getName());
		
		items.get(arena).add(new MapItem(arena, player, team.getColor()));
		mappings.put(arena, maps);
	}
	
	private void prepareSpawnLocations(Arena arena) {
		if (items.containsKey(arena)) {
			items.remove(arena);
			// recalculate, in case admin added stuff
		}
		
		HashSet<MapItem> locations = new HashSet<MapItem>();
		
		for (ArenaTeam team : arena.getTeams()) {
			HashSet<PALocation> locs = SpawnManager.getSpawns(arena, team.getName());
			for (PALocation loc : locs) {
				locations.add(new MapItem(arena, new PABlockLocation(loc.toLocation()), team.getColor()));
			}
			
			for (ArenaGoal goal : arena.getGoals()) {
				if (!(goal instanceof GoalFlags)) {
					continue;
				}
				locs = SpawnManager.getSpawns(arena, team.getName() + "flag");
				for (PALocation loc : locs) {
					locations.add(new MapItem(arena, new PABlockLocation(loc.toLocation()), team.getColor()));
				}
			}
		}
		items.put(arena, locations);
	}

	@Override
	public void reset(Arena arena, boolean force) {
		mappings.remove(arena);
	}
	
	@Override
	public void teleportAllToSpawn(Arena arena) {
		HashSet<String> maps = mappings.get(arena);
		
		if (maps == null) {
			return;
		}
		
		for (String playerName : maps) {
			Player player = Bukkit.getPlayerExact(playerName);
			if (player == null) {
				continue;
			}
			if (!arena.hasPlayer(player)) {
				continue;
			}
			Short value = MyRenderer.getId(playerName);
			player.getInventory().addItem(new ItemStack(Material.MAP, 1, value));
			maps.add(player.getName());
			if (value != Short.MIN_VALUE) {
				MapView map = Bukkit.getMap(value);

				MapRenderer mr = new MyRenderer();
				map.addRenderer(mr);
			}
		}
		mappings.put(arena, maps);
	}
	
	public static boolean hasCustomMap(String sPlayer) {
		if (mappings == null) {
			return false;
		}
		for (Arena arena : mappings.keySet()) {
			if (mappings.get(arena).contains(sPlayer)) {
				return true;
			}
		}
		return false;
	}
}
