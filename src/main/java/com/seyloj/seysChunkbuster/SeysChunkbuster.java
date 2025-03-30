package com.seyloj.seysChunkbuster;

import com.seyloj.seysChunkbuster.commands.ChunkbusterCommand;
import com.seyloj.seysChunkbuster.commands.ChunkbusterTabCompleter;
import com.seyloj.seysChunkbuster.commands.RollbackCommand;
import com.seyloj.seysChunkbuster.config.ConfigManager;
import com.seyloj.seysChunkbuster.fawe.FAWEHook;
import com.seyloj.seysChunkbuster.fawe.FAWEHookImpl;
import com.seyloj.seysChunkbuster.fawe.FAWENotAvailable;
import com.seyloj.seysChunkbuster.listeners.ConfirmationClickListener;
import com.seyloj.seysChunkbuster.listeners.PlacementListener;
import com.seyloj.seysChunkbuster.model.ChunkBuster;
import com.seyloj.seysChunkbuster.util.ChunkbustExecutor;
import com.seyloj.seysChunkbuster.util.ChunkbusterStorage;
import com.seyloj.seysChunkbuster.util.RollbackManager;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public final class SeysChunkbuster extends JavaPlugin {

    private final Map<String, ChunkBuster> chunkBusters = new HashMap<>();

    public static NamespacedKey pluginKey;
    private static boolean faweEnabled;

    private ConfigManager configManager;

    private static FAWEHook faweHook;

    public static SeysChunkbuster instance;

    @Override
    public void onEnable() {
        instance = this;
        this.configManager = new ConfigManager(this);

        pluginKey = new NamespacedKey(this, "SeysChunkbusterKey");

        saveResource("chunkbusters.yml", false);
        loadChunkBusters();

        getServer().getPluginManager().registerEvents(new PlacementListener(this, chunkBusters), this);
        getServer().getPluginManager().registerEvents(new ConfirmationClickListener(), this);

        getCommand("chunkbuster").setExecutor(new ChunkbusterCommand(this, chunkBusters));
        getCommand("chunkbuster").setTabCompleter(new ChunkbusterTabCompleter(chunkBusters));

        getCommand("chunkbusterrollback").setExecutor(new RollbackCommand());

        RollbackManager.setMaxRollback(getConfig().getInt("max-rollbacks-per-player", 3));

        ChunkbustExecutor.init(this);


        faweEnabled = Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit") && configManager.getUseFAWE();

        if(faweEnabled) {
            RollbackManager.setMaxRollback(configManager.getMaxRollbacksPerPlayer());
            if (isFAWEEnabled()) {
                try {
                    faweHook = new FAWEHookImpl();
                    getLogger().info("FAWE support enabled.");
                } catch (Throwable e) {
                    getLogger().warning("Failed to initialize FAWE support: " + e.getMessage());
                }
            } else {
                getLogger().warning("FAWE not found. Falling back to vanilla chunkbusting.");
                this.faweHook = new FAWENotAvailable();
            }
        }


    }

    private void loadChunkBusters() {
        File bustersFile = new File(getDataFolder(), "chunkbusters.yml");

        if (!bustersFile.exists()) {
            saveResource("chunkbusters.yml", false);
            getLogger().warning("chunkbusters.yml not found. A default one has been generated.");
        }

        chunkBusters.clear();
        chunkBusters.putAll(ChunkbusterStorage.loadAll(bustersFile));

        getLogger().info("Loaded " + chunkBusters.size() + " chunkbuster(s).");
    }



    @Override
    public void onDisable() {

    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static boolean isFAWEEnabled() {
        return faweEnabled;
    }

    public ChunkBuster getChunkBuster(String id) {
        return chunkBusters.get(id);
    }

    public Collection<ChunkBuster> getAllChunkBusters() {
        return chunkBusters.values();
    }

    public static FAWEHook getFAWEHook() {
        return faweHook;
    }


    public void logInfo(String message) {
        getLogger().info(message);
    }
    public void logWarning(String message) {
        getLogger().warning(message);
    }
    public void logError(String message) {
        getLogger().severe(message);
    }

}
