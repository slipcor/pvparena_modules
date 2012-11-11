package net.slipcor.pvparena.modules.colorteams;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;

import net.minecraft.server.EntityPlayer;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;

public class CTManager extends ArenaModule {
	protected static String spoutHandler = null;
	private Field name = null;

	public CTManager() {
		super("ColorTeams");
	}
	
	@Override
	public String version() {
		return "v0.9.6.16";
	}

	private void colorizePlayer(Arena a, Player player) {
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
	public void displayInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6ColoredTeams:§f "
				+ StringParser.colorVar("hideName", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_HIDENAME))
				+ " || "
				+ StringParser.colorVar("colorNick", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_COLORNICK))
				+ " || "
				+ StringParser.colorVar("requireSpout", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SPOUTONLY)));
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_COLORNICK);
	}
	
	@Override
	public void onEnable() {
		if (Bukkit.getServer().getPluginManager().getPlugin("Spout") != null) {
			spoutHandler = SpoutManager.getInstance().toString();
		} else {
			try {
				// Grab the field
				for(Field field : EntityPlayer.class.getFields()) {
					if(field.getName().equalsIgnoreCase("name")) {
						name = field;
					}
				}
				name.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Arena.pmsg(Bukkit.getConsoleSender(), Language.parse((spoutHandler == null) ? MSG.MODULE_COLORTEAMS_NOSPOUT : MSG.MODULE_COLORTEAMS_SPOUT));

		if (Bukkit.getServer().getPluginManager().getPlugin("TagAPI") != null) {
			Bukkit.getPluginManager().registerEvents(new CTListener(), PVPArena.instance);
			Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.MODULE_COLORTEAMS_TAGAPI));
		}
	}

	@Override
	public void tpPlayerToCoordName(Arena arena, Player player, String place) {
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_COLORNICK)) {
			if (spoutHandler != null) {
				colorizePlayer(arena, player);	
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
