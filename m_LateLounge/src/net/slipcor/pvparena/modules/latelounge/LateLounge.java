package net.slipcor.pvparena.modules.latelounge;

import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.PAG_Join;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LateLounge extends ArenaModule {
    public LateLounge() {
        super("LateLounge");
    }

    int priority = 3;

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    private List<String> playerList = null;

    @Override
    public PACheck checkJoin(CommandSender sender,
                             PACheck res, boolean b) {
        if (!b || res.hasError() || res.getPriority() > priority) {
            return res;
        }

        Player player = (Player) sender;

        if (getPlayerList().contains(player.getName())) {
            if (getPlayerList().size() < arena.getArenaConfig().getInt(CFG.READY_MINPLAYERS)) {
                res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
                int pos = 1;

                for (String name : getPlayerList()) {
                    if (name.equals(player.getName())) {
                        break;
                    }
                    pos++;
                }

                arena.msg(player, Language.parse(MSG.MODULE_LATELOUNGE_POSITION, String.valueOf(pos)));
                return res;
            }
        }

        if (arena.getArenaConfig().getInt(CFG.READY_MINPLAYERS) > getPlayerList().size() + 1) {
            // not enough players
            getPlayerList().add(player.getName());
            final int pos = getPlayerList().size();
            Player[] aPlayers = Bukkit.getOnlinePlayers();

            for (Player p : aPlayers) {
                if (p.equals(player)) {
                    continue;
                }
                try {
                    arena.msg(p, Language.parse(MSG.MODULE_LATELOUNGE_ANNOUNCE, ArenaManager.getIndirectArenaName(arena), player.getName()));

                } catch (Exception e) {
                    //
                }
            }
            arena.msg(player, Language.parse(MSG.MODULE_LATELOUNGE_POSITION, String.valueOf(pos)));
            res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
            return res;
        } else if (arena.getArenaConfig().getInt(CFG.READY_MINPLAYERS) == getPlayerList().size() + 1) {
            // not enough players
            getPlayerList().add(player.getName());

            HashSet<String> removals = new HashSet<String>();

            for (String s : getPlayerList()) {
                Player p = Bukkit.getPlayerExact(s);

                boolean removeMe = false;

                if (p != null) {
                    for (ArenaModule mod : arena.getMods()) {
                        if (!mod.getName().equals(getName())) {
                            if (mod.checkJoin(p, new PACheck(), true).hasError()) {
                                removeMe = true;
                                break;
                            }
                        }
                    }
                }

                if (p == null || removeMe) {
                    removals.add(s);
                    if (p != null) {
                        res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_REJOIN));
                    }
                }
            }

            if (removals.size() > 0) {
                for (String s : removals) {
                    getPlayerList().remove(s);
                }
            } else {
                // SUCCESS!
                for (String s : getPlayerList()) {
                    if (s.equals(sender.getName())) {
                        continue;
                    }
                    Player p = Bukkit.getPlayerExact(s);
                    AbstractArenaCommand command = new PAG_Join();
                    command.commit(arena, p, new String[0]);
                }
                return res;
            }
            res.setError(this, Language.parse(MSG.MODULE_LATELOUNGE_WAIT));
        }
        // enough, ignore and let something else handle the start!
        return res;
    }

    private List<String> getPlayerList() {
        if (playerList == null) {
            playerList = new ArrayList<String>();
        }
        return playerList;
    }

    @Override
    public boolean hasSpawn(String name) {
        return playerList.contains(name);
    }

    @Override
    public void reset(boolean force) {
        getPlayerList().clear();
    }
}
