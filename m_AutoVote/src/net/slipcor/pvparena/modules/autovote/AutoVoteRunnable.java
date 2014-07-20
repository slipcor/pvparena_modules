package net.slipcor.pvparena.modules.autovote;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.runnables.ArenaRunnable;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class AutoVoteRunnable extends ArenaRunnable {
    private final Debug debug = new Debug(68);
    private final String definition;
    private final AutoVote module;

    public AutoVoteRunnable(Arena a, int i, AutoVote mod, String definition) {
        super(MSG.MODULE_AUTOVOTE_VOTENOW.getNode(), i, null, a, false);
        this.definition = definition;
        debug.i("AutoVoteRunnable constructor");
        module = mod;
    }

    protected void commit() {
        debug.i("ArenaVoteRunnable commiting");
        AutoVote.commit(definition, module.players);
        class RunLater implements Runnable {

            @Override
            public void run() {
                module.vote = null;
                arena.getDebugger().i("clearing 'AutoVote.players'");
                for (String player : AutoVote.votes.keySet()) {
                    arena.getDebugger().i("removing vote of: " + player);
                }
                AutoVote.votes.clear();
                module.players.clear();
            }

        }
        Bukkit.getScheduler().runTaskLater(PVPArena.instance, new RunLater(), 20L);
    }

    @Override
    protected void warn() {
        PVPArena.instance.getLogger().warning("ArenaVoteRunnable not scheduled yet!");
    }

    @Override
    public void spam() {
        if ((super.message == null) || (MESSAGES.get(seconds) == null)) {
            return;
        }
        MSG msg = MSG.getByNode(this.message);
        if (msg == null) {
            PVPArena.instance.getLogger().warning("MSG not found: " + this.message);
            return;
        }

        String arenastring;

        if (definition == null) {
            arenastring = ArenaManager.getNames();
        } else {
            Set<String> arenas = new HashSet<String>();
            for (String string : ArenaManager.getShortcutDefinitions().get(definition)) {
                arenas.add(string);
            }
            arenastring = StringParser.joinSet(arenas, ", ");
        }

        String message = seconds > 5 ? Language.parse(msg, MESSAGES.get(seconds), arenastring) : MESSAGES.get(seconds);

        for (ArenaPlayer ap : module.players) {
            if (!module.hasVoted(ap.getName())) {
                module.getArena().msg(ap.get(), message);
            }
        }
    }
}
