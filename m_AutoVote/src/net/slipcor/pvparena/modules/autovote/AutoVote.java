package net.slipcor.pvparena.modules.autovote;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.runnables.StartRunnable;

public class AutoVote extends ArenaModule {
	private static Arena a = null;
	private static HashMap<String, String> votes = new HashMap<String, String>();

	public AutoVote() {
		super("AutoVote");
	}

	@Override
	public String version() {
		return "v0.9.0.0";
	}

	@Override
	public PACheckResult checkJoin(Arena arena, CommandSender sender,
			PACheckResult res, boolean b) {
		if (res.hasError()) {
			return res;
		}
		
		if (a != null && !arena.equals(a)) {
			res.setError(Language.parse(MSG.MODULE_AUTOVOTE_ARENARUNNING, arena.getName()));
			return res;
		}
		
		return res;
	}

	@Override
	public void commitCommand(Arena arena, CommandSender sender, String[] args) {

		if (!args[0].startsWith("vote")) {
			return;
		}

		votes.put(sender.getName(), arena.getName());
		ArenaManager.tellPlayer(sender, Language.parse(MSG.MODULE_AUTOVOTE_YOUVOTED, arena.getName()));
		
		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_ARENAVOTE_EVERYONE)) {
			return;
		}
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p == null) {
				continue;
			}
			ArenaManager.tellPlayer(p, Language.parse(MSG.MODULE_AUTOVOTE_PLAYERVOTED, arena.getName(), sender.getName()));
		}
	}

	@Override
	public void displayInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6ArenaVote:§f "
				+ StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_ARENAVOTE_SECONDS))
				+ " | "
				+ StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_ARENAVOTE_READYUP)));
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return arena.getArenaConfig().getBoolean(CFG.MODULES_ARENAVOTE_ACTIVE);
	}
	
	@Override
	public boolean parseCommand(String cmd) {
		return cmd.startsWith("vote");
	}

	@Override
	public void reset(Arena arena, boolean force) {
		votes.clear();
		a = null;

		AutoVoteRunnable fr = new AutoVoteRunnable(arena,
				arena.getArenaConfig().getInt(CFG.MODULES_ARENAVOTE_SECONDS));
		fr.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, fr, 20L, 20L));
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
				a = ArenaManager.getArenaByName(name);
			}
		}

		if (a == null) {
			a = ArenaManager.getFirst();
		}

		if (a == null) {
			return;
		}

		PAG_Join pj = new PAG_Join();

		HashSet<String> toTeleport = new HashSet<String>();
		
		if (a.getArenaConfig().getBoolean(CFG.MODULES_ARENAVOTE_EVERYONE)) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				toTeleport.add(p.getName());
			}
		} else {
			for (String s : votes.keySet()) {
				toTeleport.add(s);
			}
		}
		
		for (String pName : toTeleport) {
			Player p = Bukkit.getPlayerExact(pName);
			if (p == null) {
				continue;
			}

			pj.commit(a, p, null);
		}

		new StartRunnable(a,
				a.getArenaConfig().getInt(CFG.MODULES_ARENAVOTE_READYUP));
	}
}
