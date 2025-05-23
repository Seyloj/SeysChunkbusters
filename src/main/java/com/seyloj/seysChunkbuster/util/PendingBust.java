package com.seyloj.seysChunkbuster.util;

import com.seyloj.seysChunkbuster.model.ChunkBuster;
import org.bukkit.Location;
import org.bukkit.inventory.EquipmentSlot;

public class PendingBust {
    private final Location location;
    private final ChunkBuster chunkBuster;
    private final EquipmentSlot hand;

    public PendingBust(Location location, ChunkBuster chunkBuster, EquipmentSlot hand) {
        this.location = location;
        this.chunkBuster = chunkBuster;
        this.hand = hand;
    }

    public Location getLocation() {
        return location;
    }

    public ChunkBuster getChunkBuster() {
        return chunkBuster;
    }
    public EquipmentSlot getHand() {
        return hand;
    }
}
