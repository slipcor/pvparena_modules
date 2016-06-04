package net.slipcor.pvparena.modules.scoreboards;

import net.slipcor.pvparena.events.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class PAListener implements Listener {
    private final ScoreBoards module;

    public PAListener(final ScoreBoards ea) {
        this.module = ea;
    }

    @EventHandler
    public void onDeath(final PADeathEvent event) {
        if (module.getArena() != null && module.getArena().equals(event.getArena())) {
            module.getArena().getDebugger().i("ScoreBoards: PADeathEvent");
            module.update(event.getPlayer());
        }
    }

    @EventHandler
    public void onEnd(PAEndEvent event) {
        if (module.getArena() != null && module.getArena().equals(event.getArena())) {
            module.getArena().getDebugger().i("ScoreBoards: PAEndEvent");
            module.stop();
        }
    }

    @EventHandler
    public void onExit(PAExitEvent event) {
        if (module.getArena() == null || !module.getArena().equals(event.getArena())) {
            return;
        }
        module.getArena().getDebugger().i("ScoreBoards: PAExitEvent");
        module.remove(event.getPlayer(), false);
    }

    @EventHandler
    public void onJoin(PAJoinEvent event) {
        if (module.getArena() == null || !module.getArena().isFightInProgress()) {
            return;
        }
        if (module.getArena().equals(event.getArena())) {
            module.getArena().getDebugger().i("ScoreBoards: PAJoinEvent");
            module.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onTeamChange(PATeamChangeEvent event) {
        if (module.getArena() == null || !module.getArena().isFightInProgress()) {
            return;
        }
        if (module.getArena().equals(event.getArena())) {
            module.getArena().getDebugger().i("ScoreBoards: PATeamChangeEvent");
            module.change(event.getPlayer(), event.getFrom(), event.getTo());
        }
    }

    @EventHandler
    public void onKill(PAKillEvent event) {
        if (module.getArena() != null && module.getArena().equals(event.getArena())) {
            module.getArena().getDebugger().i("ScoreBoards: PAKillEvent");
            module.update(event.getPlayer());
        }
    }

    @EventHandler
    public void onLeave(PALeaveEvent event) {
        if (module.getArena() == null || !module.getArena().equals(event.getArena())) {
            return;
        }
        module.getArena().getDebugger().i("ScoreBoards: PALeaveEvent");
        module.remove(event.getPlayer(), false);
    }

    @EventHandler
    public void onLose(PALoseEvent event) {
        if (module.getArena() != null && module.getArena().equals(event.getArena())) {
            module.getArena().getDebugger().i("ScoreBoards: PALoseEvent");
            module.remove(event.getPlayer(), false);
        }
    }

    @EventHandler
    public void onStart(PAStartEvent event) {
        if (module.getArena() != null && module.getArena().equals(event.getArena())) {
            module.getArena().getDebugger().i("ScoreBoards: PAStartEvent");
            module.start();
        }
    }
}
