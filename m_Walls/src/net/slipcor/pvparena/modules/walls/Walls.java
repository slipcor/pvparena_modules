package net.slipcor.pvparena.modules.walls;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;

public class Walls extends ArenaModule {
	WallsRunner runnable = null;
	

	public Walls() {
		super("Walls");
	}

	@Override
	public String version() {
		return "v1.0.1.60";
	}
	
	@Override
	public boolean checkCommand(String s) {
		return s.equals("walls") || s.equals("wallmaterial") || s.equals("!ww") || s.equals("!wm");
	}

	private void createWalls() {
		Material mat = null;
		try {
			Material newMat = Material.getMaterial(arena.getArenaConfig().getString(CFG.MODULES_WALLS_MATERIAL));
			mat = Material.getMaterial(newMat.name());
		} catch (Exception e) {
			mat = Material.SAND;
		}
		
		for (ArenaRegionShape region : arena.getRegions()) {
			if (region.getRegionName().toLowerCase().contains("wall")) {
				final World world = region.getWorld();
				final int x1 = region.getMinimumLocation().getX();
				final int y1 = region.getMinimumLocation().getY();
				final int z1 = region.getMinimumLocation().getZ();

				final int x2 = region.getMaximumLocation().getX();
				final int y2 = region.getMaximumLocation().getY();
				final int z2 = region.getMaximumLocation().getZ();
				
				for (int a = x1; a<=x2; a++) {
					for (int b = y1; b<=y2; b++) {
						for (int c = z1; c<=z2; c++) {
							world.getBlockAt(a, b, c).setType(mat);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		// !sf 5
		
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			arena.msg(
					sender,
					Language.parse(MSG.ERROR_NOPERM,
							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}

		if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[] { 2 })) {
			return;
		}
		
		if (args[0].equals("!ww") || args[0].equals("walls")) {
			// setting walls seconds
			int i = 0;
			try {
				i = Integer.parseInt(args[1]);
			} catch (Exception e) {
				arena.msg(sender,
						Language.parse(MSG.ERROR_NOT_NUMERIC, args[1]));
				return;
			}
			
			arena.getArenaConfig().set(CFG.MODULES_WALLS_SECONDS, i);
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_WALLS_SECONDS.getNode(), String.valueOf(i)));
		} else {
			// setting walls material
			Material mat = null;
			try {
				mat = Material.getMaterial(args[0].toUpperCase());
				debug.i("wall material: " + mat.name());
			} catch (Exception e) {
				arena.msg(sender, Language.parse(MSG.ERROR_MAT_NOT_FOUND, args[0]));
				return;
			}
			
			arena.getArenaConfig().set(CFG.MODULES_WALLS_MATERIAL, mat.name());
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.SET_DONE, CFG.MODULES_WALLS_MATERIAL.getNode(), mat.name()));
		}
	}
	
	@Override
	public void displayInfo(CommandSender sender) {
		sender.sendMessage("seconds: " + arena.getArenaConfig().getInt(CFG.MODULES_WALLS_SECONDS) +
				"material: " + arena.getArenaConfig().getString(CFG.MODULES_WALLS_MATERIAL));
	}

	@Override
	public void parseStart() {
		runnable = new WallsRunner(this, arena, arena.getArenaConfig().getInt(CFG.MODULES_WALLS_SECONDS));
		createWalls();
	}

	@Override
	public void reset(boolean force) {
		if (runnable != null) {
			runnable.cancel();
		}
		runnable = null;
		createWalls();
	}

	public void removeWalls() {
		for (ArenaRegionShape region : arena.getRegions()) {
			
			if (region.getRegionName().toLowerCase().contains("wall")) {
				final World world = region.getWorld();
				final int x1 = region.getMinimumLocation().getX();
				final int y1 = region.getMinimumLocation().getY();
				final int z1 = region.getMinimumLocation().getZ();

				final int x2 = region.getMaximumLocation().getX();
				final int y2 = region.getMaximumLocation().getY();
				final int z2 = region.getMaximumLocation().getZ();

				for (int a = x1; a<=x2; a++) {
					for (int b = y1; b<=y2; b++) {
						for (int c = z1; c<=z2; c++) {
							world.getBlockAt(a, b, c).setType(Material.AIR);
						}
					}
				}
			}
		}
	}
}
