package net.slipcor.pvparena.modules.eventactions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.commands.PAA_Edit;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.loadables.ArenaModule;

public class EventActions extends ArenaModule {
	
	public EventActions() {
		super("EventActions");
	}
	
	@Override
	public String version() {
		return "v0.9.6.16";
	}
	
	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		
		List<String> items = new ArrayList<String>();

		if (config.get("event") != null) {
			return;
		}
		
		items.add("cmd<=>deop %player%");
		items.add("brc<=>Join %arena%!");
		items.add("switch<=>switch1");
		items.add("msg<=>Welcome to %arena%!");

		arena.getArenaConfig().setManually("event.death", items);
		arena.getArenaConfig().setManually("event.end", items);
		arena.getArenaConfig().setManually("event.exit", items);
		arena.getArenaConfig().setManually("event.join", items);
		arena.getArenaConfig().setManually("event.kill", items);
		arena.getArenaConfig().setManually("event.leave", items);
		arena.getArenaConfig().setManually("event.lose", items);
		arena.getArenaConfig().setManually("event.start", items);
		arena.getArenaConfig().setManually("event.win", items);
		arena.getArenaConfig().save();
	}
	
	@Override
	public boolean isActive(Arena arena) {
		return arena.getArenaConfig().getBoolean(CFG.MODULES_EVENTACTIONS_ACTIVE);
	}
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new PAListener(this), PVPArena.instance);
	}

	protected void catchEvent(String string, Player p, Arena a) {
		
		if (a == null) {
			return;
		}
		
		if (a.getArenaConfig().getUnsafe("event." + string) == null) {
			return;
		}
		
		List<String> items = a.getArenaConfig().getStringList("event." + string, new ArrayList<String>());
		
		for (String item : items) {
			
			if (p != null) {
				item = item.replace("%player%", p.getName());
			}
			
			item = item.replace("%arena%", a.getName());
			item = ChatColor.translateAlternateColorCodes('&', item);
			
			String[] split = item.split("<=>");
			if (split.length != 2) {
				PVPArena.instance.getLogger().warning("[PE] skipping: [" + a.getName() + "]:event." + string + "=>" + item);
				continue;
			}
			/*
			items.add("cmd<=>deop %player%");
			items.add("brc<=>Join %arena%!");
			items.add("switch<=>switch1");
			items.add("msg<=>Welcome to %arena%!");
			 */
			if (split[0].equalsIgnoreCase("cmd")) {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), split[1]);
			} else if (split[0].equalsIgnoreCase("brc")) {
				Bukkit.broadcastMessage(split[1]);
			} else if (split[0].equalsIgnoreCase("switch")) {
				PABlockLocation loc = new PABlockLocation(SpawnManager.getCoords(a, split[1]).toLocation());
				
				System.out.print(loc.toLocation().toVector().toBlockVector().toString());
				/*
				PlayerInteractEvent e = new PlayerInteractEvent(p, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR,1), loc.getBlock(), BlockFace.SELF);
				Bukkit.getPluginManager().callEvent(e);*/

				MaterialData state = loc.toLocation().getBlock().getState().getData();
				
				if (state instanceof Lever) {
					((Lever)state).setPowered(true);
				} else if (state instanceof Button) {
					((Button)state).setPowered(true);
				}
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new EADelay(loc), 20L);
				
			} else if (split[0].equalsIgnoreCase("msg") && p != null) {
				p.sendMessage(split[1]);
			}
		}
	}
	
	@Override
	public boolean onPlayerInteract(PlayerInteractEvent event) {
		if (!event.hasBlock()) {
			return false;
		}
		db.i("interact eventactions");
		Arena a = PAA_Edit.activeEdits.get(event.getPlayer().getName());
		
		if (a != null) {
			db.i("found edit arena");
			Location loc = event.getClickedBlock().getLocation();
			MaterialData state = loc.getBlock().getState().getData();
			
			if ((state instanceof Lever) || (state instanceof Button)) {
				db.i("found lever/button");
				String s = "switch";
				int i = 0;
				for (String node : a.getArenaConfig().getKeys("spawns")) {
					if (node.startsWith(s)) {
						node = node.replace(s, "");
						if (Integer.parseInt(node) >= i) {
							i = Integer.parseInt(node)+1;
						}
					}
				}
				
				SpawnManager.setBlock(a, new PABlockLocation(loc), s+i);
				ArenaManager.tellPlayer(event.getPlayer(), Language.parse(MSG.SPAWN_SET, s+i));
				return true;
			}
		}
		
		return false;
	}
}
