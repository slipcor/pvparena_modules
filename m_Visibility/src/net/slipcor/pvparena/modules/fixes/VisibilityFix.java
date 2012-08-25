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
		return "v0.8.13.2";
	}
	
	public void onPlayerTeleport(Arena arena, PlayerTeleportEvent event) {
		for (ArenaPlayer ap : arena.getPlayers()) {
			if (ap.get() == null) {
				continue;
			}
			ap.get().hidePlayer(event.getPlayer());
			ap.get().showPlayer(event.getPlayer());
		}
	}
}
