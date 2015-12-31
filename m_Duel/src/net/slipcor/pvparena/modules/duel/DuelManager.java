package net.slipcor.pvparena.modules.duel;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DuelManager extends ArenaModule {
    public DuelManager() {
        super("Duel");
    }

    @Override
    public String version() {
        return "v1.3.1.39";
    }

    private String duelSender = null;
    private String duelReceiver = null;

    @Override
    public boolean checkCommand(final String s) {
        return "duel".equals(s.toLowerCase()) || "accept".equals(s.toLowerCase()) || "decline".equals(s.toLowerCase());
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("duel", "accept", "decline");
    }

    @Override
    public List<String> getShort() {
        return new ArrayList<String>();
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"{Player}"});
        return result;
    }

    @Override
    public boolean hasPerms(final CommandSender sender, final Arena arena) {
        return super.hasPerms(sender, arena) || PVPArena.hasPerms(sender, arena);
    }

    @Override
    public PACheck checkJoin(CommandSender sender, PACheck res, boolean spectate) {
        if (!spectate) {
            if (!sender.getName().equals(this.duelReceiver) && !sender.getName().equals(this.duelSender)) {
                res.setError(this, Language.parse(arena, MSG.MODULE_DUEL_NODIRECTJOIN));
            }
        }
        return null;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        if (arena.isFightInProgress()) {
            arena.msg(sender,
                    Language.parse(MSG.ERROR_FIGHT_IN_PROGRESS));
            return;
        }
        String arenaName = null;

        if (!PVPArena.hasOverridePerms(sender) && ArenaManager.isUsingShortcuts()) {
            Map<String, List<String>> vals = ArenaManager.getShortcutDefinitions();
            for (String key : vals.keySet()) {
                if (vals.get(key).contains(arena.getName())) {
                    arenaName = key;
                }
            }
            if (arenaName == null) {
                arenaName = arena.getName();
            }
        } else {
            arenaName = arena.getName();
        }
        if ("duel".equals(args[0].toLowerCase()) && args.length > 1) {
            if (sender.getName().equals(duelSender)) {
                arena.msg(sender, Language.parse(MSG.MODULE_DUEL_REQUESTED_ALREADY));
            } else {
                final Player p = Bukkit.getPlayer(args[1]);
                if (p == null) {
                    arena.msg(sender, Language.parse(MSG.ERROR_PLAYER_NOTFOUND, args[1]));
                    return;
                }
                arena.msg(p, Language.parse(MSG.MODULE_DUEL_ANNOUNCE, sender.getName(), arenaName));
                arena.msg(p, Language.parse(MSG.MODULE_DUEL_ANNOUNCE2, sender.getName(), arenaName));
                arena.msg(sender, Language.parse(MSG.MODULE_DUEL_REQUESTED, p.getName()));
                duelSender = sender.getName();
                duelReceiver = p.getName();
                class LaterRunner implements Runnable {
                    @Override
                    public void run() {
                        if (duelSender != null) {
                            if (p != null) {
                                arena.msg(p, Language.parse(MSG.MODULE_DUEL_REQUEST_EXPIRED_RECEIVER));
                            }
                            if (sender != null) {
                                arena.msg(sender, Language.parse(MSG.MODULE_DUEL_REQUEST_EXPIRED_SENDER));
                            }
                            duelSender = null;
                            duelReceiver = null;
                        }
                    }
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new LaterRunner(), 1200L);
            }
        } else if ("accept".equals(args[0].toLowerCase())) {
            if (duelSender != null && !arena.isFightInProgress()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new DuelRunnable(this, duelSender, sender.getName()), 500L);
                final Player p = Bukkit.getPlayer(duelSender);
                if (p != null) {
                    arena.msg(p, Language.parse(MSG.MODULE_DUEL_ACCEPTED, sender.getName()));
                }
                duelSender = null;
                duelReceiver = null;
            } else if (arena.isFightInProgress()) {
                arena.msg(sender,
                        Language.parse(MSG.ERROR_FIGHT_IN_PROGRESS));
            }
        } else if ("decline".equals(args[0].toLowerCase()) && duelSender != null) {
            ArenaPlayer p = ArenaPlayer.parsePlayer(duelSender);
            if (p != null && p.get() != null){
                arena.msg(p.get(), Language.parse(MSG.MODULE_DUEL_DECLINED_SENDER));
            }
            arena.msg(sender, Language.parse(MSG.MODULE_DUEL_DECLINED_RECEIVER));
            duelSender = null;
            duelReceiver = null;
        }
    }

}
