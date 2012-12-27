package net.slipcor.pvparena.modules.autovote;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.runnables.ArenaRunnable;

public class AutoVoteRunnable extends ArenaRunnable {
	private Debug db = new Debug(68);
	public AutoVoteRunnable(Arena a, int i) {
		super(MSG.MODULE_AUTOVOTE_VOTENOW.getNode(), i, null, null, true);
		db.i("AutoVoteRunnable constructor");
	}

	protected void commit() {
		db.i("ArenaVoteRunnable commiting");
		cancel();
		AutoVote.commit();
	}

	@Override
	protected void warn() {
		PVPArena.instance.getLogger().warning("ArenaVoteRunnable not scheduled yet!");
	}
	
	@Override
	public void spam() {
		if ((super.message == null) || (messages.get(seconds) == null)) {
			return;
		}
		MSG msg = MSG.getByNode(this.message);
		if (msg == null) {
			PVPArena.instance.getLogger().warning("MSG not found: " + this.message);
			return;
		}
		String message = seconds > 5 ? Language.parse(msg, messages.get(seconds), ArenaManager.getNames()) : messages.get(seconds);
		if (global) {
			Player[] players = Bukkit.getOnlinePlayers();
			
			for (Player p : players) {
				try {
					if (arena != null) {
						if (arena.hasPlayer(p)) {
							continue;
						}
					}
					if (sPlayer != null) {
						if (sPlayer.equals(p.getName())) {
							continue;
						}
					}
					Arena.pmsg(p, message);
				} catch (Exception e) {}
			}
			
			return;
		}
		if (arena != null) {
			HashSet<ArenaPlayer> players = arena.getFighters();
			for (ArenaPlayer ap : players) {
				if (sPlayer != null) {
					if (ap.getName().equals(sPlayer)) {
						continue;
					}
				}
				if (ap.get() != null) {
					arena.msg(ap.get(), message);
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
