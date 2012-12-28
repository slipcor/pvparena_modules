package net.slipcor.pvparena.modules.duel;

import org.bukkit.Bukkit;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.commands.PAI_Ready;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;

public class DuelRunnable implements Runnable {
	private final DuelManager dm;
	private final String hoster;
	private final String player;
	private Debug db = new Debug(77);

	/**
	 * create a timed arena runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public DuelRunnable(DuelManager dm, String h, String p) {
		this.dm = dm;
		this.player = p;
		this.hoster = h;
		db.i("DuelRunnable constructor", hoster);

		PAG_Join cmd = new PAG_Join();
		cmd.commit(dm.getArena(), Bukkit.getPlayer(hoster), new String[0]);
		cmd.commit(dm.getArena(), Bukkit.getPlayer(player), new String[0]);
		dm.getArena().broadcast(Language.parse(MSG.MODULE_DUEL_STARTING));
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("DuelRunnable commiting", hoster);
		if (!"none".equals(dm.getArena().getArenaConfig().getString(CFG.READY_AUTOCLASS))) {
			PAI_Ready cmd = new PAI_Ready();
			cmd.commit(dm.getArena(), Bukkit.getPlayer(hoster), new String[0]);
			cmd.commit(dm.getArena(), Bukkit.getPlayer(player), new String[0]);
			dm.getArena().countDown();
		}
	}
}
