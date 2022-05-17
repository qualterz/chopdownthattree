package me.qualterz.minecraft.chopdownthattree.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CreativePlayerTreeBreakHandler extends PlayerTreeBreakHandler {
    public CreativePlayerTreeBreakHandler(BlockPos pos, World world, PlayerEntity player) {
        super(pos, world, player);
    }

    @Override
    public boolean handleBreak() {
        getTreeBreaker().breakTree();
        return true;
    }
}
