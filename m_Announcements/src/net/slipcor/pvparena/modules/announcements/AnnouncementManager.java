package net.slipcor.pvparena.modules.announcements;

import java.util.HashMap;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.neworder.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class AnnouncementManager extends ArenaModule {
	
	public AnnouncementManager() {
		super("Announcements");
	}
	
	@Override
	public String version() {
		return "v0.7.25.0";
	}

	@Override
	public void addSettings(HashMap<String, String> types) {
		types.put("announcements.join", "boolean");
		types.put("announcements.start", "boolean");
		types.put("announcements.end", "boolean");
		types.put("announcements.winner", "boolean");
		types.put("announcements.loser", "boolean");
		types.put("announcements.prize", "boolean");
		types.put("announcements.radius", "int");
		types.put("announcements.color", "string");
	}

	@Override
	public void announceLoser(Arena arena, String message) {
		Announcement.announce(arena, Announcement.type.LOSER, message);
	}

	@Override
	public void announcePrize(Arena arena, String message) {
		Announcement.announce(arena, Announcement.type.PRIZE, message);
	}

	@Override
	public void announceWinner(Arena arena, String message) {

		Announcement.announce(arena, Announcement.type.WINNER, message);
	}
	
	@Override
	public void choosePlayerTeam(Arena arena, Player player, String coloredTeam) {

		Announcement.announce(
				arena,
				Announcement.type.JOIN,
				Language.parse("playerjoined" + (arena.type().getName().equals("free") ? "free" : ""),
						player.getName(),
						coloredTeam));
	}
	
	@Override
	public void commitPlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
		Announcement.announce(arena, Announcement.type.LOSER, Language.parse("killedby",
				player.getName(), arena.parseDeathCause(player, cause.getCause(), ArenaPlayer
						.getLastDamagingPlayer(cause))));
	}
	
	@Override
	public void configParse(Arena arena, YamlConfiguration config, String type) {
		config.addDefault("announcements.join", Boolean.valueOf(false));
		config.addDefault("announcements.start", Boolean.valueOf(false));
		config.addDefault("announcements.end", Boolean.valueOf(false));
		config.addDefault("announcements.winner", Boolean.valueOf(false));
		config.addDefault("announcements.loser", Boolean.valueOf(false));
		config.addDefault("announcements.prize", Boolean.valueOf(false));
		config.addDefault("announcements.radius", Integer.valueOf(0));
		config.addDefault("announcements.color", "AQUA");
		
		config.options().copyDefaults(true);
	}
	
	@Override
	public void initLanguage(YamlConfiguration config) {
		config.addDefault("lang.killedby", "%1% has been killed by %2%!");
	}
	
	@Override
	public void parseJoin(Arena arena, Player player, String coloredTeam) {

		if (Teams.countPlayersInTeams(arena) < 2) {
			Announcement.announce(arena, Announcement.type.START,
					Language.parse("joinarena", arena.name));
		}
		Announcement.announce(arena, Announcement.type.JOIN,
				Language.parse("playerjoined", player.getName(), coloredTeam));
	}

	@Override
	public void playerLeave(Arena arena, Player player, ArenaTeam team) {
		if (team == null) {
			Announcement.announce(arena, Announcement.type.LOSER,
					Language.parse("playerleave", player.getName()));
		} else {
			Announcement.announce(arena, Announcement.type.LOSER,
					Language.parse("playerleave", team.colorizePlayer(player)));
		}
	}

	@Override
	public void parseInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6Announcements:§f radius: "
				+ StringParser.colorVar(arena.cfg.getInt("announcements.radius", 0))
				+ " || color: "
				+ StringParser.colorVar(arena.cfg.getString("announcements.color"))
				+ " || "
				+ StringParser.colorVar("join", arena.cfg.getBoolean("announcements.join")));
		player.sendMessage(StringParser.colorVar("start", arena.cfg.getBoolean("announcements.start"))
				+ " || "
				+ StringParser.colorVar("end", arena.cfg.getBoolean("announcements.end"))
				+ " || "
				+ StringParser.colorVar("winner", arena.cfg.getBoolean("announcements.winner"))
				+ " || "
				+ StringParser.colorVar("loser", arena.cfg.getBoolean("announcements.loser"))
				+ " || "
				+ StringParser.colorVar("prize", arena.cfg.getBoolean("announcements.prize")));
	}

	@Override
	public void teleportAllToSpawn(Arena arena) {
		Announcement.announce(arena, Announcement.type.START,
				Language.parse("begin"));
	}
}
