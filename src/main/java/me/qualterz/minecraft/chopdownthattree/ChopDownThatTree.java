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
	private final List<Tree> treesBreaked = new LinkedList<>();
	private final HashMap<Tree, Queue<BlockPos>> treeLogsToBreak = new LinkedHashMap<>();
	private final HashMap<Tree, HashSet<BlockPos>> treeLogsBreaked = new LinkedHashMap<>();
	private final HashMap<Tree, PlayerEntity> treeBreakers = new LinkedHashMap<>();

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register(this::beforeBlockBreak);
		ServerTickEvents.END_WORLD_TICK.register(this::onEndTick);
	}

	private boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		// TODO: implement branch break feature
		if (Utils.isLogBlock(world.getBlockState(pos))) {
			var hasAxe = player.getMainHandStack().getItem().getName().getString().contains("Axe");
			var isCreative = player.isCreative();
			var isSneaking = player.isSneaking();

			var shouldIgnore = (isCreative && !hasAxe) || (!isCreative && isSneaking);
			if (shouldIgnore)
				return true;

			var existingTree = treeLogsBreaked.entrySet().stream().filter(entry ->
					entry.getValue().stream().anyMatch(p -> p.equals(pos))).findAny().map(Map.Entry::getKey);

			if (existingTree.isEmpty()) {
				var tree = new Tree(world, pos);

				var shouldTraverseUpwardsOnly = !isCreative || !isSneaking;
				if (shouldTraverseUpwardsOnly)
					tree.traverseUpwardsOnly();

				treeLogsToBreak.put(tree, new PriorityQueue<>());
				treeLogsBreaked.put(tree, new LinkedHashSet<>());

				treeBreakers.put(tree, player);
				trees.add(tree);

				existingTree = Optional.of(tree);
			}

			var logsToBreak = treeLogsToBreak.get(existingTree.get());

			if (logsToBreak.isEmpty() && !existingTree.get().isBlocksTraversed()) {
				logsToBreak.add(existingTree.get().traverse());

				if (existingTree.get().getDiscoveredBlocks().stream().noneMatch(p ->
						Utils.isLogBlock(world.getBlockState(p)))) {
					treesBreaked.add(existingTree.get());
					return true;
				}
			}

			if (!logsToBreak.isEmpty()) {
				var logToBreak = logsToBreak.poll();
				var breakedLogs = treeLogsBreaked.get(existingTree.get());

				Optional<Tree> finalExistingTree = existingTree;
				BlockPos finalLogToBreak = logToBreak;

				var treesToMerge = treeLogsBreaked.entrySet().stream()
						.filter(entry -> !entry.getKey().equals(finalExistingTree.get()))
						.filter(entry -> entry.getValue().stream()
								.anyMatch(p -> p.equals(finalLogToBreak)))
						.toList();

				var anotherBreakedLogs = treeLogsBreaked.entrySet().stream()
						.filter(entry -> !entry.getKey().equals(finalExistingTree.get()))
						.filter(entry -> entry.getValue().stream()
								.anyMatch(p -> p.equals(finalLogToBreak)))
						.map(Map.Entry::getValue)
						.flatMap(Collection::stream)
						.collect(Collectors.toSet());

				if (!anotherBreakedLogs.isEmpty()) {
					breakedLogs.addAll(anotherBreakedLogs);
					logsToBreak.removeAll(anotherBreakedLogs);

					logToBreak = logsToBreak.poll();

					treeLogsToBreak.entrySet().stream()
									.filter(entry -> treesToMerge.stream()
											.anyMatch(tree -> entry.getKey() == tree))
									.map(Map.Entry::getValue)
							.forEach(collection -> collection.removeAll(breakedLogs));
				}

				if (logToBreak == null)
					return true;

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
				// TODO: add new placed neighbor logs
				treeLogsToBreak.get(tree).add(tree.traverse());
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

				blocksToBreak.removeIf(pos -> !Utils.isLogBlock(world.getBlockState(pos)));
				blocksToBreak.addAll(attachedBlocks);

				var shouldBlocksDrop = !breaker.isCreative();

				blocksToBreak.forEach(log -> world.breakBlock(log, shouldBlocksDrop, breaker));

				treesBreaked.add(tree);
			}
		});

		treesBreaked.forEach(tree -> {
			trees.remove(tree);
			treeLogsToBreak.remove(tree);
			treeLogsBreaked.remove(tree);
		});

		treesBreaked.clear();
	}
}
