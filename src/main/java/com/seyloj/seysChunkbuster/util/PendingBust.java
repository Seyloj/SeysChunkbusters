package com.seyloj.seysChunkbuster.util;

import com.seyloj.seysChunkbuster.model.ChunkBuster;
import org.bukkit.Location;

public class PendingBust {
    private final Location location;
    private final ChunkBuster chunkBuster;

    public PendingBust(Location location, ChunkBuster chunkBuster) {
        this.location = location;
        this.chunkBuster = chunkBuster;
    }

    public Location getLocation() {
        return location;
    }

    public ChunkBuster getChunkBuster() {
        return chunkBuster;
    }
}
