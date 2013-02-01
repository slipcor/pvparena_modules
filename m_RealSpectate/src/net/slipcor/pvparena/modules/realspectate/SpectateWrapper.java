package net.slipcor.pvparena.modules.realspectate;

import java.util.HashSet;

import net.minecraft.server.v1_4_6.EntityPlayer;
import net.minecraft.server.v1_4_6.Packet16BlockItemSwitch;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.managers.InventoryManager;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SpectateWrapper {
	final private Player suspect;
	final private HashSet<Player> spectators = new HashSet<Player>();
	final private RealSpectateListener listener;
	
	public SpectateWrapper(Player spectator, Player fighter, RealSpectateListener listener) {
		suspect = fighter;
		spectators.add(spectator);
		ArenaPlayer.parsePlayer(spectator.getName()).setTelePass(true);
		this.listener = listener;
	}

	public void update(final Player s) {
		if (!spectators.contains(s)) {
			spectators.add(s);

			class LaterRun implements Runnable {
				@Override
				public void run() {
					s.setHealth(suspect.getHealth()>0?suspect.getHealth():1);

					InventoryManager.clearInventory(s);
					s.getInventory().setArmorContents(suspect.getInventory().getArmorContents());
					s.getInventory().setContents(suspect.getInventory().getContents());
					s.updateInventory();
					
					s.teleport(suspect.getLocation());
					
					for (ArenaPlayer ap : listener.rs.getArena().getEveryone()) {
						ap.get().hidePlayer(s);
					}
					s.hidePlayer(suspect);
				}
			}
			Bukkit.getScheduler().runTaskLater(PVPArena.instance, new LaterRun(), 5L);
		}
	}

	public void update() {
		for (Player s : spectators) {

			class LaterRun implements Runnable {
				private final Player s;
				LaterRun(final Player p) {
					s = p;
				}
				@Override
				public void run() {
					s.setHealth(suspect.getHealth()>0?suspect.getHealth():1);

					InventoryManager.clearInventory(s);
					s.getInventory().setArmorContents(suspect.getInventory().getArmorContents());
					s.getInventory().setContents(suspect.getInventory().getContents());
					s.updateInventory();
					
					s.teleport(suspect.getLocation());
					
					for (ArenaPlayer ap : listener.rs.getArena().getEveryone()) {
						ap.get().hidePlayer(s);
					}
					s.hidePlayer(suspect);
				}
			}
			Bukkit.getScheduler().runTaskLater(PVPArena.instance, new LaterRun(s), 5L);
		}
	}

	public Player getSuspect() {
		return suspect;
	}

	public boolean hasSpectator(Player p) {
		return spectators.contains(p);
	}
	
	/*
	 * ------------------------------------
	 * ------------------------------------
	 * ------------------------------------
	 * 
	 */
	

	public void closeInventory(Inventory inventory) {
		for (Player p : spectators) {
			p.closeInventory();
		}
	}

	public void openInventory(Inventory inventory) {
		for (Player p : spectators) {
			p.openInventory(inventory);
		}
	}

	public void removeSpectator(Player spectator) {
		spectators.remove(spectator);
		if (spectators.size() < 1) {
			listener.spectated_players.remove(suspect);
		}
	}

	public void selectItem(int newSlot) {
		
		for (Player p : spectators) {
			CraftPlayer cp = (CraftPlayer) p;
			EntityPlayer player = (EntityPlayer) cp.getHandle();
			player.playerConnection.sendPacket(new Packet16BlockItemSwitch(newSlot));
		}
	}

	public void stopSpectating() {
		for (Player p : spectators) {
			if (listener.spectated_players.size() < 1) {
				Bukkit.getServer().dispatchCommand(p, "pa leave");
			} else {
				listener.switchPlayer(p, suspect, true);
			}
		}
		spectators.clear();
	}

	public void updateHealth() {
		class LaterRun implements Runnable {
			@Override
			public void run() {
				for (Player p : spectators) {
					p.setHealth(suspect.getHealth()>0?suspect.getHealth():1);
				}
			}
		}
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new LaterRun(), 5L);
	}

	public void updateInventory() {
		class LaterRun implements Runnable {
			@Override
			public void run() {
				for (Player p : spectators) {
					InventoryManager.clearInventory(p);
					p.getInventory().setArmorContents(suspect.getInventory().getArmorContents());
					p.getInventory().setContents(suspect.getInventory().getContents());
					p.updateInventory();
				}
			}
		}
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new LaterRun(), 5L);
	}

	public void updateLocation() {
		class LaterRun implements Runnable {
			@Override
			public void run() {
				for (Player p : spectators) {
					p.teleport(suspect.getLocation());
				}
			}
		}
		Bukkit.getScheduler().runTaskLater(PVPArena.instance, new LaterRun(), 5L);
	}

	public void stopHard() {
		for (Player p : spectators) {
			Bukkit.getServer().dispatchCommand(p, "pa leave");
		}
	}
}
