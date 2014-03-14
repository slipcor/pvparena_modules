package net.slipcor.pvparena.modules.autovote;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import sun.security.ssl.Debug;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.events.PAJoinEvent;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.runnables.StartRunnable;

public class AutoVote extends ArenaModule implements Listener {
	private static Map<String, String> votes = new HashMap<String, String>();

	protected AutoVoteRunnable vote = null;
	Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();
	
	public AutoVote() {
		super("AutoVote");
	}

	@Override
	public String version() {
		return "v1.1.2.420";
	}
	
	@Override
	public boolean checkCommand(String cmd) {
		return cmd.startsWith("vote") || cmd.equals("!av") || cmd.equals("autovote") || cmd.equals("votestop");
	}

	@Override
	public PACheck checkJoin(CommandSender sender,
			PACheck res, boolean b) {
		if (res.hasError() || !b) {
			return res;
		}
		
		if (vote != null) {
			res.setError(this, "voting");
			return res;
		}
		
		if (arena.getArenaConfig().getBoolean(CFG.PERMS_JOINWITHSCOREBOARD)) {
			return res;
		}
		
		Player p = (Player) sender;

		for (Team team : p.getScoreboard().getTeams()) {
			for (OfflinePlayer player : team.getPlayers()) {
				if (player.getName().equals(p.getName())) {
					res.setError(this, Language.parse(MSG.ERROR_COMMAND_BLOCKED, "You already have a scoreboard!"));
					return res;
				}
			}
		}
		
		return res;
	}

	@Override
	public void commitCommand(CommandSender sender, String[] args) {

		if (args[0].startsWith("vote")) {
			
			if (!arena.getArenaConfig().getBoolean(CFG.PERMS_JOINWITHSCOREBOARD)) {
				Player p = (Player) sender;

				for (Team team : p.getScoreboard().getTeams()) {
					for (OfflinePlayer player : team.getPlayers()) {
						if (player.getName().equals(p.getName())) {
							return;
						}
					}
				}
			}

			votes.put(sender.getName(), arena.getName());
			Arena.pmsg(sender, Language.parse(MSG.MODULE_AUTOVOTE_YOUVOTED, arena.getName()));
			
			return;
		} else if (args[0].equals("votestop")) {
			if (vote != null) {
				vote.cancel();
			}
		} else {
			if (!PVPArena.hasAdminPerms(sender)
					&& !(PVPArena.hasCreatePerms(sender, arena))) {
				arena.msg(
						sender,
						Language.parse(MSG.ERROR_NOPERM,
								Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
				return;
			}
			
			if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[] { 2,3 })) {
				return;
			}
			
			// !av everyone
			// !av readyup X
			// !av seconds X
			
			if (args.length < 3 || args[1].equals("everyone")) {
				boolean b = arena.getArenaConfig().getBoolean(CFG.MODULES_ARENAVOTE_EVERYONE);
				arena.getArenaConfig().set(CFG.MODULES_ARENAVOTE_EVERYONE, !b);
				arena.getArenaConfig().save();
				arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_ARENAVOTE_EVERYONE.getNode(), String.valueOf(!b)));
				return;
			} else {
				CFG c = null;
				if (args[1].equals("readyup")) {
					c = CFG.MODULES_ARENAVOTE_READYUP;
				} else if (args[1].equals("seconds")) {
					c = CFG.MODULES_ARENAVOTE_SECONDS;
				}
				if (c != null) {
					int i = 0;
					try {
						i = Integer.parseInt(args[2]);
					} catch (Exception e) {
						arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[2]));
						return;
					}
					
					arena.getArenaConfig().set(c, i);
					arena.getArenaConfig().save();
					arena.msg(sender, Language.parse(MSG.SET_DONE, c.getNode(), String.valueOf(i)));
					return;
				}
			}
			
			arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "everyone | readyup | seconds"));
			return;
		}
	}

	@Override
	public void displayInfo(CommandSender player) {
		player.sendMessage("seconds:"
				+ StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_ARENAVOTE_SECONDS))
				+ " | readyup: "
				+ StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_ARENAVOTE_READYUP))
				+ " | "
				+ StringParser.colorVar("everyone", arena.getArenaConfig().getBoolean(CFG.MODULES_ARENAVOTE_EVERYONE)));
	}

	@Override
	public void reset(boolean force) {
		
		String definition = getDefinitionFromArena(arena);
		
		if (definition == null) {
			removeFromVotes(arena.getName());
		} else {
			removeValuesFromVotes(definition);
		}
		
		if (vote == null) {
			
			for (String def : ArenaManager.getShortcutValues().keySet()) {
				if (ArenaManager.getShortcutValues().get(def).equals(arena)) {
					
					vote = new AutoVoteRunnable(arena,
					arena.getArenaConfig().getInt(CFG.MODULES_ARENAVOTE_SECONDS), this, def);
					break;
				}
			}
			arena.getDebugger().i("AutoVote not setup via shortcuts, ignoring");
		}
	}

	private void removeValuesFromVotes(String definition) {
		boolean done = false;
		while (done == false) {
			done = true;
			for (String player : votes.keySet()) {
				if (votes.get(player).equalsIgnoreCase(definition)) {
					votes.remove(player);
					done = false;
					break;
				}
			}
		}
	}

	private void removeFromVotes(String name) {
		List<String> names = ArenaManager.getShortcutDefinitions().get(name);
		
		if (names != null) {
			for (String arena : names) {
				boolean done = false;
				while (done == false) {
					done = true;
					for (String player : votes.keySet()) {
						if (votes.get(player).equalsIgnoreCase(arena)) {
							votes.remove(player);
							done = false;
							break;
						}
					}
				}
			}
		}
	}

	static String getDefinitionFromArena(Arena arena) {
		for (String name : ArenaManager.getShortcutValues().keySet()) {
			if (arena.equals(ArenaManager.getShortcutValues().get(name))) {
				return name;
			}
		}
		
		for (Entry<String, List<String>> name : ArenaManager.getShortcutDefinitions().entrySet()) {
			if (name.getValue().contains(arena.getName())) {
				return name.getKey();
			}
		}
		
		return null;
	}

	public static void commit(String definition, Set<ArenaPlayer> players) {
		HashMap<String, String> tempVotes = new HashMap<String, String>();
		
		debug.i("committing definition " + definition + " for " + players.size());
		
		List<String> arenas = ArenaManager.getShortcutDefinitions().get(definition);
		
		if (arenas == null || arenas.size() < 1) {
			debug.i("this definition has no arenas!");
			return;
		}
		
		for (String node : votes.keySet()) {
			debug.i(node + " voted " + votes.get(node));
			if (!arenas.contains(votes.get(node))) {
				debug.i("not our business!");
				continue;
			}
			tempVotes.put(node, votes.get(node));
		}
		
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		int max = 0;
		
		String voted = null;
		
		for (String name : tempVotes.values()) {
			int i = 0;
			if (counts.containsKey(name)) {
				i = counts.get(name);
			}

			counts.put(name, ++i);

			if (i > max) {
				max = i;
				voted = name;
			}
		}
		debug.i("max voted: " + String.valueOf(voted));

		Arena a = ArenaManager.getArenaByName(voted);
		
		if (a == null || !ArenaManager.getShortcutDefinitions().get(definition).contains(a.getName())) {
			PVPArena.instance.getLogger().warning("Vote resulted in NULL for result '"+voted+"'!");
			
			ArenaManager.advance(definition);
			debug.i("getting next definition value");
			a = ArenaManager.getShortcutValues().get(definition);
		}
		
		if (a == null) {
			debug.i("this didn't work oO - still null!");
			return;
		}
		
		ArenaManager.getShortcutValues().put(definition, a);
	}
	
	@Override
	public void onThisLoad() {
		boolean active = false;
		
		for (Arena arena : ArenaManager.getArenas()) {
			for (ArenaModule mod : arena.getMods()) {
				if (mod.getName().equals(this.getName())) {
					Bukkit.getPluginManager().registerEvents((AutoVote) mod, PVPArena.instance);
					if (!arena.getArenaConfig().getBoolean(CFG.MODULES_ARENAVOTE_AUTOSTART)) {
						continue;
					}
					Bukkit.getPluginManager().registerEvents((AutoVote) mod, PVPArena.instance);
					active = true;
				}
			}
		}
		
		if (!active) {
			return;
		}
		
		class RunLater implements Runnable {

			@Override
			public void run() {
				reset(false);
			}
			
		}
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 200L);
	}
	
	@Override
	public void parseJoin(CommandSender sender, ArenaTeam team) {
		players.add(ArenaPlayer.parsePlayer(sender.getName()));
	}
	
	@Override
	public void parsePlayerLeave(Player player, ArenaTeam team) {
		players.remove(player.getName());
	}

	public boolean hasVoted(String name) {
		return votes.containsKey(name);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onTryJoin(PAJoinEvent event) {
		arena.getDebugger().i("tryJoin " + event.getPlayer().getName());
		if (vote != null) {
			arena.getDebugger().i("vote is not null! denying " + event.getPlayer().getName());
			event.setCancelled(true);
		}
	}
}
