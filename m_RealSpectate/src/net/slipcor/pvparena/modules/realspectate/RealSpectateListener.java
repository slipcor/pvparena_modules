package net.slipcor.pvparena.modules.realspectate;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAG_Spectate;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.events.PADeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class RealSpectateListener implements Listener {
    final RealSpectate rs;
    HashMap<Player, SpectateWrapper> spectated_players = new HashMap<Player, SpectateWrapper>();
    Debug debug = new Debug(456);

    public RealSpectateListener(RealSpectate realSpectate) {
        rs = realSpectate;
    }

    void initiate(ArenaPlayer ap) {
        for (ArenaPlayer a : rs.getArena().getEveryone()) {
            update(ap, a);

            break;
        }
    }

    void update(ArenaPlayer spectator, ArenaPlayer fighter) {
        Player s = spectator.get();
        Player f = fighter.get();

        createSpectateWrapper(s, f);
    }

    SpectateWrapper createSpectateWrapper(Player s,
                                          Player f) {
        //debug.i("createSwapper", s);
        debug.i("create wrapper: " + s.getName() + "+" + String.valueOf(f), s);
        if (!spectated_players.containsKey(f)) {
            spectated_players.put(f, new SpectateWrapper(s, f, this));
        }
        for (SpectateWrapper sw : spectated_players.values()) {
            sw.update(s);
            sw.update();
        }
        return spectated_players.get(f);
    }

    private Player getSpectatedSuspect(Player p) {
        rs.getArena().getDebugger().i("getSpecated: " + p.getName());
        for (SpectateWrapper sw : spectated_players.values()) {
            rs.getArena().getDebugger().i("found wrapper: " + sw.getSuspect().getName());
            sw.debug(rs.getArena().getDebugger());
            if (sw.hasSpectator(p)) {
                return sw.getSuspect();
            }
        }

        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();
        ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

        if (rs.getArena() == null || !rs.getArena().equals(aPlayer.getArena())) {
            return;
        }
        rs.getArena().getDebugger().i("RealSpectateListener oEEDBE");

        Player subject = getSpectatedSuspect(player);

        if (subject != null) {
            // subject is being spectated

            debug.i("player is spectating and being damaged", subject);

            if (event.getDamager() instanceof Projectile) {

                debug.i("relay damage", subject);
                // Damage is a Projectile that should have hit the subject
                // --> relay damage to subject

                EntityDamageByEntityEvent projectileEvent = new EntityDamageByEntityEvent(
                        event.getDamager(), subject, event.getCause(), event.getDamage());

                subject.setLastDamageCause(projectileEvent);
                subject.damage(event.getDamage(), event.getDamager());

            }

            // spectators don't receive damage

            event.setCancelled(true);
            event.getDamager().remove();

            return;
        }

        subject = ((Player) event.getEntity());

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        spectated_players.get(subject).updateHealth();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player subject = getSpectatedSuspect((Player) event.getEntity());

        if (subject != null) {
            // subject is being spectated

            Player spectator = (Player) event.getEntity();
            // player is spectating and has died. wait, what?
            // --> hack reset!
            spectator.setHealth(1);
            event.getDrops().clear();
            return;
        }

        subject = ((Player) event.getEntity());

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        spectated_players.get(subject).stopSpectating();
        spectated_players.remove(subject);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player subject = getSpectatedSuspect((Player) event.getEntity());

        if (subject != null) {
            // subject is being spectated

            // player is spectating and wanting to regain health
            // --> cancelling
            event.setCancelled(true);
            return;
        }

        subject = ((Player) event.getEntity());

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        spectated_players.get(subject).updateHealth();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() == null || event.getTarget().getType() != EntityType.PLAYER) {
            return;
        }

        Player subject = ((Player) event.getTarget());

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        // --> nope. DON'T LOOK AT ME!
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player subject = getSpectatedSuspect((Player) event.getWhoClicked());

        if (subject != null) {
            // subject is being spectated

            // player is spectating
            // --> no clicking!

            event.setCancelled(true);
            return;
        }

        subject = ((Player) event.getWhoClicked());

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        spectated_players.get(subject).updateInventory();
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player subject = getSpectatedSuspect((Player) event.getPlayer());

        if (subject != null) {
            // subject is being spectated

            // player is spectating
            // --> don't care
            return;
        }

        subject = ((Player) event.getPlayer());

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        // --> close all other inventories

        spectated_players.get(subject).closeInventory();
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player subject = getSpectatedSuspect((Player) event.getPlayer());

        if (subject != null) {
            // subject is being spectated

            // player is spectating
            // --> no opening!
            event.setCancelled(true);
            return;
        }

        subject = ((Player) event.getPlayer());

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        spectated_players.get(subject).openInventory(event.getInventory());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player subject = getSpectatedSuspect(event.getPlayer());

        if (subject != null) {
            // subject is being spectated

            // player is spectating
            // --> no dropping!
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player subject = getSpectatedSuspect(event.getPlayer());

        if (subject != null) {
            // subject is being spectated

            Player spectator = event.getPlayer();
            // player is spectating
            // --> cancel and switch
            String actionName = event.getAction().name();

            event.setCancelled(true);

            if (actionName.startsWith("LEFT_")) {
                switchPlayer(spectator, subject, false);
            } else if (actionName.startsWith("RIGHT_")) {
                switchPlayer(spectator, subject, true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player subject = getSpectatedSuspect(event.getPlayer());

        if (subject != null) {
            // subject is being spectated

            Player spectator = event.getPlayer();
            // player is spectating
            // --> no clicking!!!
            event.setCancelled(true);
            switchPlayer(spectator, subject, event.getRightClicked() != null);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player subject = getSpectatedSuspect(event.getPlayer());

        if (subject != null) {
            // subject is being spectated

            // player is spectating
            // --> so what?
            return;
        }

        subject = event.getPlayer();

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        // --> so what?
        spectated_players.get(subject).selectItem(event.getNewSlot());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player subject = getSpectatedSuspect(event.getPlayer());

        if (subject != null) {
            // subject is being spectated

            // player is spectating
            // --> NO MOVING!
            event.setCancelled(true);
            return;
        }

        subject = event.getPlayer();

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        spectated_players.get(subject).updateLocation();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player subject = getSpectatedSuspect(event.getPlayer());

        if (subject != null) {
            // subject is being spectated

            // player is spectating
            // --> no pickup!
            event.setCancelled(true);
            return;
        }

        subject = event.getPlayer();

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        spectated_players.get(subject).updateInventory();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player subject = getSpectatedSuspect(event.getPlayer());

        if (subject != null) {
            // subject is being spectated

            Player spectator = event.getPlayer();
            // player is spectating
            // --> remove from spectators
            spectated_players.get(subject).removeSpectator(spectator);
            return;
        }

        subject = event.getPlayer();

        if (!spectated_players.containsKey(subject)) {
            return;
        }

        // subject is being spectated
        spectated_players.get(subject).stopSpectating();
        spectated_players.remove(subject);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        debug.i("ProjectileLaunch!");
        if (event == null ||
                event.getEntity() == null ||
                event.getEntity().getShooter() == null ||
                !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player subject = getSpectatedSuspect((Player) event.getEntity().getShooter());

        if (subject != null) {
            debug.i("subject != null", subject);
            // subject is being spectated
            // player is spectating
            // --> cancel and out
            event.setCancelled(true);
            return;
        }

        subject = ((Player) event.getEntity().getShooter());

        if (!spectated_players.containsKey(subject)) {
            debug.i("not being spectated", subject);
            return;
        }

        debug.i("subject is being spectated", subject);

        Projectile projectile = event.getEntity();
        Location location = subject.getLocation();

        debug.i(String.valueOf(new PABlockLocation(location)), subject);
        Vector direction = location.getDirection();

        location.add(direction.normalize().multiply(1));
        //location.setY(subject.getEyeLocation().getY());
        location.setY(location.getY() + 1.4D);

        debug.i(String.valueOf(new PABlockLocation(location)), subject);

        projectile.teleport(location);

    }

    @EventHandler
    public void onPADeath(final PADeathEvent event) {
        if (!event.getArena().equals(rs.getArena())) {
            return;
        }
        if (!event.isRespawning()) {
            try {
                class RunLater implements Runnable {

                    @Override
                    public void run() {
                        if (event.getArena().isFightInProgress()) {
                            PAG_Spectate spec = new PAG_Spectate();
                            spec.commit(event.getArena(), event.getPlayer(), new String[0]);
                        }
                    }

                }
                Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 5L);
            } catch (Exception e) {

            }
        }
    }

    void switchPlayer(Player spectator, Player subject, boolean forward) {
        if (subject != null) {
            spectator.showPlayer(subject);
        }

        if (rs.getArena().getFighters().size() < 1) {
            debug.i("< 1", spectator);
            return;
        }

        Player nextPlayer = null;
        boolean next = false;
        for (ArenaPlayer ap : rs.getArena().getFighters()) {
            debug.i("checking " + ap.getName(), spectator);
            Player p = ap.get();

            if (ap.getName().equals(spectator.getName())) {
                debug.i("we are still in -.-", spectator);
                continue;
            }

            if (subject == null) {
                debug.i("subject == null", spectator);
                nextPlayer = p;
                break;
            }


            if (!p.equals(subject) || next) {
                debug.i("||", spectator);
                nextPlayer = p;
                if (next) { //TODO: next is always false!
                    debug.i("next", spectator);
                    break;
                }
                continue;
            }

            // p == subject

            if (!forward) {
                debug.i("step back", spectator);
                if (nextPlayer == null) {
                    debug.i("get last element", spectator);
                    for (ArenaPlayer ap2 : rs.getArena().getFighters()) {
                        debug.i(ap2.getName(), spectator);
                        nextPlayer = ap2.get();
                    }
                } // else: nextPlayer has content. yay!

                debug.i("==>" + nextPlayer.getName(), spectator);
                break;
            }
        }
        if (subject != null)
            spectated_players.get(subject).removeSpectator(spectator);
        createSpectateWrapper(spectator, nextPlayer);
    }
}
