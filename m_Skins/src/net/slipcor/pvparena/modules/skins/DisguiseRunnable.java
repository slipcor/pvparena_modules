package net.slipcor.pvparena.modules.skins;

import org.bukkit.entity.Player;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;

class DisguiseRunnable implements Runnable {
    private final Disguise disguise;
    private final Player player;

    public DisguiseRunnable(Player p, Disguise d) {
        player = p;
        disguise = d;
    }

    @Override
    public void run() {
        DisguiseCraft.getAPI().disguisePlayer(player, disguise);
    }

}
