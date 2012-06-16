package net.slipcor.pvparena.modules.blockrestore;

import java.util.HashMap;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaModule;
import net.slipcor.pvparena.neworder.ArenaRegion;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.material.Attachable;

public class Blocks extends ArenaModule {
	
	public Blocks() {
		super("BlockRestore");
	}
	
	@Override
	public String version() {
		return "v0.8.8.0";
	}

	public static HashMap<Location, ArenaBlock> blocks = new HashMap<Location, ArenaBlock>();
	public static HashMap<Location, String[]> signs = new HashMap<Location, String[]>();
	
	private static HashMap<ArenaRegion, RestoreContainer> containers = new HashMap<ArenaRegion, RestoreContainer>();

	private static Debug db = new Debug(24);
	
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
				Arenas.tellPlayer(sender,
						Language.parse("nopermto", Language.parse("admin")));
				return;
			}
			arena.cfg.set("inventories", null);
			Arenas.tellPlayer(sender, "Inventories cleared. Expect lag on next arena start!");
		}
	}
	
	@Override
	public void configParse(Arena arena, YamlConfiguration config, String type) {
		config.addDefault("blockRestore.hard", Boolean.valueOf(false));
		config.options().copyDefaults(true);
	}

	/**
	 * get all blocks that have to be reset (arena wise)
	 * 
	 * @param arena
	 *            the arena to check
	 * @return a map of location=>block to reset
	 */
	private HashMap<Location, ArenaBlock> getBlocks(Arena arena) {
		HashMap<Location, ArenaBlock> result = new HashMap<Location, ArenaBlock>();

		db.i("reading all arenablocks");
		for (Location l : blocks.keySet()) {
			if (blocks.get(l).arena.equals(arena.name)
					|| blocks.get(l).arena.equals("")) {
				result.put(l, blocks.get(l));
				db.i(" - " + l.toString());
			}
		}

		return result;
	}
	
	@Override
	public void onEntityExplode(Arena arena, EntityExplodeEvent event) {
		if (arena.fightInProgress &&
				!arena.cfg.getBoolean("blockRestore.hard")) {
			for (Block b : event.blockList()) {
				saveBlock(b);
			}
		}
	}

	
	@Override
	public void onBlockBreak(Arena arena, Block block) {
		db.i("block break in blockRestore");
		if (arena == null || arena.cfg.getBoolean("blockRestore.hard")) {
			db.i(arena + " || blockRestore.hard: " + arena.cfg.getBoolean("blockRestore.hard"));
			return;
		}
		if (arena.fightInProgress
				&& arena.cfg.getBoolean("protection.restore")) {
			
			checkBlock(block.getRelative(BlockFace.NORTH), BlockFace.SOUTH);
			checkBlock(block.getRelative(BlockFace.SOUTH), BlockFace.NORTH);
			checkBlock(block.getRelative(BlockFace.EAST), BlockFace.WEST);
			checkBlock(block.getRelative(BlockFace.WEST), BlockFace.EAST);
			
			saveBlock(block);
		}
		db.i("arena.fightInProgress " + arena.fightInProgress + " && restore " + arena.cfg.getBoolean("protection.restore"));
	}
	@Override
	public void onBlockPiston(Arena arena, Block block) {
		if (arena.fightInProgress
				&& arena.cfg.getBoolean("protection.restore")
				&& !arena.cfg.getBoolean("blockRestore.hard")) {
			saveBlock(block);
		}
	}

	@Override
	public void onBlockPlace(Arena arena, Block block, Material mat) {
		if (arena.fightInProgress
				&& arena.cfg.getBoolean("protection.restore")
				&& !arena.cfg.getBoolean("blockRestore.hard")) {
			saveBlock(block, mat);
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
				+ StringParser.colorVar("hard", arena.cfg.getBoolean("blockRestore.hard")));
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
		HashMap<Location, ArenaBlock> removals = getBlocks(arena);
		for (Location l : removals.keySet()) {
			db.i("location: " + l.toString());
			removals.get(l).reset();
			blocks.remove(l);
		}
	}


	/**
	 * restore chests, if wanted and possible
	 * 
	 * @param arena
	 *            the arena to restore
	 */
	public void restoreChests(Arena arena) {
		ArenaRegion bfRegion = arena.regions.get("battlefield");

		if (bfRegion == null) {
			db.i("no battlefield region, skipping restoreChests");
			return;
		}

		if (!arena.cfg.getBoolean("general.restoreChests")) {
			db.i("not restoring chests, skipping restoreChests");
			return;
		}
		if (containers.get(bfRegion) != null)
			containers.get(bfRegion).restoreChests();
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
		ArenaRegion bfRegion = arena.regions.get("battlefield");

		if (bfRegion == null) {
			db.i("no battlefield region, skipping saveChests");
			return;
		}

		if (!arena.cfg.getBoolean("general.restoreChests")) {
			db.i("not restoring chests, skipping saveChests");
			return;
		}

		containers.get(bfRegion).saveChests();
	}
	
	@Override
	public void teleportAllToSpawn(Arena arena) {
		ArenaRegion region = arena.regions.get("battlefield");
		
		if (region == null) {
			return;
		}
		
		containers.put(region, new RestoreContainer(arena, region));
		
		saveChests(arena);
		
		if (!arena.cfg.getBoolean("blockRestore.hard")) {
			return;
		}

		Location min = region.getAbsoluteMinimum();
		Location max = region.getAbsoluteMaximum();
		
		for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
			for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
				for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
					saveBlock(min.getWorld().getBlockAt(x, y, z));
				}
			}
		}
	}
}
