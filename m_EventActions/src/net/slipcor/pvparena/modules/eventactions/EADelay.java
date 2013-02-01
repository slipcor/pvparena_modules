package net.slipcor.pvparena.modules.eventactions;

import net.slipcor.pvparena.classes.PABlockLocation;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;

public class EADelay implements Runnable {
	private final PABlockLocation loc;
	
	public EADelay(PABlockLocation loc2) {
		loc = loc2;
	}
	@Override
	public void run() {
		MaterialData state = loc.toLocation().getBlock().getState().getData();
		if (state instanceof Lever) {
			((Lever)state).setPowered(false);
		} else if (state instanceof Button) {
			((Button)state).setPowered(false);
		}
	}

}
