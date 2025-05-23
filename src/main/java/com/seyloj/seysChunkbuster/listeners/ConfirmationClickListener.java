package com.seyloj.seysChunkbuster.listeners;

import com.seyloj.seysChunkbuster.gui.ChunkbusterConfirmGUI;
import com.seyloj.seysChunkbuster.model.ChunkBuster;
import com.seyloj.seysChunkbuster.model.PlacementBehavior;
import com.seyloj.seysChunkbuster.util.BustCache;
import com.seyloj.seysChunkbuster.util.ChunkbustExecutor;
import com.seyloj.seysChunkbuster.util.ConfirmationCache;
import com.seyloj.seysChunkbuster.util.PendingBust;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ConfirmationClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();
        if (!(e.getInventory().getHolder() instanceof ChunkbusterConfirmGUI.ConfirmHolder)) return;


        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (name.equalsIgnoreCase("No")) {
            player.closeInventory();
            BustCache.remove(player);
            return;
        }

        PendingBust bust = BustCache.get(player);
        if (bust == null) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "(!) No pending chunkbuster found.");
            return;
        }

        BustCache.remove(player);
        player.closeInventory();

        if (name.startsWith("Yes (Don't ask again")) {
            ConfirmationCache.suppress(player);
        }

        player.sendMessage(ChatColor.GREEN + "Chunkbuster activated!");

        ChunkBuster buster = bust.getChunkBuster();
        if (player.getGameMode() != GameMode.CREATIVE) {
            switch (buster.getPlacementBehavior()) {
                case BREAK:
                    ItemStack breakItem = player.getInventory().getItem(bust.getHand());
                    if (breakItem != null && breakItem.getAmount() > 0) {
                        breakItem.setAmount(breakItem.getAmount() - 1);
                        player.getInventory().setItem(bust.getHand(), breakItem);
                    }
                    break;

                case DROP:
                    ItemStack dropItem = player.getInventory().getItem(bust.getHand());
                    if (dropItem != null && dropItem.getAmount() > 0) {
                        ItemStack toDrop = dropItem.clone();
                        toDrop.setAmount(1);
                        player.getWorld().dropItemNaturally(bust.getLocation(), toDrop);
                        dropItem.setAmount(dropItem.getAmount() - 1);
                        player.getInventory().setItem(bust.getHand(), dropItem);
                    }
                    break;
            }
        }


        ChunkbustExecutor.execute(bust.getLocation(), buster, player);
    }
}
