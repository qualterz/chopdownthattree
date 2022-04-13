package me.qualterz.minecraft.chopdownthattree;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

import me.qualterz.minecraft.chopdownthattree.utils.Tree;
import me.qualterz.minecraft.chopdownthattree.utils.Utils;
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

// TODO: implement branch break feature
// TODO: implement tree data load and save

@Log4j2
public class ChopDownThatTreeMod implements ModInitializer {
	public static final String MOD_ID = "chopdownthattree";

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

	private boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState block, BlockEntity blockEntity) {
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
				var breakedLogs = treeLogsBreaked.get(existingTree.get());
				var logToBreak = logsToBreak.poll();

				while (!Utils.isLogBlock(world.getBlockState(logToBreak))) {
					breakedLogs.add(logToBreak);
					logToBreak = logsToBreak.poll();

					if (logToBreak == null)
						return true;
				}

				Optional<Tree> finalExistingTree = existingTree;
				BlockPos finalLogToBreak = logToBreak;

				var treesToMerge = treeLogsBreaked.entrySet().stream()
						.filter(entry -> !entry.getKey().equals(finalExistingTree.get()))
						.filter(entry -> entry.getValue().stream()
								.anyMatch(p -> p.equals(finalLogToBreak)))
						.map(Map.Entry::getKey)
						.toList();

				if (!treesToMerge.isEmpty()) {
					var anotherBreakedLogs = treeLogsBreaked.entrySet().stream()
							.filter(entry -> !entry.getKey().equals(finalExistingTree.get()))
							.filter(entry -> entry.getValue().stream()
									.anyMatch(p -> p.equals(finalLogToBreak)))
							.map(Map.Entry::getValue)
							.flatMap(Collection::stream)
							.collect(Collectors.toSet());

					breakedLogs.addAll(anotherBreakedLogs);
					logsToBreak.removeAll(anotherBreakedLogs);

					logToBreak = logsToBreak.poll();

					treesToMerge.forEach(tree -> {
						trees.remove(tree);
						treeLogsToBreak.remove(tree);
						treeLogsBreaked.remove(tree);
					});
				}

				if (logToBreak == null)
					return true;

				breakedLogs.add(logToBreak);
				treeBreakers.put(existingTree.get(), player);

				var logBlock = world.getBlockState(logToBreak);
				world.breakBlock(logToBreak, false);
				world.setBlockState(logToBreak, logBlock);
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
				treeLogsToBreak.get(tree).add(tree.traverse());
		});

		trees.stream().filter(Tree::isBlocksTraversed).forEach(tree -> {
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
