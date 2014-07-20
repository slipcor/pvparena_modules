package net.slipcor.pvparena.modules.arenaboards;

import net.slipcor.pvparena.core.Debug;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

class ArenaBoardSign {
    private final BlockState state;
    private final Debug debug = new Debug(12);

    /**
     * create an arena board sign instance
     *
     * @param loc the location where the sign resides
     */
    public ArenaBoardSign(final Location loc) {
        state = loc.getBlock().getState();
        debug.i("adding sign at location " + loc.toString());
    }

    /**
     * set a line
     *
     * @param i      the line to set
     * @param string the string to set
     */
    public void set(final int i, final String string) {
        ((Sign) state).setLine(i, string);
    }

    /**
     * update the sign
     */
    public void update() {
        state.update();
    }
}
