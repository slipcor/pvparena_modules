package net.slipcor.pvparena.modules.factions;

import org.bukkit.Bukkit;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class FactionsSupport extends ArenaModule {
	
	public FactionsSupport() {
		super("Factions");
	}
	
	@Override
	public String version() {
		return "v0.9.0.0";
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return arena.getArenaConfig().getBoolean(CFG.MODULES_FACTIONS_ACTIVE);
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new FactionsListener(), PVPArena.instance);
	}
}
