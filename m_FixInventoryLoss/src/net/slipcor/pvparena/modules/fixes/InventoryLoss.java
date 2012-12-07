package net.slipcor.pvparena.modules.fixes;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.PAA__Command;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.loadables.ArenaModule;

public class InventoryLoss extends ArenaModule {

	public InventoryLoss() {
		super("FixInventoryLoss");
	}

	@Override
	public String version() {
		return "v0.10.0.0";
	}
	
	@Override
	public boolean checkCommand(String s) {
		return s.equals("!fil") || s.equals("fixinventoryloss");
	}

	@Override
	public PACheck checkJoin(CommandSender sender,
			PACheck res, boolean b) {
		Player player = (Player) sender;
		int priority = 5;
		
		if (res.hasError() || res.getPriority() > priority) {
			return res;
		}
		
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_FIXINVENTORYLOSS_GAMEMODE)) {
			if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
				res.setError(this, Language.parse(MSG.MODULE_FIXINVENTORYLOSS_GAMEMODE));
				return res;
			}
		}
		if (arena.getArenaConfig().getBoolean(CFG.MODULES_FIXINVENTORYLOSS_INVENTORY)) {
			for (ItemStack item : player.getInventory().getContents()) {
				if (item != null && !item.getType().equals(Material.AIR)) {
					res.setError(this, Language.parse(MSG.MODULE_FIXINVENTORYLOSS_INVENTORY));
					return res;
				}
			}
			for (ItemStack item : player.getInventory().getArmorContents()) {
				if (item != null && !item.getType().equals(Material.AIR)) {
					res.setError(this, Language.parse(MSG.MODULE_FIXINVENTORYLOSS_INVENTORY));
					return res;
				}
			}
		}
		return res;
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		// !fil [value]
		
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			arena.msg(
					sender,
					Language.parse(MSG.ERROR_NOPERM,
							Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}

		if (!PAA__Command.argCountValid(sender, arena, args, new Integer[] { 2 })) {
			return;
		}
		
		CFG c = null;
		
		if (args[1].equals("gamemode")) {
			c = CFG.MODULES_FIXINVENTORYLOSS_GAMEMODE;
		} else if (args[1].equals("inventory")) {
			c = CFG.MODULES_FIXINVENTORYLOSS_INVENTORY;
		}
		
		if (c == null) {
			arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "gamemode | inventory"));
			return;
		}
		
		boolean b = arena.getArenaConfig().getBoolean(c);
		arena.getArenaConfig().set(c, !b);
		arena.getArenaConfig().save();
		arena.msg(sender, Language.parse(MSG.SET_DONE, c.getNode(), String.valueOf(!b)));
		
	}

	@Override
	public void displayInfo(CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6FixInventoryLoss:§f "
				+ StringParser.colorVar("inventory", arena.getArenaConfig().getBoolean(CFG.MODULES_FIXINVENTORYLOSS_INVENTORY))
				+ " || "
				+ StringParser.colorVar("gamemode", arena.getArenaConfig().getBoolean(CFG.MODULES_FIXINVENTORYLOSS_GAMEMODE)));
	}
}
