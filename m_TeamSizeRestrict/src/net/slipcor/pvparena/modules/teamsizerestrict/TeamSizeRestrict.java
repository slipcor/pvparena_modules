package net.slipcor.pvparena.modules.teamsizerestrict;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.PAG_Leave;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;

public class TeamSizeRestrict extends ArenaModule {
	public TeamSizeRestrict() {
		super("TeamSizeRestrict");
	}
	
	int priority = 10;
	
	@Override
	public String version() {
		return "v1.0.9.281";
	}
	
	@Override
	public void parseJoin(final CommandSender sender, final ArenaTeam team) {
		try {
			Integer i = Integer.parseInt(arena.getArenaConfig().getUnsafe("modules.teamsize."+team.getName()).toString());
			if (team.getTeamMembers().size() > i) {
				class RunLater implements Runnable {

					@Override
					public void run() {
						arena.msg(sender, ChatColor.RED + Language.parse(MSG.ERROR_JOIN_TEAM_FULL));
						new PAG_Leave().commit(arena, sender, new String[0]);
					}
					
				}
				
				Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 1L);
				
			}
		} catch (Exception e) {
			arena.getArenaConfig().setManually("modules.teamsize."+team.getName(), -1);
			arena.getArenaConfig().save();
		}
	}
}
