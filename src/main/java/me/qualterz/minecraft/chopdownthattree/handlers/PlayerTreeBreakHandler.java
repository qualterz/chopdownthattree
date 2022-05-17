package me.qualterz.minecraft.chopdownthattree.handlers;

import me.qualterz.minecraft.chopdownthattree.helpers.TreeBreaker;
import me.qualterz.minecraft.chopdownthattree.setups.TreeBreakerSetup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class PlayerTreeBreakHandler extends BreakHandler {
    protected final PlayerEntity player;

    public PlayerTreeBreakHandler(BlockPos pos, World world, PlayerEntity player) {
        super(pos, world);
        this.player = player;
    }

    TreeBreaker getTreeBreaker() {
        return TreeBreakerSetup.initialize(new TreeBreaker(pos, world)).forPlayer(player);
    }
}
