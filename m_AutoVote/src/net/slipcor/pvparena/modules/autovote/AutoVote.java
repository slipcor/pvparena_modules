package net.slipcor.pvparena.modules.autovote;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.command.PAAJoin;
import net.slipcor.pvparena.command.PAA_Command;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaModule;
import net.slipcor.pvparena.runnables.StartRunnable;

public class AutoVote extends ArenaModule {
	private static Arena a = null;
	private static HashMap<String, String> votes = new HashMap<String, String>();
	public AutoVote() {
		super("AutoJoin");
	}

	@Override
	public String version() {
		return "v0.8.11.0";
	}
	
	@Override
	public boolean checkJoin(Arena arena, Player player) {
		if (a != null && !arena.equals(a)) {
			Arenas.tellPlayer(player, Language.parse("arenarunning", arena.name));
			return false;
		}
		return true;
	}

	@Override
	public void addSettings(HashMap<String, String> types) {
		types.put("autojoin.seconds", "int");
	}

	@Override
	public void commitCommand(Arena arena, CommandSender sender, String[] args) {

		if (!args[0].startsWith("vote") || !PAA_Command.checkArgs(sender, args, 2)) {
			return;
		}
		
		Arena aA = Arenas.getArenaByName(args[1]);
		
		if (aA == null) {
			Arenas.tellPlayer(sender, Language.parse("unknownarena") + ": " + args[1]);
			return;
		}
		
		votes.put(sender.getName(), aA.name);
		Arenas.tellPlayer(sender, Language.parse("youvoted",aA.name));
		
		return;
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config, String type) {
		config.addDefault("autojoin.seconds", Integer.valueOf(30));
		config.options().copyDefaults(true);
	}

	public void initLanguage(YamlConfiguration config) {
		config.addDefault("lang.arenarunning", "Arena running: %1%");
		config.addDefault("lang.autojoin", "Arena auto join started!");
		config.addDefault("lang.youvoted", "You voted for arena %1%!");
	}

	@Override
	public boolean parseCommand(String cmd) {
		return cmd.startsWith("vote");
	}

	@Override
	public void parseInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6AutoJoin:§f "
				+ StringParser.colorVar(arena.cfg
						.getBoolean("autojoin.autojoin"))
				+ "("
				+ StringParser.colorVar(arena.cfg
						.getInt("autojoin.seconds")) + ")");
	}

	@Override
	public void reset(Arena arena, boolean force) {
		votes.clear();
		a = null;

		AutoVoteRunnable fr = new AutoVoteRunnable(a, 5, 5);
		fr.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, fr, 20L, 20L));
	}

	public static void commit() {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		int max = 0;
		for (String name : votes.values()) {
			int i = 0;
			if (counts.containsKey(name)) {
				i = counts.get(name);
			}
			
			counts.put(name, ++i);
			
			if (i > max) {
				max = i;
			}
		}
		
		for (String name : counts.keySet()) {
			if (counts.get(name) == max) {
				a = Arenas.getArenaByName(name);
			}
		}
		
		if (a == null) {
			a = Arenas.getFirst();
		}
		
		if (a == null) {
			return;
		}

		PAAJoin pj = new PAAJoin();
		
		for (String pName : votes.keySet()) {
			Player p = Bukkit.getPlayerExact(pName);
			if (p == null) {
				continue;
			}
			
			pj.commit(a, p, null);
		}
		
		StartRunnable fr = new StartRunnable(a, 5, 5);
		fr.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, fr, 20L, 20L));
	}
}
