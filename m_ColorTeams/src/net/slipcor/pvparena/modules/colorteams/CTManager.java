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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CTManager extends ArenaModule implements Listener {
    private static boolean tagAPIenabled = false;
    private Scoreboard board = null;
    private final Map<String, Scoreboard> backup = new HashMap<String, Scoreboard>();

    public CTManager() {
        super("ColorTeams");
    }

    @Override
    public String version() {
        return "v1.3.0.500";
    }

    @Override
    public boolean checkCommand(String s) {
        return s.equals("!ct") || s.equals("colorteams");
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("colorteams");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!ct");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"hidename"});
        return result;
    }

    @Override
    public void commitCommand(CommandSender sender, String[] args) {
        // !ct [value]

        if (!PVPArena.hasAdminPerms(sender)
                && !(PVPArena.hasCreatePerms(sender, arena))) {
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

        if (args[1].equals("hidename")) {
            c = CFG.CHAT_COLORNICK;
        }

        if (c == null) {
            arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "hidename"));
            return;
        }

        boolean b = arena.getArenaConfig().getBoolean(c);
        arena.getArenaConfig().set(c, !b);
        arena.getArenaConfig().save();
        arena.msg(sender, Language.parse(MSG.SET_DONE, c.getNode(), String.valueOf(!b)));

    }

    @Override
    public void displayInfo(CommandSender player) {
        player.sendMessage(StringParser.colorVar("enabled", arena.getArenaConfig().getBoolean(CFG.CHAT_COLORNICK))
                + " | "
                + StringParser.colorVar("scoreboard", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD))
                + " | "
                + StringParser.colorVar("hidename", arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_HIDENAME)));
    }

    @Override
    public void configParse(YamlConfiguration config) {
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
    public void tpPlayerToCoordName(Player player, String place) {
        ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
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
            } catch (IllegalPluginAccessException e) {

            }
        }
    }

    void updateName(Player player) {
        if (tagAPIenabled &&
                !arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD)) {
            try {
                TagAPI.refreshPlayer(player);
            } catch (Exception e) {

            }
        }
    }

    public void parseJoin(CommandSender sender, ArenaTeam team) {
        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD)) {
            return;
        }
        Scoreboard board = getScoreboard();
        ((Player) sender).setScoreboard(board);
        for (Team sTeam : board.getTeams()) {
            if (sTeam.getName().equals(team.getName())) {
                sTeam.addPlayer((Player) sender);
                return;
            }
        }
        Team sTeam = board.registerNewTeam(team.getName());
        sTeam.setPrefix(team.getColor().toString());
        sTeam.addPlayer((Player) sender);
    }

    @Override
    public void parsePlayerLeave(Player player, ArenaTeam team) {
        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD)) {
            return;
        }
        getScoreboard().getTeam(team.getName()).removePlayer(player);
        if (backup.containsKey(player.getName()) && backup.get(player.getName()) != null) {
            player.setScoreboard(backup.get(player.getName()));
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    @Override
    public void resetPlayer(Player player, boolean force) {
        if (!arena.getArenaConfig().getBoolean(CFG.MODULES_COLORTEAMS_SCOREBOARD)) {
            return;
        }
        ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();

        if (team != null) {
            getScoreboard().getTeam(team.getName()).removePlayer(player);
        }
        if (backup.containsKey(player.getName()) && backup.get(player.getName()) != null) {
            player.setScoreboard(backup.get(player.getName()));
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    private Scoreboard getScoreboard() {
        if (board == null) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            for (ArenaTeam team : arena.getTeams()) {
                Team sTeam = board.registerNewTeam(team.getName());
                sTeam.setPrefix(team.getColor().toString());
                for (ArenaPlayer aPlayer : team.getTeamMembers()) {
                    sTeam.addPlayer(aPlayer.get());
                }
            }
        }
        return board;
    }

    public void reset(boolean force) {
        if (force) {
            backup.clear();
        } else {
            class RunLater implements Runnable {

                @Override
                public void run() {
                    backup.clear();
                }

            }
            Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 5L);
        }
    }

    @EventHandler
    public void onChange(PATeamChangeEvent event) {
        if (event.getArena() != arena) {
            return;
        }

        board.getTeam(event.getFrom().getName()).removePlayer(event.getPlayer());


        for (Team sTeam : board.getTeams()) {
            if (sTeam.getName().equals(event.getTo().getName())) {
                sTeam.addPlayer(event.getPlayer());
                return;
            }
        }
        Team sTeam = board.registerNewTeam(event.getTo().getName());
        sTeam.setPrefix(event.getTo().getColor().toString());
        sTeam.addPlayer(event.getPlayer());
    }
}
