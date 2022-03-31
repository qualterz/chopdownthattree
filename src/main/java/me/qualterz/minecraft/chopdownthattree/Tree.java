package me.qualterz.minecraft.chopdownthattree;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;

public class Tree {
    private boolean shouldTraverseUpwardsOnly;

    private final PriorityQueue<BlockPos> blocksToTraverse = new PriorityQueue<>();
    private final LinkedHashSet<BlockPos> discoveredBlocks = new LinkedHashSet<>();
    private final LinkedHashSet<BlockPos> traversedBlocks = new LinkedHashSet<>();

    private final World world;
    private final BlockPos startPos;
    private final Block nodeBlock;

    public Tree(World world, BlockPos blockPos) {
        var block = world.getBlockState(blockPos);

        this.world = world;
        this.startPos = blockPos;
        this.nodeBlock = block.getBlock();

        blocksToTraverse.add(blockPos);
    }

    public BlockPos traverse() {
        if (blocksToTraverse.isEmpty())
            return null;

        var pos = blocksToTraverse.poll();

        var neighborBlocks = Utils.getNeighborBlocks(pos);

        if (shouldTraverseUpwardsOnly) {
            neighborBlocks.removeIf(p -> p.getY() <= startPos.getY());
        }

        var neighborNodes = neighborBlocks.stream().filter(
                p -> world.getBlockState(p).getBlock().equals(nodeBlock)
        ).toList();

        var nonTraversedNodes = neighborNodes.stream().filter(
                p -> !traversedBlocks.contains(p) && !blocksToTraverse.contains(p)
        ).toList();

        blocksToTraverse.addAll(nonTraversedNodes);
        discoveredBlocks.addAll(neighborBlocks);
        traversedBlocks.add(pos);

        return pos;
    }

    public HashSet<BlockPos> getDiscoveredBlocks() {
        return discoveredBlocks;
    }

    public HashSet<BlockPos> getTraversedBlocks() {
        return traversedBlocks;
    }

    public boolean isBlocksTraversed() {
        return blocksToTraverse.isEmpty();
    }

    public Tree traverseUpwardsOnly() {
        shouldTraverseUpwardsOnly = true;
        return this;
    }

    public BlockPos getStartPos() {
        return startPos;
    }
}
