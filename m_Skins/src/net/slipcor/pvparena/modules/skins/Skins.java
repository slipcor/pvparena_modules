package net.slipcor.pvparena.modules.skins;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Skins extends ArenaModule {
    private static boolean dcHandler;
    private static boolean ldHandler;
    private static boolean enabled;
    private DisguiseCraftAPI dapi;

    private final Set<String> disguised = new HashSet<>();

    public Skins() {
        super("Skins");
    }

    @Override
    public String version() {
        return "v1.3.2.61";
    }

    @Override
    public boolean checkCommand(final String s) {
        return "!sk".equals(s) || s.startsWith("skins");
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("skins");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!sk");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        if (arena == null) {
            return result;
        }
        for (final String team : arena.getTeamNames()) {
            result.define(new String[]{team});
        }
        for (final ArenaClass aClass : arena.getClasses()) {
            result.define(new String[]{aClass.getName()});
        }
        return result;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        // !sk [teamname] [skin] |
        // !sk [classname] [skin] |
        if (!PVPArena.hasAdminPerms(sender)
                && !PVPArena.hasCreatePerms(sender, arena)) {
            arena.msg(
                    sender,
                    Language.parse(MSG.ERROR_NOPERM,
                            Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{3})) {
            return;
        }

        final ArenaClass c = arena.getClass(args[1]);

        if (c == null) {
            final ArenaTeam team = arena.getTeam(args[1]);
            if (team != null) {
                // !sk [teamname] [skin]

                if (args.length == 2) {
                    arena.msg(sender, Language.parse(
                            MSG.MODULE_SKINS_SHOWTEAM,
                            team.getColoredName(),
                            (String) arena.getArenaConfig().getUnsafe("skins." + team.getName())));
                    return;
                }

                arena.getArenaConfig().setManually("skins." + team.getName(), args[2]);
                arena.getArenaConfig().save();
                arena.msg(sender, Language.parse(MSG.SET_DONE, team.getName(), args[2]));

                return;
            }
            // no team AND no class!

            arena.msg(sender,
                    Language.parse(MSG.ERROR_CLASS_NOT_FOUND, args[1]));
            arena.msg(sender, Language.parse(MSG.ERROR_TEAMNOTFOUND, args[1]));
            printHelp(arena, sender);
            return;
        }
        // !sk [classname] | show
        // !bg [classname] [skin]

        if (args.length == 2) {
            arena.msg(
                    sender,
                    Language.parse(MSG.MODULE_SKINS_SHOWCLASS,
                            (String) arena.getArenaConfig().getUnsafe("skins." + c.getName())));
            return;
        }

        arena.getArenaConfig().setManually("skins." + c.getName(), args[2]);
        arena.getArenaConfig().save();
        arena.msg(sender, Language.parse(MSG.SET_DONE, c.getName(), args[2]));

    }

    @Override
    public void configParse(final YamlConfiguration config) {
        if (config.get("skins") == null) {
            for (final ArenaTeam team : arena.getTeams()) {
                final String sName = team.getName();
                config.addDefault("skins." + sName, "Herobrine");
            }
            config.options().copyDefaults(true);
        }
    }

    @Override
    public void parseRespawn(final Player player, final ArenaTeam team, final DamageCause lastDamageCause, final Entity damager) {
        if (ldHandler) {
            if (DisguiseAPI.isDisguised(player)) {
                DisguiseAPI.undisguiseToAll(player);
            }
        } else if (dcHandler) {
            if (dapi.isDisguised(player)) {
                dapi.undisguisePlayer(player);
            }
        }
    }

    @Override
    public void onThisLoad() {
        if (arena == null) {
            return;
        }
        if (enabled || arena.getArenaConfig().getBoolean(CFG.MODULES_SKINS_VANILLA)) {
            enabled = true;
            return;
        }
        MSG m = MSG.MODULE_SKINS_NOMOD;
        if (Bukkit.getServer().getPluginManager().getPlugin("LibsDisguises") != null) {
            ldHandler = true;
            m = MSG.MODULE_SKINS_LIBSDISGUISE;
        } else if (Bukkit.getServer().getPluginManager().getPlugin("DisguiseCraft") != null) {
            dcHandler = true;
            m = MSG.MODULE_SKINS_DISGUISECRAFT;

            dapi = DisguiseCraft.getAPI();
        }

        enabled = true;

        Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(m));
    }

    private void printHelp(final Arena arena, final CommandSender sender) {
        arena.msg(sender, "/pa [arenaname] !sk [teamname]  | show team disguise");
        arena.msg(sender, "/pa [arenaname] !sk [teamname] [skin] | set team disguise");
        arena.msg(sender, "/pa [arenaname] !sk [classname]  | show class disguise");
        arena.msg(sender, "/pa [arenaname] !sk [classname] [skin] | set class disguise");
    }

    @Override
    public void tpPlayerToCoordName(final Player player, final String place) {
        if (dcHandler) {
            dapi = DisguiseCraft.getAPI();
        }

        if (!dcHandler && !ldHandler) {
            final ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
            if (team != null) {
                final ItemStack is = new ItemStack(Material.SKULL_ITEM, 1);
                final String disguise = (String) arena.getArenaConfig().getUnsafe("skins." + team.getName());
                if (disguise == null) {
                    return;
                }
                if ("SKELETON".equals(disguise)) {
                    is.setDurability((short) 0);
                } else if ("WITHER_SKELETON".equals(disguise)) {
                    is.setDurability((short) 1);
                } else if ("ZOMBIE".equals(disguise)) {
                    is.setDurability((short) 2);
                } else if ("PLAYER".equals(disguise)) {
                    is.setDurability((short) 3);
                } else if ("CREEPER".equals(disguise)) {
                    is.setDurability((short) 4);
                } else {
                    is.setDurability((short) 3);
                    final SkullMeta sm = (SkullMeta) is.getItemMeta();
                    sm.setOwner(disguise);
                    is.setItemMeta(sm);
                }

                class TempRunnable implements Runnable {
                    @Override
                    public void run() {
                        player.getInventory().setHelmet(is);
                    }

                }
                Bukkit.getScheduler().runTaskLater(PVPArena.instance, new TempRunnable(), 5L);
            }
            return;
        }

        if (disguised.contains(player.getName()) || !arena.hasPlayer(player)) {
            return;
        }

        final ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
        if (team == null) {
            return;
        }
        String disguise = (String) arena.getArenaConfig().getUnsafe("skins." + team.getName());

        final ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

        if (ap.getArenaClass() != null && (disguise == null || "none".equals(disguise))) {
            disguise = (String) arena.getArenaConfig().getUnsafe("skins." + ap.getArenaClass().getName());
        }

        if (disguise == null || "none".equals(disguise)) {
            return;
        }

        if (ldHandler) {
            try {
                me.libraryaddict.disguise.disguisetypes.DisguiseType type =
                        me.libraryaddict.disguise.disguisetypes.DisguiseType.getType(EntityType.fromName(disguise));
                try {
                    MobDisguise md = new MobDisguise(type, false);
                } catch (Exception e) {
                    MiscDisguise md = new MiscDisguise(type);
                }
            } catch (Exception e) {
                PlayerDisguise pd = new PlayerDisguise(disguise);
                if (DisguiseAPI.isDisguised(player)) {
                    DisguiseAPI.undisguiseToAll(player);
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new LibsDisguiseRunnable(player, pd), 3L);
            }
        } else if (dcHandler) {
            final DisguiseType t = DisguiseType.fromString(disguise);
            final Disguise d = new Disguise(dapi.newEntityID(), disguise, t == null ? DisguiseType.Player : t);
            if (dapi.isDisguised(player)) {
                dapi.undisguisePlayer(player);
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new DisguiseRunnable(player, d), 3L);

        }

        disguised.add(player.getName());
    }

    @Override
    public void unload(final Player player) {
        if (ldHandler) {
            DisguiseAPI.undisguiseToAll(player);
        } else if (dcHandler) {
            dapi.undisguisePlayer(player);
        }
        disguised.remove(player.getName());
    }
}
