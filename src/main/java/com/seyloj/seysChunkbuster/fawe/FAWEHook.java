package com.seyloj.seysChunkbuster.fawe;

import com.seyloj.seysChunkbuster.model.ChunkBuster;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface FAWEHook {
    void execute(Location location, ChunkBuster buster, Player player);
}
