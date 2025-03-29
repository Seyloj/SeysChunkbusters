package com.seyloj.seysChunkbuster.fawe;

import com.seyloj.seysChunkbuster.model.ChunkBuster;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FAWENotAvailable implements FAWEHook {
    @Override
    public void execute(Location location, ChunkBuster buster, Player player) {
        player.sendMessage("§cFAWE is not available on this server.");
    }
}
