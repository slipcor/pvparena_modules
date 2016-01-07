package net.slipcor.pvparena.modules.colorteams;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.events.PATeamChangeEvent;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.kitteh.tag.TagAPI;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CTManager extends ArenaModule implements Listener {
    private static boolean tagAPIenabled;
    private Scoreboard board;
    private final Map<String, Scoreboard> backup = new HashMap<>();
    private final Map<String, Team> backupTeams = new HashMap<>();

    public CTManager() {
        super("ColorTeams");
    }

    @Override
    public String version() {
        return "v1.3.2.51";
    }

    @Override
    public boolean checkCommand(final String s) {
        return "!ct".equals(s) || "colorteams".equals(s);
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("colorteams");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!ct");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"hidename"});
        return result;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        // !ct [value]

        if (!PVPArena.hasAdminPerms(sender)
                && !PVPArena.hasCreatePerms(sender, arena)) {
            arena.msg(
                    sender,
                    Language.parse(MSG.ERROR_NOPERM,
                            Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2})) {
            return;
        }

        CFG c = null;

        if ("hidename".equals(args[1])) {
            c = CFG.CHAT_COLORNICK;
        }

        if (c == null) {
            arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "hidename"));
            return;
        }

        final boolean b = arena.getArenaConfig().getBoolean(c);
        arena.getArenaConfig().set(c, !b);
        arena.getArenaConfig().save();
        arena.msg(sender, Language.parse(MSG.SET_DONE, c.getNode(), String.valueOf(!b)));

    }

    @Override
    public void displayInfo(final CommandSender player) {
        player.sendMessage(StringParser.colorVar("enabled", arena.getArenaConfig().getBoolean(CFG.CHAT_COLORNICK))
                + " | "
                + StringParser.colorVar("scoreboard", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD))
                + " | "
                + StringParser.colorVar("hidename", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_HIDENAME)));
    }

    @Override
    public void configParse(final YamlConfiguration config) {
        Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
        if (tagAPIenabled) {
            return;
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("TagAPI") != null) {
            Bukkit.getPluginManager().registerEvents(new CTListener(), PVPArena.instance);
            Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(MSG.MODULE_COLORTEAMS_TAGAPI));
            tagAPIenabled = true;
        }
    }

    @Override
    public void tpPlayerToCoordName(final Player player, final String place) {
        final ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
        if (arena.getArenaConfig().getBoolean(CFG.CHAT_COLORNICK)) {
            String n;
            if (team == null) {
                n = player.getName();
            } else {
                n = team.getColorCodeString() + player.getName();
            }
            n = ChatColor.translateAlternateColorCodes('&', n);

            player.setDisplayName(n);

        }
        updateName(player);
    }

    @Override
    public void unload(final Player player) {
        if (arena.getArenaConfig().getBoolean(CFG.CHAT_COLORNICK)) {
            player.setDisplayName(player.getName());
        }
        if (tagAPIenabled) {
            class TempRunner implements Runnable {

                @Override
                public void run() {
                    updateName(player);
                }
            }
            try {
                Bukkit.getScheduler().runTaskLater(PVPArena.instance, new TempRunner()
                        , 20 * 3L);
            } catch (final IllegalPluginAccessException e) {

            }
        }
    }

    void updateName(final Player player) {
        if (tagAPIenabled &&
                !arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD)) {
            try {
                TagAPI.refreshPlayer(player);
            } catch (final Exception e) {

            }
        }
    }

    @Override
    public void parseJoin(final CommandSender sender, final ArenaTeam team) {
        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD)) {
            return;
        }
        final Scoreboard board = getScoreboard();
        backup.put(sender.getName(), ((Player) sender).getScoreboard());
        backupTeams.put(sender.getName(), ((Player) sender).getScoreboard().getPlayerTeam((Player) sender));
        ((Player) sender).setScoreboard(board);
        for (final Team sTeam : board.getTeams()) {
            if (sTeam.getName().equals(team.getName())) {
                sTeam.addPlayer((Player) sender);
                return;
            }
        }
        final Team sTeam = board.registerNewTeam(team.getName());
        sTeam.setPrefix(team.getColor().toString());
        sTeam.addPlayer((Player) sender);
    }

    @Override
    public void parsePlayerLeave(final Player player, final ArenaTeam team) {
        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD)) {
            return;
        }
        getScoreboard().getTeam(team.getName()).removePlayer(player);
        if (backup.containsKey(player.getName()) && backup.get(player.getName()) != null) {
            player.setScoreboard(backup.get(player.getName()));
            if (backupTeams.containsKey(player.getName()) && backupTeams.get(player.getName()) != null) {
                // TODO: fix
                backupTeams.get(player.getName()).addPlayer(player);
            }
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    @Override
    public void resetPlayer(final Player player, final boolean force) {
        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD)) {
            return;
        }
        final ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();

        if (team != null) {
            getScoreboard().getTeam(team.getName()).removePlayer(player);
        }
        if (backup.containsKey(player.getName()) && backup.get(player.getName()) != null) {
            player.setScoreboard(backup.get(player.getName()));
            if (backupTeams.containsKey(player.getName()) && backupTeams.get(player.getName()) != null) {
                // TODO: fix
                backupTeams.get(player.getName()).addPlayer(player);
            }
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    private Scoreboard getScoreboard() {
        if (board == null) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            for (final ArenaTeam team : arena.getTeams()) {
                final Team sTeam = board.registerNewTeam(team.getName());
                sTeam.setPrefix(team.getColor().toString());
                for (final ArenaPlayer aPlayer : team.getTeamMembers()) {
                    sTeam.addPlayer(aPlayer.get());
                }
            }
        }
        return board;
    }

    @Override
    public void reset(final boolean force) {
        if (force) {
            backup.clear();
            backupTeams.clear();
        } else {
            class RunLater implements Runnable {

                @Override
                public void run() {
                    backup.clear();
                    backupTeams.clear();
                }

            }
            Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 5L);
        }
    }

    @EventHandler
    public void onChange(final PATeamChangeEvent event) {
        if (event.getArena() != arena) {
            return;
        }

        board.getTeam(event.getFrom().getName()).removePlayer(event.getPlayer());


        for (final Team sTeam : board.getTeams()) {
            if (sTeam.getName().equals(event.getTo().getName())) {
                sTeam.addPlayer(event.getPlayer());
                return;
            }
        }
        final Team sTeam = board.registerNewTeam(event.getTo().getName());
        sTeam.setPrefix(event.getTo().getColor().toString());
        sTeam.addPlayer(event.getPlayer());
    }
}
