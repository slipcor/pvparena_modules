package net.slipcor.pvparena.command;

import org.bukkit.command.CommandSender;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

public class BanRunnable implements Runnable {
	private final Arena a;
	private final CommandSender admin;
	private final String player;
	private final boolean ban;
	private Debug db = new Debug(68);

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public BanRunnable(Arena a, CommandSender admin, String p, boolean b) {
		this.a = a;
		this.admin = admin;
		this.player = p;
		this.ban = b;
		db.i("BanRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("BanRunnable commiting");
		if (ban) {
			BanKick.doBan(admin, a, player);
		} else {
			BanKick.doUnBan(admin, a, player);
		}
	}
}
