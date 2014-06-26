package net.slipcor.pvparena.modules.powerups;

import net.slipcor.pvparena.core.Debug;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Powerups {
    private Debug debug = new Debug(47);

    public HashMap<Player, Powerup> puActive = new HashMap<Player, Powerup>();
    public List<Powerup> puTotal = new ArrayList<Powerup>();

    @SuppressWarnings("unchecked")
    public Powerups(HashMap<String, Object> powerUps) {

        debug.i("initialising powerupmanager");
        Powerup p;
        for (String pName : powerUps.keySet()) {
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
        for (Powerup p : puActive.values()) {
            if (p.canBeTriggered()) {
                p.tick();
            }
        }
    }

}
