package me.qualterz.minecraft.chopdownthattree;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Log4j2
public class ChopDownThatTree implements ModInitializer {
	private final List<Tree> trees = new LinkedList<>();
	private final HashMap<Tree, Stack<BlockPos>> treeLogsToBreak = new LinkedHashMap<>();
	private final HashMap<Tree, Vector<BlockPos>> treeLogsBreaked = new LinkedHashMap<>();
	private final HashMap<Tree, Boolean> treeBreaked = new LinkedHashMap<>();
	private final HashMap<Tree, PlayerEntity> treeBreakers = new LinkedHashMap<>();

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

			var existingTree = treeLogsToBreak.entrySet().stream().filter(entry ->
					entry.getValue().stream().anyMatch(p -> p.equals(pos))).findAny().map(Map.Entry::getKey);

			if (existingTree.isEmpty()) {
				var tree = new Tree(world, pos);

				var shouldTraverseUpwardsOnly = !isCreative || !isSneaking;
				if (shouldTraverseUpwardsOnly)
					tree.traverseUpwardsOnly();

				treeLogsToBreak.put(tree, new Stack<>());
				treeLogsBreaked.put(tree, new Vector<>());

				treeBreakers.put(tree, player);
				trees.add(tree);

				treeLogsToBreak.get(tree).push(pos);

				existingTree = Optional.of(tree);
			}

			var logs = treeLogsToBreak.get(existingTree.get());

			if (!logs.isEmpty()) {
				var logToBreak = logs.pop();
				var breakedLogs = treeLogsBreaked.get(existingTree.get());

				breakedLogs.add(logToBreak);

				var block = world.getBlockState(logToBreak);

				world.breakBlock(logToBreak, false);
				world.setBlockState(logToBreak, block);
			}

			var tool = player.getMainHandStack();
			tool.damage(1, player, (entity) ->
					entity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));

			return false;
		}

		return true;
	}

	private void onEndTick(ServerWorld world) {
		trees.forEach(tree -> {
			if (!tree.isBlocksTraversed())
				treeLogsToBreak.get(tree).push(tree.traverse());
		});

		trees.stream().filter(Tree::isBlocksTraversed).forEach(tree -> {
			// TODO: check for blocks that does not exists
			var isAllLogsBreaked = treeLogsBreaked.get(tree).containsAll(tree.getTraversedBlocks());
			var isPlayerInCreative = treeBreakers.get(tree).isCreative();

			if (isAllLogsBreaked || isPlayerInCreative) {
				var attachedBlocks = tree.getDiscoveredBlocks().stream().filter(pos ->
						Utils.isBeeBlock(world.getBlockState(pos))).collect(Collectors.toSet());

				var breaker = treeBreakers.get(tree);

				var blocksToBreak = tree.getTraversedBlocks();
				blocksToBreak.addAll(attachedBlocks);

				var shouldBlocksDrop = !breaker.isCreative();

				blocksToBreak.forEach(log -> world.breakBlock(log, shouldBlocksDrop, breaker));

				treeBreaked.put(tree, true);
			}
		});

		treeBreaked.forEach((tree, isBreaked) -> {
			trees.remove(tree);
			treeLogsToBreak.remove(tree);
			treeLogsBreaked.remove(tree);
		});

		treeBreaked.entrySet().removeIf(Map.Entry::getValue);
	}
}
