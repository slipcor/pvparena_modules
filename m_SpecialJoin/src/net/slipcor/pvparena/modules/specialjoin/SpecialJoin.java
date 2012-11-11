package net.slipcor.pvparena.modules.specialjoin;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class SpecialJoin extends ArenaModule {
	public SpecialJoin() {
		super("SpecialJoin");
	}
	
	@Override
	public String version() {
		return "v0.9.6.16";
	}
	
	@Override
	public boolean checkCommand(String s) {
		return s.toLowerCase().equals("setjoin");
	}
	
	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		List<String> res;
		
		try {
			res = config.getStringList("modules.specialjoin.places");
			for (String s : res) {
				SpecialJoinListener.places.put(Config.parseBlockLocation(s), arena);
			}
		} catch (Exception e) {
			
		}
	}
	
	@Override
	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			arena.msg(sender,
					Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}
		
		// /pa [arenaname] setjoin
		
		if (SpecialJoinListener.selections.containsKey(sender.getName())) {
			// remove & announce
			SpecialJoinListener.selections.remove(sender.getName());
			arena.msg(sender,
					Language.parse(MSG.MODULE_SPECIALJOIN_STOP));
		} else {
			// add & announce
			SpecialJoinListener.selections.put(sender.getName(), arena);
			arena.msg(sender,
					Language.parse(MSG.MODULE_SPECIALJOIN_START));
		}
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return arena.getArenaConfig().getBoolean(CFG.MODULES_SPECIALJOIN_ACTIVE);
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new SpecialJoinListener(), PVPArena.instance);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, new SpecialJoinRunnable(this), 1200L, 1200L);
	}
}
