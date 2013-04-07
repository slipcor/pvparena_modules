package net.slipcor.pvparena.modules.scoreboards;

import net.slipcor.pvparena.events.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PAListener implements Listener {
	private final ScoreBoards module;
	
	public PAListener(ScoreBoards ea) {
		this.module = ea;
	}
	
	@EventHandler
	public void onDeath(PADeathEvent event) {
		module.update(event.getPlayer());
	}

	@EventHandler
	public void onEnd(PAEndEvent event) {
		module.stop(event.getArena());
	}

	@EventHandler
	public void onExit(PAExitEvent event) {
		module.remove(event.getPlayer());
	}

	@EventHandler
	public void onJoin(PAJoinEvent event) {
		module.add(event.getPlayer());
	}

	@EventHandler
	public void onKill(PAKillEvent event) {
		module.update(event.getPlayer());
	}

	@EventHandler
	public void onLeave(PALeaveEvent event) {
		module.remove(event.getPlayer());
	}

	@EventHandler
	public void onLose(PALoseEvent event) {
		module.remove(event.getPlayer());
	}

	@EventHandler
	public void onStart(PAStartEvent event) {
		module.start();
	}
}
