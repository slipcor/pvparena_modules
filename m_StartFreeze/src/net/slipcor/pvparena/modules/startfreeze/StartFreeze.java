package net.slipcor.pvparena.modules.startfreeze;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerMoveEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.loadables.ArenaModule;

public class StartFreeze extends ArenaModule {
	protected static HashMap<Arena, StartFreezer> runnables = new HashMap<Arena, StartFreezer>();
	protected static HashMap<Arena, Integer> ids = new HashMap<Arena, Integer>();

	public StartFreeze() {
		super("StartFreeze");
	}

	@Override
	public String version() {
		return "v0.7.20.0";
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		config.addDefault("freeze.timer", Integer.valueOf(10));
	}

	@Override
	public void initLanguage(YamlConfiguration config) {
		config.addDefault("lang.startfreeze",
				"The game will start in %1% seconds!");
	}

	@Override
	public void reset(Arena arena, boolean force) {
		if (runnables.containsKey(arena))
			Bukkit.getScheduler().cancelTask(ids.get(arena));
		runnables.remove(arena);
		ids.remove(arena);
	}

	@Override
	public void teleportAllToSpawn(Arena arena) {
		StartFreezer sf = new StartFreezer(arena);
		runnables.put(arena, sf);
		ids.put(arena,
				Bukkit.getScheduler().scheduleSyncDelayedTask(
						PVPArena.instance, sf,
						arena.getArenaConfig().getInt("freeze.timer") * 20L));
		arena.tellEveryone(Language.parse("startfreeze",
				String.valueOf(arena.getArenaConfig().getInt("freeze.timer"))));
	}

	@Override
	public void parseMove(Arena arena, PlayerMoveEvent event) {
		if (runnables.containsKey(arena)) {
			Location from = event.getFrom();
			Location to = event.getTo();
			if ((from.getBlockX() != to.getBlockX()) ||
					(from.getBlockY() != to.getBlockY()) ||
					(from.getBlockZ() != to.getBlockZ())) {
			event.setCancelled(true);
			}
		}
	}
}
