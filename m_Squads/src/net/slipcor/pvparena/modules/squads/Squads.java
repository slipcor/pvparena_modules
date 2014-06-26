package net.slipcor.pvparena.modules.squads;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.commands.AbstractArenaCommand;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class Squads extends ArenaModule {
    Debug debug = new Debug(690);
    Set<ArenaSquad> squads = new HashSet<ArenaSquad>();
    ArenaSquad auto = null;
    boolean ingame = false;

    public Squads() {
        super("Squads");
        debug = new Debug(697);
    }

    @Override
    public String version() {
        return "v1.3.0.495";
    }

    @Override
    public void configParse(YamlConfiguration cfg) {
        if (cfg.get("modules.squads.limits") == null) {
            cfg.addDefault("modules.squads.autoSquad", "none");
            cfg.addDefault("modules.squads.ingameSquadSwitch", false);
            return;
        }
        ConfigurationSection cs = cfg.getConfigurationSection("modules.squads");
        ConfigurationSection sqs = cs.getConfigurationSection("limits");
        for (String name : sqs.getKeys(false)) {
            ArenaSquad squad = new ArenaSquad(name, sqs.getInt(name));
            if (name.equals(cs.getString("autoSquad"))) {
                auto = squad;
            }
            squads.add(squad);
        }
        ingame = cs.getBoolean("ingameSquadSwitch");
    }

    @Override
    public boolean checkCommand(String s) {
        return s.equals("!sq") || s.equals("squad");
    }

    @Override
    public List<String> getMain() {
        return Arrays.asList("squads");
    }

    @Override
    public List<String> getShort() {
        return Arrays.asList("!sq");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        CommandTree<String> result = new CommandTree<String>(null);
        result.define(new String[]{"add"});
        for (ArenaSquad squad : squads) {
            result.define(new String[]{"remove", squad.getName()});
            result.define(new String[]{"set", squad.getName()});
        }
        return result;
    }

    @Override
    public void commitCommand(CommandSender sender, String[] args) {
        // !sq | show the arena squads
        // !sq add [name] | add squad [name]
        // !sq add [name] [limit] | add squad with player limit
        // !sq remove [name] | remove squad [name]
        // !sq set [name] [limit] | set player limit for squad

        if (!PVPArena.hasAdminPerms(sender)
                && !(PVPArena.hasCreatePerms(sender, arena))) {
            arena.msg(
                    sender,
                    Language.parse(MSG.ERROR_NOPERM,
                            Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{0, 2, 3})) {
            return;
        }

        if (args == null || args.length < 1) {
            // !sq | show the arena squads

            if (squads.size() < 1) {
                arena.msg(sender, "No squads loaded! Add some: /pa [arena] !sq add [name]");
            } else {
                arena.msg(sender, "Squads for Arena " + ChatColor.AQUA + arena.getName());
                for (ArenaSquad squad : squads) {
                    String suffix = squad.equals(auto) ? " (auto)" : "";
                    String max = squad.getMax() > 0 ? String.valueOf(squad.getMax()) : "none";
                    arena.msg(sender, "Squad '" + squad.getName() + "' (max: " + max + ")" + suffix);

                }
            }

            return;
        }

        if (args.length >= 2) {


            if (args[0].equals("add")) {
                // !sq add [name] | add squad [name]
                // !sq add [name] [limit] | add squad with player limit

                return;
            } else if (args[0].equals("remove")) {
                // !sq remove [name] | remove squad [name]

                return;
            } else if (args[0].equals("set")) {
                // /pa !sq set [name] [limit] | set player limit for squad

                return;
            }
        }
        arena.msg(sender, "/pa !sq | show the arena squads");
        arena.msg(sender, "/pa !sq add [name] | add squad [name]");
        arena.msg(sender, "/pa !sq add [name] [limit] | add squad with player limit");
        arena.msg(sender, "/pa !sq set [name] [limit] | set player limit for squad");
        arena.msg(sender, "/pa !sq remove [name] | remove squad [name]");
    }

    @Override
    public void reset(boolean force) {
        for (ArenaSquad squad : squads) {
            squad.reset();
        }
        for (Sign sign : signs.keySet()) {
            sign.setLine(1, "----------");
            sign.setLine(2, "");
            sign.setLine(3, "");
        }
        signs.clear();
    }

    @Override
    public boolean onPlayerInteract(PlayerInteractEvent event) {
        ArenaPlayer ap = ArenaPlayer.parsePlayer(event.getPlayer().getName());

        if (!arena.equals(ap.getArena())) {
            return false;
        }

        if (arena.isFightInProgress() && !((Boolean) arena.getArenaConfig().getUnsafe(
                "modules.squads.ingameSquadSwitch"))) {
            return false;
        }

        if (ap.getStatus().equals(Status.DEAD)
                || ap.getStatus().equals(Status.LOST)
                || ap.getStatus().equals(Status.NULL)
                || ap.getStatus().equals(Status.WARM)
                || ap.getStatus().equals(Status.WATCH)) {
            return false;
        }

        if (!event.hasBlock() || !(event.getClickedBlock().getState() instanceof Sign)) {
            return false;
        }

        Sign sign = (Sign) event.getClickedBlock().getState();

        for (ArenaSquad squad : squads) {
            if (squad.getName().equals(sign.getLine(0))) {
                if (squad.getCount() >= squad.getMax()) {
                    arena.msg(ap.get(), "This squad is full!");
                    return false;
                }
                for (ArenaSquad s : squads) {
                    if (s.equals(squad)) {
                        continue;
                    }
                    if (s.contains(ap)) {
                        s.remove(ap);
                        remove(ap);
                        break;
                    }
                }
                squad.add(ap);
                add(sign, squad, ap);
                return true;
            }
        }

        return false;
    }

    private void add(final Sign sign, final ArenaSquad squad, final ArenaPlayer ap) {
        for (int i = 2; i < 4; i++) {
            if (sign.getLine(i) == null || sign.getLine(i).equals("")) {
                sign.setLine(i, ap.getName());
                sign.update();
                return;
            }
        }

        final Block block = sign.getBlock().getRelative(BlockFace.DOWN);

        if (block.getState() instanceof Sign) {
            final Sign sign2 = (Sign) block.getState();
            for (int i = 2; i < 4; i++) {
                if (sign2.getLine(i) == null || sign2.getLine(i).equals("")) {
                    sign2.setLine(i, ap.getName());
                    sign2.update();
                    return;
                }
            }
        }
    }

    private void remove(ArenaPlayer ap) {
        for (Sign sign : signs.keySet()) {
            for (int i = 0; i < 4; i++) {
                if (ap.getName().equals(sign.getLine(i))) {
                    sign.setLine(i, "");
                }
            }
            sign.update();

            final Block block = sign.getBlock().getRelative(BlockFace.DOWN);

            if (block.getState() instanceof Sign) {
                final Sign sign2 = (Sign) block.getState();
                for (int i = 2; i < 4; i++) {
                    if (ap.getName().equals(sign2.getLine(i))) {
                        sign2.setLine(i, "");
                    }
                }
                sign2.update();
            }
        }
    }

    Map<Sign, ArenaSquad> signs = new HashMap<Sign, ArenaSquad>();

    @Override
    public void parseJoin(CommandSender sender, ArenaTeam team) {
        if (auto != null) {
            auto.add(ArenaPlayer.parsePlayer(sender.getName()));
        }
    }

    @Override
    public void parseRespawn(final Player player, final ArenaTeam team,
                             final DamageCause cause, final Entity damager) {

        ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
        for (ArenaSquad squad : squads) {
            if (squad.contains(ap) && squad.getCount() > 1) {
                int pos = new Random().nextInt(squad.getCount() - 1);
                for (final ArenaPlayer tap : squad.getPlayers()) {
                    if (--pos <= 0) {
                        class RunLater implements Runnable {

                            @Override
                            public void run() {
                                player.teleport(tap.get());
                            }

                        }
                        try {
                            Bukkit.getScheduler().runTaskLater(PVPArena.instance,
                                    new RunLater(), 10L);
                        } catch (Exception e) {

                        }
                    }
                }
            }
        }
    }
}
