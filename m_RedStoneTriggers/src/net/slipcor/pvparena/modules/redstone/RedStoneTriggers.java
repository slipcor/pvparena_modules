package net.slipcor.pvparena.modules.redstone;

import org.bukkit.Bukkit;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.loadables.ArenaModule;

public class RedStoneTriggers extends ArenaModule {
	public RedStoneTriggers() {
		super("RedStoneTriggers");
	}
	
	@Override
	public String version() {
		return "v0.8.8.5";
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new RedStoneListener(), PVPArena.instance);
	}
}
