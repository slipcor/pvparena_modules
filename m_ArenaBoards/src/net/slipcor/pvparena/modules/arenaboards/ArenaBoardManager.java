package net.slipcor.pvparena.modules.arenaboards;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.loadables.ArenaModule;

public class ArenaBoardManager extends ArenaModule {
	protected HashMap<PALocation, ArenaBoard> boards = new HashMap<PALocation, ArenaBoard>();
	protected int BOARD_ID = -1;
	protected static int GLOBAL_ID = -1;
	protected static ArenaBoard globalBoard = null;

	public ArenaBoardManager() {
		super("ArenaBoards");
		ArenaBoard.abm = this;
		BoardRunnable.abm = this;
	}

	@Override
	public String version() {
		return "v0.9.0.0";
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		if (config.get("spawns") != null) {
			db.i("checking for leaderboard");
			if (config.get("spawns.leaderboard") != null) {
				db.i("leaderboard exists");
				PALocation loc = Config.parseLocation(config.getString("spawns.leaderboard"));

				boards.put(loc, new ArenaBoard(loc, arena));
			}
		}
	}

	@Override
	public void load_arenas() {
		String leaderboard = PVPArena.instance.getConfig().getString(
				"leaderboard");
		if (leaderboard != null) {
			PALocation lbLoc = Config.parseLocation(leaderboard);
			globalBoard = new ArenaBoard(lbLoc, null);

			GLOBAL_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
					PVPArena.instance, new BoardRunnable(null), 100L, 100L);
		}
	}
	
	@Override
	public void onBlockBreak(Arena arena, Block block) {
		if (globalBoard != null && globalBoard.getLocation().equals(block.getLocation())) {
			globalBoard.destroy();
			globalBoard = null;
			Bukkit.getScheduler().cancelTask(GLOBAL_ID);
			GLOBAL_ID = -1;
		} else if (boards.containsKey(block.getLocation())) {
			boards.get(block.getLocation()).destroy();
			boards.remove(block.getLocation());
		} else {
			return;
		}

		String msg = Language.parse(MSG.MODULE_ARENABOARDS_DESTROYED);
		for (Entity e : Bukkit.getWorld(arena.getWorld()).getEntities()) {
			if (e instanceof Player) {
				Player player = (Player) e;
				if (player.getLocation().distance(block.getLocation()) > 5) {
					continue;
				}
				arena.msg(player, msg);
			}
		}
	}

	@Override
	public void onSignChange(SignChangeEvent event) {

		String headline = event.getLine(0);
		if ((headline == null) || (headline.equals(""))) {
			return;
		}

		if (!headline.startsWith("[PAA]")) {
			return;
		}

		headline = headline.replace("[PAA]", "");

		Arena a = ArenaManager.getArenaByName(headline);

		// trying to create an arena leaderboard

		if (boards.containsKey(event.getBlock().getLocation())) {
			ArenaManager.tellPlayer(event.getPlayer(), Language.parse(MSG.MODULE_ARENABOARDS_EXISTS));
			return;
		}

		if (!PVPArena.hasAdminPerms(event.getPlayer())
				&& ((a != null) && !PVPArena.hasCreatePerms(event.getPlayer(),
						a))) {
			a.msg(
					event.getPlayer(),
					Language.parse(MSG.ERROR_NOPERM,
							Language.parse(MSG.MODULE_ARENABOARDS_CREATE)));
			return;
		}

		event.setLine(0, headline);
		if (a == null) {
			db.i("creating global leaderboard");
			globalBoard = new ArenaBoard(new PALocation(event.getBlock().getLocation()), null);
			Location loc = event.getBlock().getLocation();
			Integer x = Integer.valueOf(loc.getBlockX());
			Integer y = Integer.valueOf(loc.getBlockY());
			Integer z = Integer.valueOf(loc.getBlockZ());
			Float yaw = Float.valueOf(loc.getYaw());
			Float pitch = Float.valueOf(loc.getPitch());

			String s = loc.getWorld().getName() + "," + x.toString() + ","
					+ y.toString() + "," + z.toString() + "," + yaw.toString()
					+ "," + pitch.toString();
			PVPArena.instance.getConfig().set("leaderboard", s);
			PVPArena.instance.saveConfig();

			GLOBAL_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
					PVPArena.instance, new BoardRunnable(null), 100L, 100L);
		} else {
			PALocation loc = new PALocation(event.getBlock().getLocation());
			boards.put(loc, new ArenaBoard(loc, a));
			SpawnManager.setCoords(a, event.getBlock().getLocation(), "leaderboard");
		}
	}

	@Override
	public boolean onPlayerInteract(PlayerInteractEvent event) {
		return ArenaBoard.checkInteract(event);
	}

	@Override
	public void reset(Arena arena, boolean force) {
		if (BOARD_ID > -1)
			Bukkit.getScheduler().cancelTask(BOARD_ID);
		BOARD_ID = -1;
	}

	@Override
	public void teleportAllToSpawn(Arena arena) {
		this.BOARD_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, new BoardRunnable(arena), 100L, 100L);
	}
}
