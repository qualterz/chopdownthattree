package me.qualterz.minecraft.chopdownthattree;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

import me.qualterz.minecraft.chopdownthattree.utils.TreeCrawler;
import me.qualterz.minecraft.chopdownthattree.utils.Utils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// TODO: implement branch break feature
// TODO: decompose callback functions
// TODO: account dimensions
// TODO: use Multimap instead of HashMap
// TODO: connect stripped logs
// TODO: store crawlers in state object

@Log4j2
public class ChopDownThatTreeMod implements ModInitializer {
	public static final String MOD_ID = "chopdownthattree";

	private final List<TreeCrawler> treeCrawlers = new LinkedList<>();
	private final List<TreeCrawler> treesBreaked = new LinkedList<>();
	private final HashMap<TreeCrawler, Queue<BlockPos>> treeLogsToBreak = new LinkedHashMap<>();
	private final HashMap<TreeCrawler, HashSet<BlockPos>> treeLogsBreaked = new LinkedHashMap<>();
	private final HashMap<TreeCrawler, PlayerEntity> treeBreakers = new LinkedHashMap<>();

	@Override
	public void onInitialize() {
		ServerWorldEvents.LOAD.register(this::onWorldLoad);
		ServerWorldEvents.UNLOAD.register(this::onWorldUnload);
		PlayerBlockBreakEvents.BEFORE.register(this::beforeBlockBreak);
		ServerTickEvents.END_WORLD_TICK.register(this::onEndTick);
	}

	private void onWorldLoad(MinecraftServer server, ServerWorld world) {
		var state = (TreesState) world.getPersistentStateManager()
				.getOrCreate(TreesState::fromNbt, TreesState::new, MOD_ID);

		treeCrawlers.addAll(state.treePositions.stream().map(pos -> new TreeCrawler(world, pos)).toList());

		state.treePositions.forEach(pos -> {
			var treeCrawler = new TreeCrawler(world, pos);
			var breaker = state.treeBreakers.get(pos);

			treeCrawlers.add(treeCrawler);

			treeCrawlers.forEach(t -> {
				treeLogsToBreak.put(t, new PriorityQueue<>());
				treeLogsBreaked.put(t, new LinkedHashSet<>());

				treeLogsBreaked.get(t).addAll(state.treeLogsBreaked.get(t.getStartPos()));
			});

			treeBreakers.put(treeCrawler, server.getPlayerManager().getPlayer(breaker));
		});

	}

	private void onWorldUnload(MinecraftServer server, ServerWorld world) {
		treeCrawlers.clear();
		treesBreaked.clear();
		treeLogsToBreak.clear();
		treeLogsBreaked.clear();
		treeBreakers.clear();
	}

	private boolean beforeBlockBreak(World world,
									 PlayerEntity player,
									 BlockPos pos,
									 BlockState block,
									 BlockEntity blockEntity)
	{
		if (Utils.isLogBlock(world.getBlockState(pos))) {
			var hasAxe = player.getMainHandStack().getItem().getName().getString().contains("Axe");
			var isCreative = player.isCreative();
			var isSneaking = player.isSneaking();

			var shouldIgnore = (isCreative && !hasAxe) || (!isCreative && isSneaking);
			if (shouldIgnore)
				return true;

			var existingTree = treeLogsBreaked.entrySet().stream().filter(entry ->
					entry.getValue().stream().anyMatch(p -> p.equals(pos))).findAny().map(Map.Entry::getKey);

			var state = (TreesState) world.getServer().getWorld(world.getRegistryKey())
					.getPersistentStateManager().get(TreesState::fromNbt, MOD_ID);

			if (existingTree.isEmpty()) {
				var treeCrawler = new TreeCrawler(world, pos);

				var shouldTraverseUpwardsOnly = !isCreative || !isSneaking;
				if (shouldTraverseUpwardsOnly)
					treeCrawler.traverseUpwardsOnly();

				treeLogsToBreak.put(treeCrawler, new PriorityQueue<>());
				treeLogsBreaked.put(treeCrawler, new LinkedHashSet<>());

				treeCrawlers.add(treeCrawler);

				state.treePositions.add(treeCrawler.getStartPos());
				state.markDirty();

				existingTree = Optional.of(treeCrawler);
			}

			var logsToBreak = treeLogsToBreak.get(existingTree.get());

			if (logsToBreak.isEmpty() && !existingTree.get().isBlocksTraversed()) {
				logsToBreak.add(existingTree.get().traverse());

				if (existingTree.get().getDiscoveredBlocks().stream().noneMatch(p ->
						Utils.isLogBlock(world.getBlockState(p)))) {
					treesBreaked.add(existingTree.get());
					treeBreakers.put(existingTree.get(), player);

					state.treeLogsBreaked.put(existingTree.get().getStartPos(), pos);
					state.treeBreakers.put(existingTree.get().getStartPos(), player.getUuid());
					state.markDirty();

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

				Optional<TreeCrawler> finalExistingTree = existingTree;
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

					state.treeLogsBreaked.putAll(existingTree.get().getStartPos(), anotherBreakedLogs);

					logToBreak = logsToBreak.poll();

					treesToMerge.forEach(treeCrawler -> {
						treeCrawlers.remove(treeCrawler);
						treeLogsToBreak.remove(treeCrawler);
						treeLogsBreaked.remove(treeCrawler);
						treeBreakers.remove(treeCrawler);

						state.treePositions.remove(treeCrawler.getStartPos());
						state.treeBreakers.remove(treeCrawler.getStartPos());
						state.treeLogsBreaked.removeAll(treeCrawler.getStartPos());
					});

					state.markDirty();
				}

				if (logToBreak == null)
					return true;

				breakedLogs.add(logToBreak);
				treeBreakers.put(existingTree.get(), player);

				state.treeLogsBreaked.put(existingTree.get().getStartPos(), logToBreak);
				state.treeBreakers.put(existingTree.get().getStartPos(), player.getUuid());
				state.markDirty();

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
		var state = (TreesState) world.getServer().getWorld(world.getRegistryKey())
				.getPersistentStateManager().get(TreesState::fromNbt, MOD_ID);

		treeCrawlers.forEach(treeCrawler -> {
			if (!treeCrawler.isBlocksTraversed()) {
				var traversedLog = treeCrawler.traverse();

				while (treeLogsBreaked.get(treeCrawler).contains(traversedLog)) {
					traversedLog = treeCrawler.traverse();
				}

				if (traversedLog != null)
					treeLogsToBreak.get(treeCrawler).add(traversedLog);
				else
					treesBreaked.add(treeCrawler);
			}
		});

		treeCrawlers.stream().filter(TreeCrawler::isBlocksTraversed).forEach(treeCrawler -> {
			var player = treeBreakers.get(treeCrawler);

			var isAllLogsBreaked = treeLogsBreaked.get(treeCrawler)
					.containsAll(treeCrawler.getTraversedBlocks());
			var isPlayerInCreative = player != null && player.isCreative();

			if (isAllLogsBreaked || isPlayerInCreative) {
				var attachedBlocks = treeCrawler.getDiscoveredBlocks().stream().filter(pos ->
						Utils.isBeeBlock(world.getBlockState(pos))).collect(Collectors.toSet());

				var breaker = treeBreakers.get(treeCrawler);

				var blocksToBreak = treeCrawler.getTraversedBlocks();

				blocksToBreak.removeIf(pos -> !Utils.isLogBlock(world.getBlockState(pos)));
				blocksToBreak.addAll(attachedBlocks);

				var shouldBlocksDrop = !breaker.isCreative();

				blocksToBreak.forEach(log -> world.breakBlock(log, shouldBlocksDrop, breaker));

				treesBreaked.add(treeCrawler);
			}
		});

		treesBreaked.forEach(treeCrawler -> {
			treeCrawlers.remove(treeCrawler);
			treeLogsToBreak.remove(treeCrawler);
			treeLogsBreaked.remove(treeCrawler);
			treeBreakers.remove(treeCrawler);

			state.treePositions.remove(treeCrawler.getStartPos());
			state.treeLogsBreaked.removeAll(treeCrawler.getStartPos());
			state.treeBreakers.remove(treeCrawler.getStartPos());
			state.markDirty();
		});

		treesBreaked.clear();
	}
}
