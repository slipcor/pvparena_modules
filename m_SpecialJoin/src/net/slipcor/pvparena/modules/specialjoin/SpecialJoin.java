package net.slipcor.pvparena.modules.specialjoin;

import org.bukkit.Bukkit;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.loadables.ArenaModule;

public class SpecialJoin extends ArenaModule {
	public SpecialJoin() {
		super("SpecialJoin");
	}
	
	@Override
	public String version() {
		return "v0.9.0.0";
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return true;
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new SpecialJoinListener(), PVPArena.instance);
	}
}
