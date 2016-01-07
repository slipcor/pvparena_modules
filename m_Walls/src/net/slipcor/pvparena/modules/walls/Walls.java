package net.slipcor.pvparena.modules.walls;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegion;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class Walls extends ArenaModule {
    WallsRunner runnable;


    public Walls() {
        super("Walls");
    }

    @Override
    public String version() {
        return "v1.3.2.51";
    }

    @Override
    public boolean checkCommand(final String s) {
        return "walls".equals(s) || "wallmaterial".equals(s) || "!ww".equals(s) || "!wm".equals(s);
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("walls", "wallmaterial");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!ww", "!wm");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"{Material}"});
        return result;
    }

    private void createWalls() {
        Material mat;
        try {
            final Material newMat = Material.getMaterial(arena.getArenaConfig().getString(CFG.MODULES_WALLS_MATERIAL));
            mat = Material.getMaterial(newMat.name());
        } catch (final Exception e) {
            mat = Material.SAND;
        }

        for (final ArenaRegion region : arena.getRegions()) {
            if (region.getRegionName().toLowerCase().contains("wall")) {
                final World world = region.getWorld();
                final int x1 = region.getShape().getMinimumLocation().getX();
                final int y1 = region.getShape().getMinimumLocation().getY();
                final int z1 = region.getShape().getMinimumLocation().getZ();

                final int x2 = region.getShape().getMaximumLocation().getX();
                final int y2 = region.getShape().getMaximumLocation().getY();
                final int z2 = region.getShape().getMaximumLocation().getZ();

                for (int a = x1; a <= x2; a++) {
                    for (int b = y1; b <= y2; b++) {
                        for (int c = z1; c <= z2; c++) {
                            world.getBlockAt(a, b, c).setType(mat);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        // !sf 5

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

        if ("!ww".equals(args[0]) || "walls".equals(args[0])) {
            // setting walls seconds
            final int i;
            try {
                i = Integer.parseInt(args[1]);
            } catch (final Exception e) {
                arena.msg(sender,
                        Language.parse(MSG.ERROR_NOT_NUMERIC, args[1]));
                return;
            }

            arena.getArenaConfig().set(CFG.MODULES_WALLS_SECONDS, i);
            arena.getArenaConfig().save();
            arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_WALLS_SECONDS.getNode(), String.valueOf(i)));
        } else {
            // setting walls material
            final Material mat;
            try {
                mat = Material.getMaterial(args[0].toUpperCase());
                debug.i("wall material: " + mat.name());
            } catch (final Exception e) {
                arena.msg(sender, Language.parse(MSG.ERROR_MAT_NOT_FOUND, args[0]));
                return;
            }

            arena.getArenaConfig().set(CFG.MODULES_WALLS_MATERIAL, mat.name());
            arena.getArenaConfig().save();
            arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_WALLS_MATERIAL.getNode(), mat.name()));
        }
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage("seconds: " + arena.getArenaConfig().getInt(CFG.MODULES_WALLS_SECONDS) +
                "material: " + arena.getArenaConfig().getString(CFG.MODULES_WALLS_MATERIAL));
    }

    @Override
    public void parseStart() {
        runnable = new WallsRunner(this, arena, arena.getArenaConfig().getInt(CFG.MODULES_WALLS_SECONDS));
        createWalls();
    }

    @Override
    public void reset(final boolean force) {
        if (runnable != null) {
            runnable.cancel();
        }
        runnable = null;
        createWalls();
    }

    public void removeWalls() {
        for (final ArenaRegion region : arena.getRegions()) {

            if (region.getRegionName().toLowerCase().contains("wall")) {
                final World world = region.getWorld();
                final int x1 = region.getShape().getMinimumLocation().getX();
                final int y1 = region.getShape().getMinimumLocation().getY();
                final int z1 = region.getShape().getMinimumLocation().getZ();

                final int x2 = region.getShape().getMaximumLocation().getX();
                final int y2 = region.getShape().getMaximumLocation().getY();
                final int z2 = region.getShape().getMaximumLocation().getZ();

                for (int a = x1; a <= x2; a++) {
                    for (int b = y1; b <= y2; b++) {
                        for (int c = z1; c <= z2; c++) {
                            world.getBlockAt(a, b, c).setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
}
