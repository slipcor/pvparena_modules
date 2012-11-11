package net.slipcor.pvparena.modules.skins;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.desmin88.mobdisguise.api.MobDisguiseAPI;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class Skins extends ArenaModule {
	protected static boolean mdHandler = false;

	public Skins() {
		super("Skins");
	}

	@Override
	public String version() {
		return "v0.9.6.16";
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		if (config.get("skins") == null) {
			for (ArenaTeam team : arena.getTeams()) {
				String sName = team.getName();
				config.addDefault("skins." + sName, "Herobrine");
			}
			config.options().copyDefaults(true);
		}
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return arena.getArenaConfig().getBoolean(CFG.MODULES_SKINS_ACTIVE);
	}

	@Override
	public void onEnable() {
		if (Bukkit.getServer().getPluginManager().getPlugin("MobDisguise") != null) {
			mdHandler = true;
		}
		Arena.pmsg(Bukkit.getConsoleSender(), Language.parse((!mdHandler) ? MSG.MODULE_SKINS_NOMOBDISGUISE : MSG.MODULE_SKINS_MOBDISGUISE));
	}

	@Override
	public void tpPlayerToCoordName(Arena arena, Player player, String place) {
		if (!mdHandler || !place.contains("lounge")) {
			return;
		}
		if (arena.hasPlayer(player)) {
			ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
			if (team == null) {
				return;
			}
			String disguise = (String) arena.getArenaConfig().getUnsafe("skins." + team.getName());
			
			if (!MobDisguiseAPI.disguisePlayer(player, disguise)) {
				if (!MobDisguiseAPI.disguisePlayerAsPlayer(player, disguise)) {
					PVPArena.instance.getLogger().warning("Unable to disguise " + player.getName() + " as " + disguise);
				}
			}
		}
	}

	@Override
	public void unload(Player player) {
		if (mdHandler) {
			MobDisguiseAPI.undisguisePlayer(player);
		}
	}
}
