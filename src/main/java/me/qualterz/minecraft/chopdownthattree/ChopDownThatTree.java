package me.qualterz.minecraft.chopdownthattree;

import java.util.LinkedList;
import java.util.List;

import lombok.extern.log4j.Log4j2;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Log4j2
public class ChopDownThatTree implements ModInitializer {
	private final List<Tree> trees = new LinkedList<>();

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register(this::beforeBlockBreak);
		ServerTickEvents.END_WORLD_TICK.register(this::onEndTick);
	}

	private boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (!player.isSneaking() && Utils.isLogBlock(world.getBlockState(pos))) {
			trees.add(new Tree(world, pos));
		}

		return true;
	}

	private void onEndTick(ServerWorld world) {
		trees.forEach(tree -> {
			var log = tree.traverseLog();

			if (log != null) {
				world.breakBlock(log, true);
			}
		});

		trees.removeIf(Tree::isLogsTraversed);
	}
}
