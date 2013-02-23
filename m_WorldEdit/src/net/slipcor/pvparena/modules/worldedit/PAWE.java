package net.slipcor.pvparena.modules.worldedit;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;

public class PAWE extends ArenaModule {
	private static WorldEditPlugin worldEdit;
	public PAWE() {
		super("WorldEdit");
	}
	
	@Override
	public String version() {
		return "v1.0.1.54";
	}
	
	@Override
	public void onThisLoad() {
		Plugin pwep = Bukkit.getPluginManager().getPlugin("WorldEdit");
	    if ((pwep != null) && (pwep.isEnabled()) && ((pwep instanceof WorldEditPlugin))) {
	    	worldEdit = (WorldEditPlugin)pwep;
	    }
	}

	@Override
	public void reset(boolean force) {
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_WORLDEDIT_AUTOLOAD)) {
			for (ArenaRegionShape ars : arena.getRegionsByType(RegionType.BATTLE)) {
				load(ars);
			}
		}
	}
	
	public void save(ArenaRegionShape ars) {
		save(ars, ars.getArena().getName() + "_" + ars.getRegionName());
	}
	
	public void save(ArenaRegionShape ars, String regionName) {
		CuboidSelection cs = new CuboidSelection(Bukkit.getWorld(ars.getWorldName()), ars.getMinimumLocation().toLocation(), ars.getMaximumLocation().toLocation());
		Vector min = cs.getNativeMinimumPoint();
		Vector max = cs.getNativeMaximumPoint();
		
		min = min.subtract(1, 1, 1);
		max = max.add(1, 1, 1);
		
		PABlockLocation lmin = ars.getMinimumLocation();
		PABlockLocation lmax = ars.getMaximumLocation();
		int size = (lmax.getX() - lmin.getX()) * 
				(lmax.getY() - lmin.getY()) *
				(lmax.getZ() - lmin.getZ());
		
		CuboidClipboard cc = new CuboidClipboard(max.subtract(min), min);
		
		EditSession es = new EditSession(new BukkitWorld(Bukkit.getWorld(ars.getWorldName())), size);

		cc.copy(es);
		
		try {
			SchematicFormat.MCEDIT.save(cc, new File(PVPArena.instance.getDataFolder(), regionName + ".schematic"));
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataException e) {
			e.printStackTrace();
		}
	}

	public void load(ArenaRegionShape ars) {
		load(ars, ars.getArena().getName() + "_" + ars.getRegionName());
	}
	
	public void load(ArenaRegionShape ars, String regionName) {

		try {
			CuboidClipboard cc = SchematicFormat.MCEDIT.load(new File(PVPArena.instance.getDataFolder(), regionName + ".schematic"));
			PABlockLocation min = ars.getMinimumLocation();
			PABlockLocation max = ars.getMaximumLocation();
			int size = (max.getX() + 2 - min.getX()) * 
					(max.getY() + 2 - min.getY()) *
					(max.getZ() + 2 - min.getZ());
			
			EditSession es = new EditSession(new BukkitWorld(Bukkit.getWorld(ars.getWorldName())), size);
			PABlockLocation loc = ars.getMinimumLocation();
			cc.place(es, new Vector(loc.getX() - 1, loc.getY() - 1, loc.getZ() - 1), false);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataException e) {
			e.printStackTrace();
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void parseStart() {
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_WORLDEDIT_AUTOSAVE)) {
			for (ArenaRegionShape ars : arena.getRegionsByType(RegionType.BATTLE)) {
				save(ars);
			}
		}
	}

	@Override
	public boolean checkCommand(String s) {
		return (s.equals("regload") || s.equals("regsave") || s.equals("regcreate")
				|| s.equals("!we") || s.equals("worldedit"));
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		if (!sender.hasPermission("pvparena.admin")) {
			arena.msg(sender, Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}
		
		// /pa reg[save|load] [regionname] {filename}
		
		if (args.length < 2) {
			helpCommands(arena, sender);
			return;
		}
		
		ArenaRegionShape ars = arena.getRegion(args[1]);
		
		if (args.length < 3) {
		
			if (args[0].endsWith("load")) {
				load(ars);
				arena.msg(sender, Language.parse(MSG.MODULE_WORLDEDIT_LOADED, args[1]));
				return;
			} else if (args[0].endsWith("save")) {
				save(ars);
				arena.msg(sender, Language.parse(MSG.MODULE_WORLDEDIT_SAVED, args[1]));
				return;
			} else if (args[0].endsWith("create")) {
				create((Player) sender, arena, args[1]);
				arena.msg(sender, Language.parse(MSG.MODULE_WORLDEDIT_CREATED, args[1]));
				return;
			} else if (args[0].equals("!we") || args[0].equals("!we")) {
				
				if (args[1].endsWith("save")) {
					boolean b = arena.getArenaConfig().getBoolean(CFG.MODULES_WORLDEDIT_AUTOSAVE);
					arena.getArenaConfig().set(CFG.MODULES_WORLDEDIT_AUTOSAVE, !b);
					arena.getArenaConfig().save();
					arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_WORLDEDIT_AUTOSAVE.getNode(), String.valueOf(!b)));
					return;
				} else if (args[1].endsWith("load")) {
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

	private void helpCommands(Arena arena, CommandSender sender) {
		arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa regsave [regionname] {filename}"));
		arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa regload [regionname] {filename}"));
		arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa regcreate [regionname]"));
		arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa !we autoload"));
		arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa !we autosave"));
		arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa !we create"));
		return;
	}

	private void create(Player p, Arena arena, String regionName, String regionShape) {
		Selection s = worldEdit.getSelection(p);
		if (s == null) {
			Arena.pmsg(p, Language.parse(MSG.ERROR_REGION_SELECT_2));
			return;
		}
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(p.getName());
		ap.setSelection(s.getMinimumPoint(), false);
		ap.setSelection(s.getMaximumPoint(), true);
		
		PAA_Region cmd = new PAA_Region();
		String[] args = {regionName, regionShape};
		
		cmd.commit(arena, p, args);
	}

	private void create(Player p, Arena arena, String regionName) {
		create(p, arena, regionName, "CUBOID");
	}
}
