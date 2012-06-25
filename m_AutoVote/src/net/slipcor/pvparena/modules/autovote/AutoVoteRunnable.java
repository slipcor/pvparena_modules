package net.slipcor.pvparena.modules.autovote;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.runnables.TimerInfo;

public class AutoVoteRunnable implements Runnable {
	private final Arena a;
	private Debug db = new Debug(68);
	private int id;

	private int count = 0;
	public AutoVoteRunnable(Arena a, int i, int iid) {
		this.a = a;
		id = 0;
		count = i+1;
		db.i("AutoJoinRunnable constructor");
	}

	@Override
	public void run() {
		TimerInfo.spam("votenow", --count, null, a, false);
		if (count == 0) {
			Bukkit.getScheduler().cancelTask(a.START_ID);
			commit();
		} if (count < 0) {
			System.out.print("running");
		}
	}

	private void commit() {
		db.i("AutoJoinRunnable commiting");
		Bukkit.getScheduler().cancelTask(id);
		AutoVote.commit();
	}
	
	public void setId(int i) {
		id = i;
	}
}
