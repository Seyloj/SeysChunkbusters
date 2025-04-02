package com.seyloj.seysChunkbuster.util;

import com.seyloj.seysChunkbuster.model.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ChunkbusterStorage {

    public static Map<String, ChunkBuster> loadAll(File file) {
        Map<String, ChunkBuster> map = new HashMap<>();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) continue;

            try {
                ChunkBuster buster = parseBuster(id, section);
                map.put(id.toLowerCase(), buster);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return map;
    }

    public static void save(ChunkBuster buster, File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.createSection(buster.getId());
        saveToSection(buster, section);

        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chunkbuster to " + file.getName(), e);
        }
    }

    public static void saveAll(Map<String, ChunkBuster> chunkBusters, File file) {
        FileConfiguration config = new YamlConfiguration();

        for (ChunkBuster buster : chunkBusters.values()) {
            ConfigurationSection section = config.createSection(buster.getId());
            saveToSection(buster, section);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save all chunkbusters to " + file.getName(), e);
        }
    }

    private static void saveToSection(ChunkBuster buster, ConfigurationSection section) {
        section.set("material", buster.getMaterial().name());
        section.set("title", buster.getTitle());
        section.set("lore", buster.getLore());
        section.set("bottomlayeroffset", buster.getBottomLayerOffset());
        section.set("radius", buster.getRadius());
        section.set("shape", buster.getShape().name());
        section.set("blacklist", Optional.ofNullable(buster.getBlacklist())
                .orElse(Collections.emptyList())
                .stream()
                .map(Material::name)
                .collect(Collectors.toList()));
        section.set("invertblacklist", buster.isInvertBlacklist());
        section.set("require_confirmation", buster.isRequireConfirmation());
        section.set("sound_on_bust", buster.getSoundOnBust() != null ? buster.getSoundOnBust().name() : "NONE");
        section.set("particle_on_bust", buster.getParticleOnBust() != null ? buster.getParticleOnBust().name() : "NONE");
        section.set("cooldown", buster.getCooldown());
        section.set("placement_behavior", buster.getPlacementBehavior().name());
        section.set("vertical_behavior", buster.getVerticalBehavior().name());
        section.set("break_behavior", buster.getBreakBehavior().name());
        section.set("fill_material", buster.getFillMaterial().name());
    }

    private static ChunkBuster parseBuster(String id, ConfigurationSection section) {
        Material material = Material.getMaterial(section.getString("material", "BEACON"));
        if (material == null) throw new IllegalArgumentException("Invalid material for chunkbuster: " + id);

        String title = ColorUtil.color(section.getString("title", ""));
        List<String> lore = section.getStringList("lore").stream()
                .map(ColorUtil::color)
                .collect(Collectors.toList());
        int bottomLayerOffset = section.getInt("bottomlayeroffset", 0);
        int radius = section.getInt("radius", -1);
        Shape shape = Shape.valueOf(section.getString("shape", "SQUARE").toUpperCase());

        List<Material> blacklist = section.getStringList("blacklist").stream()
                .map(Material::getMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        boolean invertBlacklist = section.getBoolean("invertblacklist", false);
        boolean requireConfirmation = section.getBoolean("require_confirmation", false);

        String soundStr = section.getString("sound_on_bust", "ENTITY_GENERIC_EXPLODE").toUpperCase();
        Sound sound = soundStr.equals("NONE") ? null : Sound.valueOf(soundStr);

        String particleStr = section.getString("particle_on_bust", "EXPLOSION_LARGE").toUpperCase();
        Particle particle = particleStr.equals("NONE") ? null : Particle.valueOf(particleStr);

        int cooldown = section.getInt("cooldown", 0);
        PlacementBehavior placementBehavior = PlacementBehavior.valueOf(section.getString("placement_behavior", "BREAK").toUpperCase());
        VerticalBehavior verticalBehavior = VerticalBehavior.valueOf(section.getString("vertical_behavior", "BELOW").toUpperCase());
        BreakBehavior breakBehavior = BreakBehavior.valueOf(section.getString("break_behavior", "CLEAR").toUpperCase());
        Material fillMaterial = Material.getMaterial(section.getString("fill_material", "AIR").toUpperCase());

        return new ChunkBuster.Builder()
                .setId(id)
                .setMaterial(material)
                .setTitle(title)
                .setLore(lore)
                .setBottomLayerOffset(bottomLayerOffset)
                .setRadius(radius)
                .setShape(shape)
                .setBlacklist(blacklist)
                .setInvertBlacklist(invertBlacklist)
                .setRequireConfirmation(requireConfirmation)
                .setSoundOnBust(sound)
                .setParticleOnBust(particle)
                .setCooldown(cooldown)
                .setPlacementBehavior(placementBehavior)
                .setVerticalBehavior(verticalBehavior)
                .setBreakBehavior(breakBehavior)
                .setFillMaterial(fillMaterial)
                .build();
    }
}