package net.slipcor.pvparena.modules.redstone;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.managers.ArenaManager;

public class RedStoneTriggers extends ArenaModule implements Listener {
	public RedStoneTriggers() {
		super("RedStoneTriggers");
		db = new Debug(403);
	}
	
	@Override
	public String version() {
		return "v0.10.0.0";
	}
	
	@Override
	public void parseEnable() {
		Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
	}
	
	@EventHandler
	public void onRedStone(BlockRedstoneEvent event) {
		db.i("redstone");
		Arena arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getBlock().getLocation()));
		if (arena == null || !arena.equals(this.arena)) {
			return;
		}
		db.i("redstone in arena " + arena.toString());
		
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
						PlayerListener.finallyKillPlayer(arena, ap.get(), e);
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
						PlayerListener.finallyKillPlayer(arena, ap.get(), e);
					}
				}
			}
		}
	}
}
