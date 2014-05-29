package net.slipcor.pvparena.modules.worldguard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegion;

public class PAWG extends ArenaModule {
	private static WorldGuardPlugin worldGuard;
	public PAWG() {
		super("WorldGuard");
	}
	
	@Override
	public String version() {
		return "v1.2.3.459";
	}

	@Override
	public boolean checkCommand(String s) {
		return (s.equals("wgload")
				|| s.equals("!wgl") || s.equals("worldguard"));
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		if (!sender.hasPermission("pvparena.admin")) {
			arena.msg(sender, Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}
		
		// /pa wgsth [regionname] [wgregionname]
		
		if (args.length != 3) {
			helpCommands(arena, sender);
			return;
		}
		
		ArenaRegion ars = arena.getRegion(args[1]);
		
		if (ars == null) {
			create((Player) sender, arena, args[1], args[2]);
		} else {
			update((Player) sender, arena, ars, args[2]);
		}
	}

	private void create(Player p, Arena arena, String regionName, String wgRegion) {
		
ProtectedRegion region = worldGuard.getRegionManager(p.getWorld()).getRegionExact(wgRegion);
		
		if (region == null) {
			arena.msg(p, Language.parse(MSG.MODULE_WORLDGUARD_NOTFOUND, wgRegion));
			return;
		}
		BlockVector v = region.getMinimumPoint();
		
		Location loc1 = new Location(p.getWorld(),
				region.getMinimumPoint().getBlockX(),
				region.getMinimumPoint().getBlockY(),
				region.getMinimumPoint().getBlockZ());
		Location loc2 = new Location(p.getWorld(),
				region.getMaximumPoint().getBlockX(),
				region.getMaximumPoint().getBlockY(),
				region.getMaximumPoint().getBlockZ());
		
		ArenaPlayer ap = ArenaPlayer.parsePlayer(p.getName());
		ap.setSelection(loc1, false);
		ap.setSelection(loc2, true);
		
		PAA_Region cmd = new PAA_Region();
		String[] args = {regionName, "CUBOID"};
		
		cmd.commit(arena, p, args);
	}

	private void update(Player p, Arena arena, ArenaRegion ars,
			String wgRegion) {
		ProtectedRegion region = worldGuard.getRegionManager(p.getWorld()).getRegionExact(wgRegion);
		
		if (region == null) {
			arena.msg(p, Language.parse(MSG.MODULE_WORLDGUARD_NOTFOUND, wgRegion));
			return;
		}
		
		Location loc1 = new Location(p.getWorld(),
				region.getMinimumPoint().getBlockX(),
				region.getMinimumPoint().getBlockY(),
				region.getMinimumPoint().getBlockZ());
		Location loc2 = new Location(p.getWorld(),
				region.getMaximumPoint().getBlockX(),
				region.getMaximumPoint().getBlockY(),
				region.getMaximumPoint().getBlockZ());

		ars.locs[0] = new PABlockLocation(loc1);
		ars.locs[1] = new PABlockLocation(loc2);
		ars.saveToConfig();
		arena.msg(p, Language.parse(MSG.MODULE_WORLDGUARD_SAVED, ars.getRegionName(), wgRegion));
	}

	private void helpCommands(Arena arena, CommandSender sender) {
		arena.msg(sender, Language.parse(MSG.ERROR_ERROR, "/pa wgload [regionname] [wgregionname]"));
		return;
	}
	
	@Override
	public void onThisLoad() {
		Plugin pwep = Bukkit.getPluginManager().getPlugin("WorldGuard");
	    if ((pwep != null) && (pwep.isEnabled()) && ((pwep instanceof WorldGuardPlugin))) {
	    	worldGuard = (WorldGuardPlugin)pwep;
	    }
	}
}
