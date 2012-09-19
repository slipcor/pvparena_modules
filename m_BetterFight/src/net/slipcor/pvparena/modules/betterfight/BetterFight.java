package net.slipcor.pvparena.modules.betterfight;

import java.util.HashMap;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModule;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class BetterFight extends ArenaModule {
	
	HashMap<String, Integer> kills = new HashMap<String, Integer>();
	
	public BetterFight() {
		super("BetterFight");
	}
	
	@Override
	public String version() {
		return "v0.9.0.0";
	}

	@Override
	public void addSettings(HashMap<String, String> types) {
		types.put("betterfight.activate", "boolean");
		types.put("betterfight.resetonkill", "boolean");
		types.put("betterfight.oneHit", "string");
	}
	
	@Override
	public void commitPlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_ACTIVE)) {
			return;
		}
		
		Player p = ArenaPlayer.getLastDamagingPlayer(cause);
		
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_RESETKILLSTREAKONDEATH)) {
			kills.put(player.getName(), 0);
		}
		
		if (p == null || kills.get(p.getName()) == null) {
			return;
		}
		int killcount = kills.get(p.getName());

		kills.put(player.getName(), ++killcount);
		
		String msg = (String) arena.getArenaConfig().getUnsafe("betterfight.messages.m"+killcount);
		
		if (msg == null || msg.equals("")) {
			return;
		}
		
		arena.broadcast(msg);
	}
	
	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		config.addDefault("betterfight.activate", Boolean.valueOf(false));
		config.addDefault("betterfight.resetonkill", Boolean.valueOf(true));
		config.addDefault("betterfight.oneHit", "none");
		
		if (config.get("betterfight.messages") == null) {
			config.addDefault("betterfight.messages.m0", "This is a dummy message. You can remove the other messages if you don't want messages!");
			config.addDefault("betterfight.messages.m2", "Double Kill!");
			config.addDefault("betterfight.messages.m3", "Triple Kill!");
			config.addDefault("betterfight.messages.m4", "Quadra Kill!");
			config.addDefault("betterfight.messages.m5", "Super Kill!");
			config.addDefault("betterfight.messages.m6", "Ultra Kill!");
			config.addDefault("betterfight.messages.m7", "Godlike!");
			config.addDefault("betterfight.messages.m8", "Monster!");
		}
		
		config.options().copyDefaults(true);
	}
	
	@Override
	public void onEntityDamageByEntity(Arena arena, Player attacker,
			Player defender, EntityDamageByEntityEvent event) {
		if (!arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_ACTIVE)) {
			return;
		}
		
		String s = arena.getArenaConfig().getString(CFG.MODULES_BETTERFIGHT_ONEHITITEMS);
		if (s.equalsIgnoreCase("none")) {
			return;
		}
		
		if (event.getDamager() instanceof Projectile) {
			if (event.getDamager() instanceof Snowball) {
				if (s.toLowerCase().contains("snow")) {
					event.setDamage(1000);
				}
			}
			if (event.getDamager() instanceof Arrow) {
				if (s.toLowerCase().contains("arrow")) {
					event.setDamage(1000);
				}
			}
			if (event.getDamager() instanceof Fireball) {
				if (s.toLowerCase().contains("fireball")) {
					event.setDamage(1000);
				}
			}
			if (event.getDamager() instanceof Egg) {
				if (s.toLowerCase().contains("egg")) {
					event.setDamage(1000);
				}
			}
		}
	}

	@Override
	public void parseInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6Betterfight:§f "
				+ StringParser.colorVar(arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_ACTIVE)));
	}

	@Override
	public void parseRespawn(Arena arena, Player player, ArenaTeam team,
			DamageCause cause, Entity damager) {
		
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_BETTERFIGHT_RESETKILLSTREAKONDEATH)) {
			kills.put(player.getName(), 0);
		}
		
		if (! (damager instanceof Player)) {
			return;
		}
		Player p = (Player) damager;
		if (p == null || kills.get(p.getName()) == null) {
			return;
		}
		int killcount = kills.get(p.getName());

		kills.put(player.getName(), ++killcount);
		
		String msg = (String) arena.getArenaConfig().getUnsafe("betterfight.messages.m"+killcount);
		
		if (msg == null || msg.equals("")) {
			return;
		}
		
		arena.broadcast(msg);
	}
	
	@Override
	public void teleportAllToSpawn(Arena arena) {
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				kills.put(ap.getName(), 0);
			}
		}
	}
}
