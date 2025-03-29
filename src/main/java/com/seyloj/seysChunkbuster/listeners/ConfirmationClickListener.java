package com.seyloj.seysChunkbuster.listeners;

import com.seyloj.seysChunkbuster.gui.ChunkbusterConfirmGUI;
import com.seyloj.seysChunkbuster.util.BustCache;
import com.seyloj.seysChunkbuster.util.ChunkbustExecutor;
import com.seyloj.seysChunkbuster.util.ConfirmationCache;
import com.seyloj.seysChunkbuster.util.PendingBust;
import org.bukkit.ChatColor;
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
        if (!e.getView().getTitle().equals(ChunkbusterConfirmGUI.GUI_TITLE)) return;

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
        ChunkbustExecutor.execute(bust.getLocation(), bust.getChunkBuster(), player);
    }
}
