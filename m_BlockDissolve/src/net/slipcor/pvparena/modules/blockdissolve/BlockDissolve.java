package net.slipcor.pvparena.modules.blockdissolve;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class BlockDissolve extends ArenaModule {
	private boolean setup = false;

	public BlockDissolve() {
		super("BlockDissolve");
	}
	
	@Override
	public String version() {
		return "v1.2.3.435";
	}

	@Override
	public void configParse(YamlConfiguration config) {
		if (setup) {
			return;
		}
		new MoveChecker(arena, arena.getArenaConfig().getString(CFG.MODULES_BLOCKDISSOLVE_MATERIALS));
		setup = true;
	}
	
	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("ticks",arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_TICKS));
		sender.sendMessage("materials",arena.getArenaConfig().getString(CFG.MODULES_BLOCKDISSOLVE_MATERIALS));
	}
}
