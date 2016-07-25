package net.slipcor.pvparena.modules.thermosfix;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ThermosFix extends ArenaModule {
    public ThermosFix() {
        super("ThermosFix");
    }

    @Override
    public String version() {
        return "v1.3.3.180";
    }

    int before = 10;
    int protect = 200;

    @Override
    public void configParse(final YamlConfiguration config) {
        config.addDefault("thermos.ticks.before", 20);
        config.addDefault("thermos.ticks.protect", 200);
        config.options().copyDefaults(true);

        before = config.getInt("thermos.ticks.before", 20);
        protect = config.getInt("thermos.ticks.protect", 200);
    }

    @Override
    public void resetPlayer(final Player player, final boolean soft, final boolean force) {
        if (!force && !soft) {
            Bukkit.getScheduler().runTaskLater(PVPArena.instance, new Runnable() {
                @Override
                public void run() {
                    player.setNoDamageTicks(protect);
                }
            }, before);
        }
    }
}
