package net.slipcor.pvparena.modules.bettergears;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;

public class BetterGears extends ArenaModule {
    private static Map<String, String> defaultColors;
    private Debug debug = new Debug(600);

    private Map<ArenaTeam, Short[]> colorMap = null;
    private Map<ArenaClass, Short> levelMap = null;

    public BetterGears() {
        super("BetterGears");
        debug = new Debug(401);
    }

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    @Override
    public boolean checkCommand(String s) {
        return s.equals("!bg") || s.startsWith("bettergear");
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("bettergears");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!bg");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        if (arena == null) {
            return result;
        }
        for (String team : arena.getTeamNames()) {
            result.define(new String[]{team, "color"});
        }
        for (ArenaClass aClass : arena.getClasses()) {
            result.define(new String[]{aClass.getName(), "level"});
        }
        return result;
    }

    @Override
    public void commitCommand(CommandSender sender, String[] args) {
        // !bg [teamname] | show
        // !bg [teamname] color <R> <G> <B> | set color
        // !bg [classname] | show
        // !bg [classname] level <level> | protection level
        if (!PVPArena.hasAdminPerms(sender)
                && !(PVPArena.hasCreatePerms(sender, arena))) {
            arena.msg(
                    sender,
                    Language.parse(MSG.ERROR_NOPERM,
                            Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2,
                4, 6})) {
            return;
        }

        ArenaClass c = arena.getClass(args[1]);

        if (c == null) {
            ArenaTeam team = arena.getTeam(args[1]);
            if (team != null) {
                // !bg [teamname] | show
                // !bg [teamname] color <R> <G> <B> | set color

                if (args.length == 2) {
                    arena.msg(sender, Language.parse(
                            MSG.MODULE_BETTERGEARS_SHOWTEAM,
                            team.getColoredName(),
                            String.valueOf(getColorMap().get(team))));
                    return;
                }

                if ((args.length != 6) || !args[2].equalsIgnoreCase("color")) {
                    printHelp(sender);
                    return;
                }

                try {
                    Short[] rgb = new Short[3];
                    rgb[0] = Short.parseShort(args[3]);
                    rgb[1] = Short.parseShort(args[4]);
                    rgb[2] = Short.parseShort(args[5]);
                    arena.getArenaConfig().setManually(
                            "modules.bettergears.colors." + team.getName(),
                            StringParser.joinArray(rgb, ","));
                    arena.getArenaConfig().save();
                    getColorMap().put(team, rgb);
                    arena.msg(sender, Language.parse(
                            MSG.MODULE_BETTERGEARS_TEAMDONE,
                            team.getColoredName(), args[3]));
                } catch (Exception e) {
                    arena.msg(sender,
                            Language.parse(MSG.ERROR_NOT_NUMERIC, args[3]));
                }

                return;
            }
            // no team AND no class!

            arena.msg(sender,
                    Language.parse(MSG.ERROR_CLASS_NOT_FOUND, args[1]));
            arena.msg(sender, Language.parse(MSG.ERROR_TEAMNOTFOUND, args[1]));
            printHelp(sender);
            return;
        }
        // !bg [classname] | show
        // !bg [classname] level <level> | protection level

        if (args.length == 2) {
            arena.msg(
                    sender,
                    Language.parse(MSG.MODULE_BETTERGEARS_SHOWCLASS,
                            c.getName(), String.valueOf(getLevelMap().get(c))));
            return;
        }

        if ((args.length != 4) || !args[2].equalsIgnoreCase("level")) {
            printHelp(sender);
            return;
        }

        try {
            short l = Short.parseShort(args[3]);
            arena.getArenaConfig().setManually(
                    "modules.bettergears.levels." + c.getName(), l);
            arena.getArenaConfig().save();
            getLevelMap().put(c, l);
            arena.msg(
                    sender,
                    Language.parse(MSG.MODULE_BETTERGEARS_CLASSDONE,
                            c.getName(), args[3]));
        } catch (Exception e) {
            arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[3]));
        }
    }

    @Override
    public void configParse(YamlConfiguration cfg) {
        for (ArenaClass c : arena.getClasses()) {
            if (cfg.get("modules.bettergears.levels." + c.getName()) == null)
                cfg.set("modules.bettergears.levels." + c.getName(),
                        parseClassNameToDefaultProtection(c.getName()));
        }
        for (ArenaTeam t : arena.getTeams()) {
            if (cfg.get("modules.bettergears.colors." + t.getName()) == null)
                cfg.set("modules.bettergears.colors." + t.getName(),
                        parseTeamColorStringToRGB(t.getColor().name()));
        }
        if (getColorMap().isEmpty()) {
            setup();
        }
    }

    @Override
    public void displayInfo(CommandSender sender) {
        for (ArenaTeam team : colorMap.keySet()) {
            Short[] colors = colorMap.get(team);
            sender.sendMessage(team.getName() + ": " +
                    StringParser.joinArray(colors, ""));
        }

        for (ArenaClass aClass : levelMap.keySet()) {
            sender.sendMessage(aClass.getName() + ": " + levelMap.get(aClass));
        }
    }

    void equip(ArenaPlayer ap) {

        short r;
        short g;
        short b;

        if (getArena().isFreeForAll()) {
            r = (short) ((new Random()).nextInt(256));
            g = (short) ((new Random()).nextInt(256));
            b = (short) ((new Random()).nextInt(256));
        } else {
            r = getColorMap().get(ap.getArenaTeam())[0];
            g = getColorMap().get(ap.getArenaTeam())[1];
            b = getColorMap().get(ap.getArenaTeam())[2];
        }


        ItemStack[] isArmor = new ItemStack[4];
        isArmor[0] = new ItemStack(Material.LEATHER_HELMET, 1);
        isArmor[1] = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        isArmor[2] = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        isArmor[3] = new ItemStack(Material.LEATHER_BOOTS, 1);

        Color c = Color.fromBGR(b, g, r);
        for (int i = 0; i < 4; i++) {
            LeatherArmorMeta lam = (LeatherArmorMeta) isArmor[i].getItemMeta();

            lam.setColor(c);
            isArmor[i].setItemMeta(lam);
        }

        Short s = getLevelMap().get(ap.getArenaClass());

        if (s == null) {
            String autoClass = getArena().getArenaConfig().getString(CFG.READY_AUTOCLASS);
            ArenaClass ac = getArena().getClass(autoClass);
            s = getLevelMap().get(ac);
        }


        isArmor[0].addUnsafeEnchantment(
                Enchantment.PROTECTION_ENVIRONMENTAL, s);
        isArmor[0]
                .addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, s);
        isArmor[0].addUnsafeEnchantment(Enchantment.PROTECTION_FALL, s);
        isArmor[0].addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, s);
        isArmor[0]
                .addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, s);

        if (arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERGEARS_HEAD) &&
                (!arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERGEARS_ONLYIFLEATHER) ||
                        ap.get().getInventory().getHelmet() != null &&
                                ap.get().getInventory().getHelmet().getType().name().contains("LEATHER"))) {
            ap.get().getInventory().setHelmet(isArmor[0]);
        }
        if (arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERGEARS_CHEST) &&
                (!arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERGEARS_ONLYIFLEATHER) ||
                        ap.get().getInventory().getChestplate() != null &&
                                ap.get().getInventory().getChestplate().getType().name().contains("LEATHER"))) {
            ap.get().getInventory().setChestplate(isArmor[1]);
        }
        if (arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERGEARS_LEG) &&
                (!arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERGEARS_ONLYIFLEATHER) ||
                        ap.get().getInventory().getLeggings() != null &&
                                ap.get().getInventory().getLeggings().getType().name().contains("LEATHER"))) {
            ap.get().getInventory().setLeggings(isArmor[2]);
        }
        if (arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERGEARS_FOOT) &&
                (!arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERGEARS_ONLYIFLEATHER) ||
                        ap.get().getInventory().getBoots() != null &&
                                ap.get().getInventory().getBoots().getType().name().contains("LEATHER"))) {
            ap.get().getInventory().setBoots(isArmor[3]);
        }
    }

    private Map<ArenaTeam, Short[]> getColorMap() {
        if (colorMap == null) {
            colorMap = new HashMap<ArenaTeam, Short[]>();
        }
        return colorMap;
    }

    private Map<ArenaClass, Short> getLevelMap() {
        if (levelMap == null) {
            levelMap = new HashMap<ArenaClass, Short>();
        }
        return levelMap;
    }

    @Override
    public void initiate(Player player) {
        if (getColorMap().isEmpty()) {
            setup();
        }
    }

    @Override
    public void lateJoin(Player player) {
        equip(ArenaPlayer.parsePlayer(player.getName()));
    }

    @Override
    public void reset(boolean force) {
        getColorMap().clear();
        getLevelMap().clear();
    }

    @Override
    public void parseStart() {

        if (getColorMap().isEmpty()) {
            setup();
        }
        // debug();

        for (ArenaPlayer ap : arena.getFighters()) {
            equip(ap);
        }
    }

    private Short parseClassNameToDefaultProtection(String name) {
        if (name.equals("Tank")) {
            return 10;
        } else if (name.equals("Swordsman")) {
            return 4;
        } else if (name.equals("Ranger")) {
            return 1;
        } else if (name.equals("Pyro")) {
            return 1;
        }
        return null;
    }

    public void parseRespawn(Player player, ArenaTeam team, DamageCause cause,
                             Entity damager) {
        ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
        if (arena.getArenaConfig().getBoolean(CFG.PLAYER_REFILLINVENTORY)) {
            new EquipRunnable(ap, this);
        }
    }

    private Short[] parseRGBToShortArray(Object o) {
        Short[] result = new Short[3];
        result[0] = 255;
        result[1] = 255;
        result[2] = 255;

        debug.i("parsing RGB:");
        debug.i(String.valueOf(o));

        if (!(o instanceof String)) {
            return result;
        }

        String s = (String) o;

        if (s.equals("") || !s.contains(",")
                || s.split(",").length < 3) {
            return result;
        }

        try {
            String[] split = s.split(",");
            result[0] = Short.parseShort(split[0]);
            result[1] = Short.parseShort(split[1]);
            result[2] = Short.parseShort(split[2]);
        } catch (Exception e) {
        }
        return result;
    }

    private String parseTeamColorStringToRGB(String name) {
        if (defaultColors == null) {
            defaultColors = new HashMap<String, String>();

            defaultColors.put("BLACK", "0,0,0");
            defaultColors.put("DARK_BLUE", "0,0,153");
            defaultColors.put("DARK_GREEN", "0,68,0");
            defaultColors.put("DARK_AQUA", "0,153,153");
            defaultColors.put("DARK_RED", "153,0,0");

            defaultColors.put("DARK_PURPLE", "153,0,153");
            defaultColors.put("GOLD", "0,0,0");
            defaultColors.put("GRAY", "153,153,153");
            defaultColors.put("DARK_GRAY", "68,68,68");
            defaultColors.put("BLUE", "0,0,255");
            defaultColors.put("GREEN", "0,255,0");

            defaultColors.put("AQUA", "0,255,255");
            defaultColors.put("RED", "255,0,0");
            defaultColors.put("LIGHT_PURPLE", "255,0,255");
            defaultColors.put("PINK", "255,0,255");
            defaultColors.put("YELLOW", "255,255,0");
            defaultColors.put("WHITE", "255,255,255");
        }

        String s = defaultColors.get(name);
        debug.i("team " + name + " : " + s);
        return s == null ? "255,255,255" : s;
    }

    private void printHelp(CommandSender sender) {
        arena.msg(sender, "/pa [arenaname] !bg [teamname]  | show team color");
        arena.msg(sender,
                "/pa [arenaname] !bg [teamname] color " + ChatColor.RED + "<R> " + ChatColor.GREEN + "<G> " + ChatColor.BLUE + "<B>" + ChatColor.RESET + " | set color");
        arena.msg(sender,
                "/pa [arenaname] !bg [classname] | show protection level");
        arena.msg(sender,
                "/pa [arenaname] !bg [classname] level <level> | set protection level");
    }

    private void setup() {
        debug.i("Setting up BetterGears");

        for (ArenaClass c : arena.getClasses()) {
            Short s = 0;
            try {
                s = Short
                        .valueOf(String.valueOf(arena.getArenaConfig()
                                .getUnsafe(
                                        "modules.bettergears.levels."
                                                + c.getName())));
                debug.i(c.getName() + " : " + s);
            } catch (Exception e) {
            }
            getLevelMap().put(c, s);
        }

        for (ArenaTeam t : arena.getTeams()) {
            Short[] s = parseRGBToShortArray(arena.getArenaConfig().getUnsafe(
                    "modules.bettergears.colors." + t.getName()));
            getColorMap().put(t, s);
            debug.i(t.getName() + " : " + StringParser.joinArray(s, ","));
        }
    }
}
