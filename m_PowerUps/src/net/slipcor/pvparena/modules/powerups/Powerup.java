package net.slipcor.pvparena.modules.powerups;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import java.util.HashMap;
import java.util.Map;

class Powerup {
    public final String name; // PowerUp display name
    public final Material item; // item that triggers this Powerup
    private PowerupEffect[] effects; // Effects the Powerup has
    private final Debug debug = new Debug(16);

    /**
     * construct a powerup instance
     *
     * @param pName     the powerup name
     * @param puEffects the powerup effects
     */
    @SuppressWarnings("unchecked")
    public Powerup(String pName, Map<String, Object> puEffects) {
        int count = 0;
        this.name = pName;
        debug.i("creating powerup " + pName);
        this.item = Material.valueOf((String) puEffects.get("item"));
        debug.i("item added: " + this.item.toString());
        for (String eClass : puEffects.keySet()) {
            PowerupType pec = PowerupEffect.parseClass(eClass);
            if (pec == null) {
                if (!eClass.equals("item"))
                    PVPArena.instance.getLogger().warning("unknown effect class: " + eClass);
                continue;
            }
            PowerupEffect pe = new PowerupEffect(eClass,
                    (HashMap<String, Object>) puEffects.get(eClass),
                    PowerupEffect.parsePotionEffect(eClass));
            if (pe.type == null) {
                continue;
            }
            count++;
        }
        debug.i("effects found: " + count);
        if (count < 1)
            return;

        effects = new PowerupEffect[count];

        count = 0;
        for (String eClass : puEffects.keySet()) {
            PowerupType pec = PowerupEffect.parseClass(eClass);
            if (pec == null) {
                continue;
            }
            PowerupEffect pe = new PowerupEffect(eClass,
                    (HashMap<String, Object>) puEffects.get(eClass),
                    PowerupEffect.parsePotionEffect(eClass));
            if (pe.type == null) {
                continue;
            }
            effects[count++] = pe;
        }
    }

    /**
     * second constructor, referencing instead of creating
     *
     * @param p the Powerup reference
     */
    public Powerup(Powerup p) {
        this.name = p.name;
        this.effects = p.effects;
        this.item = p.item;
    }

    /**
     * check if a powerup has active effects
     *
     * @return true if an effect still is active, false otherwise
     */
    public boolean isActive() {
        for (PowerupEffect pe : effects) {
            if (pe.active)
                return true;
        }
        return false;
    }

    /**
     * check if a powerup running effect is running
     *
     * @param peClass the class to check
     * @return true if an effect still is active, false otherwise
     */
    public boolean isEffectActive(PowerupType peClass) {
        for (PowerupEffect pe : effects) {
            if (pe.uses != 0 && pe.duration != 0)
                if (pe.type.equals(peClass))
                    return true;
        }
        return false;
    }

    /**
     * check if any effect can be fired
     *
     * @return true if an event can be fired, false otherwise
     */
    public boolean canBeTriggered() {
        for (PowerupEffect pe : effects) {
            if (pe.uses != 0 && pe.duration != 0)
                return true; // one effect still can be triggered
        }
        return false;
    }

    /**
     * initiate Powerup effects
     *
     * @param player the player to commit the effect on
     */
    public void activate(Player player) {
        debug.i("activating! - " + name, player);
        for (PowerupEffect pe : effects) {
            if (pe.uses != 0 && pe.duration != 0)
                pe.init(player);
        }
    }

    /**
     * commit PowerupEffect in combat
     *
     * @param attacker the attacking player to access
     * @param defender the defending player to access
     * @param event    the triggering event
     * @param isAttacker is the player attacking
     */
    public void commit(Player attacker, Player defender,
                       EntityDamageByEntityEvent event, boolean isAttacker) {
        debug.i("committing effects:", attacker);
        debug.i("committing effects:", defender);
        for (PowerupEffect pe : effects) {
            if (pe.uses != 0 && pe.duration != 0) {
                pe.commit(attacker, defender, event, isAttacker);
            }
        }
    }

    /**
     * commit all PowerupEffects
     *
     * @param event the triggering event
     */
    public void commit(EntityRegainHealthEvent event) {
        for (PowerupEffect pe : effects) {
            if (pe.uses != 0 && pe.duration != 0)
                if (pe.type.equals(PowerupType.HEAL))
                    pe.commit(event);
        }
    }

    /**
     * commit all PowerupEffects
     *
     * @param event the triggering event
     */
    public void commit(PlayerVelocityEvent event) {
        for (PowerupEffect pe : effects) {
            if (pe.uses != 0 && pe.duration != 0)
                if (pe.type.equals(PowerupType.HEAL))
                    pe.commit(event);
        }
    }

    /**
     * calculate down the duration
     */
    public void tick() {
        for (PowerupEffect pe : effects) {
            if (pe.uses != 0 && pe.duration > 0)
                pe.duration--;
        }
    }

    public void deactivate(Player player) {
        if (effects == null) {
            return;
        }
        for (PowerupEffect eff : effects) {
            eff.removeEffect(player);
        }
    }
}