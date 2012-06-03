package net.slipcor.pvparena.modules.maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Spawns;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;
import org.bukkit.map.MinecraftFont;

public class MyRenderer extends MapRenderer {
	static HashMap<ChatColor, Byte> colors = new HashMap<ChatColor, Byte>();
	private String playerName;
	private Arena arena;
	private static YamlConfiguration playerMaps;
	private boolean showPlayers;
	private boolean showSpawns;
	private boolean showLives;

	public MyRenderer() {
		playerName = null;
		arena = null;
	}

	static {
		colors.put(ChatColor.AQUA, MapPalette.matchColor(0, 255, 255));
		colors.put(ChatColor.BLACK, MapPalette.matchColor(0, 0, 0));
		colors.put(ChatColor.BLUE, MapPalette.matchColor(0, 0, 255));
		colors.put(ChatColor.DARK_AQUA, MapPalette.matchColor(0, 128, 128));
		colors.put(ChatColor.DARK_BLUE, MapPalette.matchColor(0, 0, 128));
		colors.put(ChatColor.DARK_GRAY, MapPalette.matchColor(128, 128, 128));
		colors.put(ChatColor.DARK_GREEN, MapPalette.matchColor(0, 128, 0));
		colors.put(ChatColor.DARK_PURPLE, MapPalette.matchColor(128, 0, 128));
		colors.put(ChatColor.DARK_RED, MapPalette.matchColor(128, 0, 0));
		colors.put(ChatColor.GOLD, MapPalette.matchColor(128, 128, 0));
		colors.put(ChatColor.GRAY, MapPalette.matchColor(192, 192, 192));
		colors.put(ChatColor.GREEN, MapPalette.matchColor(0, 255, 0));
		colors.put(ChatColor.LIGHT_PURPLE, MapPalette.matchColor(255, 0, 255));
		colors.put(ChatColor.RED, MapPalette.matchColor(255, 0, 0));
		colors.put(ChatColor.WHITE, MapPalette.matchColor(255, 255, 255));
		colors.put(ChatColor.YELLOW, MapPalette.matchColor(255, 255, 0));

		new File("plugins/pvparena").mkdir();
		File configFile = new File("plugins/pvparena/maps.yml");
		if (!(configFile.exists())) {
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				Bukkit.getLogger().severe(
						"[PVP Arena] Error when creating map file.");
			}
		}

		playerMaps = new YamlConfiguration();
		try {
			playerMaps.load(configFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
	}

	private static void savePlayers() {
		try {
			playerMaps.save(new File("plugins/pvparena/maps.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static HashSet<Short> done = new HashSet<Short>();

	@Override
	public void render(MapView map, MapCanvas canvas, Player player) {
		if (playerName == null) {
			// map.removeRenderer(this);
			// eventual first initialisation
			if (Maps.hasCustomMap(player.getName()) && !done.contains(map.getId())) {
				
				playerName = player.getName();
				arena = Arenas.getArenaByPlayer(player);
				if ((playerMaps.get(player.getName()) == null)) {
					playerMaps.set(player.getName(), map.getId());
					savePlayers();
				} else if (playerMaps.getInt(playerName) != map.getId()) {
					// wrong id, return
					return;
				}

				map.setScale(Scale.CLOSE);

				done.add(map.getId());

				showSpawns = arena.cfg.getBoolean("maps.showSpawns", Boolean.valueOf(true));
				showPlayers = arena.cfg.getBoolean("maps.showPlayers", Boolean.valueOf(true));
				showLives = arena.cfg.getBoolean("maps.showLives", Boolean.valueOf(true));
			}
			return;
		}
		
		if (!player.getName().equals(playerName)) {
			return;
		}
		
		if (arena != null && arena.cfg.getBoolean("maps.playerPosition", false)) {
		
			map.setCenterX(player.getLocation().getBlockX());
			map.setCenterZ(player.getLocation().getBlockZ());
		} else if (arena != null) {
			Location loc = Spawns.getRegionCenter(arena);
			map.setCenterX(loc.getBlockX());
			map.setCenterZ(loc.getBlockZ());
		} else {
			System.out.print("arena null");
		}
		int mapcenterx = map.getCenterX();
		int mapcenterz = map.getCenterZ();

		for (int x = 0; x < 128; x++) {
			for (int z = 0; z < 128; z++) {
				canvas.setPixel(x, z, (byte) -1);
			}
		}

		HashSet<MapItem> items = Maps.getItems(arena);
		
		if (showSpawns) {
		
			for (MapItem item : items) {
				if (item.isPlayer()) {
					continue;
				}
				
				// JUST SPAWNS
				
				byte color = colors.get(item.getColor());
	
				byte outline;
	
				outline = color;
	
				int mapX = ((item.getX() - mapcenterx) / 2) + 64;
				int mapZ = ((item.getZ() - mapcenterz) / 2) + 64;
	
				if ((mapX >= 1) && (mapX < 127) && (mapZ >= 1) && (mapZ < 127)) {
					canvas.setPixel(mapX, mapZ, color);
	
					canvas.setPixel(mapX - 1, mapZ - 1, outline);
					canvas.setPixel(mapX, mapZ - 1, outline);
					canvas.setPixel(mapX + 1, mapZ - 1, outline);
					canvas.setPixel(mapX - 1, mapZ, outline);
					canvas.setPixel(mapX + 1, mapZ, outline);
					canvas.setPixel(mapX - 1, mapZ + 1, outline);
					canvas.setPixel(mapX, mapZ + 1, outline);
					canvas.setPixel(mapX + 1, mapZ + 1, outline);
				}
			}
		}

		if (showPlayers) {
			for (MapItem item : items) {
				if (!item.isPlayer()) {
					continue;
				}
				
				// JUST PLAYERS
				
				byte color = colors.get(item.getColor());
	
				byte outline;
	
				outline = MapPalette.matchColor(0, 0, 0);
	
				int mapX = ((item.getX() - mapcenterx) / 2) + 64;
				int mapZ = ((item.getZ() - mapcenterz) / 2) + 64;
	
				if ((mapX >= 1) && (mapX < 127) && (mapZ >= 1) && (mapZ < 127)) {
					canvas.setPixel(mapX, mapZ, color);
					if (item.getName().equals(playerName)) {
						continue;
					}
	
					canvas.setPixel(mapX - 1, mapZ - 1, outline);
					canvas.setPixel(mapX, mapZ - 1, outline);
					canvas.setPixel(mapX + 1, mapZ - 1, outline);
					canvas.setPixel(mapX - 1, mapZ, outline);
					canvas.setPixel(mapX + 1, mapZ, outline);
					canvas.setPixel(mapX - 1, mapZ + 1, outline);
					canvas.setPixel(mapX, mapZ + 1, outline);
					canvas.setPixel(mapX + 1, mapZ + 1, outline);
				}
			}
		}
		
		if (!showLives) {
			return;
		}
		
		HashMap<String, Integer> lives = new HashMap<String, Integer>();
		
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getName().equals("free")) {
				continue;
			}
			for (ArenaPlayer ap : team.getTeamMembers()) {
				

				lives.put(team.getName(), arena.type().getLives(ap.get()));
				break;
			}
		}
		
		String string = "";
		
		for (String s : lives.keySet()) {
			if (!string.equals("")) {
				string += " | ";
			}
			string += calculate(s, lives.get(s));
		}
		try {
			canvas.drawText(0, 10, MinecraftFont.Font, string);
		} catch (Exception e) {
			canvas.drawText(0, 10, MinecraftFont.Font, "invalid team name");
		}
	}

	private String calculate(String s, Integer i) {
		return s + ": " + binaryToRoman(i);
	}
	
	// Parallel arrays used in the conversion process.
    private static final String[] RCODE = {"M", "CM", "D", "CD", "C", "XC", "L",
                                           "XL", "X", "IX", "V", "IV", "I"};
    private static final int[]    BVAL  = {1000, 900, 500, 400,  100,   90,  50,
                                           40,   10,    9,   5,   4,    1};
    
    private String binaryToRoman(int binary) {
        String roman = "";         // Roman notation will be accumualated here.
        
        // Loop from biggest value to smallest, successively subtracting,
        // from the binary value while adding to the roman representation.
        for (int i = 0; i < RCODE.length; i++) {
            while (binary >= BVAL[i]) {
                binary -= BVAL[i];
                roman  += RCODE[i];
            }
        }
        return roman;
    }  

	public static Short getId(String sPlayer) {
		if (playerMaps.get(sPlayer) == null) {
			return Short.MIN_VALUE;
		}
		return (short) playerMaps.getInt(sPlayer);
	}
}