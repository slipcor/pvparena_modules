package net.slipcor.pvparena.modules.latelounge;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.PAA__Command;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class LateLounge extends ArenaModule {
	public LateLounge() {
		super("LateLounge");
	}
	
	int priority = 3; 
	
	@Override
	public String version() {
		return "v0.10.0.0";
	}
	
	private static HashSet<String> players = new HashSet<String>();
	
	/**
	 * hook into a player trying to join the arena
	 * 
	 * @param arena
	 *            the arena the player wants to join
	 * @param player
	 *            the trying player
	 * @return false if a player should not be granted permission
	 */
	@Override
	public PACheck checkJoin(CommandSender sender,
			PACheck res, boolean b) {
		if (res.hasError() || res.getPriority() > priority) {
			return res;
		}
		
		Player player = (Player) sender;
		
		if (players.contains(player.getName())) {
			if (players.size() < arena.getArenaConfig().getInt(CFG.READY_MINPLAYERS)) {
				res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
				return res;
			}
		}
		
		if (arena.getArenaConfig().getInt(CFG.READY_MINPLAYERS) > players.size() + 1) {
			// not enough players
			players.add(player.getName());
			Player[] aPlayers = Bukkit.getOnlinePlayers();
			
			for (Player p : aPlayers) {
				if (p.equals(player)) {
					continue;
				}
				try {
					Arena.pmsg(p, Language.parse(MSG.MODULE_LATELOUNGE_ANNOUNCE, arena.getName(), player.getName()));
				} catch (Exception e) {
					//
				}
			}
			res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
			return res;
		} else if (arena.getArenaConfig().getInt(CFG.READY_MINPLAYERS) == players.size() + 1) {
			// not enough players
			players.add(player.getName());
			
			HashSet<String> removals = new HashSet<String>();
			
			for (String s : players) {
				Player p = Bukkit.getPlayerExact(s);
				
				boolean removeMe = false;
				
				if (p != null) {
					for (ArenaModule mod : arena.getMods()) {
						if (!mod.getName().equals(getName())) {
							if (mod.checkJoin(p, new PACheck(), true).hasError()) {
								removeMe = true;
								break;
							}
						}
					}
				}
				
				if (p == null || removeMe) {
					removals.add(s);
					if (p != null) {
						res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_REJOIN));
					}
				}
			}

			if (removals.size() > 0) {
				for (String s : removals) {
					players.remove(s);
				}
			} else {
				// SUCCESS!
				for (String s : players) {
					if (s.equals(sender.getName())) {
						continue;
					}
					Player p = Bukkit.getPlayerExact(s);
					PAA__Command command = new PAG_Join();
					command.commit(arena, p, new String[0]);
				}
				return res;
			}
			res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
		}
		// enough, ignore and let something else handle the start!
		return res;
	}
	
	@Override
	public void reset(boolean force) {
		players.remove(arena);
	}
}
