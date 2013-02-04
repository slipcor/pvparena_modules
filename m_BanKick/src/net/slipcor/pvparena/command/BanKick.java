package net.slipcor.pvparena.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class BanKick extends ArenaModule {
	static HashSet<String> commands = new HashSet<String>();
	
	static {
		commands.add("ban");
		commands.add("kick");
		commands.add("tempban");
		commands.add("unban");
		commands.add("tempunban");
	}
	
	public BanKick() {
		super("BanKick");
	}
	
	@Override
	public String version() {
		return "v1.0.0.25";
	}

	public List<String> banList = null;
	
	@Override
	public boolean checkCommand(String s) {
		return commands.contains(s.toLowerCase());
	}
	
	@Override
	public PACheck checkJoin(CommandSender sender,
			PACheck res, boolean b) {
		if (res.hasError()) {
			return res;
		}
		if (getBans().contains(sender.getName())) {
			res.setError(this, Language.parse(MSG.MODULE_BANVOTE_YOUBANNED, arena.getName()));
		}
		return res;
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		
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
			if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2})) {
				return;
			}
			tryKick(sender, args[1]);
		} else if (cmd.equals("tempban")) {
			if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{3})) {
				return;
			}
			tryKick(sender, args[1]);
			long time = parseStringToSeconds(args[2]);
			BanRunnable run = new BanRunnable(this, sender, args[1], false);
			Bukkit.getScheduler().runTaskLaterAsynchronously(PVPArena.instance, run, 20 * time);
			doBan(sender, args[1]);
		} else if (cmd.equals("ban")) {
			if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2})) {
				return;
			}
			tryKick(sender, args[1]);
			doBan(sender, args[1]);
		} else if (cmd.equals("unban")) {
			if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2})) {
				return;
			}
			doUnBan(sender, args[1]);
		} else if (cmd.equals("tempunban")) {
			if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{3})) {
				return;
			}
			long time = parseStringToSeconds(args[2]);
			BanRunnable run = new BanRunnable(this, sender, args[1], true);
			Bukkit.getScheduler().runTaskLaterAsynchronously(PVPArena.instance, run, 20 * time);
			doUnBan(sender, args[1]);
		}
	}

	@Override
	public void configParse(YamlConfiguration config) {
		List<String> lBans = config.getStringList("bans");
		
		HashSet<String> hsBans = new HashSet<String>();
		
		
		for (String s : lBans) {
			hsBans.add(s);
		}
		
		getBans().clear();
		for (String s : hsBans) {
			getBans().add(s);
		}
	}
	
	private List<String> getBans() {
		if (banList == null) {
			banList = new ArrayList<String>();
		}
		return banList;
	}
	
	protected void doBan(CommandSender admin, String player) {
		getBans().add(player);
		if (admin != null) {
			arena.msg(admin, Language.parse(MSG.MODULE_BANVOTE_BANNED, player));
		}
		tryNotify(admin, player, Language.parse(MSG.MODULE_BANVOTE_YOUBANNED, arena.getName()));
		arena.getArenaConfig().setManually("bans", getBans());
		arena.getArenaConfig().save();
	}

	protected void doUnBan(CommandSender admin, String player) {
		getBans().remove(player);
		if (admin != null) {
			arena.msg(admin, Language.parse(MSG.MODULE_BANVOTE_UNBANNED, player));
		}
		tryNotify(admin, player, Language.parse(MSG.MODULE_BANVOTE_YOUBANNED, arena.getName()));
		arena.getArenaConfig().setManually("bans", getBans());
		arena.getArenaConfig().save();
	}
	
	private long parseStringToSeconds(String string) {
		String input = "";
		
		int pos = 0;
		char type = 's';
		
		while (pos < string.length()) {
			Character c = string.charAt(pos);
			
			try {
				int i = Integer.parseInt("" + c);
				input += String.valueOf(i);
			} catch (Exception e) {
				if (c == '.' || c == ',') {
					input += ".";
				} else {
					type = c;
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
	
	private void tryKick(CommandSender sender, String string) {
		Player p = Bukkit.getPlayer(string);
		if (p == null) {
			arena.msg(sender, Language.parse(MSG.MODULE_BANVOTE_NOTKICKED,string));
			return;
		}
		arena.playerLeave(p, CFG.TP_EXIT, true);
		arena.msg(p, Language.parse(MSG.MODULE_BANVOTE_YOUKICKED, arena.getName()));
		arena.msg(sender, Language.parse(MSG.MODULE_BANVOTE_KICKED,string));
	}

	private void tryNotify(CommandSender sender, String player, String string) {
		Player p = Bukkit.getPlayer(string);
		if (p == null) {
			return;
		}
		arena.msg(p, string);
	}
	/*
/pa tempban [player] [timediff*]                             <----- This means banning the Player temporary from ALL Arenas!
/pa ban [player]                                                     <----- The Player can't play PVP-Arena anymore. He is banned from ALL Arenas!
/pa unban [player]                                                 <----- Unbans a Player from ALL Arenas!
/pa tempunban [player] [timediff*]                         <----- Unbans a Player temporary from ALL Arenas!
	 */
}
