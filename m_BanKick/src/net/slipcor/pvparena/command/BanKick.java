package net.slipcor.pvparena.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.commands.PAA__Command;
import net.slipcor.pvparena.commands.PAI_Ready;
import net.slipcor.pvparena.commands.PAI_Version;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.loadables.ArenaModule;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class BanKick extends ArenaModule {
	HashSet<String> commands = new HashSet<String>();
	public BanKick() {
		super("BanKick");
		commands.add("ban");
		commands.add("kick");
		commands.add("tempban");
		commands.add("unban");
		commands.add("tempunban");
	}
	
	@Override
	public String version() {
		return "v0.8.7.2";
	}

	public static HashMap<Arena, List<String>> bans = new HashMap<Arena, List<String>>();
	
	@Override
	public boolean checkJoin(Arena arena, Player player) {
		if (bans.get(arena).contains(player.getName())) {
			ArenaManager.tellPlayer(player, Language.parse("youwerebanned", arena.getName()));
			return false;
		}
		return true;
	}
	
	@Override
	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
		
		if (!commands.contains(args[0].toLowerCase())) {
			return;
		}
		
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			arena.msg(sender,
					Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}
		
		/*
/pa [arenaname] kick [player]
/pa [arenaname] tempban [player] [timediff*]
/pa [arenaname] ban [player]
/pa [arenaname] unban [player]
/pa [arenaname] tempunban [player] [timediff*]
		 */
		
		String cmd = args[0].toLowerCase();
		
		Player p = Bukkit.getPlayer(args[1]);
		if (p != null) {
			args[1] = p.getName();
		}
		
		if (cmd.equals("kick")) {
			if (!(new PAI_Ready()).argCountValid(sender, arena, args, new Integer[]{2})) {
				return;
			}
			tryKick(sender, arena, args[1]);
		} else if (cmd.equals("tempban")) {
			if (!(new PAI_Ready()).argCountValid(sender, arena, args, new Integer[]{3})) {
				return;
			}
			tryKick(sender, arena, args[1]);
			long time = parseStringToSeconds(args[2]);
			BanRunnable run = new BanRunnable(arena, sender, args[1], false);
			Bukkit.getScheduler().scheduleAsyncDelayedTask(PVPArena.instance, run, 20 * time);
			doBan(sender, arena, args[1]);
		} else if (cmd.equals("ban")) {
			if (!(new PAI_Ready()).argCountValid(sender, arena, args, new Integer[]{2})) {
				return;
			}
			tryKick(sender, arena, args[1]);
			doBan(sender, arena, args[1]);
		} else if (cmd.equals("unban")) {
			if (!(new PAI_Ready()).argCountValid(sender, arena, args, new Integer[]{2})) {
				return;
			}
			doUnBan(sender, arena, args[1]);
		} else if (cmd.equals("tempunban")) {
			if (!(new PAI_Ready()).argCountValid(sender, arena, args, new Integer[]{3})) {
				return;
			}
			long time = parseStringToSeconds(args[2]);
			BanRunnable run = new BanRunnable(arena, sender, args[1], true);
			Bukkit.getScheduler().scheduleAsyncDelayedTask(PVPArena.instance, run, 20 * time);
			doUnBan(sender, arena, args[1]);
		}
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		List<String> lBans = config.getStringList("bans");
		
		HashSet<String> hsBans = new HashSet<String>();
		
		
		for (String s : lBans) {
			hsBans.add(s);
		}
		
		lBans.clear();
		for (String s : hsBans) {
			lBans.add(s);
		}
		
		bans.put(arena, lBans);
	}
	
	protected static void doBan(CommandSender admin, Arena arena, String player) {
		bans.get(arena).add(player);
		if (admin != null) {
			ArenaManager.tellPlayer(admin, Language.parse("playerbanned", player), arena);
		}
		tryNotify(player, Language.parse("youwerebanned", arena.getName()));
		arena.getArenaConfig().set("bans", bans.get(arena));
		arena.getArenaConfig().save();
	}

	protected static void doUnBan(CommandSender admin, Arena arena, String player) {
		bans.get(arena).remove(player);
		if (admin != null) {
			ArenaManager.tellPlayer(admin, Language.parse("playerunbanned", player), arena);
		}
		tryNotify(player, Language.parse("youwereunbanned", arena.getName()));
		arena.getArenaConfig().set("bans", bans.get(arena));
		arena.getArenaConfig().save();
	}

	@Override
	public void initLanguage(YamlConfiguration config) {
		config.addDefault("lang.playerkicked", "Player kicked: %1%");
		config.addDefault("lang.playerbanned", "Player banned: %1%");
		config.addDefault("lang.playerunbanned", "Player unbanned: %1%");
		config.addDefault("lang.playernotkicked", "Player not kicked: %1%");
		config.addDefault("lang.playernotonline", "Player is not online: %1%");
		config.addDefault("lang.youwerebanned", "You are banned from arena %1%");
		config.addDefault("lang.youwerekicked", "You were kicked from arena %1%");
		config.addDefault("lang.youwereunbanned", "You are unbanned from arena %1%");
		config.options().copyDefaults(true);
	}
	
	@Override
	public boolean parseCommand(String s) {
		return commands.contains(s.toLowerCase());
	}
	
	private long parseStringToSeconds(String string) {
		String input = "";
		
		int pos = 0;
		char type = 's';
		
		while (pos < string.length()) {
			String sChar = string.substring(pos, pos+1);
			
			try {
				int i = Integer.parseInt(sChar);
				input += String.valueOf(i);
			} catch (Exception e) {
				if (sChar.equals(".") || sChar.equals(",")) {
					input += ".";
				} else {
					type = sChar.charAt(0);
					break;
				}
			}
			
			pos++;
		}
		
		float time = Float.parseFloat(input);

		if (type == 'd') {
			time *= 24;
			type = 'h';
		}
		if (type == 'h') {
			time *= 60;
			type = 'm';
		}
		if (type == 'm') {
			time *= 60;
			type = 's';
		}

		return (long) time;
	}
	
	private void tryKick(CommandSender sender, Arena arena, String string) {
		Player p = Bukkit.getPlayer(string);
		if (p == null) {
			ArenaManager.tellPlayer(sender, Language.parse("playernotkicked",string), arena);
			return;
		}
		arena.playerLeave(p, "exit");
		ArenaManager.tellPlayer(p, Language.parse("youwerekicked", arena.getName()), arena);
		ArenaManager.tellPlayer(sender, Language.parse("playerkicked",string), arena);
	}
	/*
/pa tempban [player] [timediff*]                             <----- This means banning the Player temporary from ALL Arenas!
/pa ban [player]                                                     <----- The Player can't play PVP-Arena anymore. He is banned from ALL Arenas!
/pa unban [player]                                                 <----- Unbans a Player from ALL Arenas!
/pa tempunban [player] [timediff*]                         <----- Unbans a Player temporary from ALL Arenas!
	 */
}
