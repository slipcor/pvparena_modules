
package net.slipcor.pvparena.modules.points;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.events.PAGoalEvent;
import net.slipcor.pvparena.events.PAPlayerClassChangeEvent;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaModuleManager;

public class Points extends ArenaModule implements Listener {
	private static final Map<String, Double> globalpoints = new HashMap<String, Double>();
	private final Map<String, Double> points = new HashMap<String, Double>();
	private static YamlConfiguration globalconfig = null;
	private static File gcf = null;
	
	public Points() {
		super("Points");
	}

	@Override
	public String version() {
		return "v1.2.3.476";
	}
	
	@Override
	public void configParse(YamlConfiguration config) {
		Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
		for (ArenaClass aClass : arena.getClasses()) {
			config.addDefault("modules.points.classes."+aClass.getName(), 0d);
		}
		
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_POINTS_GLOBAL)) {
			if (globalconfig == null) {
				gcf = new File(PVPArena.instance.getDataFolder(), "points.yml");

				if (!gcf.exists()) {
					try {
						gcf.createNewFile();
						globalconfig = YamlConfiguration.loadConfiguration(gcf);
						globalconfig.addDefault("slipcor", 0d);
						globalconfig.save(gcf);
					} catch (IOException e) {
						globalconfig = null;
						e.printStackTrace();
					}
				} else {
					globalconfig = YamlConfiguration.loadConfiguration(gcf);
				}

                if (config.get("modules.points.players") == null) {
                    return;
                }

                ConfigurationSection cs = config.getConfigurationSection("modules.points.players");
				for(String playerName : globalconfig.getKeys(false)) {
					globalpoints.put(playerName, cs.getDouble(playerName));
				}
			}
		} else {
            if (config.get("modules.points.players") == null) {
                return;
            }

            ConfigurationSection cs = config.getConfigurationSection("modules.points.players");
			for (String playerName : cs.getKeys(false)) {
				points.put(playerName, cs.getDouble(playerName));
			}
		}
		
	}

	@Override
	public void displayInfo(CommandSender player) {
		player.sendMessage(StringParser.colorVar(
				"global",arena.getArenaConfig().getBoolean(
						CFG.MODULES_POINTS_GLOBAL)));
	}
	
	@Override
	public void resetPlayer(Player player, boolean force) {
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_POINTS_GLOBAL)) {
			globalconfig.set(player.getName(), globalpoints.get(player.getName()));
		} else {
			arena.getArenaConfig().setManually("modules.points.players."+player.getName(), points.get(player.getName()));
		}
	}
	
	@Override
	public void reset(boolean force) {
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_POINTS_GLOBAL)) {
			try {
				globalconfig.save(gcf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			arena.getArenaConfig().save();
		}
		
	}
	
	@Override
	public boolean cannotSelectClass(Player player, String className) {
		Object o = arena.getArenaConfig().getUnsafe("modules.points.classes."+className);
		
		if (o == null) {
			// no requirement, out!
			return false;
		}
		
		double d = (Double) o;
		
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_POINTS_GLOBAL)) {
			if (globalpoints.containsKey(player.getName())) {
				return globalpoints.get(player.getName()) < d;
			}
		} else {
			if (points.containsKey(player.getName())) {
				return points.get(player.getName()) < d;
			}
		}
		return d > 0d;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onClassChange(PAPlayerClassChangeEvent event) {
		if (event.getArenaClass() == null) {
			return;
		}
		Object o = arena.getArenaConfig().getUnsafe("modules.points.classes."+event.getArenaClass().getName());
		
		if (o == null) {
			// no requirement, out!
			return;
		}
		
		double d = (Double) o;
		
		remove(event.getPlayer().getName(), d);
	}

	@EventHandler
	public void onGoalScore(PAGoalEvent event) {
		
		String lastTrigger = "";
		
		
		if (event.getArena().equals(arena)) {
			arena.getDebugger().i("[POINTS] it's us!");
			String[] contents = event.getContents();
			
			for (String node : contents) {
				node = node.toLowerCase();
				if (node.contains("trigger")) {
					lastTrigger = node.substring(8);
					newReward(lastTrigger, "TRIGGER");
				}
				
				if (node.contains("playerDeath")) {
					newReward(node.substring(12), "DEATH");
				}
				
				if (node.contains("playerKill")) {
					String[] val = node.split(":");
					if (!val[1].equals(val[2])) {
						newReward(val[1], "KILL");
					}
				}
				
				if (node.contains("score")) {
					String[] val = node.split(":");
					newReward(val[1], "SCORE", Integer.parseInt(val[3]));
				}
				
				if (node.equals("") && !lastTrigger.equals("")) {
					newReward(lastTrigger, "WIN");
				}
			}
			
		}
	}

	private void newReward(String playerName, String rewardType) {
		if (playerName == null || playerName.length() < 1) {
			PVPArena.instance.getLogger().warning("winner is empty string in " + arena.getName());
			return;
		}
		arena.getDebugger().i("new Reward: " + playerName + " -> " + rewardType);
		newReward(playerName, rewardType, 1);
	}

	private void newReward(String playerName, String rewardType, int amount) {
		if (playerName == null || playerName.length() < 1) {
			PVPArena.instance.getLogger().warning("winner is empty string in " + arena.getName());
			return;
		}
		arena.getDebugger().i("new Reward: " + amount + "x "+ playerName + " -> " + rewardType);
		try {
			
			double value = arena.getArenaConfig().getDouble(
					CFG.valueOf("MODULES_POINTS_REWARD_"+rewardType), 0d);
			
			double maybevalue = arena.getArenaConfig().getDouble(
					CFG.valueOf("MODULES_POINTS_REWARD_"+rewardType), -1d);

			if (maybevalue < 0) {
				PVPArena.instance.getLogger().warning("config value is not set: " + CFG.valueOf("MODULES_POINTS_REWARD_"+rewardType).getNode());
			}

			arena.getDebugger().i("9 depositing " + value + " to " + playerName);
			if (value > 0) {
				add(playerName, value);
				try {
					
					ArenaModuleManager.announce(
							arena,
							Language.parse(MSG.NOTICE_PLAYERAWARDED,
									value + " points"), "PRIZE");
					arena.msg(Bukkit.getPlayer(playerName), Language
							.parse(MSG.MODULE_VAULT_YOUWON, value + " points"));
							
				} catch (Exception e) {
					// nothing
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void add(String playerName, double value) {
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_POINTS_GLOBAL)) {
			if (globalpoints.containsKey(playerName)) {
				double d = globalpoints.get(playerName);
				globalpoints.put(playerName, value+d);
			} else {
				globalpoints.put(playerName, value);
			}
		} else {
			if (points.containsKey(playerName)) {
				double d = points.get(playerName);
				points.put(playerName, value+d);
			} else {
				points.put(playerName, value);
			}
		}
	}
	
	private void remove(String playerName, double value) {
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_POINTS_GLOBAL)) {
			if (globalpoints.containsKey(playerName)) {
				double d = globalpoints.get(playerName);
				globalpoints.put(playerName, d-value);
			}
		} else {
			if (points.containsKey(playerName)) {
				double d = points.get(playerName);
				points.put(playerName, d-value);
			}
		}
	}
}
