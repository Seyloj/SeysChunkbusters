package com.seyloj.seysChunkbuster.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmationCache {

    private static final Map<UUID, Long> suppressedUntil = new HashMap<>();
    private static final long DURATION = 10 * 60 * 1000; // 10 minutes in ms

    public static boolean isSuppressed(UUID uuid) {
        if (!suppressedUntil.containsKey(uuid)) return false;
        long expires = suppressedUntil.get(uuid);
        if (System.currentTimeMillis() > expires) {
            suppressedUntil.remove(uuid);
            return false;
        }
        return true;
    }

    public static void suppress(UUID uuid) {
        suppressedUntil.put(uuid, System.currentTimeMillis() + DURATION);
    }

    public static void suppress(org.bukkit.entity.Player player) {
        suppress(player.getUniqueId());
    }
}
