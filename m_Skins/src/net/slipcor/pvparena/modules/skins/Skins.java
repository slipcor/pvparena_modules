package net.slipcor.pvparena.modules.skins;

import me.desmin88.mobdisguise.api.MobDisguiseAPI;
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
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Skins extends ArenaModule {
    private static boolean mdHandler = false;
    private static boolean dcHandler = false;
    private static boolean enabled = false;
    private DisguiseCraftAPI dapi = null;

    private final Set<String> disguised = new HashSet<String>();

    public Skins() {
        super("Skins");
    }

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    @Override
    public boolean checkCommand(String s) {
        return s.equals("!sk") || s.startsWith("skins");
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("skins");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!sk");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        if (arena == null) {
            return result;
        }
        for (String team : arena.getTeamNames()) {
            result.define(new String[]{team});
        }
        for (ArenaClass aClass : arena.getClasses()) {
            result.define(new String[]{aClass.getName()});
        }
        return result;
    }

    @Override
    public void commitCommand(CommandSender sender, String[] args) {
        // !sk [teamname] [skin] |
        // !sk [classname] [skin] |
        if (!PVPArena.hasAdminPerms(sender)
                && !(PVPArena.hasCreatePerms(sender, arena))) {
            arena.msg(
                    sender,
                    Language.parse(MSG.ERROR_NOPERM,
                            Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{3})) {
            return;
        }

        ArenaClass c = arena.getClass(args[1]);

        if (c == null) {
            ArenaTeam team = arena.getTeam(args[1]);
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
    public void configParse(YamlConfiguration config) {
        if (config.get("skins") == null) {
            for (ArenaTeam team : arena.getTeams()) {
                String sName = team.getName();
                config.addDefault("skins." + sName, "Herobrine");
            }
            config.options().copyDefaults(true);
        }
    }

    @Override
    public void parseRespawn(Player player, ArenaTeam team, DamageCause lastDamageCause, Entity damager) {
        if (dcHandler) {
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
        if (Bukkit.getServer().getPluginManager().getPlugin("DisguiseCraft") != null) {
            dcHandler = true;
            m = MSG.MODULE_SKINS_DISGUISECRAFT;

            dapi = DisguiseCraft.getAPI();
        } else if (Bukkit.getServer().getPluginManager().getPlugin("MobDisguise") != null) {
            mdHandler = true;
            m = MSG.MODULE_SKINS_MOBDISGUISE;
        }

        enabled = true;

        Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(m));
    }

    private void printHelp(Arena arena, CommandSender sender) {
        arena.msg(sender, "/pa [arenaname] !sk [teamname]  | show team disguise");
        arena.msg(sender, "/pa [arenaname] !sk [teamname] [skin] | set team disguise");
        arena.msg(sender, "/pa [arenaname] !sk [classname]  | show class disguise");
        arena.msg(sender, "/pa [arenaname] !sk [classname] [skin] | set class disguise");
    }

    @Override
    public void tpPlayerToCoordName(final Player player, String place) {
        if (dcHandler) {
            dapi = DisguiseCraft.getAPI();
        }

        if (!dcHandler && !mdHandler) {
            ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
            if (team != null) {
                final ItemStack is = new ItemStack(Material.SKULL_ITEM, 1);
                String disguise = (String) arena.getArenaConfig().getUnsafe("skins." + team.getName());
                if (disguise == null) {
                    return;
                }
                if (disguise.equals("SKELETON")) {
                    is.setDurability((short) 0);
                } else if (disguise.equals("WITHER_SKELETON")) {
                    is.setDurability((short) 1);
                } else if (disguise.equals("ZOMBIE")) {
                    is.setDurability((short) 2);
                } else if (disguise.equals("PLAYER")) {
                    is.setDurability((short) 3);
                } else if (disguise.equals("CREEPER")) {
                    is.setDurability((short) 4);
                } else {
                    is.setDurability((short) 3);
                    SkullMeta sm = (SkullMeta) is.getItemMeta();
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

        ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
        if (team == null) {
            return;
        }
        String disguise = (String) arena.getArenaConfig().getUnsafe("skins." + team.getName());

        ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

        if (ap.getArenaClass() != null && (disguise == null || disguise.equals("none"))) {
            disguise = (String) arena.getArenaConfig().getUnsafe("skins." + ap.getArenaClass().getName());
        }

        if (disguise == null || disguise.equals("none")) {
            return;
        }

        if (dcHandler) {
            DisguiseType t = DisguiseType.fromString(disguise);
            Disguise d = new Disguise(dapi.newEntityID(), disguise, t == null ? DisguiseType.Player : t);
            if (dapi.isDisguised(player)) {
                dapi.undisguisePlayer(player);
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new DisguiseRunnable(player, d), 3L);

        } else if (mdHandler) {
            if (!MobDisguiseAPI.disguisePlayer(player, disguise)) {
                if (!MobDisguiseAPI.disguisePlayerAsPlayer(player, disguise)) {
                    PVPArena.instance.getLogger().warning("Unable to disguise " + player.getName() + " as " + disguise);
                }
            }
        }

        disguised.add(player.getName());
    }

    @Override
    public void unload(Player player) {
        if (dcHandler) {
            dapi.undisguisePlayer(player);
        } else if (mdHandler) {
            MobDisguiseAPI.undisguisePlayer(player);
        }
        disguised.remove(player.getName());
    }
}
