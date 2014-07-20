package net.slipcor.pvparena.modules.betterkillstreaks;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.events.PADeathEvent;
import net.slipcor.pvparena.events.PAKillEvent;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class BetterKillstreaks extends ArenaModule implements Listener {
    public BetterKillstreaks() {
        super("BetterKillstreaks");
    }

    private final Map<String, Integer> streaks = new HashMap<String, Integer>();

    private boolean setup = false;

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    @Override
    public boolean checkCommand(String s) {
        return s.equals("!bk") || s.startsWith("betterkillstreaks");
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("betterkillstreaks");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!bk");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"{int}", "potion"});
        result.define(new String[]{"{int}", "clear"});
        result.define(new String[]{"{int}", "items"});
        return result;
    }

    @Override
    public void commitCommand(CommandSender sender, String[] args) {

        if (!PVPArena.hasAdminPerms(sender)
                && !(PVPArena.hasCreatePerms(sender, arena))) {
            arena.msg(sender,
                    Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2, 3, 4, 5, 6})) {
            return;
        }

        Integer level;

        try {
            level = Integer.parseInt(args[1]);
        } catch (Exception e) {
            arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[1]));
            return;
        }
        ConfigurationSection cs = arena.getArenaConfig().getYamlConfiguration().getConfigurationSection("modules.betterkillstreaks.definitions");
        if (args.length < 3) {
            // !bk [level] | show level content
            if (cs.get("d" + level) == null) {
                arena.msg(sender, "--------");
            } else {
                cs = cs.getConfigurationSection("d" + level);
                arena.msg(sender, "items: " + cs.getString("items", "NONE"));
                arena.msg(sender, "potion: " + cs.getString("potion", "NONE"));
            }
            return;
        }

        if (args[2].equals("clear")) {
            // !bk [level] clear | clear the definition
            cs.set("d" + level, null);
            arena.msg(sender, "level " + level + " removed!");
            arena.getArenaConfig().save();
            return;
        }

        if (args[2].equals("items")) {
            // !bk [level] items | set the items to your inventory
            ItemStack[] items = ((Player) sender).getInventory().getContents().clone();
            String val = StringParser.getStringFromItemStacks(items);
            cs.set("d" + level + ".items", val);
            arena.getArenaConfig().save();
            arena.msg(sender, "Items of level " + level + " set to: " + val);
            return;
        }

        if (!args[2].equals("potion")) {
            arena.msg(sender, "/pa [arenaname] !bk [level] | list level settings");
            arena.msg(sender, "/pa [arenaname] !bk [level] clear | clear level settings");
            arena.msg(sender, "/pa [arenaname] !bk [level] potion [type] {amp} {duration} | add potion effect");
            arena.msg(sender, "/pa [arenaname] !bk [level] items | set the level's items");

            return;
        }

        // !bk [level] potion [def]| add potioneffect definition

        HashSet<PotionEffect> ape = new HashSet<PotionEffect>();


        // 0   1           2      3     4    5
        // !bc [level] potion    [def] [amp] [dur]| add
        PotionEffectType pet = null;

        for (PotionEffectType x : PotionEffectType.values()) {
            if (x == null) {
                continue;
            }
            if (x.getName().equalsIgnoreCase(args[3])) {
                pet = x;
                break;
            }
        }

        if (pet == null) {
            arena.msg(sender, Language.parse(MSG.ERROR_POTIONEFFECTTYPE_NOTFOUND, args[3]));
            return;
        }

        int amp = 1;

        if (args.length >= 5) {
            try {
                amp = Integer.parseInt(args[4]);
            } catch (Exception e) {
                arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[4]));
                return;
            }
        }

        int duration = 2400;

        if (args.length >= 6) {
            try {
                duration = Integer.parseInt(args[5]);
            } catch (Exception e) {
                arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[5]));
                return;
            }
        }

        ape.add(new PotionEffect(pet, duration, amp));

        String val = parsePotionEffectsToString(ape);

        cs.set("d" + level + ".potion", val);

        arena.getArenaConfig().save();
        arena.msg(sender, "Level " + level + " now has potion effect: " + val);
    }

    @Override
    public void configParse(YamlConfiguration config) {
        if (!setup) {
            Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
            setup = true;
        }
    }

    @Override
    public void parseStart() {
        streaks.clear();
    }

    @Override
    public void reset(boolean force) {
        streaks.clear();
    }

    @EventHandler
    public void onPlayerDeath(PADeathEvent event) {
        streaks.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerKill(PAKillEvent event) {
        int value;
        if (streaks.containsKey(event.getPlayer().getName())) {
            value = streaks.get(event.getPlayer().getName()) + 1;

        } else {
            value = 1;
        }
        streaks.put(event.getPlayer().getName(), value);
        reward(event.getPlayer(), value);
    }

    private String parsePotionEffectsToString(Iterable<PotionEffect> ape) {
        Set<String> result = new HashSet<String>();
        for (PotionEffect pe : ape) {
            result.add(pe.getType().getName() + ":" + pe.getAmplifier());
        }
        return StringParser.joinSet(result, ",");
    }

    private Iterable<PotionEffect> parseStringToPotionEffects(String s) {
        HashSet<PotionEffect> spe = new HashSet<PotionEffect>();

        if (s == null || s.equals("none") || s.equals("")) {
            return spe;
        }

        String current = null;

        try {
            String[] ss = s.split(",");
            for (String sss : ss) {
                current = sss;

                String[] values = sss.split(":");

                PotionEffectType type = PotionEffectType.getByName(values[0].toUpperCase());

                int amp = values.length < 2 ? 1 : Integer.parseInt(values[1]);

                int duration = values.length < 3 ? 2400 : Integer.parseInt(values[2]);

                PotionEffect pe = new PotionEffect(type, duration, amp - 1);
                spe.add(pe);
            }
        } catch (Exception e) {
            PVPArena.instance.getLogger().warning("error while parsing POTION EFFECT DEFINITION \"" + s + "\" : " + current);
        }

        return spe;
    }


    private void reward(Player player, int value) {
        ConfigurationSection cs = arena.getArenaConfig().getYamlConfiguration().getConfigurationSection("modules.betterkillstreaks.definitions");
        for (String key : cs.getKeys(false)) {
            if (key.equals("d" + value)) {
                ItemStack[] items = StringParser.getItemStacksFromString(cs.getString("items", "AIR"));
                for (ItemStack item : items) {
                    player.getInventory().addItem(item);
                }

                String pot = cs.getString("d" + value, "���");

                if (!pot.equals("���")) {
                    for (PotionEffect pe : parseStringToPotionEffects(pot)) {
                        player.addPotionEffect(pe);
                    }
                }
            }
        }
    }
}
