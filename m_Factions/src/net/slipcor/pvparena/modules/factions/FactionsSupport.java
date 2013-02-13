package net.slipcor.pvparena.modules.factions;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.loadables.ArenaModule;

public class FactionsSupport extends ArenaModule {
	
	public FactionsSupport() {
		super("Factions");
	}
	
	private boolean setup = false;
	
	@Override
	public String version() {
		return "v1.0.1.44";
	}
	
	@Override
	public void configParse(YamlConfiguration config) {
		if (setup)
			return;
		Bukkit.getPluginManager().registerEvents(new FactionsListener(this), PVPArena.instance);
		setup = true;
	}
}
