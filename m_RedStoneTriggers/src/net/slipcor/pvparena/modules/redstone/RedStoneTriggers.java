package net.slipcor.pvparena.modules.redstone;

import org.bukkit.Bukkit;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.neworder.ArenaModule;

public class RedStoneTriggers extends ArenaModule {
	public RedStoneTriggers() {
		super("RedStoneTriggers");
	}
	
	@Override
	public String version() {
		return "v0.8.4.1";
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new RedStoneListener(), PVPArena.instance);
	}
}
