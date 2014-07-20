package net.slipcor.pvparena.modules.arenaboards;

import net.slipcor.pvparena.core.Debug;

class BoardRunnable implements Runnable {
    private final ArenaBoardManager abm;
    private final Debug debug = new Debug(38);

    /**
     * create a timed arena runnable
     *
     * @param m the module
     */
    public BoardRunnable(ArenaBoardManager m) {
        abm = m;
        debug.i("BoardRunnable constructor");
    }

    /**
     * the run method, commit arena end
     */
    @Override
    public void run() {
        debug.i("BoardRunnable commiting");
        if (abm == null) {
            if (ArenaBoardManager.globalBoard != null) {
                ArenaBoardManager.globalBoard.update();
            }
        } else {
            for (ArenaBoard ab : abm.boards.values()) {
                ab.update();
            }
        }
    }
}
