package me.qualterz.minecraft.chopdownthattree;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

public class Tree {
    private final PriorityQueue<BlockPos> logsToTraverse = new PriorityQueue<>();
    private final LinkedHashSet<BlockPos> traversedLogs = new LinkedHashSet<>();

    private final World world;
    private final Block logBlock;

    public Tree(World world, BlockPos blockPos) {
        var block = world.getBlockState(blockPos);

        this.world = world;
        this.logBlock = block.getBlock();

        logsToTraverse.add(blockPos);
    }

    public BlockPos traverseLog() {
        if (logsToTraverse.isEmpty())
            return null;

        var pos = logsToTraverse.poll();

        var neighborBlocks = Utils.getNeighborBlocks(pos);

        var neighborLogs = neighborBlocks.stream().filter(
                p -> world.getBlockState(p).getBlock().equals(logBlock)
        ).toList();

        var nonTraversedLogs = neighborLogs.stream().filter(
                p -> !traversedLogs.contains(p) && !logsToTraverse.contains(p)
        ).toList();

        traversedLogs.add(pos);
        logsToTraverse.addAll(nonTraversedLogs);

        return pos;
    }

    public Collection<BlockPos> getTraversedLogs() {
        return traversedLogs;
    }

    public boolean isLogsTraversed() {
        return logsToTraverse.isEmpty();
    }
}
