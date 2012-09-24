package net.slipcor.pvparena.modules.blockrestore;

import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.ArenaManager;
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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.material.Attachable;

public class Blocks extends ArenaModule {
	
	public Blocks() {
		super("BlockRestore");
	}
	
	@Override
	public String version() {
		return "v0.9.0.0";
	}

	public static HashMap<Location, ArenaBlock> blocks = new HashMap<Location, ArenaBlock>();
	public static HashMap<Location, String[]> signs = new HashMap<Location, String[]>();
	
	private static HashMap<ArenaRegionShape, RestoreContainer> containers = new HashMap<ArenaRegionShape, RestoreContainer>();

	private static Debug db = new Debug(24);

	@Override
	public void addSettings(HashMap<String, String> types) {
		types.put("blockRestore.hard", "boolean");
		types.put("blockRestore.offset", "int");
		types.put("protection.pickup", "boolean");
	}
	
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
	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
		
		if (args[0].toLowerCase().startsWith("clearinv")) {
			if (!PVPArena.hasAdminPerms(sender)
					&& !(PVPArena.hasCreatePerms(sender, arena))) {
				ArenaManager.tellPlayer(sender,
						Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
				return;
			}
			arena.getArenaConfig().setManually("inventories", null);
			ArenaManager.tellPlayer(sender, "Inventories cleared. Expect lag on next arena start!");
		}
	}
	
	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		config.addDefault("blockRestore.hard", Boolean.valueOf(false));
		config.addDefault("protection.pickup", Boolean.valueOf(false));
		config.addDefault("blockRestore.offset", Integer.valueOf(0));
		config.options().copyDefaults(true);
	}
	
	@Override
	public void onEntityExplode(Arena arena, EntityExplodeEvent event) {
		if (!arena.isLocked() &&
				!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)) {
			for (Block b : event.blockList()) {
				saveBlock(b);
			}
		}
	}

	
	@Override
	public void onBlockBreak(Arena arena, Block block) {
		db.i("block break in blockRestore");
		if (arena == null || arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)) {
			db.i(arena + " || blockRestore.hard: " + arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD));
			return;
		}
		if (!arena.isLocked()
				&& arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_ACTIVE)) {
			
			checkBlock(block.getRelative(BlockFace.NORTH), BlockFace.SOUTH);
			checkBlock(block.getRelative(BlockFace.SOUTH), BlockFace.NORTH);
			checkBlock(block.getRelative(BlockFace.EAST), BlockFace.WEST);
			checkBlock(block.getRelative(BlockFace.WEST), BlockFace.EAST);
			
			saveBlock(block);
		}
		db.i("!arena.isLocked() " + !arena.isLocked() + " && restore " + arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_ACTIVE));
	}
	@Override
	public void onBlockPiston(Arena arena, Block block) {
		if (!arena.isLocked()
				&& arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_ACTIVE)
				&& !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)) {
			saveBlock(block);
		}
	}

	@Override
	public void onBlockPlace(Arena arena, Block block, Material mat) {
		if (!arena.isLocked()
				&& arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_ACTIVE)
				&& !arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)) {
			saveBlock(block, mat);
		}
	}
	
	@Override
	public void onPlayerPickupItem(Arena arena, PlayerPickupItemEvent event) {
		if (!arena.isLocked()
				&& arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_ACTIVE)) {
			event.setCancelled(true);
		}
	}
	
	@Override
	public boolean parseCommand(String s) {
		return (s.toLowerCase().startsWith("clearinv"));
	}

	@Override
	public void parseInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6BlockRestore:§f "
				+ StringParser.colorVar("hard", arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD)));
	}
	
	@Override
	public void reset(Arena arena, boolean force) {
		resetBlocks(arena);
		restoreChests(arena);
	}

	/**
	 * reset all blocks belonging to an arena
	 * 
	 * @param arena
	 *            the arena to reset
	 */
	private void resetBlocks(Arena arena) {
		db.i("resetting blocks");
		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new BlockRestoreRunnable(arena, blocks));
	}


	/**
	 * restore chests, if wanted and possible
	 * 
	 * @param arena
	 *            the arena to restore
	 */
	public void restoreChests(Arena arena) {
		HashSet<ArenaRegionShape> bfs = arena.getRegionsByType(RegionType.BATTLE);
		
		if (bfs.size() < 1) {
			db.i("no battlefield region, skipping restoreChests");
			return;
		}
		
		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTORECHESTS)) {
			db.i("not restoring chests, skipping restoreChests");
			return;
		}
		
		for (ArenaRegionShape bfRegion : bfs) {

			if (containers.get(bfRegion) != null)
				containers.get(bfRegion).restoreChests();
		
		}
	}

	/**
	 * save a block to be restored (block destroy)
	 * 
	 * @param block
	 *            the block to save
	 */
	private void saveBlock(Block block) {
		db.i("save block at " + block.getLocation().toString());
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
		db.i("save block at " + block.getLocation().toString());
		db.i(" - type: " + type.toString());
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
	public void saveChests(Arena arena) {
		HashSet<ArenaRegionShape> bfs = arena.getRegionsByType(RegionType.BATTLE);
		
		if (bfs.size() < 1) {
			db.i("no battlefield region, skipping saveChests");
			return;
		}
		
		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_RESTORECHESTS)) {
			db.i("not restoring chests, skipping saveChests");
			return;
		}
		
		for (ArenaRegionShape bfRegion : bfs) {
			containers.get(bfRegion).saveChests();
		}
	}
	
	@Override
	public void teleportAllToSpawn(Arena arena) {
		HashSet<ArenaRegionShape> bfs = arena.getRegionsByType(RegionType.BATTLE);
		
		if (bfs.size() < 1) {
			db.i("no battlefield region, skipping restoreChests");
			return;
		}
		
		for (ArenaRegionShape region : bfs) {
			saveRegion(arena, region);
		}
		
		for(ArenaRegionShape r : arena.getRegions()) {
			if (r.getName().startsWith("restore")) {
				saveRegion(arena, r);
			}
		}
	}

	private void saveRegion(Arena arena, ArenaRegionShape region) {
		if (region == null) {
			return;
		}
		
		containers.put(region, new RestoreContainer(arena, region));
		
		saveChests(arena);
		
		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BLOCKRESTORE_HARD) && !region.getName().startsWith("restore")) {
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
