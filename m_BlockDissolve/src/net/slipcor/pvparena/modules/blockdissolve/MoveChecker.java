package net.slipcor.pvparena.modules.blockdissolve;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
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
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;

public class MoveChecker implements Listener {
	private Debug debug = new Debug(42);
	private final ItemStack[] materials;
	private final Arena arena;
	private Map<Block, Runnable> map = new HashMap<Block, Runnable>();

	/**
	 * construct a powerup spawn runnable
	 * 
	 * @param a
	 *            the arena it's running in
	 */
	public MoveChecker(Arena arena, String definition) {
		materials = StringParser.getItemStacksFromString(definition);
		debug.i("BattleRunnable constructor");
		this.arena = arena;
		Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
	}

	@EventHandler(ignoreCancelled=true)
	public void onMove(PlayerMoveEvent event) {
		if (arena.isFightInProgress()) {
			ArenaPlayer player = ArenaPlayer.parsePlayer(event.getPlayer().getName());
			if (player.getStatus() == Status.FIGHT) {
				Block block = player.get().getLocation().getBlock().getRelative(BlockFace.DOWN);
				Material mat = block.getType();
				
				for (ItemStack stack : materials) {
					if (mat.equals(stack.getType())) {
						access(block, false);
					}
				}
			}
		}
	}
	
	private synchronized void access(Block block, boolean remove) {
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
			Bukkit.getScheduler().runTask(PVPArena.instance, this);
		}
		@Override
		public void run() {
			block.setType(Material.AIR);
			access(block, true);
		}
		
	}
}
