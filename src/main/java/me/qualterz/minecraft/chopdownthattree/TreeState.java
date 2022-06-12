package me.qualterz.minecraft.chopdownthattree;

import java.util.*;

import com.google.common.collect.*;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class TreeState extends PersistentState {
    public Set<BlockPos> chopPositions = new LinkedHashSet<>();
    public SetMultimap<BlockPos, BlockPos> breakedLogs = LinkedHashMultimap.create();
    public HashMap<BlockPos, Integer> lastBreakedLogsCount = new LinkedHashMap<>();
    public HashMap<BlockPos, Integer> chopCount = new LinkedHashMap<>();

    public static PersistentState fromNbt(NbtCompound nbt) {
        var state = new TreeState();
        {
            var trees = nbt.getList("Trees", NbtCompound.COMPOUND_TYPE);
            for (NbtElement tree : trees) {
                var chopPos = BlockPos.fromLong(((NbtCompound) tree).getLong("ChopPos"));
                var breakedLogs = ((NbtCompound) tree).getList("BreakedLogs", NbtLong.LONG_TYPE);
                var lastBreakedLogsCount = ((NbtCompound) tree).getInt("LastBreakedLogsCount");
                var chopCount = ((NbtCompound) tree).getInt("ChopCount");

                state.chopPositions.add(chopPos);
                state.breakedLogs.putAll(chopPos, breakedLogs.stream().map(e ->
                        BlockPos.fromLong(((NbtLong) e).longValue())).toList());
                state.lastBreakedLogsCount.put(chopPos, lastBreakedLogsCount);
                state.chopCount.put(chopPos, chopCount);
            }
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var trees = new NbtList();
        {
            for (BlockPos chopPos : chopPositions) {
                var tree = new NbtCompound();
                var breakedLogs = new NbtList();

                breakedLogs.addAll(this.breakedLogs.get(chopPos).stream()
                        .map(pos -> NbtLong.of(pos.asLong())).toList());

                tree.putLong("ChopPos", chopPos.asLong());
                tree.put("BreakedLogs", breakedLogs);
                tree.putInt("LastBreakedLogsCount", lastBreakedLogsCount.get(chopPos));
                tree.putInt("ChopCount", chopCount.get(chopPos));

                trees.add(tree);
            }
        }

        nbt.put("Trees", trees);
        return nbt;
    }

    public static TreeState getState(World world) {
        return (TreeState) world.getServer().getWorld(world.getRegistryKey())
                .getPersistentStateManager().getOrCreate(TreeState::fromNbt, TreeState::new, MainEntrypoint.MOD_ID);
    }

    public void addBreakedLogs(BlockPos pos, Set<BlockPos> logs) {
        breakedLogs.putAll(pos, logs);
        lastBreakedLogsCount.put(pos, logs.size());
    }

    public void removeBreakedLogs(BlockPos pos, Set<BlockPos> logs) {
        breakedLogs.get(pos).removeAll(logs);
    }

    public void removeBreakedLogs(BlockPos pos) {
        breakedLogs.get(pos).clear();
    }

    public boolean isTreeExists(BlockPos pos) {
        return chopPositions.contains(pos);
    }

    public void addTreeIfNotExists(BlockPos pos) {
        if (!isTreeExists(pos))
            addTree(pos);
    }

    public void addTree(BlockPos pos) {
        chopPositions.add(pos);
        lastBreakedLogsCount.put(pos, 0);
        chopCount.put(pos, 0);
        markDirty();
    }

    public void removeTree(BlockPos pos) {
        breakedLogs.removeAll(pos);
        chopPositions.remove(pos);
        lastBreakedLogsCount.remove(pos);
        chopCount.remove(pos);
        markDirty();
    }
}
