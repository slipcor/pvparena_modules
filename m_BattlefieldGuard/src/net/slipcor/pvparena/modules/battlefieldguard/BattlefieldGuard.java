package net.slipcor.pvparena.modules.battlefieldguard;

import java.util.HashMap;
import org.bukkit.Bukkit;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.loadables.ArenaModule;

public class BattlefieldGuard extends ArenaModule {
	protected HashMap<Arena, Integer> runnables = new HashMap<Arena, Integer>();

	public BattlefieldGuard() {
		super("BattlefieldGuard");
	}
	
	@Override
	public String version() {
		return "v0.10.0.0";
	}
	
	@Override
	public boolean hasSpawn(String s) {
		return s.equalsIgnoreCase("exit");
	}

	@Override
	public void parseEnable() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, new BattleRunnable(), 20L, 20L);
	}
}
