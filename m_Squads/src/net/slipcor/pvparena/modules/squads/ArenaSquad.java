package net.slipcor.pvparena.modules.squads;

import net.slipcor.pvparena.arena.ArenaPlayer;

import java.util.HashSet;
import java.util.Set;

class ArenaSquad {
    private final String name;
    private final int max;
    private final Set<ArenaPlayer> players = new HashSet<ArenaPlayer>();

    public ArenaSquad(String sName, int iMax) {
        name = sName;
        max = iMax;
    }

    public int getCount() {
        return players.size();
    }

    public int getMax() {
        return max;
    }

    public String getName() {
        return name;
    }

    public void add(ArenaPlayer player) {
        players.add(player);
    }

    public boolean contains(ArenaPlayer player) {
        return players.contains(player);
    }

    public void remove(ArenaPlayer player) {
        players.remove(player);
    }

    public void reset() {
        players.clear();
    }

    public Set<ArenaPlayer> getPlayers() {
        Set<ArenaPlayer> result = new HashSet<ArenaPlayer>();
        for (ArenaPlayer ap : players) {
            result.add(ap);
        }
        return result;
    }
}
