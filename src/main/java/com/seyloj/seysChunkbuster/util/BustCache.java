package com.seyloj.seysChunkbuster.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BustCache {

    private static final Map<UUID, PendingBust> pending = new HashMap<>();

    public static void put(Player player, PendingBust bust) {
        pending.put(player.getUniqueId(), bust);
    }

    public static PendingBust get(Player player) {
        return pending.get(player.getUniqueId());
    }

    public static void remove(Player player) {
        pending.remove(player.getUniqueId());
    }

    public static boolean has(Player player) {
        return pending.containsKey(player.getUniqueId());
    }
}
