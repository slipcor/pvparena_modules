package net.slipcor.pvparena.modules.battlefieldguard;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class BattlefieldGuard extends ArenaModule {
    private boolean setup = false;

    public BattlefieldGuard() {
        super("BattlefieldGuard");
    }

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    @Override
    public void configParse(YamlConfiguration config) {
        if (setup)
            return;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, new BattleRunnable(), 20L, 20L);
        setup = true;
    }

    @Override
    public void displayInfo(CommandSender sender) {
        sender.sendMessage(StringParser.colorVar("enterdeath", arena.getArenaConfig().getBoolean(CFG.MODULES_BATTLEFIELDGUARD_ENTERDEATH)));
    }

    @Override
    public boolean hasSpawn(String s) {
        return s.equalsIgnoreCase("exit");
    }

    @Override
    public boolean needsBattleRegion() {
        return true;
    }
}
