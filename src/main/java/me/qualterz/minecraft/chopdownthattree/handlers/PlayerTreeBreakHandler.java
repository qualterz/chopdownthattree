package me.qualterz.minecraft.chopdownthattree.handlers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import me.qualterz.minecraft.chopdownthattree.TreeState;
import me.qualterz.minecraft.chopdownthattree.helpers.TreeBreaker;
import me.qualterz.minecraft.chopdownthattree.setups.TreeBreakerSetup;

public abstract class PlayerTreeBreakHandler extends BreakHandler {
    protected final PlayerEntity player;
    protected final TreeState state;
    protected BlockPos treePos;

    public PlayerTreeBreakHandler(BlockPos breakPos, World world, PlayerEntity player) {
        super(breakPos, world);
        this.player = player;

        state = TreeState.getState(world);

        if (state.isTreeExists(breakPos))
            treePos = breakPos;
        else {
            state.breakedLogs.entries().stream()
                    .filter(breaked -> breaked.getValue().equals(breakPos))
                    .findAny().ifPresentOrElse(
                            entry -> treePos = entry.getKey(),
                            () -> treePos = breakPos
                    );
        }
    }
}
