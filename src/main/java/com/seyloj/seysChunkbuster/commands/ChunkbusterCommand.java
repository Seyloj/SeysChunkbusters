package com.seyloj.seysChunkbuster.commands;

import com.seyloj.seysChunkbuster.SeysChunkbuster;
import com.seyloj.seysChunkbuster.model.ChunkBuster;
import com.seyloj.seysChunkbuster.util.ChunkbusterStorage;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class ChunkbusterCommand implements CommandExecutor {

    private final Map<String, ChunkBuster> chunkBusters;
    private final SeysChunkbuster plugin;
    private final File bustersFile;

    public ChunkbusterCommand(SeysChunkbuster plugin, Map<String, ChunkBuster> chunkBusters) {
        this.plugin = plugin;
        this.chunkBusters = chunkBusters;
        this.bustersFile = new File(plugin.getDataFolder(), "chunkbusters.yml");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seyschunkbuster.admin")) {
            sender.sendMessage(ChatColor.RED + "(!) You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                return handleGive(sender, args);
            case "reload":
                return handleReload(sender);
            case "setitem":
                return handleSetItem(sender, args);
            case "addchunkbuster":
                return handleAddChunkbuster(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "(!) Unknown subcommand. Use /chunkbuster help");
                return true;
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "§m--------------------");
        sender.sendMessage(ChatColor.YELLOW + "§lChunkbuster Commands:");
        sender.sendMessage(ChatColor.GRAY + "/chunkbuster give <player> <type> [amount] §7- Give a chunkbuster item");
        sender.sendMessage(ChatColor.GRAY + "/chunkbuster setitem <type> §7- Set a chunkbuster's item to what you're holding");
        sender.sendMessage(ChatColor.GRAY + "/chunkbuster addchunkbuster <name> §7- Create a new chunkbuster from held item");
        sender.sendMessage(ChatColor.GRAY + "/chunkbuster reload §7- Reload the config and chunkbusters.yml");
        sender.sendMessage(ChatColor.GOLD + "§m--------------------");
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "(!) Usage: /chunkbuster give <player> <type> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "(!) Player not found.");
            return true;
        }

        String id = args[2].toLowerCase();
        ChunkBuster buster = chunkBusters.get(id);
        if (buster == null) {
            sender.sendMessage(ChatColor.RED + "(!) Unknown chunkbuster type: " + id);
            return true;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException ignored) {
                sender.sendMessage(ChatColor.RED + "(!) Amount must be a number.");
                return true;
            }
        }

        ItemStack item = new ItemStack(buster.getMaterial(), amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', buster.getTitle()));
            meta.setLore(new ArrayList<>(buster.getLore()));
            meta.getPersistentDataContainer().set(SeysChunkbuster.pluginKey, org.bukkit.persistence.PersistentDataType.STRING, id);
            item.setItemMeta(meta);
        }

        target.getInventory().addItem(item);
        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + "x " + id + " to " + target.getName());
        target.sendMessage(ChatColor.YELLOW + "You received " + amount + " chunkbuster(s)!");
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.getConfigManager().load();
        chunkBusters.clear();
        chunkBusters.putAll(ChunkbusterStorage.loadAll(bustersFile));
        sender.sendMessage(ChatColor.GREEN + "Reloaded config and chunkbusters successfully.");
        return true;
    }

    private boolean handleSetItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "(!) Only players can use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "(!) Usage: /chunkbuster setitem <type>");
            return true;
        }

        String id = args[1].toLowerCase();
        ChunkBuster existing = chunkBusters.get(id);
        if (existing == null) {
            sender.sendMessage(ChatColor.RED + "(!) Chunkbuster type not found: " + id);
            return true;
        }

        Player player = (Player) sender;
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            sender.sendMessage(ChatColor.RED + "(!) You're not holding an item.");
            return true;
        }

        ItemMeta meta = handItem.getItemMeta();
        if (meta == null) {
            sender.sendMessage(ChatColor.RED + "(!) Item has no metadata.");
            return true;
        }

        ChunkBuster updated = new ChunkBuster.Builder(existing)
                .setMaterial(handItem.getType())
                .setTitle(meta.hasDisplayName() ? meta.getDisplayName() : "")
                .setLore(meta.hasLore() ? meta.getLore() : Collections.emptyList())
                .build();

        chunkBusters.put(id, updated);
        ChunkbusterStorage.save(updated, bustersFile);
        sender.sendMessage(ChatColor.GREEN + "Updated and saved chunkbuster: " + id);
        return true;
    }

    private boolean handleAddChunkbuster(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "(!) Only players can use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "(!) Usage: /chunkbuster addchunkbuster <name>");
            return true;
        }

        String id = args[1].toLowerCase();
        if (chunkBusters.containsKey(id)) {
            sender.sendMessage(ChatColor.RED + "(!) A chunkbuster with that name already exists.");
            return true;
        }

        Player player = (Player) sender;
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            sender.sendMessage(ChatColor.RED + "(!) You're not holding an item.");
            return true;
        }

        ItemMeta meta = handItem.getItemMeta();
        if (meta == null) {
            sender.sendMessage(ChatColor.RED + "(!) Held item has no metadata.");
            return true;
        }

        ChunkBuster buster = new ChunkBuster.Builder()
                .setId(id)
                .setMaterial(handItem.getType())
                .setTitle(meta.hasDisplayName() ? meta.getDisplayName() : "")
                .setLore(meta.hasLore() ? meta.getLore() : Collections.emptyList())
                .build();

        chunkBusters.put(id, buster);
        ChunkbusterStorage.save(buster, bustersFile);
        sender.sendMessage(ChatColor.GREEN + "Created and saved new chunkbuster: " + id);
        return true;
    }
}