package com.seyloj.seysChunkbuster.util;

import com.seyloj.seysChunkbuster.SeysChunkbuster;
import com.seyloj.seysChunkbuster.model.ChunkBuster;
import com.seyloj.seysChunkbuster.model.Shape;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.EditSession;

import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.seyloj.seysChunkbuster.model.BreakBehavior;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkbustExecutor {

    private static final Map<UUID, Long> globalCooldowns = new HashMap<>();
    private static final Map<UUID, Map<String, Long>> perBusterCooldowns = new HashMap<>();

    private static SeysChunkbuster plugin;

    public static void init(SeysChunkbuster plugin) {
        ChunkbustExecutor.plugin = plugin;
    }

    public static boolean hasCooldown(Player player, ChunkBuster buster) {
        if (buster.getCooldown() < 0) return false;

        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        if (plugin.getConfigManager().getGlobalCooldown()) {
            long expiresAt = globalCooldowns.getOrDefault(uuid, 0L);
            return now < expiresAt;
        } else {
            Map<String, Long> playerCooldowns = perBusterCooldowns.get(uuid);
            if (playerCooldowns == null) return false;
            long expiresAt = playerCooldowns.getOrDefault(buster.getId(), 0L);
            return now < expiresAt;
        }
    }


    public static long getRemainingCooldown(Player player, ChunkBuster buster) {
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        if (plugin.getConfigManager().getGlobalCooldown()) {
            long expiresAt = globalCooldowns.getOrDefault(uuid, 0L);
            return Math.max(0, (expiresAt - now) / 1000);
        } else {
            Map<String, Long> playerCooldowns = perBusterCooldowns.get(uuid);
            if (playerCooldowns == null) return 0;
            long expiresAt = playerCooldowns.getOrDefault(buster.getId(), 0L);
            return Math.max(0, (expiresAt - now) / 1000);
        }
    }


    public static void execute(Location placedLocation, ChunkBuster buster, Player player) {
        if (SeysChunkbuster.isFAWEEnabled()) {
            executeFAWE(placedLocation, buster, player);
        } else {
            executeBukkit(placedLocation, buster, player);
        }
        if (buster.getCooldown() > 0 && !player.hasPermission("seyschunkbuster.bypasscooldown")) {
            long expiresAt = System.currentTimeMillis() + buster.getCooldown() * 1000L;
            UUID uuid = player.getUniqueId();

            if (plugin.getConfigManager().getGlobalCooldown()) {
                globalCooldowns.put(uuid, expiresAt);
            } else {
                perBusterCooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                        .put(buster.getId(), expiresAt);
            }
        }
    }

    private static void executeFAWE(Location placedLocation, ChunkBuster buster, Player player) {
        World world = placedLocation.getWorld();
        if (world == null) return;

        int worldMinY = VersionUtil.isAtLeast(17) ? world.getMinHeight() : 0;
        int minY = worldMinY + buster.getBottomLayerOffset();
        int maxY = getMaxY(buster, placedLocation);

        int maxAllowedY = world.getMaxHeight();
        int minAllowedY = VersionUtil.isAtLeast(17) ? world.getMinHeight() : 0;

        minY = Math.max(minY, minAllowedY);
        maxY = Math.min(maxY, maxAllowedY);

        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);

        BlockVector3 min;
        BlockVector3 max;

        int radius = buster.getRadius();
        if (radius < 0) {
            Chunk chunk = placedLocation.getChunk();
            int x1 = chunk.getX() << 4;
            int z1 = chunk.getZ() << 4;
            int x2 = x1 + 15;
            int z2 = z1 + 15;
            min = BlockVector3.at(x1, minY, z1);
            max = BlockVector3.at(x2, maxY - 1, z2);
        } else {
            int centerX = placedLocation.getBlockX();
            int centerZ = placedLocation.getBlockZ();
            int x1 = centerX - radius;
            int z1 = centerZ - radius;
            int x2 = centerX + radius;
            int z2 = centerZ + radius;
            min = BlockVector3.at(x1, minY, z1);
            max = BlockVector3.at(x2, maxY - 1, z2);
        }

        EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld);

        try {
            if (buster.getBreakBehavior() == BreakBehavior.CLEAR) {
                applyFAWEClear(editSession, placedLocation, buster, minY, maxY);
            } else {
                Map<ItemStack, Integer> dropMap = new HashMap<>();
                applyFAWETracked(editSession, placedLocation, buster, minY, maxY, dropMap);

                for (Map.Entry<ItemStack, Integer> entry : dropMap.entrySet()) {
                    ItemStack drop = entry.getKey().clone();
                    drop.setAmount(entry.getValue());

                    switch (buster.getBreakBehavior()) {
                        case DROPRAWITEM:
                        case DROPSILKITEM:
                            world.dropItemNaturally(placedLocation, drop);
                            break;

                        case GIVERAWITEM:
                        case GIVESILKITEM:
                            player.getInventory().addItem(drop);
                            break;
                    }
                }
            }

            RollbackManager.rememberEdit(player, editSession);

        } finally {
            editSession.close();
        }

        doEffects(placedLocation, buster);
    }

    private static void applyFAWEClear(EditSession editSession, Location center, ChunkBuster buster, int minY, int maxY) {
        World world = center.getWorld();
        if (world == null) return;

        int radius = buster.getRadius();
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        int startX = (radius < 0) ? (center.getChunk().getX() << 4) : centerX - radius;
        int startZ = (radius < 0) ? (center.getChunk().getZ() << 4) : centerZ - radius;
        int endX = (radius < 0) ? startX + 15 : centerX + radius;
        int endZ = (radius < 0) ? startZ + 15 : centerZ + radius;

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                double dx = x - centerX;
                double dz = z - centerZ;

                if (buster.getShape() == Shape.CIRCLE && (dx * dx + dz * dz) > buster.getRadius() * buster.getRadius()) {
                    continue;
                }

                for (int y = minY; y <= maxY; y++) {
                    Material type = world.getBlockAt(x, y, z).getType();
                    boolean isIgnored = buster.getBlacklist().contains(type);
                    if ((isIgnored && !buster.isInvertBlacklist()) || (!isIgnored && buster.isInvertBlacklist())) {
                        continue;
                    }

                    BlockVector3 pos = BlockVector3.at(x, y, z);
                    editSession.setBlock(pos, BlockTypes.AIR.getDefaultState());
                }
            }
        }
    }

    private static void applyFAWETracked(EditSession editSession, Location center, ChunkBuster buster, int minY, int maxY, Map<ItemStack, Integer> dropMap) {
        World world = center.getWorld();
        if (world == null) return;

        int radius = buster.getRadius();
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        int startX = (radius < 0) ? (center.getChunk().getX() << 4) : centerX - radius;
        int startZ = (radius < 0) ? (center.getChunk().getZ() << 4) : centerZ - radius;
        int endX = (radius < 0) ? startX + 15 : centerX + radius;
        int endZ = (radius < 0) ? startZ + 15 : centerZ + radius;

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                double dx = x - centerX;
                double dz = z - centerZ;

                if (buster.getShape() == Shape.CIRCLE && (dx * dx + dz * dz) > buster.getRadius() * buster.getRadius()) continue;

                for (int y = minY; y <= maxY; y++) {
                    Block bukkitBlock = world.getBlockAt(x, y, z);
                    Material type = bukkitBlock.getType();

                    if (type == Material.AIR) continue;

                    boolean isIgnored = buster.getBlacklist().contains(type);
                    if ((isIgnored && !buster.isInvertBlacklist()) || (!isIgnored && buster.isInvertBlacklist())) {
                        continue;
                    }

                    BlockVector3 pos = BlockVector3.at(x, y, z);
                    editSession.setBlock(pos, BlockTypes.AIR.getDefaultState());

                    switch (buster.getBreakBehavior()) {
                        case DROPSILKITEM:
                        case GIVESILKITEM:
                            ItemStack silk = new ItemStack(type);
                            dropMap.merge(silk, 1, Integer::sum);
                            break;

                        case DROPRAWITEM:
                        case GIVERAWITEM:
                            Collection<ItemStack> rawDrops = bukkitBlock.getDrops(); // Simulate mining
                            for (ItemStack drop : rawDrops) {
                                dropMap.merge(drop, drop.getAmount(), Integer::sum);
                            }
                            break;
                    }
                }
            }
        }
    }








    private static void executeBukkit(Location placedLocation, ChunkBuster buster, Player player) {
        World world = placedLocation.getWorld();
        if (world == null) return;

        doEffects(placedLocation, buster);

        int worldMinY = VersionUtil.isAtLeast(17) ? world.getMinHeight() : 0;
        int minY = worldMinY + buster.getBottomLayerOffset();
        int maxY = getMaxY(buster, placedLocation);

        int maxAllowedY = world.getMaxHeight();
        int minAllowedY = VersionUtil.isAtLeast(17) ? world.getMinHeight() : 0;

        minY = Math.max(minY, minAllowedY);
        maxY = Math.min(maxY, maxAllowedY);

        int radius = buster.getRadius();
        if (radius < 0) {
            Chunk chunk = placedLocation.getChunk();
            int chunkMinX = chunk.getX() << 4;
            int chunkMinZ = chunk.getZ() << 4;
            clearChunk(world, chunkMinX, chunkMinZ, minY, maxY, buster, player);
        } else {
            int centerX = placedLocation.getBlockX();
            int centerZ = placedLocation.getBlockZ();
            int size = radius * 2 + 1;
            int startX = centerX - radius;
            int startZ = centerZ - radius;

            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    double dx = x - radius;
                    double dz = z - radius;
                    if (buster.getShape() == Shape.CIRCLE && (dx * dx + dz * dz) > radius * radius) {
                        continue;
                    }
                    int worldX = startX + x;
                    int worldZ = startZ + z;
                    clearColumn(world, worldX, worldZ, minY, maxY, buster, player);
                }
            }
        }
    }

    private static void doEffects(Location placedLocation, ChunkBuster buster) {
        World world = placedLocation.getWorld();
        Location effectLoc = placedLocation.clone().add(0.5, 0.5, 0.5);
        if(buster.getParticleOnBust() != null) {
            world.spawnParticle(buster.getParticleOnBust(), effectLoc, 1);
        }
        world.playSound(effectLoc, buster.getSoundOnBust(), 1, 1);
    }

    private static void clearChunk(World world, int startX, int startZ, int minY, int maxY, ChunkBuster buster, Player player) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                clearColumn(world, startX + x, startZ + z, minY, maxY, buster, player);
            }
        }
    }

    private static void clearColumn(World world, int x, int z, int minY, int maxY, ChunkBuster buster, Player player) {
        for (int y = minY; y <= maxY; y++) {
            Block block = world.getBlockAt(x, y, z);
            Material type = block.getType();

            if(type.equals(Material.AIR)) {
                continue;
            }

            boolean isIgnored = buster.getBlacklist().contains(type);
            if ((isIgnored && !buster.isInvertBlacklist()) || (!isIgnored && buster.isInvertBlacklist())) {
                continue;
            }

            BreakBehavior behavior = buster.getBreakBehavior();

            switch (behavior) {
                case CLEAR:
                    block.setType(Material.AIR, false);
                    break;

                case DROPRAWITEM:
                    block.breakNaturally(); // Equivalent to raw drop
                    break;

                case DROPSILKITEM:
                    ItemStack silkItem = new ItemStack(type);
                    world.dropItemNaturally(block.getLocation(), silkItem);
                    block.setType(Material.AIR, false);
                    break;

                case GIVERAWITEM:
                    Collection<ItemStack> drops = block.getDrops(); // Assume no tool
                    for (ItemStack drop : drops) {
                        player.getInventory().addItem(drop);
                    }
                    block.setType(Material.AIR, false);
                    break;

                case GIVESILKITEM:
                    player.getInventory().addItem(new ItemStack(type));
                    block.setType(Material.AIR, false);
                    break;
            }
        }
    }

    private static int getMaxY(ChunkBuster buster, Location placedLocation) {
        switch (buster.getVerticalBehavior()) {
            case FULLCHUNK:
                return placedLocation.getWorld().getMaxHeight();
            case EQUAL:
                return placedLocation.getBlockY();
            case BELOW:
                return placedLocation.getBlockY() - 1;
            default:
                return placedLocation.getBlockY() - 1;
        }
    }
}
