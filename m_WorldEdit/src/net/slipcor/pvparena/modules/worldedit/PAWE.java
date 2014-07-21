package net.slipcor.pvparena.modules.worldedit;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PAWE extends ArenaModule {
    private static WorldEditPlugin worldEdit;

    public PAWE() {
        super("WorldEdit");
    }

    @Override
    public String version() {
        return "v1.3.0.515";
    }

    @Override
    public boolean checkCommand(final String s) {
        return "regload".equals(s) || "regsave".equals(s) || "regcreate".equals(s)
                || "!we".equals(s) || "worldedit".equals(s);
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("regload", "regsave", "regcreate", "worldedit");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!we");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<String>(null);
        if (arena == null) {
            return result;
        }
        for (final ArenaRegion region : arena.getRegions()) {
            result.define(new String[]{region.getRegionName()});
        }
        return result;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("pvparena.admin")) {
            arena.msg(sender, Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        // /pa reg[save|load] [regionname] {filename}

        if (args.length < 2) {
            helpCommands(arena, sender);
            return;
        }

        final ArenaRegion ars = arena.getRegion(args[1]);

        if (args.length < 3) {

            if (args[0].endsWith("load")) {
                load(ars);
                arena.msg(sender, Language.parse(MSG.MODULE_WORLDEDIT_LOADED, args[1]));
                return;
            }
            if (args[0].endsWith("save")) {
                save(ars);
                arena.msg(sender, Language.parse(MSG.MODULE_WORLDEDIT_SAVED, args[1]));
                return;
            }
            if (args[0].endsWith("create")) {
                create((Player) sender, arena, args[1]);
                arena.msg(sender, Language.parse(MSG.MODULE_WORLDEDIT_CREATED, args[1]));
                return;
            }
            if (args[0].equals("!we") || args[0].equals("!we")) {

                if (args[1].endsWith("save")) {
                    boolean b = arena.getArenaConfig().getBoolean(CFG.MODULES_WORLDEDIT_AUTOSAVE);
                    arena.getArenaConfig().set(CFG.MODULES_WORLDEDIT_AUTOSAVE, !b);
                    arena.getArenaConfig().save();
                    arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_WORLDEDIT_AUTOSAVE.getNode(), String.valueOf(!b)));
                    return;
                }
                if (args[1].endsWith("load")) {
                    boolean b = arena.getArenaConfig().getBoolean(CFG.MODULES_WORLDEDIT_AUTOLOAD);
                    arena.getArenaConfig().set(CFG.MODULES_WORLDEDIT_AUTOLOAD, !b);
                    arena.getArenaConfig().save();
                    arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_WORLDEDIT_AUTOLOAD.getNode(), String.valueOf(!b)));
                    return;
                }

                create((Player) sender, arena, args[1]);
                arena.msg(sender, Language.parse(MSG.MODULE_WORLDEDIT_CREATED, args[1]));
                return;
            }
            helpCommands(arena, sender);
        } else {
            if (args[0].endsWith("load")) {
                load(ars, args[2]);
                arena.msg(sender, Language.parse(MSG.MODULE_WORLDEDIT_LOADED, args[1]));
            } else if (args[0].endsWith("save")) {
                save(ars, args[2]);
                arena.msg(sender, Language.parse(MSG.MODULE_WORLDEDIT_SAVED, args[1]));
            } else {
                create((Player) sender, arena, args[1], args[2]);
            }
        }
    }

    private void create(final Player p, final Arena arena, final String regionName, final String regionShape) {
        final Selection s = worldEdit.getSelection(p);
        if (s == null) {
            Arena.pmsg(p, Language.parse(MSG.ERROR_REGION_SELECT_2));
            return;
        }

        final ArenaPlayer ap = ArenaPlayer.parsePlayer(p.getName());
        ap.setSelection(s.getMinimumPoint(), false);
        ap.setSelection(s.getMaximumPoint(), true);

        final PAA_Region cmd = new PAA_Region();
        final String[] args = {regionName, regionShape};

        cmd.commit(arena, p, args);
    }

    private void create(final Player p, final Arena arena, final String regionName) {
        create(p, arena, regionName, "CUBOID");
    }

    @Override
    public void displayInfo(final CommandSender sender) {
        sender.sendMessage(StringParser.colorVar("autoload", arena.getArenaConfig().getBoolean(CFG.MODULES_WORLDEDIT_AUTOLOAD)) +
                " | " + StringParser.colorVar("autosave", arena.getArenaConfig().getBoolean(CFG.MODULES_WORLDEDIT_AUTOSAVE)));
    }

    private void helpCommands(final Arena arena, final CommandSender sender) {
        arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa regsave [regionname] {filename}"));
        arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa regload [regionname] {filename}"));
        arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa regcreate [regionname]"));
        arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa !we autoload"));
        arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa !we autosave"));
        arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa !we create"));
    }

    void load(final ArenaRegion ars) {
        load(ars, ars.getArena().getName() + '_' + ars.getRegionName());
    }

    void load(final ArenaRegion ars, final String regionName) {

        try {
            final CuboidClipboard cc = SchematicFormat.MCEDIT.load(new File(PVPArena.instance.getDataFolder(), regionName + ".schematic"));
            final PABlockLocation min = ars.getShape().getMinimumLocation();
            final PABlockLocation max = ars.getShape().getMaximumLocation();
            final int size = (max.getX() + 2 - min.getX()) *
                    (max.getY() + 2 - min.getY()) *
                    (max.getZ() + 2 - min.getZ());
            final EditSession es = worldEdit.getWorldEdit().getEditSessionFactory().
                    getEditSession(new BukkitWorld(Bukkit.getWorld(ars.getWorldName())), size);
            final PABlockLocation loc = ars.getShape().getMinimumLocation();
            cc.place(es, new Vector(loc.getX() - 1, loc.getY() - 1, loc.getZ() - 1), false);
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final DataException e) {
            e.printStackTrace();
        } catch (final MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean needsBattleRegion() {
        return true;
    }

    @Override
    public void onThisLoad() {
        final Plugin pwep = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (pwep != null && pwep.isEnabled() && pwep instanceof WorldEditPlugin) {
            worldEdit = (WorldEditPlugin) pwep;
        }
    }

    @Override
    public void parseStart() {
        if (arena.getArenaConfig().getBoolean(CFG.MODULES_WORLDEDIT_AUTOSAVE)) {
            for (final ArenaRegion ars : arena.getRegionsByType(RegionType.BATTLE)) {
                save(ars);
            }
        }
    }

    @Override
    public void reset(final boolean force) {
        if (arena.getArenaConfig().getBoolean(CFG.MODULES_WORLDEDIT_AUTOLOAD)) {
            for (final ArenaRegion ars : arena.getRegionsByType(RegionType.BATTLE)) {
                load(ars);
            }
        }
    }

    void save(final ArenaRegion ars) {
        save(ars, ars.getArena().getName() + '_' + ars.getRegionName());
    }

    void save(final ArenaRegion ars, final String regionName) {
        final CuboidSelection cs = new CuboidSelection(Bukkit.getWorld(ars.getWorldName()), ars.getShape().getMinimumLocation().toLocation(), ars.getShape().getMaximumLocation().toLocation());
        Vector min = cs.getNativeMinimumPoint();
        Vector max = cs.getNativeMaximumPoint();

        min = min.subtract(1, 1, 1);
        max = max.add(1, 1, 1);

        final PABlockLocation lmin = ars.getShape().getMinimumLocation();
        final PABlockLocation lmax = ars.getShape().getMaximumLocation();
        final int size = (lmax.getX() - lmin.getX()) *
                (lmax.getY() - lmin.getY()) *
                (lmax.getZ() - lmin.getZ());

        final CuboidClipboard cc = new CuboidClipboard(max.subtract(min), min);

        final EditSession es = worldEdit.getWorldEdit().getEditSessionFactory().
                getEditSession(new BukkitWorld(Bukkit.getWorld(ars.getWorldName())), size);

        cc.copy(es);

        try {
            SchematicFormat.MCEDIT.save(cc, new File(PVPArena.instance.getDataFolder(), regionName + ".schematic"));

        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final DataException e) {
            e.printStackTrace();
        }
    }
}
