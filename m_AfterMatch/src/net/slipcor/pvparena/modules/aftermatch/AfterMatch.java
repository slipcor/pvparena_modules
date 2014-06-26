package net.slipcor.pvparena.modules.aftermatch;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.runnables.ArenaRunnable;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AfterMatch extends ArenaModule implements Cloneable {
    protected ArenaRunnable afterTask = null;
    private boolean aftermatch = false;

    public AfterMatch() {
        super("AfterMatch");
    }


    @Override
    public String version() {
        return "v1.3.0.495";
    }

    public void afterMatch() {
        for (ArenaTeam t : arena.getTeams()) {
            for (ArenaPlayer p : t.getTeamMembers()) {
                if (!p.getStatus().equals(Status.FIGHT)) {
                    continue;
                }
                Player player = p.get();
                if (player != null) {
                    arena.tpPlayerToCoordName(player, "after");
                }
            }
        }
        arena.broadcast(Language.parse(MSG.MODULE_AFTERMATCH_STARTING));
        PVPArena.instance.getAgm().setPlayerLives(arena, 0);
        aftermatch = true;
        try {
            afterTask.cancel();
        } catch (Exception e) {

        }
        afterTask = null;
    }

    @Override
    public boolean checkCommand(String s) {
        return s.equals("!am") || s.equals("aftermatch");
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("aftermatch");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!am");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"off"});
        result.define(new String[]{"time"});
        result.define(new String[]{"death"});
        return result;
    }

    @Override
    public String checkForMissingSpawns(Set<String> list) {
        for (String s : list) {
            if (s.startsWith("after")) {
                return null;
            }
        }
        return Language.parse(MSG.MODULE_AFTERMATCH_SPAWNNOTSET);
    }

    @Override
    public void commitCommand(CommandSender sender, String[] args) {
        // !am time 6
        // !am death 4

        if (!PVPArena.hasAdminPerms(sender)
                && !(PVPArena.hasCreatePerms(sender, arena))) {
            arena.msg(
                    sender,
                    Language.parse(MSG.ERROR_NOPERM,
                            Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2, 3})) {
            return;
        }

        if (args[0].equals("!am") || args[0].equals("aftermatch")) {
            if (args.length == 2) {
                if (args[1].equals("off")) {
                    arena.getArenaConfig().set(CFG.MODULES_AFTERMATCH_AFTERMATCH, args[1]);
                    arena.getArenaConfig().save();
                    arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_AFTERMATCH_AFTERMATCH.getNode(), args[1]));
                    return;
                }
                arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "off"));
                return;
            }
            int i;
            try {
                i = Integer.parseInt(args[2]);
            } catch (Exception e) {
                arena.msg(sender,
                        Language.parse(MSG.ERROR_NOT_NUMERIC, args[2]));
                return;
            }
            if (args[1].equals("time") || args[1].equals("death")) {
                arena.getArenaConfig().set(CFG.MODULES_AFTERMATCH_AFTERMATCH, args[1] + ":" + i);
                arena.getArenaConfig().save();
                arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_AFTERMATCH_AFTERMATCH.getNode(), args[1] + ":" + i));
                return;
            }

            arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "time | death"));
        }
    }

    @Override
    public void configParse(YamlConfiguration config) {
        String pu = config.getString(CFG.MODULES_AFTERMATCH_AFTERMATCH.getNode(), "off");

        if (!pu.startsWith("death") && !pu.startsWith("time")) {
            PVPArena.instance.getLogger().warning("error activating aftermatch module");
        }
    }

    @Override
    public void displayInfo(CommandSender player) {
        player.sendMessage("active: "
                + StringParser.colorVar(!arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH).equals("off"))
                + "("
                + StringParser.colorVar(arena.getArenaConfig()
                .getString(CFG.MODULES_AFTERMATCH_AFTERMATCH)) + ")");
    }

    @Override
    public boolean hasSpawn(String string) {
        return string.equals("after");
    }

    @Override
    public void parsePlayerDeath(Player player,
                                 EntityDamageEvent cause) {
        String pu = arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH);

        if (pu.equals("off") || aftermatch) {
            return;
        }

        String[] ss = pu.split(":");
        if (pu.startsWith("time") || afterTask != null) {
            return;
        }

        int i = Integer.parseInt(ss[1]);

        for (ArenaTeam t : arena.getTeams()) {
            for (ArenaPlayer p : t.getTeamMembers()) {
                if (!p.getStatus().equals(Status.FIGHT)) {
                    continue;
                }
                if (--i < 0) {
                    return;
                }
            }
        }

        afterTask = null;

        afterMatch();
    }

    @Override
    public void reset(boolean force) {
        if (afterTask != null) {
            afterTask.cancel();
            afterTask = null;
        }
        aftermatch = false;
    }

    @Override
    public void parseStart() {
        String pu = arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH);

        if (afterTask != null) {
            afterTask.cancel();
            afterTask = null;
        }

        int i;
        String[] ss = pu.split(":");
        if (pu.startsWith("time")) {
            // arena.powerupTrigger = "time";
            i = Integer.parseInt(ss[1]);
        } else {
            return;
        }

        debug.i("using aftermatch : "
                + arena.getArenaConfig().getString(CFG.MODULES_AFTERMATCH_AFTERMATCH) + " : "
                + i);
        if (i > 0) {
            debug.i("aftermatch time trigger!");
            afterTask = new AfterRunnable(this, i);
        }
    }
}
