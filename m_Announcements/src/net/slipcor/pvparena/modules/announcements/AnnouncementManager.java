package net.slipcor.pvparena.modules.announcements;

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
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class AnnouncementManager extends ArenaModule {
	
	public AnnouncementManager() {
		super("Announcements");
	}
	
	@Override
	public String version() {
		return "v0.9.2.7";
	}

	@Override
	public void announce(Arena arena, String message, String type) {
		Announcement.announce(arena, Announcement.type.valueOf(type), message);
	}
	
	@Override
	public void choosePlayerTeam(Arena arena, Player player, String coloredTeam) {
		if (arena.isFreeForAll()) {
			Announcement.announce(
					arena,
					Announcement.type.JOIN,
					arena.getArenaConfig().getString(CFG.MSG_PLAYERJOINED).replace("%1%",player.getName()));
		} else {
			Announcement.announce(
					arena,
					Announcement.type.JOIN,
					arena.getArenaConfig().getString(CFG.MSG_PLAYERJOINEDTEAM).replace("%1%",player.getName())
					.replace("%2%",coloredTeam));
		}
	}
	
	@Override
	public void commitPlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
		Announcement.announce(arena, Announcement.type.LOSER, Language.parse(MSG.FIGHT_KILLED_BY,
				player.getName(), arena.parseDeathCause(player, cause.getCause(), ArenaPlayer
						.getLastDamagingPlayer(cause))));
	}

	@Override
	public void displayInfo(Arena arena, CommandSender player) {
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
	public boolean isActive(Arena a) {
		return a.getArenaConfig().getBoolean(CFG.MODULES_ANNOUNCEMENTS_ACTIVE);
	}
	
	@Override
	public void parseJoin(Arena arena, CommandSender sender, ArenaTeam team) {
		db.i("parseJoin ... ");

		if (TeamManager.countPlayersInTeams(arena) < 2) {
			Announcement.announce(arena, Announcement.type.START,
					Language.parse(MSG.ANNOUNCE_ARENA_STARTING, arena.getName()));
		}
		
		if (arena.isFreeForAll()) {
			Announcement.announce(
					arena,
					Announcement.type.JOIN,
					arena.getArenaConfig().getString(CFG.MSG_PLAYERJOINED).replace("%1%",sender.getName()));
		} else {
			Announcement.announce(
					arena,
					Announcement.type.JOIN,
					arena.getArenaConfig().getString(CFG.MSG_PLAYERJOINEDTEAM).replace("%1%",sender.getName())
					.replace("%2%",team.getColoredName()));
		}
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
	public void teleportAllToSpawn(Arena arena) {
		Announcement.announce(arena, Announcement.type.START,
				Language.parse(MSG.FIGHT_BEGINS));
	}
}
