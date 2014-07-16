package net.slipcor.pvparena.modules.tempperms;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.events.PAPlayerClassChangeEvent;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.util.*;

public class TempPerms extends ArenaModule implements Listener {
    private boolean listening = false;

    public TempPerms() {
        super("TempPerms");
    }

    @Override
    public String version() {
        return "v1.3.0.510";
    }

    @Override
    public boolean checkCommand(String s) {
        return s.equals("!tps") || s.equals("tempperms");
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("tempperms");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!tps");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"add"});
        result.define(new String[]{"rem"});
        if (arena == null) {
            return result;
        }
        for (ArenaClass aClass : arena.getClasses()) {
            result.define(new String[]{aClass.getName(), "add"});
            result.define(new String[]{aClass.getName(), "rem"});
        }
        for (String team : arena.getTeamNames()) {
            result.define(new String[]{team, "add"});
            result.define(new String[]{team, "rem"});
        }
        return result;
    }

    @Override
    public void commitCommand(CommandSender sender, String[] args) {
        // !tps | list perms
        // !tps add [perm] | add perm
        // !tps rem [perm] | remove perm

        // !tps [classname] | list class perms
        // !tps [classname] add [perm] | add class perm
        // !tps [classname] rem [perm] | remove class perm

        // !tps [teamname] | list team perms
        // !tps [teamname] add [perm] | add team perm
        // !tps [teamname] rem [perm] | remove team perm

        if (!PVPArena.hasAdminPerms(sender)
                && !(PVPArena.hasCreatePerms(sender, arena))) {
            arena.msg(
                    sender,
                    Language.parse(MSG.ERROR_NOPERM,
                            Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{1, 2, 3, 4})) {
            return;
        }

        Map<String, Boolean> map = getTempPerms(arena, "default");

        if (args.length == 1) {
            arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_HEAD, "default"));
            for (String s : map.keySet()) {
                arena.msg(sender, s + " - " + StringParser.colorVar(map.get(s)));
            }
            return;
        }

        if (args.length == 3 && (args[1].equals("add") || args[1].equals("rem"))) {
            if (args[1].equals("add")) {
                map.put(args[2], true);
                arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_ADDED, args[2], "default"));
            } else {
                map.remove(args[2]);
                arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_REMOVED, args[2], "default"));
            }
            setTempPerms(arena, map, "default");
            return;
        }

        if (args.length == 3) {
            arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[1], "add | remove"));
            return;
        }

        map = getTempPerms(arena, args[1]);
        if (args.length == 2) {
            arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_HEAD, args[1]));
            for (String s : map.keySet()) {
                arena.msg(sender, s + " - " + StringParser.colorVar(map.get(s)));
            }
            return;
        }

        if (args.length == 4 && (args[2].equals("add") || args[2].equals("rem"))) {
            if (args[2].equals("add")) {
                map.put(args[3], true);
                arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_ADDED, args[3], args[1]));
            } else {
                map.remove(args[3]);
                arena.msg(sender, Language.parse(MSG.MODULE_TEMPPERMS_REMOVED, args[3], args[1]));
            }
            setTempPerms(arena, map, args[1]);
            return;
        }

        arena.msg(sender, Language.parse(MSG.ERROR_ARGUMENT, args[2], "add | remove"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onClassChange(PAPlayerClassChangeEvent event) {
        ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(event.getPlayer().getName());
        if (arena != null && arena.getEveryone().contains(aPlayer)) {
            Map<String, Boolean> classPerms =
                    (aPlayer.getArenaClass()) == null?
                            new HashMap<String, Boolean>():
                            this.getTempPerms(arena, aPlayer.getArenaClass().getName());
            this.removePermissions(event.getPlayer());

            setPermissions(arena, aPlayer, getTempPerms(arena, "default"), getTempPerms(arena, aPlayer.getArenaTeam().getName()));
        }
    }

    @Override
    public void parseJoin(CommandSender player, ArenaTeam team) {
        if (!listening) {
            Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
            listening = true;
        }
        ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
        setPermissions(arena, ap, getTempPerms(arena, "default"), getTempPerms(arena, ap.getArenaTeam().getName()));
    }

    /**
     * get the permissions map
     *
     * @return the temporary permissions map
     */
    private Map<String, Boolean> getTempPerms(Arena arena, String node) {
        Map<String, Boolean> result = new HashMap<String, Boolean>();

        if (arena.getArenaConfig().getYamlConfiguration().contains("perms." + node)) {
            List<String> list = arena.getArenaConfig().getStringList("perms." + node,
                    new ArrayList<String>());
            for (String key : list) {
                result.put(key.replace("-", "").replace("^", ""),
                        !(key.startsWith("^") || key.startsWith("-")));
            }
        }

        return result;
    }

    private void setTempPerms(Arena arena, Map<String, Boolean> map, String node) {
        List<String> result = new ArrayList<String>();
        for (String s : map.keySet()) {
            result.add((map.get(s) ? "" : "^") + s);
        }
        arena.getArenaConfig().setManually("perms." + node, result);
        arena.getArenaConfig().save();
    }

    private void setPermissions(Arena arena, ArenaPlayer ap, Map<String, Boolean> mGlobal, Map<String, Boolean> mTeam) {

        Map<String, Boolean> mClass;

        if (ap.getArenaClass() == null) {
            mClass = new HashMap<String, Boolean>();
        } else {
            mClass = getTempPerms(arena, ap.getArenaClass().getName());
        }

        Map<String, Boolean> total = new HashMap<String, Boolean>();
        for (String s : mGlobal.keySet()) {
            total.put(s, mGlobal.get(s));
        }
        for (String s : mTeam.keySet()) {
            total.put(s, mTeam.get(s));
        }
        for (String s : mClass.keySet()) {
            total.put(s, mClass.get(s));
        }

        if (total.isEmpty())
            return;

        PermissionAttachment pa = ap.get().addAttachment(PVPArena.instance);

        for (String entry : total.keySet()) {
            pa.setPermission(entry, total.get(entry));
        }
        ap.get().recalculatePermissions();
        ap.getTempPermissions().add(pa);
    }

    /**
     * remove temporary permissions from a player
     *
     * @param p the player to reset
     */
    void removePermissions(Player p) {
        ArenaPlayer player = ArenaPlayer.parsePlayer(p.getName());
        if (player == null || player.getTempPermissions() == null) {
            return;
        }
        for (PermissionAttachment pa : player.getTempPermissions()) {
            if (pa != null) {
                pa.remove();
            }
        }
        p.recalculatePermissions();
    }

    @Override
    public void resetPlayer(Player player, boolean force) {
        try {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new ResetRunnable(this, player), 5L);
        } catch (IllegalPluginAccessException e) {
            // we don't care. Perms don't need to be removed on shutdown
        }
    }
    /*
	@Override
	public void parseStart() {
		HashMap<String, Boolean> mGlobal = getTempPerms(arena, "default");
		
		for (ArenaTeam team : arena.getTeams()) {
			HashMap<String, Boolean> mTeam = getTempPerms(arena, team.getName());
			for (ArenaPlayer ap : team.getTeamMembers()) {
				setPermissions(arena, ap, mGlobal, mTeam);
			}
		}
	}*/
}
