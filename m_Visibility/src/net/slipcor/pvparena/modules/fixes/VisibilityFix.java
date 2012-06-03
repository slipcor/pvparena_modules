package net.slipcor.pvparena.modules.fixes;

import org.bukkit.event.player.PlayerTeleportEvent;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.neworder.ArenaModule;

public class VisibilityFix extends ArenaModule {
	
	public VisibilityFix() {
		super("VisibilityFix");
	}
	
	@Override
	public String version() {
		return "v0.7.13.1";
	}
	
	public void onPlayerTeleport(Arena arena, PlayerTeleportEvent event) {
		for (ArenaPlayer ap : arena.getPlayers()) {
			ap.get().hidePlayer(event.getPlayer());
			ap.get().showPlayer(event.getPlayer());
		}
	}
}
