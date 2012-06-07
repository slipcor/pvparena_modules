package net.slipcor.pvparena.modules.colorteams;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.neworder.ArenaModule;

public class CTManager extends ArenaModule {
	protected static String spoutHandler = null;

	public CTManager() {
		super("ColorTeams");
	}
	
	@Override
	public String version() {
		return "v0.8.7.0";
	}

	@Override
	public void addSettings(HashMap<String, String> types) {
		types.put("game.hideName", "boolean");
		types.put("game.mustbesafe", "boolean");
		types.put("game.woolFlagHead", "boolean");
		types.put("messages.colorNick", "boolean");
	}

	private void colorizePlayer(Player player) {
		db.i("colorizing player " + player.getName() + ";");

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null) {
			db.i("> arena is null");
			if (spoutHandler != null) {
				SpoutManager.getPlayer(player).setTitle(player.getName());
			}
			return;
		}

		ArenaTeam team = Teams.getTeam(arena, ArenaPlayer.parsePlayer(player));
		String n;
		if (team == null) {
			db.i("> team is null");
			if (spoutHandler != null) {
				SpoutManager.getPlayer(player).setTitle(player.getName());
			}
			return;
		} else {
			n = team.getColorString() + player.getName();
		}
		n = n.replaceAll("(&([a-f0-9]))", "§$2");
		
		player.setDisplayName(n);

		if (arena.cfg.getBoolean("game.hideName")) {
			n = " ";
		}
		if (spoutHandler != null) {
			SpoutManager.getPlayer(player).setTitle(n);
		}
	}
	
	@Override
	public void configParse(Arena arena, YamlConfiguration config, String type) {
		config.addDefault("game.hideName", Boolean.valueOf(false));
		config.addDefault("messages.colorNick", Boolean.valueOf(true));
		config.options().copyDefaults(true);
	}
	
	@Override
	public void initLanguage(YamlConfiguration config) {
		config.addDefault("log.nospout",
				"Spout not found, you are missing some features ;)");
		config.addDefault("log.spout", "Hooking into Spout!");
	}
	
	@Override
	public void onEnable() {
		if (Bukkit.getServer().getPluginManager().getPlugin("Spout") != null) {
			spoutHandler = SpoutManager.getInstance().toString();
		}
		Language.log_info((spoutHandler == null) ? "nospout" : "spout");
	}

	@Override
	public void parseInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6ColoredTeams:§f "
				+ StringParser.colorVar("hideName", arena.cfg.getBoolean("game.hideName"))
				+ " || "
				+ StringParser.colorVar("colorNick", arena.cfg.getBoolean("messages.colorNick")));
	}

	@Override
	public void tpPlayerToCoordName(Arena arena, Player player, String place) {
		if (arena.cfg.getBoolean("messages.colorNick", true)) {
			colorizePlayer(player);	
		}
		
	}
	
	@Override
	public void unload(Player player) {
		if (spoutHandler != null) {
			SpoutManager.getPlayer(player).setTitle(player.getName());
		}
	}
}
