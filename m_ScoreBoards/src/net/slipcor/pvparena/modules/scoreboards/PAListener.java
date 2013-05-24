package net.slipcor.pvparena.modules.scoreboards;

import net.slipcor.pvparena.events.*;
import net.slipcor.pvparena.loadables.ArenaGoal;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PAListener implements Listener {
	private final ScoreBoards module;
	
	public PAListener(ScoreBoards ea) {
		this.module = ea;
	}
	
	@EventHandler
	public void onDeath(PADeathEvent event) {
		if (module.getArena() != null && module.getArena().equals(event.getArena()))
			module.update(event.getPlayer());
	}

	@EventHandler
	public void onEnd(PAEndEvent event) {
		if (module.getArena() != null && module.getArena().equals(event.getArena()))
			module.stop(event.getArena());
	}

	@EventHandler
	public void onExit(PAExitEvent event) {
		if (module.getArena() != null && module.getArena().equals(event.getArena()))
			module.remove(event.getPlayer());
	}

	@EventHandler
	public void onJoin(PAJoinEvent event) {
		if (!module.getArena().isFightInProgress()) {
			return;
		} else if (event.isSpectator()) {
			// spectator in progress
			for (ArenaGoal goal : module.getArena().getGoals()) {
				goal.initate(event.getPlayer());
			}
		}
		if (module.getArena() != null && module.getArena().equals(event.getArena()))
			module.add(event.getPlayer());
	}

	@EventHandler
	public void onKill(PAKillEvent event) {
		if (module.getArena() != null && module.getArena().equals(event.getArena()))
			module.update(event.getPlayer());
	}

	@EventHandler
	public void onLeave(PALeaveEvent event) {
		if (module.getArena() != null && module.getArena().equals(event.getArena()))
			module.remove(event.getPlayer());
	}

	@EventHandler
	public void onLose(PALoseEvent event) {
		if (module.getArena() != null && module.getArena().equals(event.getArena()))
			module.remove(event.getPlayer());
	}

	@EventHandler
	public void onStart(PAStartEvent event) {
		if (module.getArena() != null && module.getArena().equals(event.getArena()))
			module.start();
	}
}
