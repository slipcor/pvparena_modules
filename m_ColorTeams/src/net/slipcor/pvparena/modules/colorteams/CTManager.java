package net.slipcor.pvparena.modules.colorteams;

import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;

public class CTManager extends ArenaModule implements Listener {
    private Scoreboard board;

    public CTManager() {
        super("ColorTeams");
    }

    @Override
    public void parseJoin(final CommandSender sender, final ArenaTeam team) {

    }
}
