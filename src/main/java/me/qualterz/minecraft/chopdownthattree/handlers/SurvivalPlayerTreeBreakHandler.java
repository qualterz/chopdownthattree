package me.qualterz.minecraft.chopdownthattree.handlers;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.qualterz.minecraft.chopdownthattree.utils.BlockUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.EntityUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.TreeUtil.*;

import me.qualterz.minecraft.chopdownthattree.TreeState;

public class SurvivalPlayerTreeBreakHandler extends PlayerTreeBreakHandler {
    private final TreeState state;
    public SurvivalPlayerTreeBreakHandler(BlockPos pos, World world, PlayerEntity player) {
        super(pos, world, player);
        state = TreeState.getState(world);
    }

    @Override
    public boolean handleBreak() {
        state.addTreeIfNotExists(pos);

        chopTree();

        var lastBreakedLogsCount = state.lastBreakedLogsCount.get(pos);

        if (lastBreakedLogsCount == 0) {
            getTreeBreaker().breakTree();
            state.removeTree(pos);

            return true;
        }

        damageMainHandItem(player, lastBreakedLogsCount);

        return false;
    }

    private void chopTree() {
        var breakedLogs = state.breakedLogs.get(pos);

        if (breakedLogs.isEmpty()) {
            breakedLogs.add(pos);
            state.addBreakedLogs(pos, Set.of(pos));
        }

        var parts = breakedLogs.stream()
                // Last breaked logs
                .sorted(Comparator.reverseOrder())
                .limit(state.lastBreakedLogsCount.get(pos))

                // Do not process non branch blocks
                .filter(blockPos -> isLogBlock(blockAt(blockPos, world)))

                // Tree branch parts
                .flatMap(blockPos -> getTreeBranchParts(blockPos, world, GrowDirection.UPWARDS).stream())

                // Do not process tree branch parts infinitely
                .filter(blockPos -> !state.breakedLogs.get(pos).contains(blockPos))

                .collect(Collectors.toUnmodifiableSet());

        state.addBreakedLogs(pos, parts);
        state.chopCount.computeIfPresent(pos, (blockPos, integer) -> integer++);

        parts.forEach(log -> updateBlockWithParticle(log, world));
    }
}
