package net.slipcor.pvparena.modules.colorteams;

import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class CTListener implements Listener {

	@EventHandler
	public void onNameReceive(PlayerReceiveNameTagEvent event) {
		Player p = event.getNamedPlayer();
		
		if (p == null) {
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(p);
		
		if (ap == null || ap.getArena() == null || !ap.getArena().cfg.getBoolean("colors.tagapi")) {
			return;
		}
		
		for (ArenaTeam at : ap.getArena().getTeams()) {
			if (at.getTeamMembers().contains(ap)) {
				event.setTag(at.colorizePlayer(p));
				return;
			}
		}
	}
}
