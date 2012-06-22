package net.slipcor.pvparena.modules.forcestart;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.neworder.ArenaModule;
import net.slipcor.pvparena.runnables.StartRunnable;

public class ForceStart extends ArenaModule {
	public ForceStart() {
		super("ForceStart");
	}

	@Override
	public String version() {
		return "v0.8.9.0";
	}

	@Override
	public void addSettings(HashMap<String, String> types) {
		types.put("forcestart.forcestart", "boolean");
		types.put("forcestart.seconds", "int");
	}

	@Override
	public void commitCommand(Arena arena, CommandSender sender, String[] args) {

		if (!args[0].startsWith("forcestart")) {
			return;
		}
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			Arenas.tellPlayer(sender,
					Language.parse("nopermto", Language.parse("admin")), arena);
			return;
		}

		startArena(arena, 5);
		Arenas.tellPlayer(sender, Language.parse("forcestart"));
		return;
	}

	@Override
	public void configParse(Arena arena, YamlConfiguration config, String type) {
		config.addDefault("forcestart.forcestart", Boolean.valueOf(false));
		config.addDefault("forcestart.seconds", Integer.valueOf(30));
		config.options().copyDefaults(true);
	}

	public void initLanguage(YamlConfiguration config) {
		config.addDefault("lang.forcestart", "Arena force started!");
	}

	@Override
	public boolean parseCommand(String cmd) {
		return cmd.startsWith("forcestart");
	}

	@Override
	public void parseInfo(Arena arena, CommandSender player) {
		player.sendMessage("");
		player.sendMessage("§6ForceStart:§f "
				+ StringParser.colorVar(arena.cfg
						.getBoolean("forcestart.forcestart"))
				+ "("
				+ StringParser.colorVar(arena.cfg
						.getInt("forcestart.seconds")) + ")");
	}

	@Override
	public void reset(Arena arena, boolean force) {
		if (arena.cfg.getBoolean("forcestart.forcestart")) {
			startArena(arena, arena.cfg.getInt("forcestart.seconds"));
		}
	}

	private void startArena(Arena arena, int i) {
		StartRunnable fr = new StartRunnable(arena, i, i);
		fr.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance, fr, 20L, 20L));
	}
}
