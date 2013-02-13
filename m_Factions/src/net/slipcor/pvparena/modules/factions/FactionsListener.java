package net.slipcor.pvparena.modules.factions;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FactionsListener implements Listener {
	private Debug debug = new Debug(66);
	private final FactionsSupport fs;
	
	public FactionsListener(FactionsSupport fs) {
		this.fs = fs;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!event.isCancelled()) {
			// not cancelled, no problem ^^
			return;
		}

		Entity p1 = event.getDamager();
		Entity p2 = event.getEntity();

		debug.i("onEntityDamageByEntity: cause: " + event.getCause().name()
				+ " : " + event.getDamager().toString() + " => "
				+ event.getEntity().toString());


		if (p1 instanceof Projectile) {
			debug.i("parsing projectile");
			p1 = ((Projectile) p1).getShooter();
			debug.i("=> " + String.valueOf(p1));
		}

		if (event.getEntity() instanceof Wolf) {
			Wolf wolf = (Wolf) event.getEntity();
			if (wolf.getOwner() != null) {
				try {
					p1 = (Entity) wolf.getOwner();
				} catch (Exception e) {
					// wolf belongs to dead player or whatnot
				}
			}
		}

		if ((p1 != null && p2 != null) && p1 instanceof Player
				&& p2 instanceof Player) {
			if (PVPArena.instance.getConfig().getBoolean("onlyPVPinArena")) {
				event.setCancelled(true); // cancel events for regular no PVP
				// servers
			}
		}

		if ((p2 == null) || (!(p2 instanceof Player))) {
			return;
		}

		Arena arena = ArenaPlayer.parsePlayer(((Player) p2).getName()).getArena();
		if (arena == null || !arena.equals(fs.getArena())) {
			// defender not part of our arena => out
			return;
		}

		debug.i("onEntityDamageByEntity: fighting player");

		if ((p1 == null) || (!(p1 instanceof Player))) {
			// attacker no player => out!
			return;
		}

		debug.i("both entities are players");

		event.setCancelled(false);
	}
}
