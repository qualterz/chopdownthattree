package me.qualterz.minecraft.chopdownthattree.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DefaultTreeBreak extends PlayerTreeBreakHandler {
    public DefaultTreeBreak(BlockPos breakPos, World world, PlayerEntity player) {
        super(breakPos, world, player);
    }

    @Override
    public boolean handleBreak() {
        state.choppedLogs.remove(breakPos);
        state.markDirty();
        return true;
    }
}
