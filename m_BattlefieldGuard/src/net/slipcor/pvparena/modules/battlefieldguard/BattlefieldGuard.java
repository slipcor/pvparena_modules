package net.slipcor.pvparena.modules.battlefieldguard;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class BattlefieldGuard extends ArenaModule {
    private boolean setup;

    public BattlefieldGuard() {
        super("BattlefieldGuard");
    }

    @Override
    public String version() {
        return "v1.3.2.51";
    }

    @Override
    public void configParse(final YamlConfiguration config) {
        if (setup) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, new BattleRunnable(), 20L, 20L);
        setup = true;
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage(StringParser.colorVar("enterdeath", arena.getArenaConfig().getBoolean(CFG.MODULES_BATTLEFIELDGUARD_ENTERDEATH)));
    }

    @Override
    public boolean hasSpawn(final String s) {
        return "exit".equalsIgnoreCase(s);
    }

    @Override
    public boolean needsBattleRegion() {
        return true;
    }
}
