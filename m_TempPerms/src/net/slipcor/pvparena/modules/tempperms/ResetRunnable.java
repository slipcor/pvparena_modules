package net.slipcor.pvparena.modules.tempperms;

import org.bukkit.entity.Player;

public class ResetRunnable implements Runnable {
	
	Player p;
	TempPerms tp;

	public ResetRunnable(TempPerms tempPerms, Player player) {
		p = player;
		tp = tempPerms;
	}

	@Override
	public void run() {
		tp.removePermissions(p);
	}

}
