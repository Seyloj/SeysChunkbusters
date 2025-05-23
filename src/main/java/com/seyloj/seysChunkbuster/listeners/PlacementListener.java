package com.seyloj.seysChunkbuster.listeners;

import com.seyloj.seysChunkbuster.SeysChunkbuster;
import com.seyloj.seysChunkbuster.config.ConfigManager;
import com.seyloj.seysChunkbuster.gui.ChunkbusterConfirmGUI;
import com.seyloj.seysChunkbuster.model.ChunkBuster;
import com.seyloj.seysChunkbuster.model.Shape;
import com.seyloj.seysChunkbuster.util.BustCache;
import com.seyloj.seysChunkbuster.util.ChunkbustExecutor;
import com.seyloj.seysChunkbuster.util.ConfirmationCache;
import com.seyloj.seysChunkbuster.util.PendingBust;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class PlacementListener implements Listener {

    private final Map<String, ChunkBuster> chunkBusters;

    private final SeysChunkbuster plugin;

    public PlacementListener(SeysChunkbuster plugin, Map<String, ChunkBuster> chunkBusters) {
        this.plugin = plugin;
        this.chunkBusters = chunkBusters;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }

        ItemStack item = e.getItemInHand();
        if (!item.hasItemMeta()) return;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String busterId = container.get(SeysChunkbuster.pluginKey, PersistentDataType.STRING);
        if (busterId == null || !chunkBusters.containsKey(busterId)) {
            return;
        }

        Player player = e.getPlayer();

        if(!plugin.getConfigManager().isWorldAllowed(e.getBlock().getWorld().getName())) {
            player.sendMessage(ChatColor.RED + "(!) Placing chunkbusters is disabled in this world");
            e.setCancelled(true);
            return;
        }

        if(!player.hasPermission("seyschunkbuster.use." + busterId) && !player.hasPermission("seyschunkbuster.use.*")) {
            player.sendMessage(ChatColor.RED + "(!) You don't have permission to use this chunkbuster.");
            e.setCancelled(true);
            return;
        }

        ChunkBuster buster = chunkBusters.get(busterId);

        if (ChunkbustExecutor.hasCooldown(player, buster) && !player.hasPermission("seyschunkbuster.bypasscooldown")) {
            long remaining = ChunkbustExecutor.getRemainingCooldown(player, buster);
            if(plugin.getConfigManager().getGlobalCooldown()) {
                player.sendMessage(ChatColor.RED + "(!) You must wait " + remaining + "s before using another chunkbuster.");
            } else {
                player.sendMessage(ChatColor.RED + "(!) You must wait " + remaining + "s before using this chunkbuster.");
            }
            e.setCancelled(true);
            return;
        }

        if (buster.isRequireConfirmation() && !ConfirmationCache.isSuppressed(player.getUniqueId())) {
            BustCache.put(player, new PendingBust(e.getBlock().getLocation(), buster, e.getHand()));
            ChunkbusterConfirmGUI.open(player, buster);
            e.setCancelled(true);
            return;
        }


        switch (buster.getPlacementBehavior()) {
            case BREAK:
                e.getBlock().setType(Material.AIR);
                break;

            case DROP:
                e.getBlock().setType(Material.AIR, false);
                ItemStack dropClone = item.clone();
                dropClone.setAmount(1);
                player.getWorld().dropItemNaturally(e.getBlock().getLocation(), dropClone);
                break;

            case KEEP:
                e.setCancelled(true);
                break;
        }

        ChunkbustExecutor.execute(e.getBlock().getLocation(), buster, player);

    }



}
