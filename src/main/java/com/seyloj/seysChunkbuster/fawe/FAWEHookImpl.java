package com.seyloj.seysChunkbuster.fawe;

import com.seyloj.seysChunkbuster.model.BreakBehavior;
import com.seyloj.seysChunkbuster.model.ChunkBuster;
import com.seyloj.seysChunkbuster.model.Shape;
import com.seyloj.seysChunkbuster.util.ChunkbustExecutor;
import com.seyloj.seysChunkbuster.util.RollbackManager;
import com.seyloj.seysChunkbuster.util.VersionUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FAWEHookImpl implements FAWEHook {

    public void execute(Location placedLocation, ChunkBuster buster, Player player) {
        World world = placedLocation.getWorld();
        if (world == null) return;

        int worldMinY = VersionUtil.isAtLeast(17) ? world.getMinHeight() : 0;
        int minY = worldMinY + buster.getBottomLayerOffset();
        int maxY = ChunkbustExecutor.getMaxY(buster, placedLocation);

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

        ChunkbustExecutor.doEffects(placedLocation, buster);
    }

    private void applyFAWEClear(EditSession editSession, Location center, ChunkBuster buster, int minY, int maxY) {
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

    private void applyFAWETracked(EditSession editSession, Location center, ChunkBuster buster, int minY, int maxY, Map<ItemStack, Integer> dropMap) {
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
}
