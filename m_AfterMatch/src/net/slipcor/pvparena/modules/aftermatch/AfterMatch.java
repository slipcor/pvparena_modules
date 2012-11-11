package net.slipcor.pvparena.modules.aftermatch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;

public class AfterMatch extends ArenaModule {
	protected HashMap<Arena, Integer> runnables = new HashMap<Arena, Integer>();

	public AfterMatch() {
		super("AfterMatch");
	}
	
	private HashSet<Arena> aftermatchs = new HashSet<Arena>();
	

	@Override
	public String version() {
		return "v0.9.6.16";
	}

	public void afterMatch(Arena a) {
		for (ArenaTeam t : a.getTeams()) {
			for (ArenaPlayer p : t.getTeamMembers()) {
				if (!p.getStatus().equals(Status.FIGHT)) {
					continue;
				}
				Player player = p.get();
				a.tpPlayerToCoordName(player, "after");
			}
		}
		a.broadcast(Language.parse(MSG.MODULE_AFTERMATCH_STARTING));
		PVPArena.instance.getAgm().setPlayerLives(a, 0);
		aftermatchs.add(a);
	}

	@Override
	public String checkForMissingSpawns(Arena arena, Set<String> list) {
		
		for (String s : list) {
			if (s.startsWith("after")) {
				return null;
			}
		}
		return MSG.MODULE_AFTERMATCH_SPAWNNOTSET.toString();
	}

	@Override
	public void parsePlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
		String pu = arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH);

		if (pu.equals("off") || aftermatchs.contains(arena)) {
			return;
		}
		
		String[] ss = pu.split(":");
		if (pu.startsWith("time") || runnables.containsKey(arena)) {
			return;
		}

		int i = Integer.parseInt(ss[1]);

		for (ArenaTeam t : arena.getTeams()) {
			for (ArenaPlayer p : t.getTeamMembers()) {
				if (!p.getStatus().equals(Status.FIGHT)) {
					continue;
				}
				if (--i < 0) {
					return;
				}
			}
		}

		runnables.put(arena, 0);

		afterMatch(arena);
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		String pu = config.getString("aftermatch.aftermatch", "off");

		//String[] ss = pu.split(":");
		if (pu.startsWith("death")) {
			//
		} else if (pu.startsWith("time")) {
			// later
		} else {
			db.w("error activating aftermatch module");
		}

		config.addDefault("aftermatch.aftermatch", "off");
		config.options().copyDefaults(true);
	}

	@Override
	public void displayInfo(Arena arena, CommandSender player) {
		player.sendMessage("§bAfterMatch:§f "
				+ StringParser.colorVar(!arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH).equals("off"))
				+ "("
				+ StringParser.colorVar(arena.getArenaConfig()
						.getString(CFG.MODULES_AFTERMATCH_AFTERMATCH)) + ")");
	}
	
	@Override
	public boolean hasSpawn(Arena arena, String string) {
		return string.equals("after");
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return (arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH).contains(":"));
	}

	@Override
	public void reset(Arena arena, boolean force) {
		String pu = arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH);

		if (pu.equals("off")) {
			return;
		}
		if (runnables.containsKey(arena)) {
			Bukkit.getScheduler().cancelTask(runnables.get(arena));
			runnables.remove(arena);
		}
		aftermatchs.remove(arena);
	}

	@Override
	public void teleportAllToSpawn(Arena arena) {
		String pu = arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH);

		if (runnables.containsKey(arena)) {
			Bukkit.getScheduler().cancelTask(runnables.get(arena));
			runnables.remove(arena);
		}

		int i = 0;
		String[] ss = pu.split(":");
		if (pu.startsWith("time")) {
			// arena.powerupTrigger = "time";
			i = Integer.parseInt(ss[1]);
		} else {
			return;
		}

		db.i("using aftermatch : "
				+ arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH) + " : "
				+ i);
		if (i > 0) {
			db.i("aftermatch time trigger!");
			runnables.put(
					arena,
					Bukkit.getScheduler().scheduleSyncDelayedTask(
							PVPArena.instance, new AfterRunnable(arena, this),
							i * 20L));
		}
	}
}
