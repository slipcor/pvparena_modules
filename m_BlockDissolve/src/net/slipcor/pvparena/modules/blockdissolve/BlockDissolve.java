package net.slipcor.pvparena.modules.blockdissolve;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class BlockDissolve extends ArenaModule {
	public class RunLater implements Runnable {
		Map<ArenaPlayer, PABlockLocation> locs = new HashMap<ArenaPlayer, PABlockLocation>();
		@Override
		public void run() {
			if (arena.isFightInProgress() && arena.getPlayedSeconds() > arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_STARTSECONDS)) {
				for (ArenaPlayer ap : arena.getFighters()) {
					if (!locs.containsKey(ap)) {
						locs.put(ap, new PABlockLocation(ap.get().getLocation()));
						continue;
					}
					if (locs.get(ap).equals(new PABlockLocation(ap.get().getLocation()))) {
						checker.checkBlock(ap.get().getLocation().add(0, -1, 0));
					} else {
						locs.put(ap, new PABlockLocation(ap.get().getLocation()));
					}
				}
			} else {
				locs.clear();
			}
		}

	}

	private boolean setup = false;
	private MoveChecker checker;

	public BlockDissolve() {
		super("BlockDissolve");
	}
	
	@Override
	public String version() {
		return "v1.2.3.449";
	}
	
	Runnable runner = null;

	@Override
	public void configParse(YamlConfiguration config) {
		if (setup) {
			return;
		}
		if (checker == null) {
			checker = new MoveChecker(arena, arena.getArenaConfig().getString(CFG.MODULES_BLOCKDISSOLVE_MATERIALS), 
					arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_TICKS));
		}
		setup = true;
		runner = new RunLater();
		Bukkit.getScheduler().runTaskTimer(PVPArena.instance, runner, 20L, 20L);
	}
	
	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("ticks: "+ arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_TICKS));
		sender.sendMessage("materials: "+ arena.getArenaConfig().getString(CFG.MODULES_BLOCKDISSOLVE_MATERIALS));
	}
	
	public void reset(boolean force) {
		checker.clear();
	}
}
