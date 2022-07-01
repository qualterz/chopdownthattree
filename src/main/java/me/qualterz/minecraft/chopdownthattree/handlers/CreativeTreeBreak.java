package me.qualterz.minecraft.chopdownthattree.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CreativeTreeBreak extends PlayerTreeBreakHandler {
    public CreativeTreeBreak(BlockPos breakPos, World world, PlayerEntity player) {
        super(breakPos, world, player);
    }

    @Override
    public boolean handleBreak() {
        var parser = getTreeParserSetup().apply();

        getTreeBreaker(parser).breakTree();

        state.choppedLogs.removeAll(parser.branchBlocks());
        state.markDirty();

        return true;
    }
}
