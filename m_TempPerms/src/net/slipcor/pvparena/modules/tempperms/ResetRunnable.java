package net.slipcor.pvparena.modules.tempperms;

import org.bukkit.entity.Player;

class ResetRunnable implements Runnable {

    private final Player p;
    private final TempPerms tp;

    public ResetRunnable(TempPerms tempPerms, Player player) {
        p = player;
        tp = tempPerms;
    }

    @Override
    public void run() {
        tp.removePermissions(p);
    }

}
