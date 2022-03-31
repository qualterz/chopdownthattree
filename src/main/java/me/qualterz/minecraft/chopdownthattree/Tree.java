package me.qualterz.minecraft.chopdownthattree;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

public class Tree {
    private boolean shouldTraverseUpwardsOnly;

    private final PriorityQueue<BlockPos> logsToTraverse = new PriorityQueue<>();
    private final LinkedHashSet<BlockPos> discoveredBlocks = new LinkedHashSet<>();
    private final LinkedHashSet<BlockPos> traversedLogs = new LinkedHashSet<>();

    private final World world;
    private final BlockPos startPos;
    private final Block logBlock;

    public Tree(World world, BlockPos blockPos) {
        var block = world.getBlockState(blockPos);

        this.world = world;
        this.startPos = blockPos;
        this.logBlock = block.getBlock();

        logsToTraverse.add(blockPos);
    }

    public BlockPos traverseLog() {
        if (logsToTraverse.isEmpty())
            return null;

        var pos = logsToTraverse.poll();

        var neighborBlocks = Utils.getNeighborBlocks(pos);

        if (shouldTraverseUpwardsOnly) {
            neighborBlocks.removeIf(p -> p.getY() <= startPos.getY());
        }

        var neighborLogs = neighborBlocks.stream().filter(
                p -> world.getBlockState(p).getBlock().equals(logBlock)
        ).toList();

        var nonTraversedLogs = neighborLogs.stream().filter(
                p -> !traversedLogs.contains(p) && !logsToTraverse.contains(p)
        ).toList();

        logsToTraverse.addAll(nonTraversedLogs);
        discoveredBlocks.addAll(neighborBlocks);
        traversedLogs.add(pos);

        return pos;
    }

    public Collection<BlockPos> getDiscoveredBlocks() {
        return discoveredBlocks;
    }

    public Collection<BlockPos> getTraversedLogs() {
        return traversedLogs;
    }

    public boolean isLogsTraversed() {
        return logsToTraverse.isEmpty();
    }

    public Tree traverseUpwardsOnly() {
        shouldTraverseUpwardsOnly = true;
        return this;
    }

    public BlockPos getStartPos() {
        return startPos;
    }
}
