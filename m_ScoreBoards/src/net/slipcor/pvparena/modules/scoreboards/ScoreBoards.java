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
    private static ScoreboardManager sbm;

    private Scoreboard board;
    private Objective obj;
    private Objective oBM;
    private Objective oTB;
    private BukkitTask updateTask;

    private final Map<String, Scoreboard> playerBoards = new HashMap<String, Scoreboard>();

    public ScoreBoards() {
        super("ScoreBoards");
    }

    @Override
    public String version() {
        return "v1.3.0.495";
    }


    @Override
    public void configParse(final YamlConfiguration config) {
        if (sbm == null) {
            sbm = Bukkit.getScoreboardManager();
        }
        Bukkit.getPluginManager().registerEvents(new PAListener(this), PVPArena.instance);
    }

    public void update(final Player player) {
        if (board == null || obj == null) {
            return;
        }
        if (arena.isFreeForAll()) {
            final Score score = obj.getScore(player);
            score.setScore(PACheck.handleGetLives(arena, ArenaPlayer.parsePlayer(player.getName())));
        } else {
            final ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
            if (ap.getArenaTeam() == null) {
                return;
            }
            final OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(ap.getArenaTeam().getName());
            obj.getScore(op).setScore(PACheck.handleGetLives(arena, ap));
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

    private boolean block;

    private void update() {
        block = true;
        for (final ArenaPlayer player : arena.getEveryone()) {
            update(player.get());
        }
        block = false;

    }

    @Override
    public void resetPlayer(final Player player, final boolean force) {
        remove(player);
    }

    public void stop() {
        if (board == null) {
            return;
        }
        for (final OfflinePlayer player : board.getPlayers()) {
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

    public void remove(final Player player) {
        // after runnable: remove player's scoreboard, remove player from scoreboard
        // and update all players' scoreboards
        arena.getDebugger().i("ScoreBoards: remove: " + player.getName(), player);

        try {
            boolean found = false;
            for (final Team team : board.getTeams()) {
                if (team.hasPlayer(player)) {
                    team.removePlayer(player);
                    board.resetScores(player);
                    found = true;
                }
            }
            if (!found) {
                board.resetScores(player);
            }
        } catch (final Exception e) {

        }
        if (playerBoards.containsKey(player.getName())) {
            player.setScoreboard(playerBoards.get(player.getName()));
        } else {
            player.setScoreboard(sbm.getMainScoreboard());
        }
    }

    public void add(final Player player) {
        // after runnable: add player to scoreboard, resend all scoreboards

        playerBoards.put(player.getName(), player.getScoreboard());

        // first, check if the scoreboard exists
        class RunLater implements Runnable {

            @Override
            public void run() {

                if (board == null) {

                    board = sbm.getNewScoreboard();

                    oBM = sbm.getMainScoreboard().getObjective(DisplaySlot.BELOW_NAME);
                    if (oBM != null) {
                        oBM = board.registerNewObjective(oBM.getCriteria(), oBM.getDisplayName());
                        oBM.setDisplaySlot(DisplaySlot.BELOW_NAME);

                    }

                    oTB = sbm.getMainScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
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

        updateTask = Bukkit.getScheduler().runTaskTimer(PVPArena.instance, new UpdateTask(this), 50, 50);
    }


}
