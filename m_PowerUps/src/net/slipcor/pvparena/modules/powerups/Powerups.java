package net.slipcor.pvparena.modules.powerups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import net.slipcor.pvparena.core.Debug;

public class Powerups {
	private Debug db = new Debug(47);

	public HashMap<Player, Powerup> puActive = new HashMap<Player, Powerup>();
	public List<Powerup> puTotal = new ArrayList<Powerup>();
	
	@SuppressWarnings("unchecked")
	public Powerups(HashMap<String, Object> powerUps) {

		db.i("initialising powerupmanager");
		Powerup p;
		for (String pName : powerUps.keySet()) {
			db.i("reading powerUps");
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
