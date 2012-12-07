package net.slipcor.pvparena.command;

import org.bukkit.command.CommandSender;
import net.slipcor.pvparena.core.Debug;

public class BanRunnable implements Runnable {
	private final CommandSender admin;
	private final String player;
	private final boolean ban;
	private final BanKick bk;
	private Debug db = new Debug(68);

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public BanRunnable(BanKick m, CommandSender admin, String p, boolean b) {
		this.bk = m;
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
			bk.doBan(admin, player);
		} else {
			bk.doUnBan(admin, player);
		}
	}
}
