package me.qualterz.minecraft.chopdownthattree.helpers;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.qualterz.minecraft.chopdownthattree.utils.BlockUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.TreeUtil.*;

/**
 * A class that allows to parse tree structures
 */
@Accessors(fluent = true)
@Builder(builderMethodName = "setup", buildMethodName = "apply", builderClassName = "Config")
public class TreeParser {
    /**
     * Position of the tree branch or trunk
     */
    @Getter
    private BlockPos blockPos;

    /**
     * World where the tree is located
     */
    @Getter
    private World world;

    /**
     * Parse direction
     */
    @Getter @Builder.Default
    private GrowDirection direction = GrowDirection.BOTH;

    @Getter private final Set<BlockPos> branchBlocks = new LinkedHashSet<>();
    @Getter private final SetMultimap<BlockPos, BlockPos> branchAttachmentBlocks = LinkedHashMultimap.create();

    public Set<BlockPos> attachmentBlocks() {
        return new HashSet<>(branchAttachmentBlocks.values());
    }

    public Set<BlockPos> blocks() {
        return Stream.concat(branchBlocks.stream(), branchAttachmentBlocks.values().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * This method is used to parse the tree
     */
    public TreeParser parse() {
        Set<BlockPos> processedBranchBlocks = new HashSet<>();
        Set<BlockPos> ignoredBranchBlocks = new HashSet<>();

        if (isTreeBranchBlock(blockAt(blockPos, world))) {
            var treeSlice = getTreeSlice(blockPos);
            var nextProcessed = treeSlice.stream()
                    .flatMap(branchPos -> getTreeBranchParts(branchPos, world, direction).stream())
                    .collect(Collectors.toSet());

            processedBranchBlocks.addAll(treeSlice);
            processedBranchBlocks.addAll(nextProcessed);

            branchBlocks.addAll(processedBranchBlocks);

            if (direction != GrowDirection.BOTH) ignoredBranchBlocks.addAll(treeSlice);
        }

        while (processedBranchBlocks.size() > 0) {
            processedBranchBlocks = processedBranchBlocks.stream()
                    // Ignore blocks
                    .filter(branchPos -> !ignoredBranchBlocks.contains(branchPos))

                    // Get branch parts
                    .flatMap(branchPos -> getTreeBranchParts(branchPos, world, GrowDirection.BOTH).stream())

                    // Do not process tree branch parts infinitely
                    .filter(branchBlock -> !branchBlocks.contains(branchBlock))

                    .collect(Collectors.toUnmodifiableSet());

            branchBlocks.addAll(processedBranchBlocks);
        }

        processBranchAttachmentBlocks();

        return this;
    }

    private void processBranchAttachmentBlocks() {
        branchBlocks.forEach(block -> {
            var attachments = getTreeBranchPartAttachments(block, world);

            if (!attachments.isEmpty())
                branchAttachmentBlocks.putAll(block, attachments);
        });
    }

    private Set<BlockPos> getTreeSlice(BlockPos blockPos) {
        var crawler = new BlockCrawler(blockPos, world);

        Set<BlockPos> slice = new HashSet<>();

        while (crawler.hasBlocksToCrawl()) {
            var crawled = crawler.crawl(
                    branchPos -> branchPos.getY() == blockPos.getY() && !slice.contains(branchPos),
                    branchPos -> getTreeBranchParts(branchPos, world, GrowDirection.UPWARDS)
            );

            crawled.ifPresent(slice::add);
        }

        return slice;
    }
}
