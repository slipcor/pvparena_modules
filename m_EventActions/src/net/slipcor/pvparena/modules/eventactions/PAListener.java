package net.slipcor.pvparena.modules.eventactions;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.events.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PAListener implements Listener {
	private final EventActions ea;
	
	public PAListener(EventActions ea) {
		this.ea = ea;
	}
	
	@EventHandler
	public void onDeath(PADeathEvent event) {
		Arena a = event.getArena();
		Player p = event.getPlayer();
		ea.catchEvent("death", p, a);
	}

	@EventHandler
	public void onEnd(PAEndEvent event) {
		Arena a = event.getArena();
		ea.catchEvent("end", null, a);
	}

	@EventHandler
	public void onExit(PAExitEvent event) {
		Arena a = event.getArena();
		Player p = event.getPlayer();
		ea.catchEvent("exit", p, a);
	}

	@EventHandler
	public void onJoin(PAJoinEvent event) {
		Arena a = event.getArena();
		Player p = event.getPlayer();
		
		if (event.isSpectator()) {
			ea.catchEvent("spectate", p, a);
		} else {
			ea.catchEvent("join", p, a);
		}
	}

	@EventHandler
	public void onKill(PAKillEvent event) {
		Arena a = event.getArena();
		Player p = event.getPlayer();
		ea.catchEvent("kill", p, a);
	}

	@EventHandler
	public void onLeave(PALeaveEvent event) {
		Arena a = event.getArena();
		Player p = event.getPlayer();
		ea.catchEvent("leave", p, a);
	}

	@EventHandler
	public void onLose(PALoseEvent event) {
		Arena a = event.getArena();
		Player p = event.getPlayer();
		ea.catchEvent("lose", p, a);
	}

	@EventHandler
	public void onStart(PAStartEvent event) {
		Arena a = event.getArena();
		ea.catchEvent("start", null, a);
	}

	@EventHandler
	public void onWin(PAWinEvent event) {
		Arena a = event.getArena();
		Player p = event.getPlayer();
		ea.catchEvent("win", p, a);
	}
}
