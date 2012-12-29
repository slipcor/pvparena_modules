package net.slipcor.pvparena.modules.aftermatch;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.PAA__Command;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;

public class AfterMatch extends ArenaModule implements Cloneable {
	protected Integer runId = null;
	private boolean aftermatch = false;

	public AfterMatch() {
		super("AfterMatch");
	}
	

	@Override
	public String version() {
		return "v0.10.2.13";
	}

	public void afterMatch() {
		for (ArenaTeam t : arena.getTeams()) {
			for (ArenaPlayer p : t.getTeamMembers()) {
				if (!p.getStatus().equals(Status.FIGHT)) {
					continue;
				}
				Player player = p.get();
				arena.tpPlayerToCoordName(player, "after");
			}
		}
		arena.broadcast(Language.parse(MSG.MODULE_AFTERMATCH_STARTING));
		PVPArena.instance.getAgm().setPlayerLives(arena, 0);
		aftermatch = true;
	}
	
	@Override
	public boolean checkCommand(String s) {
		return s.equals("!am") || s.equals("aftermatch");
	}
	
	@Override
	public String checkForMissingSpawns(Set<String> list) {
		for (String s : list) {
			if (s.startsWith("after")) {
				return null;
			}
		}
		return Language.parse(MSG.MODULE_AFTERMATCH_SPAWNNOTSET);
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		// !am time 6
		// !am death 4
		
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			arena.msg(
					sender,
					Language.parse(MSG.ERROR_NOPERM,
							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}

		if (!PAA__Command.argCountValid(sender, arena, args, new Integer[] { 2,3 })) {
			return;
		}
		
		if (args[0].equals("!am") || args[0].equals("aftermatch")) {
			if (args.length == 2) {
				if (args[1].equals("off")) {
					arena.getArenaConfig().set(CFG.MODULES_AFTERMATCH_AFTERMATCH, args[1]);
					arena.getArenaConfig().save();
					arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_AFTERMATCH_AFTERMATCH.getNode(), args[1]));
					return;
				}
				arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "off"));
				return;
			}
			int i = 0;
			try {
				i = Integer.parseInt(args[2]);
			} catch (Exception e) {
				arena.msg(sender,
						Language.parse(MSG.ERROR_NOT_NUMERIC, args[2]));
				return;
			}
			if (args[1].equals("time") || args[1].equals("death")) {
				arena.getArenaConfig().set(CFG.MODULES_AFTERMATCH_AFTERMATCH, args[1]+":"+i);
				arena.getArenaConfig().save();
				arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_AFTERMATCH_AFTERMATCH.getNode(), args[1]+":"+i));
				return;
			}
			
			arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "time | death"));
			return;
		}
	}

	@Override
	public void configParse(YamlConfiguration config) {
		String pu = config.getString(CFG.MODULES_AFTERMATCH_AFTERMATCH.getNode(), "off");

		//String[] ss = pu.split(":");
		if (pu.startsWith("death")) {
			//
		} else if (pu.startsWith("time")) {
			// later
		} else {
			PVPArena.instance.getLogger().warning("error activating aftermatch module");
		}
	}

	@Override
	public void displayInfo(CommandSender player) {
		player.sendMessage("§bAfterMatch:§f "
				+ StringParser.colorVar(!arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH).equals("off"))
				+ "("
				+ StringParser.colorVar(arena.getArenaConfig()
						.getString(CFG.MODULES_AFTERMATCH_AFTERMATCH)) + ")");
	}
	
	@Override
	public boolean hasSpawn(String string) {
		return string.equals("after");
	}

	@Override
	public void parsePlayerDeath(Player player,
			EntityDamageEvent cause) {
		String pu = arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH);

		if (pu.equals("off") || aftermatch) {
			return;
		}
		
		String[] ss = pu.split(":");
		if (pu.startsWith("time") || runId != null) {
			return;
		}

		int i = Integer.parseInt(ss[1]);

		for (ArenaTeam t : arena.getTeams()) {
			for (ArenaPlayer p : t.getTeamMembers()) {
				if (!p.getStatus().equals(Status.FIGHT)) {
					continue;
				}
				if (--i < 0) {
					return;
				}
			}
		}

		runId = null;

		afterMatch();
	}

	@Override
	public void reset(boolean force) {
		String pu = arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH);

		if (pu.equals("off")) {
			return;
		}
		if (runId != null) {
			Bukkit.getScheduler().cancelTask(runId);
			runId = null;
		}
		aftermatch = false;
	}

	@Override
	public void parseStart() {
		String pu = arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH);

		if (runId != null) {
			Bukkit.getScheduler().cancelTask(runId);
			runId = null;
		}

		int i = 0;
		String[] ss = pu.split(":");
		if (pu.startsWith("time")) {
			// arena.powerupTrigger = "time";
			i = Integer.parseInt(ss[1]);
		} else {
			return;
		}

		db.i("using aftermatch : "
				+ arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH) + " : "
				+ i);
		if (i > 0) {
			db.i("aftermatch time trigger!");
			new AfterRunnable(this, arena.getArenaConfig().getInt(CFG.MODULES_AFTERMATCH_AFTERMATCH));
		}
	}
}
