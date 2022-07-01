package me.qualterz.minecraft.chopdownthattree.setups;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

import me.qualterz.minecraft.chopdownthattree.handlers.*;

import static me.qualterz.minecraft.chopdownthattree.utils.EntityUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.TreeUtil.*;

public class TreeBreakHandlerSetup {
    private final BlockPos pos;
    private final World world;

    private TreeBreakHandlerSetup(BlockPos pos, World world) {
        this.pos = pos;
        this.world = world;
    }

    public static TreeBreakHandlerSetup initialize(BlockPos pos, World world) {
        return new TreeBreakHandlerSetup(pos, world);
    }

    public PlayerTreeBreakHandler forPlayer(PlayerEntity player) {
        if (!isTreeBranch(pos, world))
            return new DefaultTreeBreak(pos, world, player);

        if (player.isCreative()) {
            if (usesAxe(player))
                return new CreativeTreeBreak(pos, world, player);
            else
                return new DefaultTreeBreak(pos, world, player);
        } else {
            // Do not process block break as tree break
            if (isTreeBranchTop(pos, world) || player.isSneaking())
                return new DefaultTreeBreak(pos, world, player);
            else
                return new SurvivalTreeBreak(pos, world, player);
        }
    }
}
