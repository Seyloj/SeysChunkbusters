package com.seyloj.seysChunkbuster.commands;

import com.seyloj.seysChunkbuster.model.ChunkBuster;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ChunkbusterTabCompleter implements TabCompleter {

    private final Map<String, ChunkBuster> chunkBusters;

    public ChunkbusterTabCompleter(Map<String, ChunkBuster> chunkBusters) {
        this.chunkBusters = chunkBusters;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("seyschunkbuster.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return Arrays.asList("give", "reload", "setitem", "addchunkbuster");
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length == 2) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            } else if (args.length == 3) {
                return new ArrayList<>(chunkBusters.keySet());
            }
        }

        if (args[0].equalsIgnoreCase("setitem") && args.length == 2) {
            return new ArrayList<>(chunkBusters.keySet());
        }

        return Collections.emptyList();
    }
}
