package me.qualterz.minecraft.chopdownthattree.setups;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

import me.qualterz.minecraft.chopdownthattree.handlers.CreativePlayerTreeBreakHandler;
import me.qualterz.minecraft.chopdownthattree.handlers.DummyBreakHandler;
import me.qualterz.minecraft.chopdownthattree.handlers.SurvivalPlayerTreeBreakHandler;
import me.qualterz.minecraft.chopdownthattree.handlers.BreakHandler;

import me.qualterz.minecraft.chopdownthattree.TreeState;

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

    public BreakHandler forPlayer(PlayerEntity player) {
        if (!isTreeBranch(pos, world))
            return new DummyBreakHandler(pos, world);

        if (player.isCreative()) {
            if (usesAxe(player))
                return new CreativePlayerTreeBreakHandler(pos, world, player);
            else
                return new DummyBreakHandler(pos, world);
        } else {
            if (isTreeBranchEnd(pos, world) || player.isSneaking()) {
                TreeState.getState(world).removeTree(pos);
                return new DummyBreakHandler(pos, world);
            }
            else
                return new SurvivalPlayerTreeBreakHandler(pos, world, player);
        }
    }
}
