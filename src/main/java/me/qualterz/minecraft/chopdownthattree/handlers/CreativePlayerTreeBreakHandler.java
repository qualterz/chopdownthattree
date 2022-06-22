package me.qualterz.minecraft.chopdownthattree.handlers;

import me.qualterz.minecraft.chopdownthattree.helpers.TreeBreaker;
import me.qualterz.minecraft.chopdownthattree.setups.TreeBreakerSetup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CreativePlayerTreeBreakHandler extends PlayerTreeBreakHandler {
    public CreativePlayerTreeBreakHandler(BlockPos breakPos, World world, PlayerEntity player) {
        super(breakPos, world, player);
    }

    @Override
    public boolean handleBreak() {
        TreeBreakerSetup.initialize(new TreeBreaker(breakPos, world)).forPlayer(player).breakTree();
        return true;
    }
}
