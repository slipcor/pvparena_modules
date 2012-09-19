package net.slipcor.pvparena.modules.announcements;

import java.util.HashMap;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.loadables.ArenaModule;

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
		return "v0.8.11.6";
	}

	@Override
	public void addSettings(HashMap<String, String> types) {
		types.put("announcements.join", "boolean");
		types.put("announcements.start", "boolean");
		types.put("announcements.end", "boolean");
		types.put("announcements.winner", "boolean");
		types.put("announcements.loser", "boolean");
		types.put("announcements.prize", "boolean");
		types.put("announcements.custom", "boolean");
		types.put("announcements.radius", "int");
		types.put("announcements.color", "string");
	}

	@Override
	public void announceCustom(Arena arena, String message) {
		Announcement.announce(arena, Announcement.type.CUSTOM, message);
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
				Language.parse(MSG.PLAYER_JOINED_TEAM,
						player.getName(),
						coloredTeam));
	}
	
	@Override
	public void commitPlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
		Announcement.announce(arena, Announcement.type.LOSER, Language.parse(MSG.FIGHT_KILLED_BY,
				player.getName(), arena.parseDeathCause(player, cause.getCause(), ArenaPlayer
						.getLastDamagingPlayer(cause))));
	}
	
	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		config.addDefault("announcements.join", Boolean.valueOf(false));
		config.addDefault("announcements.start", Boolean.valueOf(false));
		config.addDefault("announcements.end", Boolean.valueOf(false));
		config.addDefault("announcements.winner", Boolean.valueOf(false));
		config.addDefault("announcements.loser", Boolean.valueOf(false));
		config.addDefault("announcements.prize", Boolean.valueOf(false));
		config.addDefault("announcements.custom", Boolean.valueOf(false));
		config.addDefault("announcements.radius", Integer.valueOf(0));
		config.addDefault("announcements.color", "AQUA");
		
		config.options().copyDefaults(true);
	}
	
	@Override
	public void parseJoin(Arena arena, CommandSender sender, ArenaTeam team) {

		if (TeamManager.countPlayersInTeams(arena) < 2) {
			Announcement.announce(arena, Announcement.type.START,
					Language.parse(MSG.ANNOUNCE_ARENA_STARTING, arena.getName()));
		}
		Announcement.announce(arena, Announcement.type.JOIN,
				Language.parse(MSG.PLAYER_JOINED_TEAM, sender.getName(), team.getColoredName()));
	}

	@Override
	public void playerLeave(Arena arena, Player player, ArenaTeam team) {
		if (team == null) {
			Announcement.announce(arena, Announcement.type.LOSER,
					Language.parse(MSG.FIGHT_PLAYER_LEFT, player.getName()));
		} else {
			Announcement.announce(arena, Announcement.type.LOSER,
					Language.parse(MSG.FIGHT_PLAYER_LEFT, team.colorizePlayer(player)));
		}
	}

	@Override
	public void parseInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6Announcements:§f radius: "
				+ StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_ANNOUNCEMENTS_RADIUS, 0))
				+ " || color: "
				+ StringParser.colorVar(arena.getArenaConfig().getString(CFG.MODULES_ANNOUNCEMENTS_COLOR))
				+ " || "
				+ StringParser.colorVar("join", arena.getArenaConfig().getBoolean(CFG.MODULES_ANNOUNCEMENTS_JOIN)));
		player.sendMessage(StringParser.colorVar("start", arena.getArenaConfig().getBoolean(CFG.MODULES_ANNOUNCEMENTS_START))
				+ " || "
				+ StringParser.colorVar("end", arena.getArenaConfig().getBoolean(CFG.MODULES_ANNOUNCEMENTS_END))
				+ " || "
				+ StringParser.colorVar("winner", arena.getArenaConfig().getBoolean(CFG.MODULES_ANNOUNCEMENTS_WINNER))
				+ " || "
				+ StringParser.colorVar("loser", arena.getArenaConfig().getBoolean(CFG.MODULES_ANNOUNCEMENTS_LOSER))
				+ " || "
				+ StringParser.colorVar("prize", arena.getArenaConfig().getBoolean(CFG.MODULES_ANNOUNCEMENTS_PRIZE))
				+ " || "
				+ StringParser.colorVar("custom", arena.getArenaConfig().getBoolean(CFG.MODULES_ANNOUNCEMENTS_CUSTOM)));
	}

	@Override
	public void teleportAllToSpawn(Arena arena) {
		Announcement.announce(arena, Announcement.type.START,
				Language.parse(MSG.FIGHT_BEGINS));
	}
}
