package com.seyloj.seysChunkbuster.model;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChunkBuster {

    private final String id;
    private final Material material;
    private final String title;
    private final List<String> lore;
    private final int bottomLayerOffset;
    private final int radius;
    private final Shape shape;
    private final List<Material> blacklist;
    private final boolean invertBlacklist;
    private final boolean requireConfirmation;
    private final Sound soundOnBust;
    private final Particle particleOnBust;
    private final int cooldown;
    private final PlacementBehavior placementBehavior;
    private final VerticalBehavior verticalBehavior;
    private final BreakBehavior breakBehavior;
    private final Material fillMaterial;


    private ChunkBuster(Builder builder) {
        this.id = builder.id;
        this.material = builder.material;
        this.title = builder.title;
        this.lore = builder.lore;
        this.bottomLayerOffset = builder.bottomLayerOffset;
        this.radius = builder.radius;
        this.shape = builder.shape;
        this.blacklist = builder.blacklist != null ? builder.blacklist : new ArrayList<>();
        this.invertBlacklist = builder.invertBlacklist;
        this.requireConfirmation = builder.requireConfirmation;
        this.soundOnBust = builder.soundOnBust;
        this.particleOnBust = builder.particleOnBust;
        this.cooldown = builder.cooldown;
        this.placementBehavior = builder.placementBehavior;
        this.verticalBehavior = builder.verticalBehavior;
        this.breakBehavior = builder.breakBehavior;
        this.fillMaterial = builder.fillMaterial;
    }

    public static class Builder {
        private String id;
        private Material material;
        private String title;
        private List<String> lore;
        private int bottomLayerOffset = 0;
        private int radius = -1;
        private Shape shape = Shape.SQUARE;
        private List<Material> blacklist = new ArrayList<>(Arrays.asList(Material.BEDROCK));
        private boolean invertBlacklist = false;
        private boolean requireConfirmation = false;
        private Sound soundOnBust = Sound.ENTITY_GENERIC_EXPLODE;
        private Particle particleOnBust = Particle.CLOUD;
        private int cooldown = 0;
        private PlacementBehavior placementBehavior = PlacementBehavior.BREAK;
        private VerticalBehavior verticalBehavior = VerticalBehavior.BELOW;
        private BreakBehavior breakBehavior = BreakBehavior.CLEAR;
        private Material fillMaterial = Material.AIR;


        public Builder() {

        }

        public Builder(ChunkBuster existing) {
            this.id = existing.getId();
            this.material = existing.getMaterial();
            this.title = existing.getTitle();
            this.lore = new ArrayList<>(existing.getLore());
            this.bottomLayerOffset = existing.getBottomLayerOffset();
            this.radius = existing.getRadius();
            this.shape = existing.getShape();
            this.blacklist = new ArrayList<>(existing.getBlacklist());
            this.invertBlacklist = existing.isInvertBlacklist();
            this.requireConfirmation = existing.isRequireConfirmation();
            this.soundOnBust = existing.getSoundOnBust();
            this.particleOnBust = existing.getParticleOnBust();
            this.cooldown = existing.getCooldown();
            this.placementBehavior = existing.getPlacementBehavior();
            this.breakBehavior = existing.getBreakBehavior();
            this.fillMaterial = existing.getFillMaterial();
        }


        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setMaterial(Material material) {
            this.material = material;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setLore(List<String> lore) {
            this.lore = lore;
            return this;
        }

        public Builder setBottomLayerOffset(int offset) {
            this.bottomLayerOffset = offset;
            return this;
        }

        public Builder setRadius(int radius) {
            this.radius = radius;
            return this;
        }

        public Builder setShape(Shape shape) {
            this.shape = shape;
            return this;
        }

        public Builder setBlacklist(List<Material> blacklist) {
            this.blacklist = blacklist;
            return this;
        }

        public Builder setInvertBlacklist(boolean invertBlacklist) {
            this.invertBlacklist = invertBlacklist;
            return this;
        }

        public Builder setRequireConfirmation(boolean requireConfirmation) {
            this.requireConfirmation = requireConfirmation;
            return this;
        }

        public Builder setSoundOnBust(Sound soundOnBust) {
            this.soundOnBust = soundOnBust;
            return this;
        }

        public Builder setParticleOnBust(Particle particleOnBust) {
            this.particleOnBust = particleOnBust;
            return this;
        }

        public Builder setCooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder setPlacementBehavior(PlacementBehavior placementBehavior) {
            this.placementBehavior = placementBehavior;
            return this;
        }

        public Builder setVerticalBehavior(VerticalBehavior verticalBehavior) {
            this.verticalBehavior = verticalBehavior;
            return this;
        }

        public Builder setBreakBehavior(BreakBehavior breakBehavior) {
            this.breakBehavior = breakBehavior;
            return this;
        }

        public Builder setFillMaterial(Material fillMaterial) {
            this.fillMaterial = fillMaterial;
            return this;
        }

        public ChunkBuster build() {
            return new ChunkBuster(this);
        }
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getBottomLayerOffset() {
        return bottomLayerOffset;
    }

    public int getRadius() {
        return radius;
    }

    public Shape getShape() {
        return shape;
    }

    public List<Material> getBlacklist() {
        return blacklist;
    }

    public boolean isInvertBlacklist() {
        return invertBlacklist;
    }

    public boolean isRequireConfirmation() {
        return requireConfirmation;
    }

    public Sound getSoundOnBust() {
        return soundOnBust;
    }

    public Particle getParticleOnBust() {
        return particleOnBust;
    }

    public int getCooldown() {
        return cooldown;
    }

    public PlacementBehavior getPlacementBehavior() {
        return placementBehavior;
    }

    public VerticalBehavior getVerticalBehavior() {
        return verticalBehavior;
    }

    public BreakBehavior getBreakBehavior() {
        return breakBehavior;
    }

    public Material getFillMaterial() {
        return fillMaterial;
    }
}
