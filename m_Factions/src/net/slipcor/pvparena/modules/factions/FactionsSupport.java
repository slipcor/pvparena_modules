package net.slipcor.pvparena.modules.factions;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class FactionsSupport extends ArenaModule {

    public FactionsSupport() {
        super("Factions");
    }

    private boolean setup;

    @Override
    public String version() {
        return "v1.3.0.515";
    }

    @Override
    public void configParse(final YamlConfiguration config) {
        if (setup) {
            return;
        }
        Bukkit.getPluginManager().registerEvents(new FactionsListener(this), PVPArena.instance);
        setup = true;
    }
}
