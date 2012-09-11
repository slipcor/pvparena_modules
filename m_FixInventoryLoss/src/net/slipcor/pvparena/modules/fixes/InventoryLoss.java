package net.slipcor.pvparena.modules.fixes;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.loadables.ArenaModule;

public class InventoryLoss extends ArenaModule {

	public InventoryLoss() {
		super("FixInventoryLoss");
	}

	@Override
	public String version() {
		return "v0.7.25.0";
	}
	
	@Override
	public void addSettings(HashMap<String, String> types) {
		types.put("join.emptyInventory", "boolean");
		types.put("join.gamemodeSurvival", "boolean");
	}

	@Override
	public boolean checkJoin(Arena arena, Player player) {
		if (arena.getArenaConfig().getBoolean("join.gamemodeSurvival")) {
			if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
				ArenaManager.tellPlayer(player, Language.parse("gamemodeSurvival"));
				return false;
			}
		}
		if (arena.getArenaConfig().getBoolean("join.emptyInventory")) {
			for (ItemStack item : player.getInventory().getContents()) {
				if (item != null && !item.getType().equals(Material.AIR)) {
					ArenaManager.tellPlayer(player, Language.parse("emptyInventory"));
					return false;
				}
			}
			for (ItemStack item : player.getInventory().getArmorContents()) {
				if (item != null && !item.getType().equals(Material.AIR)) {
					ArenaManager.tellPlayer(player, Language.parse("emptyInventory"));
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void configParse(Arena arena, YamlConfiguration config) {
		config.addDefault("join.emptyInventory", Boolean.valueOf(false));
		config.addDefault("join.gamemodeSurvival", Boolean.valueOf(false));
		config.options().copyDefaults(true);
	}
	
	@Override
	public void initLanguage(YamlConfiguration config) {
		config.addDefault("lang.emptyInventory", "Clear your inventory before joining!");
		config.addDefault("lang.gamemodeSurvival", "Enter survival gamemode before joining!");
		config.options().copyDefaults(true);
	}

	@Override
	public void parseInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6FixInventoryLoss:§f "
				+ StringParser.colorVar("emptyInventory", arena.getArenaConfig().getBoolean("join.emptyInventory"))
				+ " || "
				+ StringParser.colorVar("gamemodeSurvival", arena.getArenaConfig().getBoolean("join.gamemodeSurvival")));
	}
}
