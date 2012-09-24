package net.slipcor.pvparena.modules.fixes;

import java.util.HashMap;

import org.bukkit.event.player.PlayerTeleportEvent;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class StuckInFloor extends ArenaModule {

	public StuckInFloor() {
		super("StuckInFloor");
	}

	@Override
	public String version() {
		return "v0.9.0.0";
	}

	@Override
	public void addSettings(HashMap<String, String> types) {
		types.put("fix.stuckInFloor", "boolean");
	}

	@Override
	public void onPlayerTeleport(Arena arena, PlayerTeleportEvent event) {
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_FIXSTUCKINFLOOR_FSIF)) {
			event.setTo(event.getTo().add(0, 0.1, 0));
		}
	}
}
