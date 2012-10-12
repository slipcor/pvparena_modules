package net.slipcor.pvparena.modules.battlefieldguard;

import java.util.HashMap;
import org.bukkit.Bukkit;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class BattlefieldGuard extends ArenaModule {
	protected HashMap<Arena, Integer> runnables = new HashMap<Arena, Integer>();

	public BattlefieldGuard() {
		super("BattlefieldGuard");
	}
	
	@Override
	public String version() {
		return "v0.9.3.8";
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return arena.getArenaConfig().getBoolean(CFG.MODULES_BATTLEFIELDGUARD_ACTIVE);
	}
	
	@Override
	public boolean hasSpawn(Arena a, String s) {
		return s.equalsIgnoreCase("exit");
	}

	@Override
	public void onEnable() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, new BattleRunnable(), 20L, 20L);
	}
}
