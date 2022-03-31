package me.qualterz.minecraft.chopdownthattree;

import java.util.*;
import java.util.stream.Collectors;

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
	private final HashMap<PlayerEntity, Tree> treeBreakers = new LinkedHashMap<>();

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register(this::beforeBlockBreak);
		ServerTickEvents.END_WORLD_TICK.register(this::onEndTick);
	}

	private boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (Utils.isLogBlock(world.getBlockState(pos))) {
			var hasAxe = player.getMainHandStack().getItem().getName().getString().contains("Axe");
			var isCreative = player.isCreative();
			var isSneaking = player.isSneaking();

			var shouldIgnore = (isCreative && !hasAxe) || (!isCreative && isSneaking);
			if (shouldIgnore)
				return true;

			var tree = new Tree(world, pos);

			var shouldTraverseUpwardsOnly = !isCreative || !isSneaking;
			if (shouldTraverseUpwardsOnly)
				tree.traverseUpwardsOnly();

			treeBreakers.put(player, tree);
			trees.add(tree);
		}

		return true;
	}

	private void onEndTick(ServerWorld world) {
		trees.forEach(Tree::traverse);

		trees.stream().filter(Tree::isBlocksTraversed).forEach(tree -> {
			var attachedBlocks = tree.getDiscoveredBlocks().stream().filter(pos ->
					Utils.isBeeBlock(world.getBlockState(pos))).collect(Collectors.toSet());

			var breaker = treeBreakers.entrySet().stream().filter(entry ->
					entry.getValue().equals(tree)).findAny().map(Map.Entry::getKey);

			var blocksToBreak = tree.getTraversedBlocks();
			blocksToBreak.addAll(attachedBlocks);

			blocksToBreak.forEach(log -> {
				if (breaker.isPresent()) {
					var shouldBlocksDrop = !breaker.get().isCreative();
					world.breakBlock(log, shouldBlocksDrop, breaker.get());
				} else {
					world.breakBlock(log, true);
				}
			});
		});

		trees.removeIf(Tree::isBlocksTraversed);
		treeBreakers.entrySet().removeIf(entry -> entry.getValue().isBlocksTraversed());
	}
}
