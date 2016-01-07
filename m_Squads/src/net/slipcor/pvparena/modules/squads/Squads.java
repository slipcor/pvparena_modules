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
    private final Set<ArenaSquad> squads = new HashSet<>();
    private ArenaSquad auto;
    private boolean ingame;

    public Squads() {
        super("Squads");
        debug = new Debug(697);
    }

    @Override
    public String version() {
        return "v1.3.2.51";
    }

    @Override
    public void configParse(final YamlConfiguration cfg) {
        if (cfg.get("modules.squads.limits") == null) {
            cfg.addDefault("modules.squads.autoSquad", "none");
            cfg.addDefault("modules.squads.ingameSquadSwitch", false);
            return;
        }
        final ConfigurationSection cs = cfg.getConfigurationSection("modules.squads");
        final ConfigurationSection sqs = cs.getConfigurationSection("limits");
        for (final String name : sqs.getKeys(false)) {
            final ArenaSquad squad = new ArenaSquad(name, sqs.getInt(name));
            if (name.equals(cs.getString("autoSquad"))) {
                auto = squad;
            }
            squads.add(squad);
        }
        ingame = cs.getBoolean("ingameSquadSwitch");
    }

    @Override
    public boolean checkCommand(final String s) {
        return "!sq".equals(s) || "squad".equals(s);
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("squads");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!sq");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"add"});
        for (final ArenaSquad squad : squads) {
            result.define(new String[]{"remove", squad.getName()});
            result.define(new String[]{"set", squad.getName()});
        }
        return result;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        // !sq | show the arena squads
        // !sq add [name] | add squad [name]
        // !sq add [name] [limit] | add squad with player limit
        // !sq remove [name] | remove squad [name]
        // !sq set [name] [limit] | set player limit for squad

        if (!PVPArena.hasAdminPerms(sender)
                && !PVPArena.hasCreatePerms(sender, arena)) {
            arena.msg(
                    sender,
                    Language.parse(MSG.ERROR_NOPERM,
                            Language.parse(MSG.ERROR_NOPERM_X_ADMIN)));
            return;
        }

        if (!AbstractArenaCommand.argCountValid(sender, arena, args, new Integer[]{0, 2, 3})) {
            return;
        }

        if (!ingame && arena.isFightInProgress()) {
            return;
        }

        if (args == null || args.length < 1) {
            // !sq | show the arena squads

            if (squads.size() < 1) {
                arena.msg(sender, "No squads loaded! Add some: /pa [arena] !sq add [name]");
            } else {
                arena.msg(sender, "Squads for Arena " + ChatColor.AQUA + arena.getName());
                for (final ArenaSquad squad : squads) {
                    final String suffix = squad.equals(auto) ? " (auto)" : "";
                    final String max = squad.getMax() > 0 ? String.valueOf(squad.getMax()) : "none";
                    arena.msg(sender, "Squad '" + squad.getName() + "' (max: " + max + ')' + suffix);

                }
            }

            return;
        }

        if (args.length >= 2) {


            if ("add".equals(args[0]) || args[0].equals("remove")) {
                // !sq add [name] | add squad [name]
                // !sq add [name] [limit] | add squad with player limit

                return;
            } else if ("set".equals(args[0])) {
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
    public void reset(final boolean force) {
        for (final ArenaSquad squad : squads) {
            squad.reset();
        }
        for (final Sign sign : signs.keySet()) {
            sign.setLine(1, "----------");
            sign.setLine(2, "");
            sign.setLine(3, "");
        }
        signs.clear();
    }

    @Override
    public boolean onPlayerInteract(final PlayerInteractEvent event) {
        final ArenaPlayer ap = ArenaPlayer.parsePlayer(event.getPlayer().getName());

        if (!arena.equals(ap.getArena())) {
            return false;
        }

        if (arena.isFightInProgress() && !((Boolean) arena.getArenaConfig().getUnsafe(
                "modules.squads.ingameSquadSwitch"))) {
            return false;
        }

        if (ap.getStatus() == Status.DEAD
                || ap.getStatus() == Status.LOST
                || ap.getStatus() == Status.NULL
                || ap.getStatus() == Status.WARM
                || ap.getStatus() == Status.WATCH) {
            return false;
        }

        if (!event.hasBlock() || !(event.getClickedBlock().getState() instanceof Sign)) {
            return false;
        }

        final Sign sign = (Sign) event.getClickedBlock().getState();

        for (final ArenaSquad squad : squads) {
            if (squad.getName().equals(sign.getLine(0))) {
                if (squad.getCount() >= squad.getMax()) {
                    arena.msg(ap.get(), "This squad is full!");
                    return false;
                }
                for (final ArenaSquad s : squads) {
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
                add(sign, ap);
                return true;
            }
        }

        return false;
    }

    private void add(final Sign sign, final ArenaPlayer ap) {
        for (int i = 2; i < 4; i++) {
            if (sign.getLine(i) == null || sign.getLine(i) != null && sign.getLine(i).isEmpty()) {
                sign.setLine(i, ap.getName());
                sign.update();
                return;
            }
        }

        final Block block = sign.getBlock().getRelative(BlockFace.DOWN);

        if (block.getState() instanceof Sign) {
            final Sign sign2 = (Sign) block.getState();
            for (int i = 2; i < 4; i++) {
                if (sign2.getLine(i) == null || sign2.getLine(i) != null && sign2.getLine(i).isEmpty()) {
                    sign2.setLine(i, ap.getName());
                    sign2.update();
                    return;
                }
            }
        }
    }

    private void remove(final ArenaPlayer ap) {
        for (final Sign sign : signs.keySet()) {
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

    private final Map<Sign, ArenaSquad> signs = new HashMap<>();

    @Override
    public void parseJoin(final CommandSender sender, final ArenaTeam team) {
        if (auto != null) {
            auto.add(ArenaPlayer.parsePlayer(sender.getName()));
        }
    }

    @Override
    public void parseRespawn(final Player player, final ArenaTeam team,
                             final DamageCause cause, final Entity damager) {

        final ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
        for (final ArenaSquad squad : squads) {
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
                        } catch (final Exception e) {

                        }
                    }
                }
            }
        }
    }
}
