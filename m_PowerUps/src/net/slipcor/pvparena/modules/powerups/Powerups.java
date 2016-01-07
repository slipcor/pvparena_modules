package net.slipcor.pvparena.modules.powerups;

import net.slipcor.pvparena.core.Debug;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Powerups {
    private final Debug debug = new Debug(47);

    public final Map<Player, Powerup> puActive = new HashMap<>();
    public final List<Powerup> puTotal = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public Powerups(final Map<String, Object> powerUps) {

        debug.i("initialising powerupmanager");
        for (final Map.Entry<String, Object> stringObjectEntry : powerUps.entrySet()) {
            debug.i("reading powerUps");
            Powerup p = new Powerup(stringObjectEntry.getKey(),
                    (HashMap<String, Object>) stringObjectEntry.getValue());
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
