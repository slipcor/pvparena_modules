package net.slipcor.pvparena.modules.skins;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

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
	protected static boolean dcHandler = false;
	DisguiseCraftAPI dapi = null;
	
	HashSet<String> disguised = new HashSet<String>();

	public Skins() {
		super("Skins");
	}

	@Override
	public String version() {
		return "v0.9.8.20";
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
		return arena.getArenaConfig().getBoolean(CFG.MODULES_SKINS_ACTIVE) && (mdHandler || dcHandler);
	}

	@Override
	public void onEnable() {
		MSG m = MSG.MODULE_SKINS_NOMOD;
		if (Bukkit.getServer().getPluginManager().getPlugin("DisguiseCraft") != null) {
			dcHandler = true;
			m = MSG.MODULE_SKINS_DISGUISECRAFT;

			dapi = DisguiseCraft.getAPI();
		} else if (Bukkit.getServer().getPluginManager().getPlugin("MobDisguise") != null) {
			mdHandler = true;
			m = MSG.MODULE_SKINS_MOBDISGUISE;
		}
		
		Arena.pmsg(Bukkit.getConsoleSender(), Language.parse(m));
	}

	@Override
	public void tpPlayerToCoordName(Arena arena, Player player, String place) {
		if (dcHandler) {
			dapi = DisguiseCraft.getAPI();
		}
		
		if (disguised.contains(player.getName())) {
			return;
		}
		
		if (arena.hasPlayer(player)) {
			ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
			if (team == null) {
				return;
			}
			String disguise = (String) arena.getArenaConfig().getUnsafe("skins." + team.getName());
			
			if (dcHandler) {
				DisguiseType t = DisguiseType.fromString(disguise);
				Disguise d = new Disguise(dapi.newEntityID(), disguise, t == null ? DisguiseType.Player : t);
				if (dapi.isDisguised(player)) {
					dapi.undisguisePlayer(player);
					Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new DisguiseRunnable(player, d), 3L);
				} else {
					dapi.disguisePlayer(player, d);
				}
			} else if (mdHandler) {
				if (!MobDisguiseAPI.disguisePlayer(player, disguise)) {
					if (!MobDisguiseAPI.disguisePlayerAsPlayer(player, disguise)) {
						PVPArena.instance.getLogger().warning("Unable to disguise " + player.getName() + " as " + disguise);
					}
				}
			}
			
			disguised.add(player.getName());
		}
	}

	@Override
	public void unload(Player player) {
		if (dcHandler) {
			dapi.undisguisePlayer(player);
		} else if (mdHandler) {
			MobDisguiseAPI.undisguisePlayer(player);
		}
		disguised.remove(player.getName());
	}
}
