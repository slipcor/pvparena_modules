package net.slipcor.pvparena.modules.battlefieldguard;

import java.util.HashMap;
import org.bukkit.Bukkit;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.neworder.ArenaModule;

public class BattlefieldGuard extends ArenaModule {
	protected HashMap<Arena, Integer> runnables = new HashMap<Arena, Integer>();

	public BattlefieldGuard() {
		super("BattlefieldGuard");
	}
	
	@Override
	public String version() {
		return "v0.8.12.6";
	}

	@Override
	public void onEnable() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, new BattleRunnable(), 20L, 20L);
	}
}
