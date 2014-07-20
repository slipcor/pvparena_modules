package net.slipcor.pvparena.modules.redstone;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.managers.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class RedStoneTriggers extends ArenaModule implements Listener {
    public RedStoneTriggers() {
        super("RedStoneTriggers");
        debug = new Debug(403);
    }

    private boolean setup;

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    @Override
    public void configParse(final YamlConfiguration config) {
        if (!setup) {
            Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
            setup = true;
        }
    }

    @EventHandler
    public void onRedStone(final BlockRedstoneEvent event) {
        final Arena arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getBlock().getLocation()));
        if (arena == null || !arena.equals(this.arena)) {
            return;
        }
        debug.i("redstone in arena " + arena);

        final Block block = event.getBlock();

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        final Sign s = (Sign) block.getState();

        if ("[WIN]".equals(s.getLine(0))) {
            for (final ArenaTeam team : arena.getTeams()) {
                if (team.getName().equalsIgnoreCase(s.getLine(1))) {
                    // skip winner
                    continue;
                }
                for (final ArenaPlayer ap : team.getTeamMembers()) {
                    if (ap.getStatus() == Status.FIGHT) {
                        event.getBlock().getWorld().strikeLightningEffect(ap.get().getLocation());
                        final EntityDamageEvent e = new EntityDamageEvent(ap.get(), DamageCause.LIGHTNING, 10);
                        PlayerListener.finallyKillPlayer(arena, ap.get(), e);
                    }
                }
            }
        } else if ("[LOSE]".equals(s.getLine(0))) {
            for (final ArenaTeam team : arena.getTeams()) {
                if (!team.getName().equalsIgnoreCase(s.getLine(1))) {
                    // skip winner
                    continue;
                }
                for (final ArenaPlayer ap : team.getTeamMembers()) {
                    if (ap.getStatus() == Status.FIGHT) {
                        event.getBlock().getWorld().strikeLightningEffect(ap.get().getLocation());
                        final EntityDamageEvent e = new EntityDamageEvent(ap.get(), DamageCause.LIGHTNING, 10);
                        PlayerListener.finallyKillPlayer(arena, ap.get(), e);
                    }
                }
            }
        }
    }
}
