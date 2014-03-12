package net.slipcor.pvparena.modules.autovote;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.runnables.ArenaRunnable;

public class AutoVoteRunnable extends ArenaRunnable {
	private Debug debug = new Debug(68);
	private final String definition;
	private final AutoVote module;
	public AutoVoteRunnable(Arena a, int i, AutoVote mod, String definition) {
		super(MSG.MODULE_AUTOVOTE_VOTENOW.getNode(), i, null, a, false);
		this.definition = definition;
		debug.i("AutoVoteRunnable constructor");
		module = mod;
	}

	protected void commit() {
		debug.i("ArenaVoteRunnable commiting");
		AutoVote.commit(definition);
		class RunLater implements Runnable {

			@Override
			public void run() {
				module.vote = null;
			}
			
		}
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 20L);
	}

	@Override
	protected void warn() {
		PVPArena.instance.getLogger().warning("ArenaVoteRunnable not scheduled yet!");
	}
	
	@Override
	public void spam() {
		if ((super.message == null) || (MESSAGES.get(seconds) == null)) {
			return;
		}
		MSG msg = MSG.getByNode(this.message);
		if (msg == null) {
			PVPArena.instance.getLogger().warning("MSG not found: " + this.message);
			return;
		}
		
		String arenastring = "";
		
		if (definition == null) {
			arenastring = ArenaManager.getNames();
		} else {
			Set<String> arenas = new HashSet<String>();
			for (String string : ArenaManager.getShortcutDefinitions().get(definition)) {
				arenas.add(string);
			}
			arenastring = StringParser.joinSet(arenas, ", ");
		}
		
		String message = seconds > 5 ? Language.parse(msg, MESSAGES.get(seconds), arenastring) : MESSAGES.get(seconds);
		if (global) {
			Player[] players = Bukkit.getOnlinePlayers();
			
			playerssss: for (Player p : players) {
				for (Arena aaa : ArenaManager.getArenas()) {
					if (!aaa.getArenaConfig().getBoolean(CFG.MODULES_ARENAVOTE_ONLYSPAMTOJOIN)) {
						Arena.pmsg(p, message);
						continue playerssss;
					}
					for (ArenaRegion region : aaa.getRegionsByType(RegionType.JOIN)) {
						if (region.getShape().contains(new PABlockLocation(p.getLocation()))) {
							Arena.pmsg(p, message);
							continue playerssss;
						}
					}
				}
			}
			
			return;
		}
		if (arena != null) {
			Set<ArenaPlayer> players = arena.getEveryone();
			playerssss: for (ArenaPlayer ap : players) {
				if (sPlayer != null) {
					if (ap.getName().equals(sPlayer)) {
						continue;
					}
				}
				if (ap.get() != null) {
					if (!arena.getArenaConfig().getBoolean(CFG.MODULES_ARENAVOTE_ONLYSPAMTOJOIN)) {
						arena.msg(ap.get(), message);
						continue playerssss;
					}
					for (ArenaRegion region : arena.getRegionsByType(RegionType.JOIN)) {
						if (region.getShape().contains(new PABlockLocation(ap.get().getLocation()))) {
							arena.msg(ap.get(), message);
							continue playerssss;
						}
					}
				}
			}
			return;
		}
		if (Bukkit.getPlayer(sPlayer) != null) {
			Arena.pmsg(Bukkit.getPlayer(sPlayer), message);
			return;
		}
	}
}
