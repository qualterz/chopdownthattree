package me.qualterz.minecraft.chopdownthattree.helpers;

import java.util.Collection;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.Getter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.qualterz.minecraft.chopdownthattree.utils.BlockUtil.*;

public class BlockCrawler {
    @Getter private final PriorityQueue<BlockPos> blocksToCrawl = new PriorityQueue<>();

    @Getter private final BlockPos initialPos;
    @Getter private final World world;

    public BlockCrawler(BlockPos initialPos, World world) {
        this.initialPos = initialPos;
        this.world = world;
    }

    private void initialize() {
        blocksToCrawl.add(initialPos);
    }

    public boolean hasBlocksToCrawl() {
        return !blocksToCrawl.isEmpty();
    }

    public BlockPos nextBlockToCrawl() {
        return blocksToCrawl.peek();
    }

    public Optional<BlockPos> crawl(Predicate<BlockPos> blockPredicate, Function<BlockPos, Collection<BlockPos>> blocksGetter) {
        var block = blocksToCrawl.poll();
        var shouldCrawl = blockPredicate.test(block);

        Optional<BlockPos> result = Optional.empty();

        if (shouldCrawl) {
            var nextToCrawl = blocksGetter.apply(block);
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