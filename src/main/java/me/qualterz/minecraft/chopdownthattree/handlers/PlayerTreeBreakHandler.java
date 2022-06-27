package me.qualterz.minecraft.chopdownthattree.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import me.qualterz.minecraft.chopdownthattree.TreeState;
import me.qualterz.minecraft.chopdownthattree.helpers.TreeParser;
import me.qualterz.minecraft.chopdownthattree.helpers.TreeBreaker;
import me.qualterz.minecraft.chopdownthattree.setups.TreeBreakerSetup;

public abstract class PlayerTreeBreakHandler extends BreakHandler {
    protected final PlayerEntity player;
    protected final TreeState state;

    public PlayerTreeBreakHandler(BlockPos breakPos, World world, PlayerEntity player) {
        super(breakPos, world);
        this.player = player;
        this.state = TreeState.getState(world);
    }

    TreeBreaker getTreeBreaker() {
        return TreeBreakerSetup.initialize(new TreeBreaker(breakPos, world)).forPlayer(player);
    }

    TreeBreaker getTreeBreaker(TreeParser parser) {
        return TreeBreakerSetup.initialize(new TreeBreaker(parser)).forPlayer(player);
    }

    TreeParser.Config getTreeParserSetup() {
        return TreeParser.setup().pos(breakPos).world(world);
    }
}
