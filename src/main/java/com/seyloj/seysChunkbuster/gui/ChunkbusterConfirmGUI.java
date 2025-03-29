package com.seyloj.seysChunkbuster.gui;

import com.seyloj.seysChunkbuster.model.ChunkBuster;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChunkbusterConfirmGUI {

    public static final String GUI_TITLE = ChatColor.RED + "Confirm Chunkbuster Use?";

    public static void open(Player player, ChunkBuster buster) {
        Inventory gui = Bukkit.createInventory(new ConfirmHolder(), 27, GUI_TITLE);

        ItemStack no = createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "No");
        ItemStack yes = createItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Yes");
        ItemStack yesNoAsk = createItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Yes (Don't ask again for 10 minutes)");

        gui.setItem(11, no);
        gui.setItem(13, yes);
        gui.setItem(15, yesNoAsk);

        player.openInventory(gui);
    }

    private static ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static class ConfirmHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null; // not needed
        }
    }
}
