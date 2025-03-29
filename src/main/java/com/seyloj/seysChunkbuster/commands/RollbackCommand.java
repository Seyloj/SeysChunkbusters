package com.seyloj.seysChunkbuster.commands;

import com.seyloj.seysChunkbuster.util.RollbackManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RollbackCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seyschunkbuster.rollback")) {
            sender.sendMessage(ChatColor.RED + "(!) You don't have permission to use this command.");
            return true;
        }

        Player target;
        if (args.length >= 1) {
            if (!sender.hasPermission("seyschunkbuster.rollback.others")) {
                sender.sendMessage(ChatColor.RED + "(!) You don't have permission to rollback others.");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "(!) Player not found: " + args[0]);
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "(!) Only players can use this command without arguments.");
                return true;
            }
            target = (Player) sender;
        }

        boolean success = RollbackManager.rollbackLast(target);
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Rolled back chunkbuster for " + target.getName() + ".");
            if (!sender.equals(target)) {
                target.sendMessage(ChatColor.YELLOW + "Your last chunkbuster was rolled back by " + sender.getName() + ".");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "(!) Failed to rollback for " + target.getName() + ". Check console for errors.");
        }

        return true;
    }
}