package net.slipcor.pvparena.modules.redstone;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class RedStoneListener implements Listener {
	@EventHandler
	public void onRedStone(BlockRedstoneEvent event) {
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock().getLocation());
		if (arena == null) {
			return;
		}
		
		Block block = event.getBlock();
		
		if (!(block.getState() instanceof Sign)) {
			return;
		}
		
		Sign s = (Sign) block.getState();
		
		if (s.getLine(0).equals("[WIN]")) {
			for (ArenaTeam team : arena.getTeams()) {
				if (team.getName().equalsIgnoreCase(s.getLine(1))) {
					// skip winner
					continue;
				}
				for (ArenaPlayer ap : team.getTeamMembers()) {
					if (ap.getStatus().equals(Status.FIGHT)) {
						event.getBlock().getWorld().strikeLightningEffect(ap.get().getLocation());
						EntityDamageEvent e = new EntityDamageEvent(ap.get(), DamageCause.LIGHTNING, 10);
						PlayerListener.commitPlayerDeath(arena, ap.get(), e);
					}
				}
			}
		} else if (s.getLine(0).equals("[LOSE]")) {
			for (ArenaTeam team : arena.getTeams()) {
				if (!team.getName().equalsIgnoreCase(s.getLine(1))) {
					// skip winner
					continue;
				}
				for (ArenaPlayer ap : team.getTeamMembers()) {
					if (ap.getStatus().equals(Status.FIGHT)) {
						event.getBlock().getWorld().strikeLightningEffect(ap.get().getLocation());
						EntityDamageEvent e = new EntityDamageEvent(ap.get(), DamageCause.LIGHTNING, 10);
						PlayerListener.commitPlayerDeath(arena, ap.get(), e);
					}
				}
			}
		}
	}
}
