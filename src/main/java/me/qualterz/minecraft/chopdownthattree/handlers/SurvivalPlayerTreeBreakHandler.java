package me.qualterz.minecraft.chopdownthattree.handlers;

import java.util.stream.Collectors;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import me.qualterz.minecraft.chopdownthattree.utils.BlockUtil;

import static me.qualterz.minecraft.chopdownthattree.utils.BlockUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.EntityUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.TreeUtil.*;

public class SurvivalPlayerTreeBreakHandler extends PlayerTreeBreakHandler {
    public SurvivalPlayerTreeBreakHandler(BlockPos breakPos, World world, PlayerEntity player) {
        super(breakPos, world, player);
    }

    @Override
    public boolean handleBreak() {
        var parser = getTreeParserSetup()
                .direction(GrowDirection.UPWARDS)
                .apply().parse();

        var branchBlocks = parser.branchBlocks();

        var nonBreakedLogs = branchBlocks.stream()
                .filter(log -> !state.breakedLogs.contains(log))
                .collect(Collectors.toUnmodifiableSet());

        var logToBreak = nonBreakedLogs.stream()
                .sorted((prev, next) -> {
                    var distancePrev = prev.getSquaredDistance(breakPos);
                    var distanceNext = next.getSquaredDistance(breakPos);
                    return (int) Math.min(distancePrev, distanceNext);
                })
                .sorted()
                .findFirst();

        logToBreak.ifPresentOrElse(
                log -> {
                    updateBlockWithParticle(log);
                    damageMainHandItem(player);
                    state.breakedLogs.add(log);
                },
                () -> {
                    getTreeBreaker(parser).breakTree();
                    state.breakedLogs.removeAll(branchBlocks);
                    state.breakedLogs.removeIf(block -> isLogBlock(blockAt(block, world)));
                });

        state.markDirty();

        return !logToBreak.isPresent();
    }

    private void updateBlockWithParticle(BlockPos pos) {
        BlockUtil.updateBlockWithParticle(pos, world);
    }
}
