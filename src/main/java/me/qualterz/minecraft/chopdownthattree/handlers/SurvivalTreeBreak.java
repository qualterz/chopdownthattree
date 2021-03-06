package me.qualterz.minecraft.chopdownthattree.handlers;

import java.util.stream.Collectors;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import me.qualterz.minecraft.chopdownthattree.utils.BlockUtil;

import static me.qualterz.minecraft.chopdownthattree.utils.EntityUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.TreeUtil.*;

public class SurvivalTreeBreak extends PlayerTreeBreakHandler {
    public SurvivalTreeBreak(BlockPos breakPos, World world, PlayerEntity player) {
        super(breakPos, world, player);
    }

    @Override
    public boolean handleBreak() {
        state.choppedLogs.add(breakPos);

        var parser = getTreeParserSetup()
                .direction(GrowDirection.UPWARDS)
                .apply().parse();

        var branchBlocks = parser.branchBlocks();

        var notChoppedLogs = branchBlocks.stream()
                .filter(log -> !state.choppedLogs.contains(log))
                .collect(Collectors.toUnmodifiableSet());

        // Closest block to break position
        var logToBreak = notChoppedLogs.stream()
                .sorted((prev, next) -> {
                    var distancePrev = prev.getSquaredDistance(breakPos);
                    var distanceNext = next.getSquaredDistance(breakPos);
                    return (int) (distancePrev - distanceNext);
                })
                .findFirst();

        logToBreak.ifPresentOrElse(
                log -> {
                    updateBlockWithParticle(log);
                    damageMainHandItem(player);
                    state.choppedLogs.add(log);
                },
                () -> {
                    getTreeBreaker(parser).breakTree();
                    state.choppedLogs.removeAll(branchBlocks);
                });

        state.markDirty();

        return logToBreak.isEmpty();
    }

    private void updateBlockWithParticle(BlockPos pos) {
        BlockUtil.updateBlockWithParticle(pos, world);
    }
}
