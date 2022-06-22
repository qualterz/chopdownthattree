package me.qualterz.minecraft.chopdownthattree.handlers;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.qualterz.minecraft.chopdownthattree.utils.BlockUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.EntityUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.TreeUtil.*;

public class SurvivalPlayerTreeBreakHandler extends PlayerTreeBreakHandler {
    public SurvivalPlayerTreeBreakHandler(BlockPos breakPos, World world, PlayerEntity player) {
        super(breakPos, world, player);
    }

    @Override
    public boolean handleBreak() {
        state.addTreeIfNotExists(treePos);

        chopTree();
        mergeTree();

        var lastBreakedLogsCount = state.lastBreakedLogsCount.get(treePos);

        if (lastBreakedLogsCount == null || lastBreakedLogsCount == 0) {
            breakTree();
            state.removeTree(treePos);

            return true;
        }

        damageMainHandItem(player, lastBreakedLogsCount);

        return false;
    }

    private void chopTree() {
        if (state.breakedLogs.get(treePos).isEmpty())
            state.addBreakedLogs(treePos, Set.of(treePos));

        var breakedLogs = state.breakedLogs.get(treePos);

        var nextBreakedLogs = breakedLogs.stream()
                // Last breaked logs
                .sorted(Comparator.reverseOrder())
                .limit(state.lastBreakedLogsCount.get(treePos))
                .sorted(Comparator.naturalOrder())

                // Do not process non branch blocks
                .filter(blockPos -> isLogBlock(blockAt(blockPos, world)))

                // Tree branch parts
                .flatMap(blockPos -> getTreeBranchParts(blockPos, world, GrowDirection.UPWARDS).stream())

                // Do not process tree branch parts infinitely
                .filter(blockPos -> !state.breakedLogs.get(treePos).contains(blockPos))

                .collect(Collectors.toUnmodifiableSet());

        state.addBreakedLogs(treePos, nextBreakedLogs);
        state.chopCount.computeIfPresent(treePos, (blockPos, integer) -> integer++);

        nextBreakedLogs.forEach(log -> updateBlockWithParticle(log, world));
    }

    private void mergeTree() {
        Set<BlockPos> treesToMerge = new HashSet<>();

        state.breakedLogs.get(treePos).forEach(breaked -> {
            if (state.isTreeExists(breaked) && !breaked.equals(treePos))
                treesToMerge.add(breaked);
        });

        treesToMerge.forEach(mergeTreePos -> {
            var mergeBreakedLogs = state.breakedLogs.get(mergeTreePos);
            state.addBreakedLogs(treePos, mergeBreakedLogs);

            state.breakedLogs.get(mergeTreePos).forEach(merged -> {
                if (!merged.equals(treePos))
                    state.removeTree(merged);
            });
            state.removeTree(mergeTreePos);
        });
    }

    private void breakTree() {
        var logs = state.breakedLogs.get(treePos);
        var attachments = logs.stream()
                .flatMap(log -> getTreeBranchPartAttachments(log, world).stream())
                .collect(Collectors.toUnmodifiableSet());

        Stream.concat(logs.stream(), attachments.stream())
                .forEach(block -> world.breakBlock(block, !player.isCreative(), player));
    }
}
