package net.slipcor.pvparena.modules.vaultsupport;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.commands.CommandTree;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.events.PAGoalEvent;
import net.slipcor.pvparena.events.PAPlayerClassChangeEvent;
import net.slipcor.pvparena.loadables.ArenaModule;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Field;
import java.util.*;

public class VaultSupport extends ArenaModule implements Listener {

    private static Economy economy;
    private static Permission permission;
    private Map<String, Double> playerBetMap;
    private double pot;
    private Map<String, Double> list;

    public VaultSupport() {
        super("Vault");
    }

    @Override
    public String version() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public boolean checkCommand(final String cmd) {
        return "bet".equalsIgnoreCase(cmd);
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("bet");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"bet", "{Player}"});
        if (arena == null) {
            return result;
        }
        for (final String team : arena.getTeamNames()) {
            result.define(new String[]{"bet", team});
        }
        return result;
    }

    public boolean checkForBalance(ArenaModule module, CommandSender sender, int amount, boolean notify) {
        debug.i("module "+module+" tries to check account "+sender.getName(), sender);
        if (economy == null) {
            return false;
        }
        if (!economy.hasAccount(sender.getName())) {
            arena.getDebugger().i("Account not found: " + sender.getName(), sender);
            return false;
        }
        if (!economy.has(sender.getName(),
                amount)) {
            // no money, no entry!
            if (notify) {
                module.getArena().msg(sender, Language.parse(MSG.MODULE_VAULT_NOTENOUGH, economy
                        .format(amount)));
            } else {
                debug.i("Not enough cash!", sender);
            }
            return false;
        }
        return true;
    }

    public boolean tryDeposit(ArenaModule module, CommandSender sender, int amount, boolean notify) {
        debug.i("module "+module+" tries to deposit "+amount+" to "+sender.getName(), sender);
        if (economy == null) {
            return false;
        }
        if (!economy.hasAccount(sender.getName())) {
            arena.getDebugger().i("Account not found: " + sender.getName(), sender);
            return false;
        }
        EconomyResponse res = economy.depositPlayer(sender.getName(), amount);
        if (res.transactionSuccess() && notify) {
            arena.msg(Bukkit.getPlayer(sender.getName()), Language
                    .parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
            return true;
        }
        return false;
    }

    public String tryFormat(ArenaModule module, int amount) {
        debug.i("module "+module+" tries to format: "+amount);
        if (economy == null) {
            return String.valueOf(amount);
        }
        return economy.format(amount);
    }

    public boolean tryRefund(ArenaModule module, CommandSender sender, int amount, boolean notify) {
        debug.i("module "+module+" tries to refund "+amount+" to "+sender.getName(), sender);
        if (economy == null) {
            return false;
        }
        if (!economy.hasAccount(sender.getName())) {
            arena.getDebugger().i("Account not found: " + sender.getName(), sender);
            return false;
        }
        EconomyResponse res = economy.depositPlayer(sender.getName(), amount);
        if (res.transactionSuccess() && notify) {
            arena.msg(Bukkit.getPlayer(sender.getName()), Language
                    .parse(MSG.MODULE_VAULT_REFUNDING, economy.format(amount)));
            return true;
        }
        return false;
    }

    public boolean tryWithdraw(ArenaModule module, CommandSender sender, int amount, boolean notify) {
        debug.i("module "+module+" tries to withdraw "+amount+" from "+sender.getName(), sender);
        if (economy == null) {
            return false;
        }
        if (!economy.hasAccount(sender.getName())) {
            arena.getDebugger().i("Account not found: " + sender.getName(), sender);
            return false;
        }
        if (!economy.has(sender.getName(),
                amount)) {
            // no money, no entry!
            if (notify) {
                module.getArena().msg(sender, Language.parse(MSG.MODULE_VAULT_NOTENOUGH, economy
                        .format(amount)));
            } else {
                debug.i("Not enough cash!", sender);
            }
            return false;
        }
        EconomyResponse res = economy.withdrawPlayer(sender.getName(), amount);
        if (res.transactionSuccess() && notify) {
            arena.msg(Bukkit.getPlayer(sender.getName()), Language
                    .parse(MSG.MODULE_VAULT_JOINPAY, economy.format(amount)));
            return true;
        }
        return false;
    }

    @Override
    public boolean hasPerms(final CommandSender sender, final Arena arena) {
        return super.hasPerms(sender, arena) || PVPArena.hasPerms(sender, arena);
    }

    @Override
    public PACheck checkJoin(final CommandSender sender,
                             final PACheck res, final boolean join) {

        if (res.hasError() || !join) {
            return res;
        }

        if (arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE) > 0) {
            if (economy != null) {
                if (!economy.hasAccount(sender.getName())) {
                    arena.getDebugger().i("Account not found: " + sender.getName(), sender);
                    res.setError(this, "account not found: " + sender.getName());
                    return res;
                }
                if (!economy.has(sender.getName(),
                        arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE))) {
                    // no money, no entry!

                    res.setError(this, Language.parse(MSG.MODULE_VAULT_NOTENOUGH, economy
                            .format(arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE))));
                    return res;
                }
            }
        }
        return res;
    }

    @Override
    public void commitCommand(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player)) { //TODO move to new parseCommand
            Language.parse(MSG.ERROR_ONLY_PLAYERS);
            return;
        }

        final Player player = (Player) sender;

        ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

        // /pa bet [name] [amount]
        if (ap.getArenaTeam() != null) {
            arena.msg(player, Language.parse(MSG.MODULE_VAULT_BETNOTYOURS));
            return;
        }

        if (economy == null) {
            return;
        }

        if ("bet".equalsIgnoreCase(args[0])) {

            final int maxTime = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_BETTIME);
            if (maxTime > 0 && maxTime > arena.getPlayedSeconds()) {
                arena.msg(player, Language.parse(MSG.ERROR_INVALID_VALUE,
                        "2l8"));
                return;
            }

            final Player p = Bukkit.getPlayer(args[1]);
            if (p != null) {
                ap = ArenaPlayer.parsePlayer(p.getName());
            }
            if (p == null && arena.getTeam(args[1]) == null
                    && ap.getArenaTeam() == null) {
                arena.msg(player, Language.parse(MSG.MODULE_VAULT_BETOPTIONS));
                return;
            }

            final double amount;

            try {
                amount = Double.parseDouble(args[2]);
            } catch (final Exception e) {
                arena.msg(player,
                        Language.parse(MSG.MODULE_VAULT_INVALIDAMOUNT, args[2]));
                return;
            }
            if (!economy.hasAccount(player.getName())) {
                arena.getDebugger().i("Account not found: " + player.getName(), sender);
                return;
            }
            if (!economy.has(player.getName(), amount)) {
                // no money, no entry!
                arena.msg(player,
                        Language.parse(MSG.MODULE_VAULT_NOTENOUGH, economy.format(amount)));
                return;
            }

            final double maxBet = arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MAXIMUMBET);

            if (amount < arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MINIMUMBET)
                    || maxBet > 0.01 && amount > maxBet) {
                // wrong amount!
                arena.msg(player, Language.parse(MSG.ERROR_INVALID_VALUE,
                        economy.format(amount)));
                return;
            }

            economy.withdrawPlayer(player.getName(), amount);
            arena.msg(player, Language.parse(MSG.MODULE_VAULT_BETPLACED, args[1]));
            getPlayerBetMap().put(player.getName() + ':' + args[1], amount);
        }
    }

    @Override
    public boolean commitEnd(final ArenaTeam aTeam) {

        if (economy != null) {
            arena.getDebugger().i("eConomy set, parse bets");
            for (final String nKey : getPlayerBetMap().keySet()) {
                arena.getDebugger().i("bet: " + nKey);
                final String[] nSplit = nKey.split(":");

                if (arena.getTeam(nSplit[1]) == null
                        || "free".equals(arena.getTeam(nSplit[1]).getName())) {
                    continue;
                }

                if (nSplit[1].equalsIgnoreCase(aTeam.getName())) {
                    double teamFactor = arena.getArenaConfig()
                            .getDouble(CFG.MODULES_VAULT_BETWINTEAMFACTOR)
                            * arena.getTeamNames().size();
                    if (teamFactor <= 0) {
                        teamFactor = 1;
                    }
                    teamFactor *= arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR);

                    final double amount = getPlayerBetMap().get(nKey) * teamFactor;

                    if (!economy.hasAccount(nSplit[0])) {
                        arena.getDebugger().i("Account not found: " + nSplit[0]);
                        return true;
                    }
                    arena.getDebugger().i("1 depositing " + amount + " to " + nSplit[0]);
                    if (amount > 0) {
                        economy.depositPlayer(nSplit[0], amount);
                        try {
                            arena.msg(Bukkit.getPlayer(nSplit[0]), Language
                                    .parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
                        } catch (final Exception e) {
                            // nothing
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void configParse(final YamlConfiguration config) {
        Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
    }

    private Map<String, Double> getPermList() {
        if (list == null) {
            list = new HashMap<>();

            if (arena.getArenaConfig().getYamlConfiguration().contains("modules.vault.permfactors")) {
                List<String> cs = arena.getArenaConfig().getYamlConfiguration().
                        getStringList("modules.vault.permfactors");
                for (String node : cs) {
                    String[] split = node.split(":");
                    try {
                        list.put(split[0], Double.parseDouble(split[1]));
                    } catch (Exception e) {
                        PVPArena.instance.getLogger().warning(
                                "string '" + node + "' could not be read in node 'modules.vault.permfactors' in arena " + arena.getName());
                    }
                }
            } else {

                list.put("pa.vault.supervip", 3.0d);
                list.put("pa.vault.vip", 2.0d);

                List<String> stringList = new ArrayList<>();

                for (Map.Entry<String, Double> stringDoubleEntry : list.entrySet()) {
                    stringList.add(stringDoubleEntry.getKey() + ':' + stringDoubleEntry.getValue());
                }
                arena.getArenaConfig().setManually("modules.vault.permfactors", stringList);
                arena.getArenaConfig().save();
            }
        }
        return list;
    }

    /**
     * bettingPlayerName:betGoal => betAmount
     *
     */
    private Map<String, Double> getPlayerBetMap() {
        if (playerBetMap == null) {
            playerBetMap = new HashMap<>();
        }
        return playerBetMap;
    }

    @Override
    public void giveRewards(final Player player) {
        if (player == null) {
            return;
        }

        final int minPlayTime = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_MINPLAYTIME);

        if (minPlayTime > arena.getPlayedSeconds()) {
            arena.getDebugger().i("no rewards, game too short!");
            return;
        }

        final int minPlayers = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_MINPLAYERS);

        Field field = null;
        try {
            field = arena.getClass().getDeclaredField("startCount");
            field.setAccessible(true);
            if (minPlayers > field.getInt(arena)) {
                arena.getDebugger().i("no rewards, not enough players!");
                return;
            }
        } catch (final NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        arena.getDebugger().i("giving rewards to player " + player.getName(), player);

        arena.getDebugger().i("giving Vault rewards to Player " + player, player);
        int winners = 0;
        for (final ArenaPlayer p : arena.getFighters()) {
            arena.getDebugger().i("- checking fighter " + p.getName(), p.getName());
            if (p.getStatus() != null && p.getStatus() == Status.FIGHT) {
                arena.getDebugger().i("-- added!", p.getName());
                winners++;
            }
        }
        arena.getDebugger().i("winners: " + winners, player);

        if (economy != null) {
            arena.getDebugger().i("checking on bet amounts!", player);
            for (final String nKey : getPlayerBetMap().keySet()) {
                final String[] nSplit = nKey.split(":");

                if (nSplit[1].equalsIgnoreCase(player.getName())) {
                    double playerFactor = arena.getFighters().size()
                            * arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINPLAYERFACTOR);

                    if (playerFactor <= 0) {
                        playerFactor = 1;
                    }

                    playerFactor *= arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR);

                    final double amount = getPlayerBetMap().get(nKey) * playerFactor;

                    arena.getDebugger().i("2 depositing " + amount + " to " + nSplit[0]);
                    if (amount > 0) {
                        economy.depositPlayer(nSplit[0], amount);
                        try {

                            ArenaModuleManager.announce(
                                    arena,
                                    Language.parse(MSG.NOTICE_PLAYERAWARDED,
                                            economy.format(amount)), "PRIZE");
                            arena.msg(Bukkit.getPlayer(nSplit[0]), Language
                                    .parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
                        } catch (final Exception e) {
                            // nothing
                        }
                    }
                }
            }

            if (arena.getArenaConfig().getBoolean(CFG.MODULES_VAULT_WINPOT)) {
                arena.getDebugger().i("calculating win pot!", player);
                double amount = winners > 0 ? pot / winners : 0;


                double factor = 1.0d;
                for (final String node : getPermList().keySet()) {
                    if (player.hasPermission(node)) {
                        factor = Math.max(factor, getPermList().get(node));
                    }
                }

                amount *= factor;

                arena.getDebugger().i("3 depositing " + amount + " to " + player.getName());
                if (amount > 0) {
                    economy.depositPlayer(player.getName(), amount);
                    arena.msg(player, Language.parse(MSG.NOTICE_AWARDED,
                            economy.format(amount)));
                }
            } else if (arena.getArenaConfig().getInt(CFG.MODULES_VAULT_WINREWARD, 0) > 0) {

                double amount = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_WINREWARD, 0);
                arena.getDebugger().i("calculating win reward: " + amount, player);


                double factor;

                try {
                    factor = Math.pow(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_WINREWARDPLAYERFACTOR)
                            , field.getInt(arena));
                } catch (final Exception e) {
                    PVPArena.instance.getLogger().warning("Failed to get playedPlayers, using winners!");
                    factor = Math.pow(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_WINREWARDPLAYERFACTOR)
                            , winners);
                }

                for (final String node : getPermList().keySet()) {
                    if (player.hasPermission(node)) {
                        factor = Math.max(factor, getPermList().get(node));
                        arena.getDebugger().i("has perm '" + node + "'; factor set to " + factor, player);
                    }
                }

                amount *= factor;

                arena.getDebugger().i("4 depositing " + amount + " to " + player.getName());
                if (amount > 0) {
                    economy.depositPlayer(player.getName(), amount);
                    arena.msg(player, Language.parse(MSG.NOTICE_AWARDED,
                            economy.format(amount)));
                }
            }
        }
    }

    private void killreward(final Entity damager) {
        Player player = null;
        if (damager instanceof Player) {
            player = (Player) damager;
        }
        if (player == null) {
            return;
        }
        double amount = arena.getArenaConfig()
                .getDouble(CFG.MODULES_VAULT_KILLREWARD);

        if (amount < 0.01) {
            return;
        }

        if (!economy.hasAccount(player.getName())) {
            arena.getDebugger().i("Account not found: " + player.getName(), player);
            return;
        }

        double factor = 1.0d;
        for (final String node : getPermList().keySet()) {
            if (player.hasPermission(node)) {
                factor = Math.max(factor, getPermList().get(node));
            }
        }

        amount *= factor;
        arena.getDebugger().i("6 depositing " + amount + " to " + player.getName());

        if (amount > 0) {
            economy.depositPlayer(player.getName(), amount);
            try {
                arena.msg(Bukkit.getPlayer(player.getName()), Language
                        .parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
            } catch (final Exception e) {
                // nothing
            }
        }
    }

    @Override
    public void displayInfo(final CommandSender player) {
        player.sendMessage("entryfee: "
                + StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE))
                + " || reward: "
                + StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_VAULT_WINREWARD))
                + " || rewardPlayerFactor: "
                + StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_WINREWARDPLAYERFACTOR))
                + " || killreward: "
                + StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_KILLREWARD))
                + " || winFactor: "
                + StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_WINFACTOR)));

        player.sendMessage("minbet: "
                + StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MINIMUMBET))
                + " || maxbet: "
                + StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MAXIMUMBET))
                + " || minplayers: "
                + StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_VAULT_MINPLAYERS))
                + " || betWinFactor: "
                + StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR)));

        player.sendMessage("betTeamWinFactor: "
                + StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINTEAMFACTOR))
                + " || betPlayerWinFactor: "
                + StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINPLAYERFACTOR)));

        player.sendMessage(StringParser.colorVar(
                "bet pot", arena.getArenaConfig().getBoolean(
                        CFG.MODULES_VAULT_BETPOT))
                + " || "
                + StringParser.colorVar(
                "win pot", arena.getArenaConfig().getBoolean(
                        CFG.MODULES_VAULT_WINPOT)));
    }

    @Override
    public void onThisLoad() {
        if (economy == null && Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
            setupPermission();

            Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
        }
    }

    @Override
    public void parseJoin(final CommandSender sender, final ArenaTeam team) {
        final int entryfee = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE, 0);
        if (entryfee > 0) {
            if (economy != null) {
                economy.withdrawPlayer(sender.getName(), entryfee);
                arena.msg(sender,
                        Language.parse(MSG.MODULE_VAULT_JOINPAY, economy.format(entryfee)));
                pot += entryfee;
            }
        }
    }

    @Override
    public void parsePlayerDeath(final Player p,
                                 final EntityDamageEvent cause) {
        killreward(ArenaPlayer.getLastDamagingPlayer(cause, p));
    }

    void pay(final Set<String> result) {
        if (result == null || result.size() == arena.getTeamNames().size()) {
            return;
        }
        arena.getDebugger().i("Paying winners: " + StringParser.joinSet(result, ", "));

        if (economy == null) {
            return;
        }

        double pot = 0;
        double winpot = 0;

        for (final String s : getPlayerBetMap().keySet()) {
            final String[] nSplit = s.split(":");

            pot += getPlayerBetMap().get(s);

            if (result.contains(nSplit)) {
                winpot += getPlayerBetMap().get(s);
            }
        }

        for (final String nKey : getPlayerBetMap().keySet()) {
            final String[] nSplit = nKey.split(":");
            final ArenaTeam team = arena.getTeam(nSplit[1]);
            if (team == null || "free".equals(team.getName())) {
                if (Bukkit.getPlayerExact(nSplit[1]) == null) {
                    continue;
                }
            }

            if (result.contains(nSplit[1])) {
                double amount = 0;

                if (arena.getArenaConfig().getBoolean(CFG.MODULES_VAULT_BETPOT)) {
                    if (winpot > 0) {
                        amount = pot * getPlayerBetMap().get(nKey) / winpot;
                    }
                } else {
                    double teamFactor = arena.getArenaConfig()
                            .getDouble(CFG.MODULES_VAULT_BETWINTEAMFACTOR)
                            * arena.getTeamNames().size();
                    if (teamFactor <= 0) {
                        teamFactor = 1;
                    }
                    teamFactor *= arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR);
                    amount = getPlayerBetMap().get(nKey) * teamFactor;
                }

                if (!economy.hasAccount(nSplit[0])) {
                    arena.getDebugger().i("Account not found: " + nSplit[0]);
                    continue;
                }

                final Player player = Bukkit.getPlayer(nSplit[0]);

                if (player == null) {
                    arena.getDebugger().i("Player is null!");
                } else {
                    double factor = 1.0d;
                    for (final String node : getPermList().keySet()) {
                        if (player.hasPermission(node)) {
                            factor = Math.max(factor, getPermList().get(node));
                        }
                    }

                    amount *= factor;
                }

                arena.getDebugger().i("7 depositing " + amount + " to " + nSplit[0]);
                if (amount > 0) {
                    economy.depositPlayer(nSplit[0], amount);
                    try {
                        arena.msg(Bukkit.getPlayer(nSplit[0]), Language
                                .parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
                    } catch (final Exception e) {
                        // nothing
                    }
                }
            }
        }
    }

    @Override
    public void reset(final boolean force) {
        getPlayerBetMap().clear();
        pot = 0;
    }

    @Override
    public void resetPlayer(final Player player, final boolean soft, final boolean force) {
        if (player == null) {
            return;
        }
        final ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
        if (ap == null) {
            return;
        }
        if (ap.getStatus() == null || force) {
            return;
        }
        if (ap.getStatus() == Status.LOUNGE ||
                ap.getStatus() == Status.READY) {
            final int entryfee = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE);
            if (entryfee < 1) {
                return;
            }
            arena.msg(player, Language.parse(MSG.MODULE_VAULT_REFUNDING, economy.format(entryfee)));
            if (!economy.hasAccount(player.getName())) {
                arena.getDebugger().i("Account not found: " + player.getName(), player);
                return;
            }
            arena.getDebugger().i("8 depositing " + entryfee + " to " + player.getName());
            economy.depositPlayer(player.getName(), entryfee);
            pot -= entryfee;

        }
    }

    private boolean setupEconomy() {
        final RegisteredServiceProvider<Economy> economyProvider = Bukkit
                .getServicesManager().getRegistration(
                        Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return economy != null;
    }

    private boolean setupPermission() {
        final RegisteredServiceProvider<Permission> permProvider = Bukkit
                .getServicesManager().getRegistration(
                        Permission.class);
        if (permProvider != null) {
            permission = permProvider.getProvider();
        }

        return permission != null;
    }

    public void timedEnd(final HashSet<String> result) {
        pay(result);
    }

    @EventHandler
    public void onClassChange(final PAPlayerClassChangeEvent event) {
        if (event.getArena() != null && event.getArena().equals(arena)) {

            final String autoClass = arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);

            if (event.getArenaClass() == null ||
                    !"none".equals(autoClass) ||
                    !event.getArenaClass().getName().equals(autoClass)) {
                return; // class will be removed OR no autoClass OR no>T< autoClass
            }

            String group = null;

            try {
                group = permission.getPrimaryGroup(event.getPlayer());
            } catch (final Exception e) {

            }
            final ArenaClass aClass = arena.getClass("autoClass_" + group);
            if (aClass != null) {
                event.setArenaClass(aClass);
            }
        }
    }

    @EventHandler
    public void onGoalScore(final PAGoalEvent event) {


        if (event.getArena().equals(arena)) {
            arena.getDebugger().i("it's us!");
            final String[] contents = event.getContents();
            /*
			* content.length == 1
			* * content[0] = "" => end!
			* 
			* content[X].contains(playerDeath) => "playerDeath:playerName"
			* content[X].contains(playerKill) => "playerKill:playerKiller:playerKilled"
			* content[X].contains(trigger) => "trigger:playerName" triggered a score
			* content[X].equals(tank) => player is tank
			* content[X].equals(infected) => player is infected
			* content[X].equals(doesRespawn) => player will respawn
			* content[X].contains(score) => "score:player:team:value"
			*
			*/

            String lastTrigger = "";
            for (String node : contents) {
                node = node.toLowerCase();
                if (node.contains("trigger")) {
                    lastTrigger = node.substring(8);
                    newReward(lastTrigger, "TRIGGER");
                }

                if (node.contains("playerDeath")) {
                    newReward(node.substring(12), "DEATH");
                }

                if (node.contains("playerKill")) {
                    final String[] val = node.split(":");
                    if (!val[1].equals(val[2])) {
                        newReward(val[1], "KILL");
                    }
                }

                if (node.contains("score")) {
                    final String[] val = node.split(":");
                    newReward(val[1], "SCORE", Integer.parseInt(val[3]));
                }

                if (node != null && node.isEmpty() && lastTrigger != null && !lastTrigger.isEmpty()) {
                    newReward(lastTrigger, "WIN");
                }
            }

        }
    }

    private void newReward(final String playerName, final String rewardType) {
        if (playerName == null || playerName.length() < 1) {
            PVPArena.instance.getLogger().warning("winner is empty string in " + arena.getName());
            return;
        }
        arena.getDebugger().i("new Reward: " + playerName + " -> " + rewardType);
        newReward(playerName, rewardType, 1);
    }

    private void newReward(final String playerName, final String rewardType, final int amount) {
        if (playerName == null || playerName.length() < 1) {
            PVPArena.instance.getLogger().warning("winner is empty string in " + arena.getName());
            return;
        }
        arena.getDebugger().i("new Reward: " + amount + "x " + playerName + " -> " + rewardType);
        try {

            double value = arena.getArenaConfig().getDouble(
                    CFG.valueOf("MODULES_VAULT_REWARD_" + rewardType), 0.0d);

            final double maybevalue = arena.getArenaConfig().getDouble(
                    CFG.valueOf("MODULES_VAULT_REWARD_" + rewardType), -1.0d);

            if (maybevalue < 0) {
                PVPArena.instance.getLogger().warning("config value is not set: " + CFG.valueOf("MODULES_VAULT_REWARD_" + rewardType).getNode());
            }
            final Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                double factor = 1.0d;
                for (final String node : getPermList().keySet()) {
                    if (player.hasPermission(node)) {
                        factor = Math.max(factor, getPermList().get(node));
                    }
                }

                value *= factor;
            }

            arena.getDebugger().i("9 depositing " + value + " to " + playerName);
            if (value > 0) {
                economy.depositPlayer(playerName, value);
                try {

                    ArenaModuleManager.announce(
                            arena,
                            Language.parse(MSG.NOTICE_PLAYERAWARDED,
                                    economy.format(value)), "PRIZE");
                    arena.msg(player, Language
                            .parse(MSG.MODULE_VAULT_YOUWON, economy.format(value)));
                } catch (final Exception e) {
                    // nothing
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
