package net.slipcor.pvparena.modules.powerups;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaRegionShape;
import net.slipcor.pvparena.loadables.ArenaRegionShape.RegionType;

public class PowerupManager extends ArenaModule {

	
	protected HashMap<Arena, Powerups> usesPowerups = new HashMap<Arena, Powerups>();

	private int powerupDiff = 0;
	private int powerupDiffI = 0;

	protected int SPAWN_ID = -1;

	public PowerupManager() {
		super("PowerUps");
	}
	
	@Override
	public String version() {
		return "v0.9.0.0";
	}

	/**
	 * calculate a powerup and commit it
	 */
	protected void calcPowerupSpawn(Arena arena) {
		db.i("powerups?");
		if (!usesPowerups.containsKey(arena))
			return;

		Powerups pus = usesPowerups.get(arena);

		if (pus.puTotal.size() <= 0)
			return;

		db.i("totals are filled");
		Random r = new Random();
		int i = r.nextInt(pus.puTotal.size());

		for (Powerup p : pus.puTotal) {
			if (--i > 0)
				continue;
			commitPowerupItemSpawn(arena, p.item);
			arena.broadcast(Language.parse(MSG.MODULE_POWERUPS_SERVER, p.name));
			return;
		}

	}

	@Override
	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			Language.parse(MSG.ERROR_ONLY_PLAYERS);
			return;
		}
		if (!args[0].startsWith("powerup")) {
			return;
		}
		
		Player player = (Player) sender;
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			arena.msg(player,
					Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}
		SpawnManager.setCoords(arena, player, args[0]);
		ArenaManager.tellPlayer(player, Language.parse(MSG.SPAWN_SET, args[0]));
		return;
	}

	@Override
	public boolean commitEnd(Arena arena, ArenaTeam arg1) {
		if (usesPowerups.containsKey(arena)) {
			if (arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE).startsWith("death")) {
				db.i("calculating powerup trigger death");
				powerupDiffI = ++powerupDiffI % powerupDiff;
				if (powerupDiffI == 0) {
					calcPowerupSpawn(arena);
				}
			}
		}
		return false;
	}

	/**
	 * commit the powerup item spawn
	 * 
	 * @param item
	 *            the material to spawn
	 */
	protected void commitPowerupItemSpawn(Arena arena, Material item) {
		db.i("dropping item?");
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_POWERUPS_DROPSPAWN)) {
			dropItemOnSpawn(arena, item);
		} else {
			HashSet<ArenaRegionShape> ars = arena.getRegionsByType(RegionType.BATTLE);
			for (ArenaRegionShape ar : ars) {
				
				PABlockLocation min = ar.getMinimumLocation();
				PABlockLocation max = ar.getMaximumLocation();
				
				Random r = new Random();

				int x = r.nextInt(max.getX() - min.getX());
				int z = r.nextInt(max.getZ() - min.getZ());
				
				World w = Bukkit.getWorld(min.getWorldName());
				
				w.dropItem(w.getHighestBlockAt(min.getX() + x, min.getZ() + z).getRelative(BlockFace.UP).getLocation(), new ItemStack(item,1));
				
				break;
			}
		}
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		HashMap<String, Object> powerups = new HashMap<String, Object>();
		if (config.getConfigurationSection("powerups") != null) {
			HashMap<String, Object> map = (HashMap<String, Object>) config
					.getConfigurationSection("powerups").getValues(false);
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			HashMap<String, Object> map3 = new HashMap<String, Object>();
			db.i("parsing powerups");
			for (String key : map.keySet()) {
				// key e.g. "OneUp"
				map2 = (HashMap<String, Object>) config
						.getConfigurationSection("powerups." + key).getValues(
								false);
				HashMap<String, Object> temp_map = new HashMap<String, Object>();
				for (String kkey : map2.keySet()) {
					// kkey e.g. "dmg_receive"
					if (kkey.equals("item")) {
						temp_map.put(kkey, String.valueOf(map2.get(kkey)));
						db.i(key + " => " + kkey + " => "
								+ String.valueOf(map2.get(kkey)));
					} else {
						db.i(key + " => " + kkey + " => "
								+ parseList(map3.values()));
						map3 = (HashMap<String, Object>) config
								.getConfigurationSection(
										"powerups." + key + "." + kkey)
								.getValues(false);
						temp_map.put(kkey, map3);
					}
				}
				powerups.put(key, temp_map);
			}
		}

		if (powerups.size() < 1) {
			return;
		}

		String pu = config.getString("game.powerups", "off");
		
		String[] ss = pu.split(":");
		if (pu.startsWith("death")) {
			powerupDiff = Integer.parseInt(ss[1]);
			usesPowerups.put(arena, new Powerups(powerups));
		} else if (pu.startsWith("time")) {
			powerupDiff = Integer.parseInt(ss[1]);
			usesPowerups.put(arena, new Powerups(powerups));
		} else {
			db.w("error activating powerup module");
		}

		config.addDefault("game.powerups", "off");
		config.options().copyDefaults(true);
	}

	/**
	 * drop an item at a powerup spawn point
	 * 
	 * @param item
	 *            the item to drop
	 */
	protected void dropItemOnSpawn(Arena arena, Material item) {
		db.i("calculating item spawn location");
		Location aim = SpawnManager.getCoords(arena, "powerup").add(0, 1, 0).toLocation();

		db.i("dropping item on spawn: " + aim.toString());
		Bukkit.getWorld(arena.getWorld()).dropItem(aim, new ItemStack(item, 1));

	}

	@Override
	public HashSet<String> getAddedSpawns() {
		HashSet<String> result = new HashSet<String>();

		result.add("powerup");
		
		return result;
	}
	
	@Override
	public void onEntityDamageByBlockDamage(Arena arena, Player defender,
			EntityDamageByEntityEvent event) {
		if (usesPowerups.containsKey(arena)) {
			db.i("committing powerup triggers");
			Powerups pus = usesPowerups.get(arena);
			Powerup p = pus.puActive.get(defender);
			if ((p != null) && (p.canBeTriggered()))
				p.commit(null, defender, event);

		}
	}

	@Override
	public void onEntityDamageByEntity(Arena arena, Player attacker,
			Player defender, EntityDamageByEntityEvent event) {
		if (usesPowerups.containsKey(arena)) {
			db.i("committing powerup triggers");
			Powerups pus = usesPowerups.get(arena);
			Powerup p = pus.puActive.get(attacker);
			if ((p != null) && (p.canBeTriggered()))
				p.commit(attacker, defender, event);

			p = pus.puActive.get(defender);
			if ((p != null) && (p.canBeTriggered()))
				p.commit(attacker, defender, event);

		}

	}

	@Override
	public void onEntityRegainHealth(Arena arena, EntityRegainHealthEvent event) {
		if (usesPowerups.containsKey(arena)) {
			db.i("regaining health");
			Powerups pus = usesPowerups.get(arena);
			Powerup p = pus.puActive.get((Player) event.getEntity());
			if (p != null) {
				if (p.canBeTriggered()) {
					if (p.isEffectActive(PowerupEffect.classes.HEAL)) {
						event.setCancelled(true);
						p.commit(event);
					}
				}
			}

		}
	}

	@Override
	public void onPlayerPickupItem(Arena arena, PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		if (usesPowerups.containsKey(arena)) {
			Powerups pus = usesPowerups.get(arena);
			db.i("onPlayerPickupItem: fighting player");
			Iterator<Powerup> pi = pus.puTotal.iterator();
			while (pi.hasNext()) {
				Powerup p = pi.next();
				if (event.getItem().getItemStack().getType().equals(p.item)) {
					Powerup newP = new Powerup(p);
					if (pus.puActive.containsKey(player)) {
						pus.puActive.get(player).disable();
					}
					pus.puActive.put(player, newP);
					arena.broadcast(Language.parse(MSG.MODULE_POWERUPS_PLAYER,
							player.getName(), newP.name));
					event.setCancelled(true);
					event.getItem().remove();
					if (newP.canBeTriggered())
						newP.activate(player); // activate for the first time

					return;
				}
			}
		}
	}
	
	@Override
	public void onPlayerVelocity(Arena arena, PlayerVelocityEvent event) {
		db.i("inPlayerVelocity: fighting player");
		if (usesPowerups.containsKey(arena)) {
			Powerups pus = usesPowerups.get(arena);
			Powerup p = pus.puActive.get(event.getPlayer());
			if (p != null) {
				if (p.canBeTriggered()) {
					if (p.isEffectActive(PowerupEffect.classes.JUMP)) {
						p.commit(event);
					}
				}
			}
		}
	}
	
	@Override
	public boolean parseCommand(String cmd) {
		return cmd.startsWith("powerup");
	}
	
	@Override
	public void parseInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6Powerups:§f "
				+ StringParser.colorVar(usesPowerups.containsKey(arena))
				+ "("
				+ StringParser.colorVar(arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE))
				+ ")");
	}

	/**
	 * turn a collection of objects into a comma separated string
	 * 
	 * @param values
	 *            the collection
	 * @return the comma separated string
	 */
	protected String parseList(Collection<Object> values) {
		String s = "";
		for (Object o : values) {
			if (!s.equals("")) {
				s += ",";
			}
			try {
				s += String.valueOf(o);
				db.i("a");
			} catch (Exception e) {
				db.i("b");
				s += o.toString();
			}
		}
		return s;
	}
	
	@Override
	public void parseMove(Arena arena, PlayerMoveEvent event) {

		// db.i("onPlayerMove: fighting player!");
		if (usesPowerups.containsKey(arena)) {
			//db.i("parsing move");
			Powerups pus = usesPowerups.get(arena);
			Powerup p = pus.puActive.get(event.getPlayer());
			if (p != null) {
				if (p.canBeTriggered()) {
					if (p.isEffectActive(PowerupEffect.classes.FREEZE)) {
						db.i("freeze in effect, cancelling!");
						event.setCancelled(true);
					}
					if (p.isEffectActive(PowerupEffect.classes.SPRINT)) {
						db.i("sprint in effect, sprinting!");
						event.getPlayer().setSprinting(true);
					}
					if (p.isEffectActive(PowerupEffect.classes.SLIP)) {
						//
					}
				}
			}
		}
	}

	/**
	 * powerup tick, tick each arena that uses powerups
	 */
	protected void powerupTick() {
		for (Arena arena : usesPowerups.keySet()) {
			db.i("ticking: arena " + arena.getName());
			usesPowerups.get(arena).tick();
		}
	}

	@Override
	public void reset(Arena arena, boolean force) {
		if (SPAWN_ID > -1)
			Bukkit.getScheduler().cancelTask(SPAWN_ID);
		SPAWN_ID = -1;
	}

	@Override
	public void teleportAllToSpawn(Arena arena) {
		if (usesPowerups.containsKey(arena)) {
			String pu = arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE);
			String[] ss = pu.split(":");
			if (pu.startsWith("time")) {
				// arena.powerupTrigger = "time";
				powerupDiff = Integer.parseInt(ss[1]);
			} else {
				return;
			}

			db.i("using powerups : "
					+ arena.getArenaConfig().getString(CFG.MODULES_POWERUPS_USAGE) + " : "
					+ powerupDiff);
			if (powerupDiff > 0) {
				db.i("powerup time trigger!");
				powerupDiff = powerupDiff * 20; // calculate ticks to seconds
				// initiate autosave timer
				SPAWN_ID = Bukkit
						.getServer()
						.getScheduler()
						.scheduleSyncRepeatingTask(PVPArena.instance,
								new PowerupRunnable(arena, this), powerupDiff,
								powerupDiff);
			}
		}
	}
}
