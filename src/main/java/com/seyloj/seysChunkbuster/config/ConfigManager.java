package com.seyloj.seysChunkbuster.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    private int maxRollbacksPerPlayer;
    private boolean useFAWE;
    private List<String> disabledWorlds;
    private boolean invertWorldCheck;
    private boolean globalCooldown;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        this.maxRollbacksPerPlayer = config.getInt("max-rollbacks-per-player", 3);
        this.useFAWE = config.getBoolean("use-fawe-if-available", true);
        this.disabledWorlds = config.getStringList("disabledworlds");
        this.invertWorldCheck = config.getBoolean("invertworldcheck", false);
        this.globalCooldown = config.getBoolean("global-cooldown", false);

    }

    public int getMaxRollbacksPerPlayer() {
        return maxRollbacksPerPlayer;
    }

    public boolean getUseFAWE() {
        return useFAWE;
    }

    public List<String> getDisabledWorlds() {
        return disabledWorlds != null ? disabledWorlds : Collections.emptyList();
    }

    public boolean getGlobalCooldown() {
        return globalCooldown;
    }

    public boolean isWorldAllowed(String worldName) {
        boolean isListed = getDisabledWorlds().contains(worldName.toLowerCase());
        return invertWorldCheck ? isListed : !isListed;
    }

}
