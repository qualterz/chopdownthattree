package me.qualterz.minecraft.chopdownthattree.helpers;

import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Predicate;

import lombok.Getter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.qualterz.minecraft.chopdownthattree.utils.BlockUtil.*;

public class BlockCrawler {
    @Getter private final PriorityQueue<BlockPos> blocksToCrawl = new PriorityQueue<>();

    @Getter private final World world;
    @Getter private final BlockPos initialPos;

    public BlockCrawler(World world, BlockPos initialPos) {
        this.world = world;
        this.initialPos = initialPos;
    }

    private void initialize() {
        blocksToCrawl.add(initialPos);
    }

    public boolean hasBlocksToCrawl() {
        return !blocksToCrawl.isEmpty();
    }

    public Optional<BlockPos> crawl(Predicate<BlockPos> predicate) {
        var block = blocksToCrawl.poll();
        var shouldCrawl = predicate.test(block);

        Optional<BlockPos> result = Optional.empty();

        if (shouldCrawl) {
            var nextToCrawl = getNeighborBlocks(block);
            blocksToCrawl.addAll(nextToCrawl);

            result = Optional.of(block);
        }

        return result;
    }

    public void reset() {
        blocksToCrawl.clear();
        initialize();
    }
}