package net.slipcor.pvparena.modules.powerups;

import net.slipcor.pvparena.core.Debug;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Powerups {
    private final Debug debug = new Debug(47);

    public final Map<Player, Powerup> puActive = new HashMap<Player, Powerup>();
    public final List<Powerup> puTotal = new ArrayList<Powerup>();

    @SuppressWarnings("unchecked")
    public Powerups(final Map<String, Object> powerUps) {

        debug.i("initialising powerupmanager");
        Powerup p;
        for (final String pName : powerUps.keySet()) {
            debug.i("reading powerUps");
            p = new Powerup(pName,
                    (HashMap<String, Object>) powerUps.get(pName));
            puTotal.add(p);
        }
    }

    /**
     * trigger all powerups
     */
    public void tick() {
        for (final Powerup p : puActive.values()) {
            if (p.canBeTriggered()) {
                p.tick();
            }
        }
    }

}
