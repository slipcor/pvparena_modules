package net.slipcor.pvparena.modules.fixes;

import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.loadables.ArenaModule;

public class VisibilityFix extends ArenaModule {
	
	public VisibilityFix() {
		super("VisibilityFix");
	}
	
	@Override
	public String version() {
		return "v0.9.0.0";
	}
	
	public void onPlayerTeleport(Arena arena, PlayerTeleportEvent event) {
		if (event.getCause().equals(TeleportCause.END_PORTAL)) {
			return;
		}
		for (ArenaPlayer ap : arena.getFighters()) {
			if (ap.get() == null) {
				continue;
			}
			ap.get().hidePlayer(event.getPlayer());
			ap.get().showPlayer(event.getPlayer());
			
			ap.get().teleport(ap.get().getLocation().add(0,0.1,0));
		}
	}
}
