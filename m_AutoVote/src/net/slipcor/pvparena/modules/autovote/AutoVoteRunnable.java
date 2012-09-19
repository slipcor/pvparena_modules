package net.slipcor.pvparena.modules.autovote;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.runnables.ArenaRunnable;

public class AutoVoteRunnable extends ArenaRunnable {
	private Debug db = new Debug(68);
	private int id;
	public AutoVoteRunnable(Arena a, int i) {
		super("votenow", i, null, null, true);
		db.i("AutoVoteRunnable constructor");
	}

	protected void commit() {
		db.i("ArenaVoteRunnable commiting");
		Bukkit.getScheduler().cancelTask(id);
		AutoVote.commit();
	}
	
	public void setId(int i) {
		id = i;
	}
}
