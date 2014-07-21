package net.slipcor.pvparena.modules.chestfiller;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
import net.slipcor.pvparena.regions.CuboidRegion;
import net.slipcor.pvparena.regions.SphericRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChestFiller extends ArenaModule {
    public ChestFiller() {
        super("ChestFiller");
    }

    private boolean setup;

    @Override
    public String version() {
        return "v1.3.0.515";
    }

    @Override
    public boolean checkCommand(final String s) {
        return "!cf".equals(s) || s.startsWith("chestfiller");
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("chestfiller");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!cf");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"clear"});
        return result;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        // !cf clear | clear inventory definitions
        if (!PVPArena.hasAdminPerms(sender)
                && !PVPArena.hasCreatePerms(sender, arena)) {
            arena.msg(
                    sender,
                    Language.parse(MSG.ERROR_NOPERM,
                            Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2})) {
            return;
        }

        if (!"clear".equals(args[1])) {
            return;
        }

        arena.getArenaConfig().setManually("inventories", null);
        arena.getArenaConfig().save();

        sender.sendMessage(Language.parse(MSG.MODULE_CHESTFILLER_CLEAR));
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage("items: " + arena.getArenaConfig().getUnsafe("modules.chestfiller.cfitems"));
        sender.sendMessage("max: " + arena.getArenaConfig().getUnsafe("modules.chestfiller.cfmaxitems")
                + " | " +
                "min: " + arena.getArenaConfig().getUnsafe("modules.chestfiller.cfminitems"));

    }

    @Override
    public boolean needsBattleRegion() {
        return true;
    }

    @Override
    public void parseStart() {
        if (!setup) {
            if (arena.getArenaConfig().getUnsafe("modules.chestfiller") == null) {
                arena.getArenaConfig().setManually("modules.chestfiller.cfitems", "1");
                arena.getArenaConfig().setManually("modules.chestfiller.cfmaxitems", 5);
                arena.getArenaConfig().setManually("modules.chestfiller.cfminitems", 0);
                arena.getArenaConfig().save();
            }
            if (arena.getArenaConfig().getUnsafe("modules.chestfiller.clear") == null) {
                arena.getArenaConfig().setManually("modules.chestfiller.clear", false);
                arena.getArenaConfig().save();
            }
            setup = true;
        }

        final String items;
        try {
            items = (String) arena.getArenaConfig().getUnsafe("modules.chestfiller.cfitems");
        } catch (final Exception e) {
            return;
        }

        final boolean clear;
        try {
            clear = (Boolean) arena.getArenaConfig().getUnsafe("modules.chestfiller.clear");
        } catch (final Exception e) {
            return;
        }

        final int cmax = Integer.parseInt(String.valueOf(arena.getArenaConfig().getUnsafe("modules.chestfiller.cfmaxitems")));
        final int cmin = Integer.parseInt(String.valueOf(arena.getArenaConfig().getUnsafe("modules.chestfiller.cfminitems")));

        final ItemStack[] stacks = StringParser.getItemStacksFromString(items);

        if (stacks.length < 1) {
            return;
        }


        // ----------------------------------------

        if (!arena.getArenaConfig().getStringList("inventories", new ArrayList<String>()).isEmpty()) {

            final List<String> tempList = arena.getArenaConfig()
                    .getStringList("inventories", null);

            debug.i("reading inventories");

            for (final String s : tempList) {
                final Location loc = parseStringToLocation(s);

                fill(loc, clear, cmin, cmax, stacks);
            }

            return;
        }
        debug.i("NO inventories");

        final List<String> result = new ArrayList<String>();

        for (final ArenaRegion bfRegion : arena.getRegionsByType(RegionType.BATTLE)) {
            final PABlockLocation min = bfRegion.getShape().getMinimumLocation();
            final PABlockLocation max = bfRegion.getShape().getMaximumLocation();

            debug.i("min: " + min);
            debug.i("max: " + max);

            final World world = Bukkit.getWorld(max.getWorldName());


            int z;
            int y;
            int x;
            if (bfRegion.getShape() instanceof CuboidRegion) {
                debug.i("cube!");

                for (x = min.getX(); x <= max.getX(); x++) {
                    for (y = min.getY(); y <= max.getY(); y++) {
                        for (z = min.getZ(); z <= max.getZ(); z++) {
                            final Location loc = saveBlock(world, x, y, z);
                            if (loc == null) {
                                continue;
                            }
                            debug.i("loc not null: " + loc);
                            result.add(parseLocationToString(loc));
                        }
                    }
                }
            } else if (bfRegion.getShape() instanceof SphericRegion) {
                debug.i("sphere!");
                for (x = min.getX(); x <= max.getX(); x++) {
                    for (y = min.getY(); y <= max.getY(); y++) {
                        for (z = min.getZ(); z <= max.getZ(); z++) {
                            final Location loc = saveBlock(world, x, y, z);
                            if (loc == null) {
                                continue;
                            }
                            debug.i("loc not null: " + loc);
                            result.add(parseLocationToString(loc));
                        }
                    }
                }
            }
        }


        arena.getArenaConfig().setManually("inventories", result);
        arena.getArenaConfig().save();

        // ----------------------------------------

    }

    private Location parseStringToLocation(final String loc) {
        // world,x,y,z
        final String[] args = loc.split(",");

        final World world = Bukkit.getWorld(args[0]);
        final int x = Integer.parseInt(args[1]);
        final int y = Integer.parseInt(args[2]);
        final int z = Integer.parseInt(args[3]);

        return new Location(world, x, y, z);
    }

    private void fill(final Location loc, final boolean clear, final int min, final int max, final ItemStack[] stacks) {
        final Chest c;

        try {
            c = (Chest) loc.getBlock().getState();
        } catch (final ClassCastException cce) {
            return;
        }

        if (clear) {
            c.getBlockInventory().clear();
        }

        final List<ItemStack> adding = new ArrayList<ItemStack>();

        final Random r = new Random();

        final int count = r.nextInt(max - min) + min;


        int i = 0;

        while (i++ < count) {
            final int d = r.nextInt(stacks.length);
            adding.add(stacks[d].clone());
        }

        for (final ItemStack it : adding) {
            c.getInventory().addItem(it);
        }
        c.update();
    }

    private Location saveBlock(final World world, final int x, final int y, final int z) {
        final Block b = world.getBlockAt(x, y, z);
        if (b.getType() == Material.CHEST) {
            return b.getLocation();
        }
        return null;
    }

    private String parseLocationToString(final Location loc) {
        return loc.getWorld().getName() + ',' + loc.getBlockX() + ','
                + loc.getBlockY() + ',' + loc.getBlockZ();
    }
}
