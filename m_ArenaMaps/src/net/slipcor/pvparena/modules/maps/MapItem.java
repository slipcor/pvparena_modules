package net.slipcor.pvparena.modules.maps;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MapItem {
    private final int x;
    private final int z;
    private final boolean player;
    private final String name;
    private final ChatColor color;

    public MapItem(Arena a, Player p, ChatColor c) {
        player = true;
        color = c;
        name = p.getName();
        x = 0;
        z = 0;
    }

    public MapItem(Arena a, PABlockLocation coord, ChatColor c) {
        player = false;
        name = null;
        color = c;
        x = coord.getX();
        z = coord.getZ();
    }

    public int getX() {
        if (player) {
            try {
                return Bukkit.getPlayerExact(name).getLocation().getBlockX();
            } catch (NullPointerException e) {

            }
        }
        return x;
    }

    public int getZ() {
        if (player) {
            try {
                return Bukkit.getPlayerExact(name).getLocation().getBlockZ();
            } catch (NullPointerException e) {

            }
        }
        return z;
    }

    public ChatColor getColor() {
        return color;
    }

    public boolean isPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }
}
