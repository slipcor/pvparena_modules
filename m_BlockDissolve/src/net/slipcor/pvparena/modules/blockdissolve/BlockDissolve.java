package net.slipcor.pvparena.modules.blockdissolve;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class BlockDissolve extends ArenaModule {

	private boolean setup = false;
	private MoveChecker checker;

	public BlockDissolve() {
		super("BlockDissolve");
	}
	
	@Override
	public String version() {
		return "v1.2.3.455";
	}

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
	}
	
	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("ticks: "+ arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_TICKS));
		sender.sendMessage("materials: "+ arena.getArenaConfig().getString(CFG.MODULES_BLOCKDISSOLVE_MATERIALS));
	}
	
	@Override
	public void parseStart() {
		checker.start();
	}
	
	public void reset(boolean force) {
		checker.clear();
	}
}
