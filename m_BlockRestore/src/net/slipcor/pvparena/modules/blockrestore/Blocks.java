package net.slipcor.pvparena.modules.blockrestore;

import java.util.HashMap;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.material.Attachable;
import org.bukkit.plugin.IllegalPluginAccessException;

public class Blocks extends ArenaModule {
	
	public Blocks() {
		super("BlockRestore");
	}
	
	@Override
	public String version() {
		return "v1.0.2.148";
	}

	public static HashMap<Location, ArenaBlock> blocks = new HashMap<Location, ArenaBlock>();
	//public static HashMap<Location, String[]> signs = new HashMap<Location, String[]>();
	
	private static HashMap<ArenaRegionShape, RestoreContainer> containers = new HashMap<ArenaRegionShape, RestoreContainer>();

	private static Debug debug = new Debug(24);
	
	protected boolean restoring = false;
	
	private void checkBlock(Block b, BlockFace bf) {
		if (b.getType().equals(Material.LADDER) ||
				b.getType().equals(Material.STONE_BUTTON) ||
				b.getType().equals(Material.LEVER) ||
				b.getType().equals(Material.WALL_SIGN)) {
			Attachable a = (Attachable) b.getState().getData();
			if (a.getAttachedFace().equals(bf)) {
				saveBlock(b);
			}
		}
	}
	
	@Override
	public boolean checkCommand(String s) {
		return (s.equals("blockrestore") || s.equals("!br"));
	}
	
	@Override
	public PACheck checkJoin(CommandSender sender,
			PACheck res, boolean join) {
		if (restoring) {
			res.setError(this, "restoring");
		}
		return res;
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		// !br hard
		// !br restorechests
		// !br clearinv
		// !br offset X
		
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			Arena.pmsg(sender,
					Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}
		
		if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[] { 2,3 })) {
			return;
		}
		
		if (args[1].startsWith("clearinv")) {
			
			arena.getArenaConfig().setManually("inventories", null);
			arena.getArenaConfig().save();
			Arena.pmsg(sender, Language.parse(MSG.MODULE_BLOCKRESTORE_CLEARINVDONE));
			return;
		}

		if (args[1].equals("hard") || args[1].equals("restorechests") || args[1].equals("restoreblocks")) {
			CFG c = null;
			if (args[1].equals("hard")) {
				c = CFG.MODULES_BLOCKRESTORE_HARD;
			} else if (args[1].contains("blocks")){
				c = CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS;
			} else {
				c = CFG.MODULES_BLOCKRESTORE_RESTORECHESTS;
			}
			boolean b = arena.getArenaConfig().getBoolean(c);
			
			arena.getArenaConfig().set(c, !b);
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.SET_DONE, c.getNode(), String.valueOf(!b)));
			
			return;
		}
		
		if (args[1].equals("offset")) {
			if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[] { 3 })) {
				return;
			}
			
			int i = 0;
			try {
				i = Integer.parseInt(args[2]);
			} catch (Exception e) {
				arena.msg(sender,
						Language.parse(MSG.ERROR_NOT_NUMERIC, args[2]));
				return;
			}
			
			arena.getArenaConfig().set(CFG.MODULES_BLOCKRESTORE_OFFSET, i);
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_BLOCKRESTORE_OFFSET.getNode(), String.valueOf(i)));
		}
	}

	@Override
	public void displayInfo(CommandSender player) {
		player.sendMessage(StringParser.colorVar("hard", arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD))
				+ " | " + StringParser.colorVar("blocks", arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS))
				+ " | " + StringParser.colorVar("chests", arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTORECHESTS))
				+ " | offset " + arena.getArenaConfig().getInt(CFG.MODULES_BLOCKRESTORE_OFFSET));
	}
	
	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		if (!arena.isLocked() &&
				(!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
						&& arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS))) {
			for (Block b : event.blockList()) {
				saveBlock(b);
			}
		}
	}

	
	@Override
	public void onBlockBreak(Block block) {
		debug.i("block break in blockRestore");
		if (arena == null || arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
				|| !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
			debug.i(arena + " || blockRestore.hard: " + arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD));
			return;
		}
		if (!arena.isLocked()) {
			
			checkBlock(block.getRelative(BlockFace.NORTH), BlockFace.SOUTH);
			checkBlock(block.getRelative(BlockFace.SOUTH), BlockFace.NORTH);
			checkBlock(block.getRelative(BlockFace.EAST), BlockFace.WEST);
			checkBlock(block.getRelative(BlockFace.WEST), BlockFace.EAST);
			
			saveBlock(block);
		}
		debug.i("!arena.isLocked() " + !arena.isLocked());
	}
	@Override
	public void onBlockPiston(Block block) {
		if (!arena.isLocked()
				&& !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
				&& arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
			saveBlock(block);
		}
	}

	@Override
	public void onBlockPlace(Block block, Material mat) {
		if (!arena.isLocked()
				&& !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)
				&& arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
			saveBlock(block, mat);
		}
	}
	
	@Override
	public void reset(boolean force) {
		resetBlocks();
		restoreChests();
	}

	/**
	 * reset all blocks belonging to an arena
	 * 
	 * @param arena
	 *            the arena to reset
	 */
	private void resetBlocks() {
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS)) {
			debug.i("resetting blocks");
			try {
				Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new BlockRestoreRunnable(arena, blocks, this));
			} catch (IllegalPluginAccessException e) {
				(new BlockRestoreRunnable(arena, blocks, this)).instantlyRestore();
			}
		}
		
	}


	/**
	 * restore chests, if wanted and possible
	 * 
	 * @param arena
	 *            the arena to restore
	 */
	public void restoreChests() {
		debug.i("resetting chests");
		Set<ArenaRegionShape> bfs = arena.getRegionsByType(RegionType.BATTLE);
		
		if (bfs.size() < 1) {
			debug.i("no battlefield region, skipping restoreChests");
			return;
		}
		
		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTORECHESTS)) {
			debug.i("not restoring chests, skipping restoreChests");
			return;
		}
		
		for (ArenaRegionShape bfRegion : bfs) {
			debug.i("resetting arena: " + bfRegion.getName());

			if (containers.get(bfRegion) != null) {
				debug.i("container not null!");
				containers.get(bfRegion).restoreChests();
			}
		
		}
	}

	/**
	 * save a block to be restored (block destroy)
	 * 
	 * @param block
	 *            the block to save
	 */
	private void saveBlock(Block block) {
		debug.i("save block at " + block.getLocation().toString());
		if (!blocks.containsKey(block.getLocation())) {
			blocks.put(block.getLocation(), new ArenaBlock(block));
		}
	}

	/**
	 * save a block to be restored (block place)
	 * 
	 * @param block
	 *            the block to save
	 * @param type
	 *            the material to override
	 */
	private void saveBlock(Block block, Material type) {
		debug.i("save block at " + block.getLocation().toString());
		debug.i(" - type: " + type.toString());
		if (!blocks.containsKey(block.getLocation())) {
			blocks.put(block.getLocation(), new ArenaBlock(block, type));
		}
	}

	/**
	 * save arena chest, if wanted and possible
	 * 
	 * @param arena
	 *            the arena to save
	 */
	public void saveChests() {
		Set<ArenaRegionShape> bfs = arena.getRegionsByType(RegionType.BATTLE);
		
		if (bfs.size() < 1) {
			debug.i("no battlefield region, skipping saveChests");
			return;
		}
		
		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTORECHESTS)) {
			debug.i("not restoring chests, skipping saveChests");
			return;
		}
		
		for (ArenaRegionShape bfRegion : bfs) {
			containers.get(bfRegion).saveChests();
		}
	}
	
	@Override
	public void parseStart() {
		Set<ArenaRegionShape> bfs = arena.getRegionsByType(RegionType.BATTLE);
		
		if (bfs.size() < 1) {
			debug.i("no battlefield region, skipping restoreChests");
			return;
		}
		
		for (ArenaRegionShape region : bfs) {
			saveRegion(region);
		}
		
		for(ArenaRegionShape r : arena.getRegions()) {
			if (r.getName().startsWith("restore")) {
				saveRegion(r);
			}
		}
	}

	private void saveRegion(ArenaRegionShape region) {
		if (region == null) {
			return;
		}
		
		containers.put(region, new RestoreContainer(this, region));
		
		saveChests();
		
		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTOREBLOCKS) ||
				(!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD) &&
				!region.getName().startsWith("restore"))) {
			return;
		}

		PABlockLocation min = region.getMinimumLocation();
		PABlockLocation max = region.getMaximumLocation();
		
		World world = Bukkit.getWorld(min.getWorldName());
		
		for (int x = min.getX(); x <= max.getX(); x++) {
			for (int y = min.getY(); y <= max.getY(); y++) {
				for (int z = min.getZ(); z <= max.getZ(); z++) {
					saveBlock(world.getBlockAt(x, y, z));
				}
			}
		}
	}
}
