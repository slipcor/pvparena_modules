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
    private boolean listening;

    public TempPerms() {
        super("TempPerms");
    }

    @Override
    public String version() {
        return "v1.3.4.267";
    }

    @Override
    public boolean checkCommand(final String s) {
        return "!tps".equals(s) || "tempperms".equals(s);
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("tempperms");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!tps");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"add"});
        result.define(new String[]{"rem"});
        if (arena == null) {
            return result;
        }
        for (final ArenaClass aClass : arena.getClasses()) {
            result.define(new String[]{aClass.getName(), "add"});
            result.define(new String[]{aClass.getName(), "rem"});
        }
        for (final String team : arena.getTeamNames()) {
            result.define(new String[]{team, "add"});
            result.define(new String[]{team, "rem"});
        }
        return result;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
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
                && !PVPArena.hasCreatePerms(sender, arena)) {
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
            for (final Map.Entry<String, Boolean> stringBooleanEntry : map.entrySet()) {
                arena.msg(sender, stringBooleanEntry.getKey() + " - " + StringParser.colorVar(stringBooleanEntry.getValue()));
            }
            return;
        }

        if (args.length == 3 && ("add".equals(args[1]) || "rem".equals(args[1]))) {
            if ("add".equals(args[1])) {
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
            for (final Map.Entry<String, Boolean> stringBooleanEntry : map.entrySet()) {
                arena.msg(sender, stringBooleanEntry.getKey() + " - " + StringParser.colorVar(stringBooleanEntry.getValue()));
            }
            return;
        }

        if (args.length == 4 && ("add".equals(args[2]) || "rem".equals(args[2]))) {
            if ("add".equals(args[2])) {
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
    public void onClassChange(final PAPlayerClassChangeEvent event) {
        if (event.getArena() != null && event.getArena().equals(arena)) {
            final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(event.getPlayer().getName());
            if (arena != null && arena.getEveryone().contains(aPlayer)) {
                removePermissions(event.getPlayer());

                setPermissions(arena, aPlayer, getTempPerms(arena, "default"), getTempPerms(arena, aPlayer.getArenaTeam().getName()));
            }
        }
    }

    @Override
    public void parseJoin(final CommandSender player, final ArenaTeam team) {
        if (!listening) {
            Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
            listening = true;
        }
        final ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
        setPermissions(arena, ap, getTempPerms(arena, "default"), getTempPerms(arena, ap.getArenaTeam().getName()));
    }

    /**
     * get the permissions map
     *
     * @return the temporary permissions map
     */
    private Map<String, Boolean> getTempPerms(final Arena arena, final String node) {
        final Map<String, Boolean> result = new HashMap<>();

        if (arena.getArenaConfig().getYamlConfiguration().contains("perms." + node)) {
            final List<String> list = arena.getArenaConfig().getStringList("perms." + node,
                    new ArrayList<String>());
            for (final String key : list) {
                result.put(key.replace("-", "").replace("^", ""),
                        !(key.startsWith("^") || key.startsWith("-")));
            }
        }

        return result;
    }

    private void setTempPerms(final Arena arena, final Map<String, Boolean> map, final String node) {
        final List<String> result = new ArrayList<>();
        for (final Map.Entry<String, Boolean> stringBooleanEntry : map.entrySet()) {
            result.add((stringBooleanEntry.getValue() ? "" : "^") + stringBooleanEntry.getKey());
        }
        arena.getArenaConfig().setManually("perms." + node, result);
        arena.getArenaConfig().save();
    }

    private void setPermissions(final Arena arena, final ArenaPlayer ap, final Map<String, Boolean> mGlobal, final Map<String, Boolean> mTeam) {

        final Map<String, Boolean> mClass;

        if (ap.getArenaClass() == null) {
            mClass = new HashMap<>();
        } else {
            mClass = getTempPerms(arena, ap.getArenaClass().getName());
        }

        final Map<String, Boolean> total = new HashMap<>();
        for (final Map.Entry<String, Boolean> stringBooleanEntry3 : mGlobal.entrySet()) {
            total.put(stringBooleanEntry3.getKey(), stringBooleanEntry3.getValue());
        }
        for (final Map.Entry<String, Boolean> stringBooleanEntry2 : mTeam.entrySet()) {
            total.put(stringBooleanEntry2.getKey(), stringBooleanEntry2.getValue());
        }
        for (final Map.Entry<String, Boolean> stringBooleanEntry1 : mClass.entrySet()) {
            total.put(stringBooleanEntry1.getKey(), stringBooleanEntry1.getValue());
        }

        if (total.isEmpty()) {
            return;
        }

        final PermissionAttachment pa = ap.get().addAttachment(PVPArena.instance);

        for (final Map.Entry<String, Boolean> stringBooleanEntry : total.entrySet()) {
            pa.setPermission(stringBooleanEntry.getKey(), stringBooleanEntry.getValue());
        }
        ap.get().recalculatePermissions();
        ap.getTempPermissions().add(pa);
    }

    /**
     * remove temporary permissions from a player
     *
     * @param p the player to reset
     */
    void removePermissions(final Player p) {
        final ArenaPlayer player = ArenaPlayer.parsePlayer(p.getName());
        if (player == null || player.getTempPermissions() == null) {
            return;
        }
        for (final PermissionAttachment pa : player.getTempPermissions()) {
            if (pa != null) {
                pa.remove();
            }
        }
        player.getTempPermissions().clear();
        p.recalculatePermissions();
    }

    @Override
    public void resetPlayer(final Player player, final boolean force) {
        try {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance, new ResetRunnable(this, player), 5L);
        } catch (final IllegalPluginAccessException e) {
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
