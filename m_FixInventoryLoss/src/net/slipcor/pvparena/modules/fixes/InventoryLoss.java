package net.slipcor.pvparena.modules.fixes;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.loadables.ArenaModule;

public class InventoryLoss extends ArenaModule {

	public InventoryLoss() {
		super("FixInventoryLoss");
	}

	@Override
	public String version() {
		return "v0.9.0.0";
	}

	@Override
	public PACheckResult checkJoin(Arena arena, CommandSender sender,
			PACheckResult res, boolean b) {
		Player player = (Player) sender;
		int priority = 5;
		
		if (res.hasError() || res.getPriority() > priority) {
			return res;
		}
		
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_FIXINVENTORYLOSS_GAMEMODE)) {
			if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
				ArenaManager.tellPlayer(player, Language.parse(MSG.MODULE_FIXINVENTORYLOSS_GAMEMODE));
				return res;
			}
		}
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_FIXINVENTORYLOSS_INVENTORY)) {
			for (ItemStack item : player.getInventory().getContents()) {
				if (item != null && !item.getType().equals(Material.AIR)) {
					res.setModName(getName());
					res.setError(Language.parse(MSG.MODULE_FIXINVENTORYLOSS_INVENTORY));
					return res;
				}
			}
			for (ItemStack item : player.getInventory().getArmorContents()) {
				if (item != null && !item.getType().equals(Material.AIR)) {
					res.setModName(getName());
					res.setError(Language.parse(MSG.MODULE_FIXINVENTORYLOSS_INVENTORY));
					return res;
				}
			}
		}
		return res;
	}

	@Override
	public void displayInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6FixInventoryLoss:§f "
				+ StringParser.colorVar("emptyInventory", arena.getArenaConfig().getBoolean(CFG.MODULES_FIXINVENTORYLOSS_INVENTORY))
				+ " || "
				+ StringParser.colorVar("gamemodeSurvival", arena.getArenaConfig().getBoolean(CFG.MODULES_FIXINVENTORYLOSS_GAMEMODE)));
	}
	
	@Override
	public boolean isActive(Arena a) {
		return true;
	}
}
