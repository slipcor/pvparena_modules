package net.slipcor.pvparena.modules.eventactions;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PABlockLocation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

public class EADelay implements Runnable {
	private final Block block;
	
	public EADelay(PABlockLocation loc2) {
		block = loc2.toLocation().getBlock();
	}
	@Override
	public void run() {
		final int type = block.getTypeId();
		final byte data = block.getData();
		final MaterialData meta = block.getState().getData();
		
		block.setTypeIdAndData(Material.REDSTONE_TORCH_ON.getId(), (byte) 5, true);
		
		class OffRunner implements Runnable {

			@Override
			public void run() {
				block.setTypeIdAndData(type, data, true);
				block.getState().setData(meta);
			}
    		
    	}
		
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new OffRunner(), 20L);
	}

}
