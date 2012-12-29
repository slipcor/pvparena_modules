package net.slipcor.pvparena.modules.respawnrelay;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.runnables.ArenaRunnable;
import net.slipcor.pvparena.runnables.InventoryRefillRunnable;

public class RelayRunnable extends ArenaRunnable {
	private Arena a;
	private ArenaPlayer ap;
	List<ItemStack> drops;
	private Debug db = new Debug(77);

	public RelayRunnable(Arena arena, ArenaPlayer ap, List<ItemStack> drops) {
		
		super(MSG.TIMER_STARTING_IN.getNode(), 10, ap.get(), null, false);
		
		a = arena;
		this.ap = ap;
		this.drops = drops;
	}

	@Override
	protected void commit() {
		db.i("RelayRunnable commiting", ap.getName());
		new InventoryRefillRunnable(a, ap.get(), drops);
		SpawnManager.respawn(a,  ap);
		a.unKillPlayer(ap.get(), ap.get().getLastDamageCause()==null?null:ap.get().getLastDamageCause().getCause(), ap.get().getKiller());
		ap.setStatus(Status.FIGHT);
		RespawnRelay.runners.remove(ap.getName());
	}

	@Override
	protected void warn() {
		PVPArena.instance.getLogger().warning("RelayRunnable not scheduled yet!");
	}
}
