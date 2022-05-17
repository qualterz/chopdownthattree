package me.qualterz.minecraft.chopdownthattree.utils;

import lombok.experimental.UtilityClass;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A utility class that is used to work with blocks
 */
@UtilityClass
public class BlockUtil {
    /**
     * This method is used to get blocks positions attached to some block
     * @return a list of attached blocks positions
     * @see #getAttachedBlocks(BlockPos, World)
     */
    public static Set<BlockPos> getAttachedBlocks(BlockPos pos) {
        throw new NotImplementedException();
    }

    /**
     * Same as the {@link #getAttachedBlocks(BlockPos)} but used to get blocks instead of its positions
     * @return a list of attached blocks
     * @see #getAttachedBlocks(BlockPos) 
     */
    public static Set<Block> getAttachedBlocks(BlockPos pos, World world) {
        throw new NotImplementedException();
    }

    /**
     * This method is used to get blocks positions around some block
     * @return a set of neighbor blocks positions
     * @see #getNeighborBlocks(BlockPos, World)
     */
    public static Set<BlockPos> getNeighborBlocks(BlockPos pos) {
        var blocks = new HashSet<BlockPos>();
        {
            var begin = pos.down().west().north();
            var end = pos.up().east().south();

            for (int y = begin.getY(); y <= end.getY(); y++)
                for (int x = begin.getX(); x <= end.getX(); x++)
                    for (int z = begin.getZ(); z <= end.getZ(); z++)
                        blocks.add(new BlockPos(x, y, z));

            blocks.remove(pos);
        }

        return blocks;
    }

    /**
     * Same as the {@link #getNeighborBlocks(BlockPos)} but used to get blocks instead of its positions
     * @return a set of neighbor blocks
     * @see #getAttachedBlocks(BlockPos) 
     */
    public static Set<Block> getNeighborBlocks(BlockPos pos, World world) {
        return getNeighborBlocks(pos).stream()
                .map(world::getBlockState).map(BlockState::getBlock)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     *  This method is used to update a block with its particle effects
     */
    public static void updateBlockWithParticle(BlockPos pos, World world) {
        var state = world.getBlockState(pos);
        world.breakBlock(pos, false);
        world.setBlockState(pos, state);
    }

    public static Block blockAt(BlockPos pos, World world) {
        return blockStateAt(pos, world).getBlock();
    }

    public static BlockState blockStateAt(BlockPos pos, World world) {
        return world.getBlockState(pos);
    }

    /**
     * This method is used to compare two blocks for a variation
     * @return <code>true</code> if the name of a comparable block contains the name of an original block
     */
    public static boolean isBlockVariantOf(Block original, Block comparable) {
        return comparable.getName().getString().contains(original.getName().getString());
    }

    /**
     * This method is used to check if a block is stripped
     * @return <code>true</code> if the name of a block starts with <code>"Stripped"</code>
     */
    public static boolean isStrippedBlock(Block block) {
        return block.getName().getString().startsWith("Stripped");
    }

    public static boolean isLogBlock(Block block) {
        return block.getDefaultState().streamTags().anyMatch(
                tagKey -> tagKey.equals(BlockTags.LOGS)
        );
    }

    public static boolean isLeavesBlock(Block block) {
        return block.getDefaultState().streamTags().anyMatch(
                tagKey -> tagKey.equals(BlockTags.LEAVES)
        );
    }

    public static boolean isAirBlock(Block block) {
        return block.equals(Blocks.AIR);
    }

    public static boolean isBeeBlock(Block block) {
        return block.equals(Blocks.BEE_NEST);
    }
}
