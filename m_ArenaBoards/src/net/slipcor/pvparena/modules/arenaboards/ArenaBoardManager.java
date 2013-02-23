package net.slipcor.pvparena.modules.arenaboards;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.loadables.ArenaModule;

public class ArenaBoardManager extends ArenaModule implements Listener {
	protected HashMap<PABlockLocation, ArenaBoard> boards = new HashMap<PABlockLocation, ArenaBoard>();
	protected int BOARD_ID = -1;
	protected int GLOBAL_ID = -1;
	protected static ArenaBoard globalBoard = null;
	
	protected int THIS_ID = -1;

	public ArenaBoardManager() {
		super("ArenaBoards");
	}

	@Override
	public String version() {
		return "v1.0.1.45";
	}

	@Override
	public void configParse(YamlConfiguration config) {
		if (config.get("spawns") != null) {
			debug.i("checking for leaderboard");
			if (config.get("spawns.leaderboard") != null) {
				debug.i("leaderboard exists");
				PABlockLocation loc = Config.parseBlockLocation(config.getString("spawns.leaderboard"));

				
				
				boards.put(loc, new ArenaBoard(this, loc, arena));
			}
		}
		String leaderboard = PVPArena.instance.getConfig().getString(
				"leaderboard");
		if (leaderboard != null && GLOBAL_ID < 0 && globalBoard == null) {
			PABlockLocation lbLoc = Config.parseBlockLocation(leaderboard);
			globalBoard = new ArenaBoard(this, lbLoc, null);

			GLOBAL_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
					PVPArena.instance, new BoardRunnable(null), 100L, 100L);
		}
		
		THIS_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, new BoardRunnable(this), 100L, 100L);
		
		Bukkit.getServer().getPluginManager().registerEvents(this, PVPArena.instance);
	}
	
	@Override
	public void onBlockBreak(Block block) {
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
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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

		if (arena == a && arena != null && boards.containsKey(event.getBlock().getLocation())) {
			Arena.pmsg(event.getPlayer(), Language.parse(MSG.MODULE_ARENABOARDS_EXISTS));
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
			debug.i("creating global leaderboard", event.getPlayer());
			globalBoard = new ArenaBoard(this, new PABlockLocation(event.getBlock().getLocation()), null);
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
			PABlockLocation loc = new PABlockLocation(event.getBlock().getLocation());
			boards.put(loc, new ArenaBoard(this, loc, a));
			SpawnManager.setBlock(a, new PABlockLocation(event.getBlock().getLocation()), "leaderboard");
		}
	}

	@Override
	public boolean onPlayerInteract(PlayerInteractEvent event) {
		return ArenaBoard.checkInteract(this, event);
	}

	@Override
	public void reset(boolean force) {
		if (BOARD_ID > -1)
			Bukkit.getScheduler().cancelTask(BOARD_ID);
		BOARD_ID = -1;
	}

	@Override
	public void parseStart() {
		if (BOARD_ID == -1)
			this.BOARD_ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				PVPArena.instance, new BoardRunnable(this), 100L, 100L);
	}
}
