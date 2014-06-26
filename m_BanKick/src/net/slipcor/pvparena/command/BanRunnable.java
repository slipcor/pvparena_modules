package net.slipcor.pvparena.command;

import net.slipcor.pvparena.core.Debug;
import org.bukkit.command.CommandSender;

public class BanRunnable implements Runnable {
    private final CommandSender admin;
    private final String player;
    private final boolean ban;
    private final BanKick bk;
    private Debug debug = new Debug(68);

    public BanRunnable(BanKick m, CommandSender admin, String p, boolean b) {
        this.bk = m;
        this.admin = admin;
        this.player = p;
        this.ban = b;
        debug.i("BanRunnable constructor", admin);
    }

    /**
     * the run method, commit arena end
     */
    @Override
    public void run() {
        debug.i("BanRunnable commiting", admin);
        if (ban) {
            bk.doBan(admin, player);
        } else {
            bk.doUnBan(admin, player);
        }
    }
}
