package net.slipcor.pvparena.modules.matchresultstats;

import java.sql.SQLException;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.modules.matchresultstats.JesiKat.MySQLConnection;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class MatchResultStats extends ArenaModule {
	
	public MatchResultStats() {
		super("MatchResultStats");
	}
	
	protected MySQLConnection sqlHandler; // MySQL handler

	// Settings Variables
	protected static String dbHost = null;
	protected static String dbUser = null;
	protected static String dbPass = null;
	protected static String dbDatabase = null;
	protected static String dbTable = null;
	protected static int dbPort = 3306;
	
	@Override
	public String version() {
		return "v1.1.0.298";
	}
	
	@Override
	public boolean checkCommand(String s) {
		return (s.equals("sqlstats") || s.equals("!ss"));
	}
	
	@Override
	public void commitCommand(CommandSender sender, String[] args) {
		// !ss reset
		// !ss reset [player]
		
		if (!PVPArena.hasAdminPerms(sender)
				&& !(PVPArena.hasCreatePerms(sender, arena))) {
			Arena.pmsg(sender,
					Language.parse(MSG.ERROR_NOPERM, Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
			return;
		}
		
		if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[] { 1,2 })) {
			return;
		}
		
		// TODO: do something
	}
	
	@Override
	public void configParse(YamlConfiguration config) {
		if (dbTable == null) {

			PVPArena.instance.getConfig().options().copyDefaults(true);
			PVPArena.instance.getConfig().addDefault("MySQLhost", "");
			PVPArena.instance.getConfig().addDefault("MySQLuser", "");
			PVPArena.instance.getConfig().addDefault("MySQLpass", "");
			PVPArena.instance.getConfig().addDefault("MySQLdb", "");
			PVPArena.instance.getConfig().addDefault("MySQLtable", "pvparena_stats");
			PVPArena.instance.getConfig().addDefault("MySQLport", 3306);
			PVPArena.instance.saveConfig();
			
 			dbHost = PVPArena.instance.getConfig().getString("MySQLhost", "");
 			dbUser = PVPArena.instance.getConfig().getString("MySQLuser", "");
 			dbPass = PVPArena.instance.getConfig().getString("MySQLpass", "");
 			dbDatabase = PVPArena.instance.getConfig().getString("MySQLdb", "");
 			dbTable = PVPArena.instance.getConfig().getString("MySQLtable", "pvparena_stats");
 			dbPort = PVPArena.instance.getConfig().getInt("MySQLport", 3306);
 			
 			if (sqlHandler == null) {
 				try {
 					sqlHandler = new MySQLConnection(dbTable, dbHost, dbPort, dbDatabase, dbUser,
 							dbPass);
 				} catch (InstantiationException e1) {
 					e1.printStackTrace();
 				} catch (IllegalAccessException e1) {
 					e1.printStackTrace();
 				} catch (ClassNotFoundException e1) {
 					e1.printStackTrace();
 				}
 	 			
 				arena.getDebugger().i("MySQL Initializing");
 	 			// Initialize MySQL Handler
 	 			
 				if (sqlHandler.connect(true)) {
 					arena.getDebugger().i("MySQL connection successful");
 	 				// Check if the tables exist, if not, create them
 					if (!sqlHandler.tableExists(dbDatabase,dbTable)) {
 						arena.getDebugger().i("Creating table "+dbTable);
 						final String query = "CREATE TABLE `"+dbTable+"` ( " +
 								"`id` int(16) NOT NULL AUTO_INCREMENT, " +
 								"`mid` int(8) not null default 0, " +
 								"`arena` varchar(42) NOT NULL, " +
 								"`playername` varchar(42) NOT NULL, " +
 								"`winning` int(1) not null default 0, " +
 								"`team` varchar(42) NOT NULL, " +
 								"`timespent` int(8) NOT NULL default 0, " +
 								"PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
 						/*
							 * 
							 * `mid`,
							 * `arena`,
							 * `playername`,
							 * `winning`,
							 * `team`,
							 * `timespent`
							 */
 						try {
 							sqlHandler.executeQuery(query, true);
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 					}
 				} else {
 					PVPArena.instance.getLogger().severe("MySQL connection failed");
 				}
 	 			MRSMySQL.initiate(this);
 			}
		}
	}
	
	private PVPData data = null;
	
	@Override
	public void giveRewards(final Player player) {
		data.winning(player.getName());
	}
	
	@Override
	public void parseJoin(final CommandSender sender, final ArenaTeam team) {
		if (data == null) {
			data = new PVPData(arena);
		}
		data.join(sender.getName(), team.getName());
	}
	
	@Override
	public void parsePlayerLeave(final Player player, final ArenaTeam team) {
		data.losing(player.getName());
	}
	
	@Override
	public void parseStart() {
		data.start();
	}
	
	@Override
	public void reset(boolean force) {
		data.reset(force);
	}
}
