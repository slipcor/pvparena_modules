package net.slipcor.pvparena.modules.scoreboards;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class ScoreBoards extends ArenaModule {
	private boolean setup = false;
	private static ScoreboardManager sbm = null;
	
	private Scoreboard board = null;
	Objective obj = null;
	private BukkitTask updateTask = null;
	
	Map<String, Scoreboard> playerBoards = new HashMap<String, Scoreboard>();
	
	public ScoreBoards() {
		super("ScoreBoards");
	}
	
	@Override
	public String version() {
		return "v1.0.1.142";
	}

	
	@Override
	public void configParse(YamlConfiguration config) {
		if (sbm == null) {
			sbm = Bukkit.getScoreboardManager();
		}
		if (setup) {
			return;
		}
		Bukkit.getPluginManager().registerEvents(new PAListener(this), PVPArena.instance);
		setup = true;
	}

	public void update(Player player) {
		if (board == null || obj == null) {
			return;
		}
		if (arena.isFreeForAll()) {
			Score score = obj.getScore(player);
			score.setScore(PACheck.handleGetLives(arena, ArenaPlayer.parsePlayer(player.getName())));
		} else {
			ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
			if (ap.getArenaTeam() == null) {
				return;
			}
			OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(ap.getArenaTeam().getName());
			obj.getScore(op).setScore(PACheck.handleGetLives(arena, ap));
		}
		player.setScoreboard(sbm.getMainScoreboard());
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

	private boolean block = false;
	
	private void update() {
		block = true;
		for (ArenaPlayer player : arena.getEveryone()) {
			update(player.get());
		}
		block = false;
	}

	public void stop(Arena arena) {
		if (board == null) {
			return;
		}
		for (OfflinePlayer player : board.getPlayers()) {
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

	public void remove(Player player) {
		// after runnable: remove player's scoreboard, remove player from scoreboard
		// and update all players' scoreboards
		if (arena.isFreeForAll()) {
			try {
				board.resetScores(player);
			} catch (Exception e) {
				
			}
		} else {
			try {
				ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
				if (ap.getArenaTeam() == null || obj == null) {
					return;
				}
				OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(ap.getArenaTeam().getName());
				obj.getScore(op).setScore(PACheck.handleGetLives(arena, ap));
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
		player.setScoreboard(playerBoards.get(player.getName()));
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
					
					for (ArenaTeam team : arena.getTeams()) {
						
						try {
							board.registerNewTeam(team.getName());
							Team bukkitTeam = board.getTeam(team.getName());
							bukkitTeam.setPrefix(team.getColor().toString());
							OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(team.getName());
							bukkitTeam.addPlayer(op);
							bukkitTeam.setAllowFriendlyFire(arena.getArenaConfig().getBoolean(CFG.PERMS_TEAMKILL));
						} catch (Exception e) {
							
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
					
					obj.setDisplayName("§aPVP Arena§f - §e" + arena.getName());
					obj.setDisplaySlot(DisplaySlot.SIDEBAR);
					
					update(player);
				} else {
					board.getTeam(ArenaPlayer.parsePlayer(player.getName()).getArenaTeam().getName()).addPlayer(player);
					update(player);
				}
			}
			
		}
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
	}

	public void start() {
		class RunLater implements Runnable {

			@Override
			public void run() {
				update();
			}
			
		}
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
		
		class UpdateTask implements Runnable {
			private final ScoreBoards mod;
			UpdateTask(ScoreBoards mod) {
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
