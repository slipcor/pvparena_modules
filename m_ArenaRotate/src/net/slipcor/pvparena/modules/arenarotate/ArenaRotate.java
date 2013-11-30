package net.slipcor.pvparena.modules.arenarotate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.runnables.StartRunnable;

public class ArenaRotate extends ArenaModule {
	private static Arena a = null;

	private static ArenaRotateRunnable vote = null;
	
	public ArenaRotate() {
		super("ArenaRotate");
	}

	@Override
	public String version() {
		return "v1.1.0.302";
	}

	@Override
	public PACheck checkJoin(CommandSender sender,
			PACheck res, boolean b) {
		if (res.hasError() || !b) {
			return res;
		}
		
		if (a != null && !arena.equals(a)) {
			res.setError(this, Language.parse(MSG.MODULE_AUTOVOTE_ARENARUNNING, arena.getName()));
			Bukkit.getServer().dispatchCommand(sender, "join "+arena.getName());
			return res;
		}
		
		if (arena.getArenaConfig().getBoolean(CFG.PERMS_JOINWITHSCOREBOARD)) {
			return res;
		}
		
		Player p = (Player) sender;

		for (Team team : p.getScoreboard().getTeams()) {
			for (OfflinePlayer player : team.getPlayers()) {
				if (player.getName().equals(p.getName())) {
					res.setError(this, Language.parse(MSG.ERROR_COMMAND_BLOCKED, "You already have a scoreboard!"));
					return res;
				}
			}
		}
		
		return res;
	}

	@Override
	public void reset(boolean force) {
		a = null;
		
		if (vote == null) {
			vote = new ArenaRotateRunnable(arena,
					arena.getArenaConfig().getInt(CFG.MODULES_ARENAVOTE_SECONDS));
		}
	}

	public static void commit() {
		
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pvparena ALL disable");
		

		if (a == null) {
			
			int pos = ((new Random()).nextInt(ArenaManager.getArenas().size()));
			
			for (Arena arena : ArenaManager.getArenas()) {
				if (--pos < 0) {
					a = arena;
					break;
				}
			}
		}

		if (a == null) {
			PVPArena.instance.getLogger().warning("Rotation resulted in NULL!");
			
			return;
		}

		final PAG_Join pj = new PAG_Join();

		final Set<String> toTeleport = new HashSet<String>();
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			toTeleport.add(p.getName());
		}
		
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pvparena "+a.getName()+" enable");
		
		class TeleportLater extends BukkitRunnable {

			@Override
			public void run() {
				for (String pName : toTeleport) {
					Player p = Bukkit.getPlayerExact(pName);
					toTeleport.remove(pName);
					if (p == null) {
						return;
					}

					pj.commit(a, p, new String[0]);
					return;
				}

				new StartRunnable(a,
						a.getArenaConfig().getInt(CFG.MODULES_ARENAVOTE_READYUP));
				class RunLater implements Runnable {

					@Override
					public void run() {
						vote = null;
					}
					
				}
				Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 20L);
				this.cancel();
			}
			
		}
		new TeleportLater().runTaskTimer(PVPArena.instance, 1L, 1L);
		
	}
	
	@Override
	public void onThisLoad() {
		
		class RunLater implements Runnable {

			@Override
			public void run() {
				boolean active = false;
				ArenaModule commitMod = null;
				for (Arena arena : ArenaManager.getArenas()) {
					for (ArenaModule mod : arena.getMods()) {
						if (mod.getName().equals(ArenaRotate.this.getName())
								&& arena.getArenaConfig().getBoolean(CFG.MODULES_ARENAVOTE_AUTOSTART)) {

							active = true;
							commitMod = mod;
							break;
						}
					}
				}
				
				if (!active) {
					return;
				}
				commitMod.reset(false);
			}
			
		}
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 200L);
	}
}
