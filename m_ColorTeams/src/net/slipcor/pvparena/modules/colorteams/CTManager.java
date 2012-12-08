package net.slipcor.pvparena.modules.colorteams;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;
import org.kitteh.tag.TagAPI;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.PAA__Command;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;

public class CTManager extends ArenaModule {
	protected static String spoutHandler = null;
	protected static boolean enabled = false;

	public CTManager() {
		super("ColorTeams");
	}
	
	@Override
	public String version() {
		return "v0.10.0.1";
	}
	
	@Override
	public boolean checkCommand(String s) {
		return s.equals("!ct") || s.equals("colorteams");
	}

	private void colorizePlayer(Player player) {
		db.i("colorizing player " + player.getName() + ";");

		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		
		Arena arena = ap.getArena();
		if (arena == null) {
			db.i("> arena is null");
			if (spoutHandler != null) {
				SpoutManager.getPlayer(player).setTitle(player.getName());
			}
			return;
		}

		ArenaTeam team = ap.getArenaTeam();
		String n;
		if (team == null) {
			db.i("> team is null");
			if (spoutHandler != null) {
				SpoutManager.getPlayer(player).setTitle(player.getName());
			}
			return;
		} else {
			n = team.getColorCodeString() + player.getName();
		}
		n = n.replaceAll("(&([a-f0-9]))", "§$2");
		
		player.setDisplayName(n);

		if (arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_HIDENAME)) {
			n = " ";
		}
		if (spoutHandler != null) {
			SpoutManager.getPlayer(player).setTitle(n);
		}
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		// !ct [value]
		
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			arena.msg(
					sender,
					Language.parse(MSG.ERROR_NOPERM,
							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}

		if (!PAA__Command.argCountValid(sender, arena, args, new Integer[] { 2 })) {
			return;
		}
		
		CFG c = null;
		
		if (args[1].equals("hidename")) {
			c = CFG.MODULES_COLORTEAMS_HIDENAME;
		} else if (args[1].equals("spoutonly")) {
			c = CFG.MODULES_COLORTEAMS_SPOUTONLY;
		} else if (args[1].equals("tagapi")) {
			c = CFG.MODULES_COLORTEAMS_TAGAPI;
		}
		
		if (c == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "hidename | spoutonly | tagapi"));
			return;
		}
		
		boolean b = arena.getArenaConfig().getBoolean(c);
		arena.getArenaConfig().set(c, !b);
		arena.getArenaConfig().save();
		arena.msg(sender, Language.parse(MSG.SET_DONE, c.getNode(), String.valueOf(!b)));
		
	}

	@Override
	public void displayInfo(CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6ColorTeams:§f "
				+ StringParser.colorVar("hidename", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_HIDENAME))
				+ " || "
				+ StringParser.colorVar("spoutonly", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SPOUTONLY))
				+ " || "
				+ StringParser.colorVar("tagapi", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_TAGAPI)));
	}
	
	@Override
	public void parseEnable() {
		if (enabled) {
			return;
		}
		if (spoutHandler == null && Bukkit.getServer().getPluginManager().getPlugin("Spout") != null) {
			spoutHandler = SpoutManager.getInstance().toString();
		}
		Arena.pmsg(Bukkit.getConsoleSender(), Language.parse((spoutHandler == null) ? MSG.MODULE_COLORTEAMS_NOSPOUT : MSG.MODULE_COLORTEAMS_SPOUT));

		enabled = true;
		
		if (Bukkit.getServer().getPluginManager().getPlugin("TagAPI") != null) {
			Bukkit.getPluginManager().registerEvents(new CTListener(), PVPArena.instance);
			Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.MODULE_COLORTEAMS_TAGAPI));
		}
	}

	@Override
	public void tpPlayerToCoordName(Player player, String place) {
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_COLORNICK)) {
			if (spoutHandler != null) {
				colorizePlayer(player);	
			} else {
				ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
				String n;
				if (team == null) {
					db.i("> team is null");
					n = player.getName();
				} else {
					n = team.getColorCodeString() + player.getName();
				}
				n = n.replaceAll("(&([a-f0-9]))", "§$2");
				
				player.setDisplayName(n);

				if (team != null && arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_HIDENAME)) {
					n = " ";
				}
				
				updateName(player, n);
			}
		}
	}
	
	@Override
	public void unload(Player player) {
		if (spoutHandler != null) {
			SpoutManager.getPlayer(player).setTitle(player.getName());
		} else {
			player.setDisplayName(player.getName());
			TagAPI.refreshPlayer(player);
		}
	}
	
	public void updateName(Player player, String team) {
		
		// Update the name
		Player[] players = Bukkit.getOnlinePlayers();
		for(Player p : players) {
			if(p != player) {
				// Refresh the packet!
				p.hidePlayer(player);
				p.showPlayer(player);
			}
		}
		//setName(player, ChatColor.stripColor(n));
	}
}
