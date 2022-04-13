package me.qualterz.minecraft.chopdownthattree.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static boolean isLogBlock(BlockState state) {
        return state.streamTags().anyMatch(
                tagKey -> tagKey.equals(BlockTags.LOGS)
        );
    }

    public static boolean isLeavesBlock(BlockState state) {
        return state.streamTags().anyMatch(
                tagKey -> tagKey.equals(BlockTags.LEAVES)
        );
    }

    public static boolean isAirBlock(BlockState state) {
        return state.getBlock().equals(Blocks.AIR);
    }

    public static boolean isBeeBlock(BlockState state) {
        return state.getBlock().equals(Blocks.BEE_NEST);
    }

    public static List<BlockPos> getNeighborBlocks(BlockPos pos) {
        var blocks = new ArrayList<BlockPos>();

        var startPos = pos.down().west().north();
        var endPos = pos.up().east().south();

        for (int y = startPos.getY(); y <= endPos.getY(); y++) {
            for (int x = startPos.getX(); x <= endPos.getX(); x++) {
                for (int z = startPos.getZ(); z <= endPos.getZ(); z++) {
                    blocks.add(new BlockPos(x, y, z));
                }
            }
        }

        blocks.remove(pos);

        return blocks;
    }
}
