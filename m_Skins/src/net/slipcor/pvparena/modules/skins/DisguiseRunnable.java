package net.slipcor.pvparena.modules.skins;

import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;

public class DisguiseRunnable implements Runnable {
	Disguise disguise;
	Player player;
	public DisguiseRunnable(Player p, Disguise d) {
		player = p;
		disguise = d;
	}

	@Override
	public void run() {
		DisguiseCraft.getAPI().disguisePlayer(player, disguise);
	}

}
