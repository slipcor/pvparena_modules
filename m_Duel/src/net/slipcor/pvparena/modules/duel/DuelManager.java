package net.slipcor.pvparena.modules.duel;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuelManager extends ArenaModule {
    public DuelManager() {
        super("Duel");
    }

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    public String duel = null;

    @Override
    public boolean checkCommand(String s) {
        return s.toLowerCase().equals("duel") || s.toLowerCase().equals("accept");
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("duel", "accept");
    }

    @Override
    public List<String> getShort() {
        return new ArrayList<String>();
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"{Player}"});
        return result;
    }

    @Override
    public boolean hasPerms(final CommandSender sender, final Arena arena) {
        return super.hasPerms(sender, arena) || PVPArena.hasPerms(sender, arena);
    }

    @Override
    public void commitCommand(CommandSender sender, String[] args) {
        if (arena.isFightInProgress()) {
            arena.msg(sender,
                    Language.parse(MSG.ERROR_FIGHT_IN_PROGRESS));
            return;
        }
        if (args[0].toLowerCase().equals("duel")) {
            Player p = Bukkit.getPlayer(args[1]);
            if (p == null) {
                arena.msg(sender, Language.parse(MSG.ERROR_PLAYER_NOTFOUND, args[1]));
                return;
            }
            arena.msg(p, Language.parse(MSG.MODULE_DUEL_ANNOUNCE, sender.getName(), arena.getName()));
            arena.msg(p, Language.parse(MSG.MODULE_DUEL_REQUESTED, sender.getName()));
            duel = sender.getName();
        } else if (args[0].toLowerCase().equals("accept")) {
            if (duel != null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new DuelRunnable(this, duel, sender.getName()), 500L);
                Player p = Bukkit.getPlayer(duel);
                if (p != null) {
                    p.sendMessage(Language.parse(MSG.MODULE_DUEL_ACCEPTED, sender.getName()));
                }
            }
        }
    }
}
