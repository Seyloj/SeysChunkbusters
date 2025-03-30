package com.seyloj.seysChunkbuster.util;

import com.fastasyncworldedit.core.history.changeset.NullChangeSet;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.World;
import org.bukkit.entity.Player;

import java.util.*;

public class RollbackManager {

    private static final Map<UUID, Deque<EditSession>> editSessions = new HashMap<>();
    private static int maxRollback = 5;

    public static void setMaxRollback(int max) {
        maxRollback = max;
    }

    public static void rememberEdit(Player player, EditSession editSession) {
        UUID playerId = player.getUniqueId();
        Deque<EditSession> sessions = editSessions.computeIfAbsent(playerId, k -> new ArrayDeque<>());
        if (sessions.size() >= maxRollback) {
            sessions.removeLast();
        }
        sessions.push(editSession);
    }

    public static boolean rollbackLast(Player player) {
        UUID playerId = player.getUniqueId();
        Deque<EditSession> sessions = editSessions.get(playerId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        EditSession editSession = sessions.pop();
        try {
            EditSession undoSession = WorldEdit.getInstance().newEditSession(editSession.getWorld());
            editSession.undo(undoSession);
            undoSession.flushQueue();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void clearHistory(Player player) {
        editSessions.remove(player.getUniqueId());
    }
}
