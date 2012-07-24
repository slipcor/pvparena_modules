package net.slipcor.pvparena.modules.autovote;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.runnables.TimerInfo;

public class AutoVoteRunnable implements Runnable {
	private Debug db = new Debug(68);
	private int id;

	private int count = 0;
	public AutoVoteRunnable(int i, int iid) {
		id = 0;
		count = i+1;
		db.i("ArenaVoteRunnable constructor");
	}

	@Override
	public void run() {
		TimerInfo.spam("votenow", --count, null, null, true);
		if (count == 0) {
			commit();
		} if (count < 0) {
			System.out.print("running");
		}
	}

	private void commit() {
		db.i("ArenaVoteRunnable commiting");
		Bukkit.getScheduler().cancelTask(id);
		AutoVote.commit();
	}
	
	public void setId(int i) {
		id = i;
	}
}
