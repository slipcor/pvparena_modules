package net.slipcor.pvparena.modules.scoreboards;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public class ScoreBoards extends ArenaModule {
    private static ScoreboardManager man;

    private Scoreboard board;
    private Objective obj;
    private Objective oBM;
    private Objective oTB;
    private BukkitTask updateTask;
    private String msgTeam = "pa_msg_";

    private boolean block;

    private final Map<String, Scoreboard> playerBoards = new HashMap<>();

    public ScoreBoards() {
        super("ScoreBoards");
    }

    @Override
    public String version() {
        return "v1.3.2.94";
    }

    private static ScoreboardManager getScoreboardManager() {
        if (man == null) {
            man = Bukkit.getScoreboardManager();
        }
        return man;
    }

    @Override
    public void configParse(final YamlConfiguration config) {
        Bukkit.getPluginManager().registerEvents(new PAListener(this), PVPArena.instance);
    }

    public void add(final Player player) {
        // after runnable: add player to scoreboard, resend all scoreboards

        playerBoards.put(player.getName(), player.getScoreboard());

        // first, check if the scoreboard exists
        class RunLater implements Runnable {

            @Override
            public void run() {

                if (board == null) {

                    board = getScoreboardManager().getNewScoreboard();

                    oBM = getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.BELOW_NAME);
                    if (oBM != null) {
                        oBM = board.registerNewObjective(oBM.getCriteria(), oBM.getDisplayName());
                        oBM.setDisplaySlot(DisplaySlot.BELOW_NAME);

                    }

                    oTB = getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
                    if (oTB != null) {
                        oTB = board.registerNewObjective(oTB.getCriteria(), oTB.getDisplayName());
                        oTB.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                    }

                    for (final ArenaTeam team : arena.getTeams()) {

                        try {
                            board.registerNewTeam(team.getName());
                            final Team bukkitTeam = board.getTeam(team.getName());
                            bukkitTeam.setPrefix(team.getColor().toString());
                            final OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(team.getName());
                            bukkitTeam.addPlayer(op);
                            bukkitTeam.setAllowFriendlyFire(arena.getArenaConfig().getBoolean(CFG.PERMS_TEAMKILL));

                            bukkitTeam.setCanSeeFriendlyInvisibles(
                                    !arena.isFreeForAll()
                            );
                        } catch (final Exception e) {

                        }

                        if (team == ArenaPlayer.parsePlayer(player.getName()).getArenaTeam()) {
                            board.getTeam(team.getName()).addPlayer(player);
                        }
                    }
                    board.registerNewTeam(msgTeam);
                    final Team mTeam = board.getTeam(msgTeam);
                    mTeam.setPrefix(ChatColor.WHITE.toString());

                    if (board.getObjective("lives") != null) {
                        board.getObjective("lives").unregister();
                        if (board.getObjective(DisplaySlot.SIDEBAR) != null) {
                            board.getObjective(DisplaySlot.SIDEBAR).unregister();
                        }
                    }

                    obj = board.registerNewObjective("lives", "dummy"); //deathCount

                    obj.setDisplayName(ChatColor.GREEN + "PVP Arena" + ChatColor.RESET + " - " + ChatColor.YELLOW + arena.getName());
                    obj.setDisplaySlot(DisplaySlot.SIDEBAR);

                    update(player);
                } else {
                    final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
                    if (aPlayer.getArenaTeam() != null) {
                        board.getTeam(aPlayer.getArenaTeam().getName()).addPlayer(player);
                    }
                    update(player);
                }
            }

        }
        Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
    }

    public boolean addCustom(final ArenaModule module, final String string, final int value) {
        debug.i("module "+module+" tries to set custom scoreboard value '"+string+"' to score "+value);
        if (board == null) {

            board = getScoreboardManager().getNewScoreboard();

            oBM = getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.BELOW_NAME);
            if (oBM != null) {
                oBM = board.registerNewObjective(oBM.getCriteria(), oBM.getDisplayName());
                oBM.setDisplaySlot(DisplaySlot.BELOW_NAME);

            }

            oTB = getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
            if (oTB != null) {
                oTB = board.registerNewObjective(oTB.getCriteria(), oTB.getDisplayName());
                oTB.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            }

            for (final ArenaTeam team : arena.getTeams()) {

                try {
                    board.registerNewTeam(team.getName());
                    final Team bukkitTeam = board.getTeam(team.getName());
                    bukkitTeam.setPrefix(team.getColor().toString());
                    final OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(team.getName());
                    bukkitTeam.addPlayer(op);
                    bukkitTeam.setAllowFriendlyFire(arena.getArenaConfig().getBoolean(CFG.PERMS_TEAMKILL));

                    bukkitTeam.setCanSeeFriendlyInvisibles(
                            !arena.isFreeForAll()
                    );
                } catch (final Exception e) {

                }
            }
            try {
                board.registerNewTeam(msgTeam);
                final Team mTeam = board.getTeam(msgTeam);
                mTeam.setPrefix(ChatColor.WHITE.toString());
                mTeam.addEntry(string);

                if (board.getObjective("lives") != null) {
                    board.getObjective("lives").unregister();
                    if (board.getObjective(DisplaySlot.SIDEBAR) != null) {
                        board.getObjective(DisplaySlot.SIDEBAR).unregister();
                    }
                }

                obj = board.registerNewObjective("lives", "dummy"); //deathCount

                obj.getScore(string).setScore(value);

                obj.setDisplayName(ChatColor.GREEN + "PVP Arena" + ChatColor.RESET + " - " + ChatColor.YELLOW + arena.getName());
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        try {
            final Team mTeam = board.getTeam(msgTeam);
            mTeam.addEntry(string);
            obj.getScore(string).setScore(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void change(final Player player, final ArenaTeam from, final ArenaTeam to) {

        class RunLater implements Runnable {

            @Override
            public void run() {

                final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
                if (aPlayer.getArenaTeam() != null) {
                    board.getTeam(from.getName()).removePlayer(player);

                    for (final Team sTeam : board.getTeams()) {
                        if (sTeam.getName().equals(to.getName())) {
                            sTeam.addPlayer(player);
                            return;
                        }
                    }
                    final Team sTeam = board.registerNewTeam(to.getName());
                    sTeam.setPrefix(to.getColor().toString());
                    sTeam.addPlayer(player);
                }
                update(player);
            }

        }
        Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
    }

    public void remove(final Player player) {
        // after runnable: remove player's scoreboard, remove player from scoreboard
        // and update all players' scoreboards
        arena.getDebugger().i("ScoreBoards: remove: " + player.getName(), player);

        try {
            boolean found = false;
            for (final Team team : board.getTeams()) {
                if (team.hasPlayer(player)) {
                    team.removePlayer(player);
                    board.resetScores(player.getName());
                    found = true;
                }
            }
            if (!found) {
                board.resetScores(player.getName());
            }
        } catch (final Exception e) {

        }
        boolean skip = false;
        if (playerBoards == null) {
            PVPArena.instance.getLogger().severe("final Map 'playerBoards' is null. Please check the server startup for errors!");
            skip = true;
        } else if (player == null) {
            PVPArena.instance.getLogger().severe("Player is null, but if they were, there should have been a NPE in this method already.");
            return;
        }
        if (playerBoards.containsKey(player.getName())) {
            player.setScoreboard(playerBoards.get(player.getName()));
        } else {
            player.setScoreboard(getScoreboardManager().getMainScoreboard());
        }
    }

    public boolean removeCustom(final ArenaModule module, final String string, final Integer position) {
        debug.i("module "+module+" tries to remove custom scoreboard value '"+string+"' to score "+position);

        try {
            Team team = board.getTeam(msgTeam);
            if (team.getEntries().contains(string)) {
                team.removeEntry(string);
                return true;
            }

            team = board.getEntryTeam(string);
            team.removeEntry(string);
            return true;
        } catch (final Exception e) {

        }
        try {
            for (String entry : board.getEntries()) {
                if (obj.getScore(entry).getScore() == position.intValue()) {
                    Team team = board.getEntryTeam(entry);
                    team.removeEntry(entry);
                    return true;
                }
            }
        } catch (final Exception e) {

        }
        return false;
    }

    @Override
    public void resetPlayer(final Player player, final boolean force) {
        remove(player);
    }

    public void start() {

        for (final ArenaPlayer player : arena.getEveryone()) {
            add(player.get());
        }

        class RunLater implements Runnable {

            @Override
            public void run() {
                update();
            }

        }
        Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);

        class UpdateTask implements Runnable {
            private final ScoreBoards mod;

            UpdateTask(final ScoreBoards mod) {
                this.mod = mod;
            }

            @Override
            public void run() {
                mod.update();
            }

        }

        int interval = arena.getArenaConfig().getInt(CFG.MODULES_SCOREBOARDS_UPDATEINTERVAL, 50);

        updateTask = Bukkit.getScheduler().runTaskTimer(PVPArena.instance, new UpdateTask(this), interval, interval);
    }

    public void stop() {
        if (board == null) {
            return;
        }
        for (final String player : board.getEntries()) {
            if (player != null) {
                board.resetScores(player);
            }
        }
        obj.unregister();
        obj = null;
        board.clearSlot(DisplaySlot.SIDEBAR);
        board = null;

        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    private void update() {
        block = true;
        for (final ArenaPlayer player : arena.getEveryone()) {
            update(player.get());
        }
        block = false;
    }

    public void update(final Player player) {
        if (board == null || obj == null) {
            return;
        }
        if (arena.isFreeForAll()) {
            final Score score = obj.getScore(player.getName());
            score.setScore(PACheck.handleGetLives(arena, ArenaPlayer.parsePlayer(player.getName())));
        } else {
            final ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
            if (ap.getArenaTeam() == null) {
                return;
            }
            obj.getScore(ap.getArenaTeam().getName()).setScore(PACheck.handleGetLives(arena, ap));
        }
        player.setScoreboard(board);

        if (!block) {
            class RunLater implements Runnable {

                @Override
                public void run() {
                    update();
                }

            }
            Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
        }
    }
}
