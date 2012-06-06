package net.slipcor.pvparena.modules.skins;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.desmin88.mobdisguise.api.MobDisguiseAPI;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Teams;
import net.slipcor.pvparena.neworder.ArenaModule;

public class Skins extends ArenaModule {
	protected static boolean mdHandler = false;

	public Skins() {
		super("Skins");
	}

	@Override
	public String version() {
		return "v0.8.6.19";
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config, String type) {
		if (config.get("skins") == null) {
			for (ArenaTeam team : arena.getTeams()) {
				String sName = team.getName();
				config.addDefault("skins." + sName, "Herobrine");
			}
			config.options().copyDefaults(true);
		}
	}

	@Override
	public void initLanguage(YamlConfiguration config) {
		config.addDefault("log.nomd",
				"MobDisguise not found, Skins module is inactive!");
		config.addDefault("log.md", "Hooking into MobDisguise!");
	}

	@Override
	public void onEnable() {
		if (Bukkit.getServer().getPluginManager().getPlugin("MobDisguise") != null) {
			mdHandler = true;
		}
		Language.log_info((!mdHandler) ? "nomd" : "md");
	}

	@Override
	public void tpPlayerToCoordName(Arena arena, Player player, String place) {
		if (!mdHandler || !place.contains("lounge")) {
			return;
		}
		if (arena.isPartOf(player)) {
			ArenaTeam team = Teams.getTeam(arena,
					ArenaPlayer.parsePlayer(player));
			if (team == null) {
				return;
			}
			String disguise = arena.cfg.getString("skins." + team.getName());
			
			if (!MobDisguiseAPI.disguisePlayer(player, disguise)) {
				if (!MobDisguiseAPI.disguisePlayerAsPlayer(player, disguise)) {
					System.out.print("Unable to disguise " + player.getName() + " as " + disguise);
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
