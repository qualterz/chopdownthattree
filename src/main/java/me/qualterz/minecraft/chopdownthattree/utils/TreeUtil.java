package me.qualterz.minecraft.chopdownthattree.utils;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.qualterz.minecraft.chopdownthattree.utils.BlockUtil.*;

/**
 * A utility class that is used to work with tree structures
 */
@UtilityClass
public class TreeUtil {
    /**
     * Represents the tree grow direction
     */
    public enum GrowDirection {
        /**
         * Upwards to the end of a tree branch
         */
        UPWARDS,

        /**
         * Downwards to the bottom of a tree
         */
        DOWNWARDS,

        /**
         * From the bottom to the top of a tree
         * @see #UPWARDS
         * @see #DOWNWARDS
         */
        BOTH
    }

    public static Set<BlockPos> getTreeBranchPartAttachments(BlockPos pos, World world) {
        return getNeighborBlocks(pos)
                .stream()
                .filter(block -> isTreeBranchAttachmentBlock(blockAt(block, world)))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Set<BlockPos> getTreeBranchParts(BlockPos pos, World world, GrowDirection direction) {
        var originalBlock = blockAt(pos, world);

        return getNeighborBlocks(pos).stream().filter(blockPos -> {
            switch (direction) {
                case UPWARDS -> {
                    if (blockPos.getY() < pos.getY())
                        return false;
                }
                case DOWNWARDS -> {
                    if (blockPos.getY() > pos.getY())
                        return false;
                }
            }

            var block = blockAt(blockPos, world);

            return isLogBlock(block) && block.equals(originalBlock)
                    || isBlockVariantOf(block, originalBlock)
                    || isStrippedBlock(block);
        }).collect(Collectors.toUnmodifiableSet());
    }

    public static boolean isTreeBranchAttachmentBlock(Block block) {
        return isBeeBlock(block);
    }

    public static boolean isTreeBranchBlock(Block block) {
        return isLogBlock(block);
    }

    public static boolean isTreeBranch(BlockPos pos, World world) {
        return isLogBlock(blockAt(pos, world))
                && !getTreeBranchParts(pos, world, GrowDirection.BOTH).isEmpty();
    }

    public static boolean isTreeBranchEnd(BlockPos pos, World world) {
        return isLogBlock(blockAt(pos, world))
                && getTreeBranchParts(pos, world, GrowDirection.UPWARDS).isEmpty();
    }
}
