package net.slipcor.pvparena.modules.respawnrelay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.loadables.ArenaModule;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnRelay extends ArenaModule {
	protected Map<String, BukkitRunnable> runnerMap;
	
	public RespawnRelay() {
		super("RespawnRelay");
	}
	
	@Override
	public String version() {
		return "v1.0.0.25";
	}
	
	protected Map<String, BukkitRunnable> getRunnerMap() {
		if (runnerMap == null) {
			runnerMap = new HashMap<String, BukkitRunnable>();
		}
		return runnerMap;
	}
	
	@Override
	public boolean hasSpawn(String s) {
		return s.equals("relay");
	}
	
	@Override
	public String checkForMissingSpawns(Set<String> list) {
		return list.contains("relay")?null:"relay not set";
	}
	
	@Override
	public boolean tryDeathOverride(ArenaPlayer ap, List<ItemStack> drops) {
		ap.setStatus(Status.DEAD);
		
		if (drops == null) {
			drops = new ArrayList<ItemStack>();
		}
		
		arena.tpPlayerToCoordName(ap.get(), "relay");
		arena.unKillPlayer(ap.get(), ap.get().getLastDamageCause()==null?null:ap.get().getLastDamageCause().getCause(), ap.get().getKiller());
		
		if (getRunnerMap().containsKey(ap.getName())) {
			return true;
		}
		
		getRunnerMap().put(ap.getName(), new RelayRunnable(this, arena, ap, drops));
		
		return true;
	}
	
	@Override
	public void reset(boolean force) {
		for (BukkitRunnable br : getRunnerMap().values()) {
			br.cancel();
		}
		getRunnerMap().clear();
	}
}
