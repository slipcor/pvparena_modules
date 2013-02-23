package net.slipcor.pvparena.modules.betterclasses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;

public class BetterClasses extends ArenaModule {
	
	HashMap<Arena, HashMap<ArenaClass, HashSet<PotionEffect>>> superMap = new HashMap<Arena, HashMap<ArenaClass, HashSet<PotionEffect>>>();
	
	public BetterClasses() {
		super("BetterClasses");
	}
	
	@Override
	public String version() {
		return "v1.0.1.59";
	}
	
	@Override
	public boolean cannotSelectClass(Player player,
			String className) {
		
		if (notEnoughEXP(player, className)) {
			arena.msg(player, Language.parse(MSG.ERROR_CLASS_NOTENOUGHEXP, className));
			return true;
		}
		
		int max = 0;
		
		try {
			max = (Integer) arena.getArenaConfig().getUnsafe("modules.betterclasses.maxPlayers." + className);
		} catch (Exception e) {
			return false;
		}
		
		if (max < 1) {
			return false;
		}
		
		int sum = 0;
		ArenaTeam team = ArenaPlayer.parsePlayer(player.getName()).getArenaTeam();
		
		if (team == null) {
			return true;
		}
		
		for (ArenaPlayer ap : team.getTeamMembers()) {
			if (ap.getArenaClass() == null) {
				continue;
			}
			if (ap.getArenaClass().getName().equals(className)) {
				sum++;
			}
		}
		
		if (sum >= max) {
			arena.msg(player, Language.parse(MSG.ERROR_CLASS_FULL, className));
			return true;
		}
		
		return false;
	}

	@Override
	public boolean checkCommand(String s) {
		return s.equals("!bc") || s.startsWith("betterclass");
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		// !bc [classname] | show
		// !bc [classname] add [def]| add
		// !bc [classname] remove [type] | remove
		// !bc [classname] clear | clear
		
		// !bc [classname] set exp [level]
		// !bc [classname] set max [count]
		
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			arena.msg(sender,
					Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}
		
		if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{2,3,4,5})) {
			return;
		}
		
		ArenaClass c = arena.getClass(args[1]);
		
		if (c == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_CLASS_NOT_FOUND, args[1]));
			return;
		}
		
		if (args.length == 5 && args[2].equals("set")) {
			int value = 0;
			try {
				value = Integer.parseInt(args[4]);
			} catch (Exception e) {
				arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[4]));
				return;
			}
			
			if (args[3].equalsIgnoreCase("exp")) {
				String node = "modules.betterclasses.neededEXPLevel." + c.getName();
				arena.getArenaConfig().setManually(node, value);
				arena.msg(sender, Language.parse(MSG.SET_DONE, node, String.valueOf(value)));
			} else if (args[3].equalsIgnoreCase("max")) {
				String node = "modules.betterclasses.maxPlayers." + c.getName();
				arena.getArenaConfig().setManually(node, value);
				arena.msg(sender, Language.parse(MSG.SET_DONE, node, String.valueOf(value)));
			}
			return;
		}
		
		
		HashSet<PotionEffect> ape = new HashSet<PotionEffect>();
		
		String s = (String) arena.getArenaConfig().getUnsafe("modules.betterclasses.permEffects." + c.getName());
		if (s != null) {
			ape = parseStringToPotionEffects(s);
		}
		
		if (args.length < 3) {
			// !bc [classname] | show
			arena.msg(sender, Language.parse(MSG.MODULE_BETTERCLASSES_LISTHEAD, c.getName()));
			if (ape.size() >= 1) {
				for (PotionEffect pe : ape) {
					arena.msg(sender, pe.getType().getName() + "x" + pe.getAmplifier());
				}
			} else {
				arena.msg(sender, "---");
			}
			return;
		}
		
		if (args.length < 4) {
			// !bc [classname] clear | clear
			if (!args[2].equals("clear")) {
				printHelp(arena, sender);
				return;
			}
			
			arena.getArenaConfig().setManually("modules.betterclasses.permEffects." + c.getName(), "none");
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.MODULE_BETTERCLASSES_CLEAR, c.getName()));
			return;
		}
		
		if (args[2].equals("add")) {
			// 0   1           2      3     4
			// !bc [classname] add    [def] [amp]| add
			PotionEffectType pet = null;
			
			for (PotionEffectType x : PotionEffectType.values()) {
				if (x == null) {
					continue;
				}
				if (x.getName().equalsIgnoreCase(args[3])) {
					pet = x;
					break;
				}
			}
			
			if (pet == null) {
				arena.msg(sender, Language.parse(MSG.ERROR_POTIONEFFECTTYPE_NOTFOUND, args[3]));
				return;
			}
			
			int amp = 1;
			
			if (args.length == 5) {
				try {
					amp = Integer.parseInt(args[4]);
				} catch (Exception e) {
					arena.msg(sender, Language.parse(MSG.ERROR_NOT_NUMERIC, args[4]));
					return;
				}
			}
			
			ape.add(new PotionEffect(pet, 2147000, amp));
			arena.getArenaConfig().setManually("modules.betterclasses.permEffects." + c.getName(), parsePotionEffectsToString(ape));
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.MODULE_BETTERCLASSES_ADD, c.getName(), pet.getName()));
			return;
		} else if (args[2].equals("remove")) {
			// 0   1           2      3
			// !bc [classname] remove [type] | remove
			PotionEffectType pet = null;
			
			for (PotionEffectType x : PotionEffectType.values()) {
				if (x == null) {
					continue;
				}
				if (x.getName().equalsIgnoreCase(args[3])) {
					pet = x;
					break;
				}
			}
			
			if (pet == null) {
				arena.msg(sender, Language.parse(MSG.ERROR_POTIONEFFECTTYPE_NOTFOUND, args[3]));
				return;
			}
			
			PotionEffect remove = null;
			
			for (PotionEffect pe : ape) {
				if (pe.getType().equals(pet)) {
					remove = pe;
					break;
				}
			}
			
			ape.remove(remove);
			arena.getArenaConfig().setManually("modules.betterclasses.permEffects." + c.getName(), parsePotionEffectsToString(ape));
			arena.getArenaConfig().save();
			arena.msg(sender, Language.parse(MSG.MODULE_BETTERCLASSES_REMOVE, c.getName(), remove.getType().getName()));
			return;
		}
		printHelp(arena, sender);
	}

	@Override
	public void configParse(YamlConfiguration cfg) {
		for (ArenaClass c : arena.getClasses()) {
			cfg.addDefault("modules.betterclasses.permEffects." + c.getName(), "none");
			cfg.addDefault("modules.betterclasses.maxPlayers." + c.getName(), 0);
			cfg.addDefault("modules.betterclasses.neededEXPLevel." + c.getName(), 0);
		}
	}
	
	@Override
	public void displayInfo(CommandSender sender) {
		if (superMap == null || !superMap.containsKey(arena)) {
			return;
		}
		
		HashMap<ArenaClass, HashSet<PotionEffect>> map = superMap.get(arena);
		
		for (ArenaClass aClass : map.keySet()) {
			Set<String> set = new HashSet<String>();
			for (PotionEffect pef : map.get(aClass)) {
				set.add(pef.getType().getName() + "x" + pef.getAmplifier());
			}
			sender.sendMessage(aClass.getName() + ": " + StringParser.joinSet(
					set, "; "));
		}
	}
	
	@Override
	public void lateJoin(Player player) {
		if (!superMap.containsKey(arena)) {
			init_map();
		}
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		debug.i("respawning player " + String.valueOf(ap), player);
		HashMap<ArenaClass, HashSet<PotionEffect>> map = superMap.get(arena);
		if (map == null) {
			PVPArena.instance.getLogger().warning("No superMap entry for arena " + arena.toString());
			return;
		}
		
		ArenaClass c = ap.getArenaClass();
		
		HashSet<PotionEffect> ape = map.get(c);
		if (ape == null) {
			debug.i("no effects for team " + String.valueOf(c), player);
			return;
		}
		for (PotionEffect pe : ape) {
			debug.i("adding " + pe.getType(), player);
			player.addPotionEffect(pe);
		}
	}
	
	private void init_map() {
		HashMap<ArenaClass, HashSet<PotionEffect>> map = new HashMap<ArenaClass, HashSet<PotionEffect>>();
		
		superMap.put(arena, map);
		
		for (ArenaClass c : arena.getClasses()) {
			String s = (String) arena.getArenaConfig().getUnsafe("modules.betterclasses.permEffects." + c.getName());
			if (s == null) {
				continue;
			}
			HashSet<PotionEffect> ape = parseStringToPotionEffects(s);
			if (ape == null || ape.size() < 1) {
				continue;
			}
			map.put(c, ape);
		}
		
		for (ArenaPlayer ap : arena.getFighters()) {
			HashSet<PotionEffect> ape = map.get(ap.getArenaClass());
			if (ape == null) {
				continue;
			}
			for (PotionEffect pe : ape) {
				ap.get().addPotionEffect(pe);
			}
		}
	}
	
	private boolean notEnoughEXP(Player player, String className) {
		int needed = 0;
		
		try {
			needed = (Integer) arena.getArenaConfig().getUnsafe("modules.betterclasses.neededEXPLevel." + className);
		} catch (Exception e) {
			return false;
		}
		
		return player.getLevel() < needed;
	}

	private String parsePotionEffectsToString(HashSet<PotionEffect> ape) {
		HashSet<String> result = new HashSet<String>();
		for (PotionEffect pe : ape) {
			result.add(pe.getType().getName() + ":" + pe.getAmplifier());
		}
		return StringParser.joinSet(result, ",");
	}
	
	public void parseRespawn(Player player, ArenaTeam team, DamageCause cause, Entity damager) {
		if (!superMap.containsKey(arena)) {
			init_map();
		}
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
		debug.i("respawning player " + String.valueOf(ap), player);
		HashMap<ArenaClass, HashSet<PotionEffect>> map = superMap.get(arena);
		if (map == null) {
			PVPArena.instance.getLogger().warning("No superMap entry for arena " + arena.toString());
			return;
		}
		
		ArenaClass c = ap.getArenaClass();
		
		HashSet<PotionEffect> ape = map.get(c);
		if (ape == null) {
			debug.i("no effects for team " + String.valueOf(c), player);
			return;
		}
		for (PotionEffect pe : ape) {
			debug.i("adding " + pe.getType(), player);
			player.addPotionEffect(pe);
		}
	}
	
	@Override
	public void parseStart() {
		if (!superMap.containsKey(arena)) {
			init_map();
		}
		for (ArenaPlayer ap : arena.getFighters()) {
			parseRespawn(ap.get(), null, null, null);
		}
	}

	private HashSet<PotionEffect> parseStringToPotionEffects(String s) {
		HashSet<PotionEffect> spe = new HashSet<PotionEffect>();
		
		if (s == null || s.equals("none") || s.equals("")) {
			return spe;
		}
		
		String current = null;
		
		try {
			String[] ss = s.split(",");
			for (String sss : ss) {
				current = sss;
				
				String[] values = sss.split(":");
				
				PotionEffectType type = PotionEffectType.getByName(values[0].toUpperCase());
				
				int amp = values.length < 2 ? 1 : Integer.parseInt(values[1]);
				
				PotionEffect pe = new PotionEffect(type, 2147000, amp-1);
				spe.add(pe);
			}
		} catch (Exception e) {
			PVPArena.instance.getLogger().warning("error while parsing POTION EFFECT DEFINITION \"" + s + "\" : " + current);
		}
		
		return spe;
	}

	private void printHelp(Arena arena, CommandSender sender) {
		arena.msg(sender, "/pa [arenaname] !bc [classname] | list potion effects");
		arena.msg(sender, "/pa [arenaname] !bc [classname] clear | clear potion effects");
		arena.msg(sender, "/pa [arenaname] !bc [classname] add [type] [amp] | add potion effect");
		arena.msg(sender, "/pa [arenaname] !bc [classname] remove [type] | remove potion effect");
	}
}
