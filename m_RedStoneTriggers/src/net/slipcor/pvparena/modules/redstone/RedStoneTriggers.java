package net.slipcor.pvparena.modules.redstone;

import org.bukkit.Bukkit;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.loadables.ArenaModule;

public class RedStoneTriggers extends ArenaModule {
	public RedStoneTriggers() {
		super("RedStoneTriggers");
	}
	
	@Override
	public String version() {
		return "v0.9.6.16";
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return true;
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new RedStoneListener(), PVPArena.instance);
	}
}
