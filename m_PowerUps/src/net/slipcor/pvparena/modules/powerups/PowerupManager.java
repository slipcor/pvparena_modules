package net.slipcor.pvparena.modules.powerups;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
import net.slipcor.pvparena.managers.SpawnManager;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PowerupManager extends ArenaModule implements Listener {


    private Powerups usesPowerups;

    private int powerupDiff;
    private int powerupDiffI;

    int SPAWN_ID = -1;

    private boolean setup;

    public PowerupManager() {
        super("PowerUps");
        debug = new Debug(402);
    }

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    /**
     * calculate a powerup and commit it
     */
    void calcPowerupSpawn() {
        debug.i("powerups?");
        if (usesPowerups == null) {
            return;
        }

        if (usesPowerups.puTotal.size() <= 0) {
            return;
        }

        debug.i("totals are filled");
        final Random r = new Random();
        int i = r.nextInt(usesPowerups.puTotal.size());

        for (final Powerup p : usesPowerups.puTotal) {
            if (--i > 0) {
                continue;
            }
            commitPowerupItemSpawn(p.item);
            arena.broadcast(Language.parse(MSG.MODULE_POWERUPS_SERVER, p.name));
            return;
        }

    }

    @Override
    public boolean checkCommand(final String s) {
        return "!pu".equals(s) || s.startsWith("powerup");
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("powerups");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!pu");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"time"});
        result.define(new String[]{"death"});
        result.define(new String[]{"off"});
        result.define(new String[]{"dropspawn"});
        return result;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        // !pu time 6
        // !pu death 4

        if (!PVPArena.hasAdminPerms(sender)
                && !PVPArena.hasCreatePerms(sender, arena)) {
            arena.msg(
                    sender,
                    Language.parse(MSG.ERROR_NOPERM,
                            Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2, 3})) {
            return;
        }

        if ("!pu".equals(args[0]) || args[0].startsWith("powerup")) {
            if (args.length == 2) {
                if ("off".equals(args[1])) {
                    arena.getArenaConfig().set(CFG.MODULES_POWERUPS_USAGE, args[1]);
                    arena.getArenaConfig().save();
                    arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_POWERUPS_USAGE.getNode(), args[1]));
                    return;
                }
                if (args[1].equals("dropspawn")) {
                    boolean b = arena.getArenaConfig().getBoolean(CFG.MODULES_POWERUPS_DROPSPAWN);
                    arena.getArenaConfig().set(CFG.MODULES_POWERUPS_DROPSPAWN, !b);
                    arena.getArenaConfig().save();
                    arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_POWERUPS_DROPSPAWN.getNode(), String.valueOf(!b)));
                }
                arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "off | dropspawn"));
                return;
            }
            final int i;
            try {
                i = Integer.parseInt(args[2]);
            } catch (final Exception e) {
                arena.msg(sender,
                        Language.parse(MSG.ERROR_NOT_NUMERIC, args[2]));
                return;
            }
            if ("time".equals(args[1]) || "death".equals(args[1])) {
                arena.getArenaConfig().set(CFG.MODULES_POWERUPS_USAGE, args[1] + ":" + i);
                arena.getArenaConfig().save();
                arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_POWERUPS_USAGE.getNode(), args[1] + ":" + i));
                return;
            }

            arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "time | death"));
        }
    }

    @Override
    public boolean commitEnd(final ArenaTeam arg1) {
        if (usesPowerups != null) {
            if (arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE).startsWith("death")) {
                debug.i("calculating powerup trigger death");
                powerupDiffI = ++powerupDiffI % powerupDiff;
                if (powerupDiffI == 0) {
                    calcPowerupSpawn();
                }
            }
        }
        return false;
    }

    /**
     * commit the powerup item spawn
     *
     * @param item the material to spawn
     */
    void commitPowerupItemSpawn(final Material item) {
        debug.i("dropping item?");
        if (arena.getArenaConfig().getBoolean(CFG.MODULES_POWERUPS_DROPSPAWN)) {
            dropItemOnSpawn(item);
        } else {
            final Set<ArenaRegion> ars = arena.getRegionsByType(RegionType.BATTLE);
            for (final ArenaRegion ar : ars) {

                final PABlockLocation min = ar.getShape().getMinimumLocation();
                final PABlockLocation max = ar.getShape().getMaximumLocation();

                final Random r = new Random();

                final int x = r.nextInt(max.getX() - min.getX());
                final int z = r.nextInt(max.getZ() - min.getZ());

                final World w = Bukkit.getWorld(min.getWorldName());

                mark(w.dropItem(w.getHighestBlockAt(min.getX() + x, min.getZ() + z).getRelative(BlockFace.UP).getLocation(), new ItemStack(item, 1)));

                break;
            }
        }
    }

    @Override
    public void configParse(final YamlConfiguration config) {
        if (!setup) {
            Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
            setup = true;
        }
        final HashMap<String, Object> powerups = new HashMap<String, Object>();
        if (config.getConfigurationSection("powerups") != null) {
            final Map<String, Object> map = (HashMap<String, Object>) config
                    .getConfigurationSection("powerups").getValues(false);
            Map<String, Object> map3 = new HashMap<String, Object>();
            debug.i("parsing powerups");
            for (final String key : map.keySet()) {
                // key e.g. "OneUp"
                Map<String, Object> map2 = (HashMap<String, Object>) config
                        .getConfigurationSection("powerups." + key).getValues(
                                false);
                final Map<String, Object> temp_map = new HashMap<String, Object>();
                for (final String kkey : map2.keySet()) {
                    // kkey e.g. "dmg_receive"
                    if ("item".equals(kkey)) {
                        temp_map.put(kkey, String.valueOf(map2.get(kkey)));
                        debug.i(key + " => " + kkey + " => "
                                + map2.get(kkey));
                    } else {
                        debug.i(key + " => " + kkey + " => "
                                + parseList(map3.values()));
                        map3 = (HashMap<String, Object>) config
                                .getConfigurationSection(
                                        "powerups." + key + "." + kkey)
                                .getValues(false);
                        temp_map.put(kkey, map3);
                    }
                }
                powerups.put(key, temp_map);
            }
        }

        if (powerups.size() < 1) {
            return;
        }

        final String pu = arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE, "off");

        final String[] ss = pu.split(":");
        if (pu.startsWith("death") || pu.startsWith("time")) {
            powerupDiff = Integer.parseInt(ss[1]);
            usesPowerups = new Powerups(powerups);
        } else {
            PVPArena.instance.getLogger().warning("error activating powerup module");
        }

        config.options().copyDefaults(true);
    }

    @Override
    public void displayInfo(final CommandSender player) {
        player.sendMessage("usage: "
                + StringParser.colorVar(usesPowerups != null)
                + "("
                + StringParser.colorVar(arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE))
                + ")");
    }

    /**
     * drop an item at a powerup spawn point
     *
     * @param item the item to drop
     */
    void dropItemOnSpawn(final Material item) {
        debug.i("calculating item spawn location");
        final Set<PALocation> locs = SpawnManager.getSpawnsContaining(arena, "powerup");
        if (locs.size() < 1) {
            PVPArena.instance.getLogger().warning("No valid powerup spawns found!");
            return;
        }
        int pos = new Random().nextInt(locs.size());
        for (final PALocation loc : locs) {
            if (--pos > 0) {
                continue;
            }
            final Location aim = loc.toLocation().add(0, 1, 0);
            debug.i("dropping item on spawn: " + aim);
            mark(Bukkit.getWorld(arena.getWorld()).dropItem(aim, new ItemStack(item, 1)));
            break;
        }

    }

    @Override
    public boolean hasSpawn(final String s) {
        return s.toLowerCase().startsWith("powerup");
    }

    private final String POWERUPSTRING = ChatColor.RED + "Power\nUp";

    private void mark(final Item drop) {
        final ItemMeta meta = drop.getItemStack().getItemMeta();

        meta.setDisplayName(POWERUPSTRING);
        drop.getItemStack().setItemMeta(meta);
    }

    private boolean isPowerup(final ItemStack item) {
        if (!item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getDisplayName().equals(POWERUPSTRING);
    }

    @Override
    public void onEntityDamageByEntity(final Player attacker,
                                       final Player defender, final EntityDamageByEntityEvent event) {
        if (usesPowerups != null) {
            debug.i("committing powerup triggers", attacker);
            debug.i("committing powerup triggers", defender);
            Powerup p = usesPowerups.puActive.get(attacker);
            if (p != null && p.canBeTriggered()) {

                p.commit(attacker, defender, event, true);
            }
            p = usesPowerups.puActive.get(defender);
            if (p != null && p.canBeTriggered()) {
                p.commit(attacker, defender, event, false);
            }
        }

    }

    @Override
    public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
        if (usesPowerups != null && event.getEntity() instanceof Player) {
            debug.i("regaining health");
            final Powerup p = usesPowerups.puActive.get(event.getEntity());
            if (p != null) {
                if (p.canBeTriggered()) {
                    if (p.isEffectActive(PowerupType.HEAL)) {
                        event.setCancelled(true);
                        p.commit(event);
                    }
                }
            }

        }
    }

    @EventHandler
    public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
        if (!arena.equals(ap.getArena())) {
            return;
        }
        if (usesPowerups != null && isPowerup(event.getItem().getItemStack())) {
            debug.i("onPlayerPickupItem: fighting player", player);
            debug.i("item: " + event.getItem().getItemStack().getType(), player);
            for (final Powerup p : usesPowerups.puTotal) {
                debug.i("is it " + p.item + "?", player);
                if (event.getItem().getItemStack().getType() == p.item) {
                    debug.i("yes!", player);
                    final Powerup newP = new Powerup(p);
                    if (usesPowerups.puActive.containsKey(player)) {
                        usesPowerups.puActive.get(player).deactivate(player);
                        usesPowerups.puActive.remove(player);
                    }
                    usesPowerups.puActive.put(player, newP);
                    arena.broadcast(Language.parse(MSG.MODULE_POWERUPS_PLAYER,
                            player.getName(), newP.name));
                    event.setCancelled(true);
                    event.getItem().remove();
                    if (newP.canBeTriggered()) {
                        newP.activate(player); // activate for the first time
                    }

                    return;
                }
            }
        }
    }

    @Override
    public void onPlayerVelocity(final PlayerVelocityEvent event) {
        debug.i("inPlayerVelocity: fighting player", event.getPlayer());
        if (usesPowerups != null) {
            final Powerup p = usesPowerups.puActive.get(event.getPlayer());
            if (p != null) {
                if (p.canBeTriggered()) {
                    if (p.isEffectActive(PowerupType.JUMP)) {
                        p.commit(event);
                    }
                }
            }
        }
    }

    /**
     * turn a collection of objects into a comma separated string
     *
     * @param values the collection
     * @return the comma separated string
     */
    String parseList(final Collection<Object> values) {
        String s = "";
        for (final Object o : values) {
            if (!"".equals(s)) {
                s += ",";
            }
            try {
                s += String.valueOf(o);
                debug.i("a");
            } catch (final Exception e) {
                debug.i("b");
                s += o.toString();
            }
        }
        return s;
    }

    @EventHandler
    public void parseMove(final PlayerMoveEvent event) {

        // debug.i("onPlayerMove: fighting player!");
        if (usesPowerups != null) {
            //debug.i("parsing move");
            final Powerup p = usesPowerups.puActive.get(event.getPlayer());
            if (p != null) {
                if (p.canBeTriggered()) {
                    if (p.isEffectActive(PowerupType.FREEZE)) {
                        debug.i("freeze in effect, cancelling!", event.getPlayer());
                        event.setCancelled(true);
                    }
                    if (p.isEffectActive(PowerupType.SPRINT)) {
                        debug.i("sprint in effect, sprinting!", event.getPlayer());
                        event.getPlayer().setSprinting(true);
                    }
                    if (p.isEffectActive(PowerupType.SLIP)) {
                        //TODO add slippery effect!
                    }
                }
            }
        }
    }

    /**
     * powerup tick, tick each arena that uses powerups
     */
    protected void powerupTick() {
        if (usesPowerups != null) {
            usesPowerups.tick();
        }
    }

    @Override
    public void reset(final boolean force) {
        if (SPAWN_ID > -1) {
            Bukkit.getScheduler().cancelTask(SPAWN_ID);
        }
        SPAWN_ID = -1;
        if (usesPowerups != null) {
            usesPowerups.puActive.clear();
        }
    }

    @Override
    public void parseStart() {
        if (usesPowerups != null) {
            final String pu = arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE);
            final String[] ss = pu.split(":");
            if (pu.startsWith("time")) {
                // arena.powerupTrigger = "time";
                powerupDiff = Integer.parseInt(ss[1]);
            } else {
                return;
            }

            debug.i("using powerups : "
                    + arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE) + " : "
                    + powerupDiff);
            if (powerupDiff > 0) {
                debug.i("powerup time trigger!");
                powerupDiff *= 20; // calculate ticks to seconds
                // initiate autosave timer
                SPAWN_ID = Bukkit
                        .getServer()
                        .getScheduler()
                        .scheduleSyncRepeatingTask(PVPArena.instance,
                                new PowerupRunnable(this), powerupDiff,
                                powerupDiff);
            }
        }
    }
}
