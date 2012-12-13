package net.slipcor.pvparena.modules.arenaboards;

import java.util.HashMap;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.StatisticsManager;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ArenaBoard {

	public static final Debug db = new Debug(10);

	private PABlockLocation location;
	protected ArenaBoardManager abm;
	public boolean global;

	public StatisticsManager.type sortBy = StatisticsManager.type.KILLS;

	private HashMap<StatisticsManager.type, ArenaBoardColumn> columns = new HashMap<StatisticsManager.type, ArenaBoardColumn>();

	/**
	 * create an arena board instance
	 * 
	 * @param loc
	 *            the location to hook to
	 * @param a
	 *            the arena to save the board to
	 */
	public ArenaBoard(ArenaBoardManager m, PABlockLocation loc, Arena a) {
		abm = m;
		location = loc;
		global = a == null;

		db.i("constructing arena board");
		construct();
	}

	/**
	 * actually construct the arena board, read colums, save signs etc
	 */
	private void construct() {
		PABlockLocation l = location;
		int border = 10;
		try {
			Sign s = (Sign) l.toLocation().getBlock().getState();
			BlockFace bf = getRightDirection(s);
			db.i("parsing signs:");
			do {
				StatisticsManager.type t = null;
				try {
					t = StatisticsManager.getTypeBySignLine(s.getLine(0));
				} catch (Exception e) {
					// nothing
				}

				columns.put(t, new ArenaBoardColumn(this, l));
				db.i("putting column type " + toString());
				l = new PABlockLocation(l.toLocation().getBlock().getRelative(bf).getLocation());
				s = (Sign) l.toLocation().getBlock().getState();
			} while (border-- > 0);
		} catch (Exception e) {
			// no more signs, out!
		}
	}

	public PABlockLocation getLocation() {
		return location;
	}

	/**
	 * get the right next board direction from the attachment data
	 * 
	 * @param s
	 *            the sign to check
	 * @return the blockface of the direction of the next column
	 */
	private BlockFace getRightDirection(Sign s) {
		byte data = s.getRawData();

		if (data == 2)
			return BlockFace.NORTH;
		if (data == 3)
			return BlockFace.SOUTH;
		if (data == 4)
			return BlockFace.WEST;
		if (data == 5)
			return BlockFace.EAST;

		return null;
	}

	/**
	 * save arena board statistics to each column
	 */
	public void update() {
		db.i("ArenaBoard update()");
		for (StatisticsManager.type t : StatisticsManager.type.values()) {
			db.i("checking stat: " + t.name());
			if (!columns.containsKey(t)) {
				continue;
			}
			db.i("found! reading!");
			String[] s = StatisticsManager.read(
					StatisticsManager.getStats(global?null:abm.getArena(), sortBy), t, global);
			columns.get(t).write(s);
		}
	}

	/**
	 * check if a player clicked a leaderboard sign
	 * 
	 * @param event
	 *            the InteractEvent
	 * @return true if the player clicked a leaderboard sign, false otherwise
	 */
	public static boolean checkInteract(ArenaBoardManager abm, PlayerInteractEvent event) {

		db.i("checking ArenaBoard interact");

		Player player = event.getPlayer();

		if (event.getClickedBlock() == null) {
			return false;
		}

		db.i("block is not null");

		if (!abm.boards.containsKey(event.getClickedBlock().getLocation())
				&& abm.globalBoard == null
				|| !abm.globalBoard.getLocation().equals(
						event.getClickedBlock().getLocation())) {
			return false;
		}

		db.i("arenaboard exists");

		ArenaBoard ab = abm.boards.get(event.getClickedBlock().getLocation());

		if (ab == null) {
			ab = abm.globalBoard;
		}

		if (ab.global) {
			db.i("global!");
			if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				ab.sortBy = StatisticsManager.type.next(ab.sortBy);
				Arena.pmsg(player,
						Language.parse(MSG.MODULE_ARENABOARDS_SORTINGBY, ab.sortBy.toString()));
				return true;
			} else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				ab.sortBy = StatisticsManager.type.last(ab.sortBy);
				Arena.pmsg(player,
						Language.parse(MSG.MODULE_ARENABOARDS_SORTINGBY, ab.sortBy.toString()));
				return true;
			}
		} else {
			db.i("not global!");
			if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				ab.sortBy = StatisticsManager.type.next(ab.sortBy);
				ab.abm.getArena().msg(player,
						Language.parse(MSG.MODULE_ARENABOARDS_SORTINGBY, ab.sortBy.toString()));
				return true;
			} else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				ab.sortBy = StatisticsManager.type.last(ab.sortBy);
				ab.abm.getArena().msg(player,
						Language.parse(MSG.MODULE_ARENABOARDS_SORTINGBY, ab.sortBy.toString()));
				return true;
			}
		}

		return false;
	}

	public void destroy() {
		// TODO clear signs
		if (global) {
			PVPArena.instance.getConfig().set("leaderboard", null);
			PVPArena.instance.saveConfig();
		} else {
			abm.getArena().getArenaConfig().setManually("spawns.leaderboard", null);
			abm.getArena().getArenaConfig().save();
		}
	}
}
