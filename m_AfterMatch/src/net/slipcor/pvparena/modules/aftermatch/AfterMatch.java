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
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.loadables.ArenaModule;

public class AfterMatch extends ArenaModule {
	protected HashMap<Arena, Integer> runnables = new HashMap<Arena, Integer>();

	public AfterMatch() {
		super("AfterMatch");
	}

	@Override
	public String version() {
		return "v0.8.10.2";
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
		a.tellEveryone(Language.parse("aftermatch"));
		HashSet<String> lives = new HashSet<String>();
		
		for (String s : a.lives.keySet()) {
			lives.add(s);
		}
		
		for (String s : lives) {
			a.lives.put(s, 0);
		}
	}

	public String checkSpawns(Set<String> list) {
		/*
		for (String s : list) {
			if (s.startsWith("after")) {
				return null;
			}
		}
		return "after not set";*/
		return null;
	}

	@Override
	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
		db.i("aftermatch command?");
		if (!(sender instanceof Player)) {
			Language.parse(MSG.ERROR_ONLY_PLAYERS);
			return;
		}
		String pu = arena.getArenaConfig().getString("aftermatch.aftermatch", "off");

		if (pu.equals("off")) {
			return;
		}
		if (!args[0].startsWith("after")) {
			return;
		}
		db.i("aftermatch command!");

		Player player = (Player) sender;

		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			arena.msg(player,
					Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}
		SpawnManager.setCoords(arena, player, args[0]);
		ArenaManager.tellPlayer(player, Language.parse(MSG.SPAWN_SET, args[0]));
		return;
	}

	@Override
	public void commitPlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
		String pu = arena.getArenaConfig().getString("aftermatch.aftermatch", "off");

		if (pu.equals("off")) {
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
	public HashSet<String> getAddedSpawns() {
		HashSet<String> result = new HashSet<String>();

		result.add("after");

		return result;
	}

	public void initLanguage(YamlConfiguration config) {
		config.addDefault("lang.aftermatch", "The aftermatch has begun!");
	}

	@Override
	public boolean parseCommand(String cmd) {
		return cmd.startsWith("after");
	}

	@Override
	public void parseInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§bAfterMatch:§f "
				+ StringParser.colorVar(!arena.getArenaConfig().getString("aftermatch.aftermatch").equals("off"))
				+ "("
				+ StringParser.colorVar(arena.getArenaConfig()
						.getString("aftermatch.aftermatch")) + ")");
	}

	@Override
	public void reset(Arena arena, boolean force) {
		String pu = arena.getArenaConfig().getString("aftermatch.aftermatch", "off");

		if (pu.equals("off")) {
			return;
		}
		if (runnables.containsKey(arena)) {
			Bukkit.getScheduler().cancelTask(runnables.get(arena));
			runnables.remove(arena);
		}
		/*
		if (playerCount.containsKey(arena)) {
			Bukkit.getScheduler().cancelTask(playerCount.get(arena));
			playerCount.remove(arena);
		}*/
	}

	@Override
	public void teleportAllToSpawn(Arena arena) {
		String pu = arena.getArenaConfig().getString("aftermatch.aftermatch", "off");

		if (pu.equals("off")) {
			return;
		}
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
				+ arena.getArenaConfig().getString("aftermatch.aftermatch", "off") + " : "
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
