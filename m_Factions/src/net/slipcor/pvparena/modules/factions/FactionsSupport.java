package net.slipcor.pvparena.modules.factions;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
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
	public void configParse(Arena arena, YamlConfiguration config) {
		config.addDefault("factions.support", Boolean.valueOf(false));
		config.addDefault("factions.overrideteamkill", Boolean.valueOf(false));
		config.options().copyDefaults(true);
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new FactionsListener(), PVPArena.instance);
	}
}
