package net.slipcor.pvparena.modules.blockdissolve;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;

public class MoveChecker implements Listener {
	private Debug debug = new Debug(42);
	private final ItemStack[] materials;
	private final Arena arena;
	private Map<Block, Runnable> map = new HashMap<Block, Runnable>();
	final int delay;
	final int startSeconds;
	boolean active = false;
	CountdownRunner runner = null;

	/**
	 * construct a powerup spawn runnable
	 * 
	 * @param a
	 *            the arena it's running in
	 */
	public MoveChecker(Arena arena, String definition, int delay) {
		materials = StringParser.getItemStacksFromString(definition);
		debug.i("BattleRunnable constructor");
		this.arena = arena;
		Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
		this.delay = delay;
		this.startSeconds = arena.getArenaConfig().getInt(CFG.MODULES_BLOCKDISSOLVE_STARTSECONDS);
	}

	@EventHandler(ignoreCancelled=true)
	public void onMove(PlayerMoveEvent event) {
		
		if (active && arena.isFightInProgress()) {
			
			if (runner == null) {
				runner = new CountdownRunner(arena, this, startSeconds);
			}
			
			ArenaPlayer player = ArenaPlayer.parsePlayer(event.getPlayer().getName());
			if (arena != player.getArena()) {
				return;
			} 	
			
			if (arena.getPlayedSeconds() > startSeconds && player.getStatus() == Status.FIGHT) {
				
				
				checkBlock(event.getPlayer().getLocation().subtract(0, 1, 0));
			}
		}
	}
	
	private void checkBlock(Location location) {
		
		double x = ((location.getX()*10) % 10)/10;
		double z = ((location.getZ()*10) % 10)/10;
		
		if (x < 0.333) {
			checkBlock(location.add(-1, 0, 0).getBlock());
		} else if (x > 0.666) {
			checkBlock(location.add(1, 0, 0).getBlock());
		}
		
		if (z < 0.333) {
			checkBlock(location.add(0, 0, -1).getBlock());
		} else if (z > 0.666) {
			checkBlock(location.add(0, 0, 1).getBlock());
		}
	
		checkBlock(location.getBlock());
	}

	private void checkBlock(Block block) {
		Material mat = block.getType();
		
		for (ItemStack stack : materials) {
			if (mat.equals(stack.getType())) {
				access(block, false);
				return;
			}
		}
	}

	private synchronized void access(Block block, boolean remove) {
		if (block == null && remove) {
			map.clear();
			return;
		}
		
		if (map.containsKey(block)) {
			return;
		}
		if (remove) {
			map.remove(block);
		} else {
			map.put(block, new RunLater(block));
		}
	}
	class RunLater implements Runnable {
		final Block block;
		RunLater(Block b) {
			block = b;
			Bukkit.getScheduler().runTaskLater(PVPArena.instance, this, delay);
		}
		@Override
		public void run() {
			access(block, true);
			block.setType(Material.AIR);
		}
		
	}
	public void clear() {
		access(null, true);
		runner = null;
	}
}
