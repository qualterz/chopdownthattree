package me.qualterz.minecraft.chopdownthattree;

import com.google.common.collect.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.*;

public class TreesState extends PersistentState {
    public final List<BlockPos> treePositions = new LinkedList<>();
    public final HashMap<BlockPos, UUID> treeBreakers = new LinkedHashMap<>();
    public final SetMultimap<BlockPos, BlockPos> treeLogsBreaked = LinkedHashMultimap.create();

    public static PersistentState fromNbt(NbtCompound nbt) {
        var state = new TreesState();

        var trees = nbt.getList("Trees", NbtCompound.COMPOUND_TYPE);

        for (NbtElement tree : trees) {
            var startPos = ((NbtCompound) tree).getLong("StartPos");
            var breaker = ((NbtCompound) tree).getUuid("Breaker");
            var breakedLogs = ((NbtCompound) tree).getList("BreakedLogs", NbtLong.LONG_TYPE);

            var startBlockPos = BlockPos.fromLong(startPos);

            state.treePositions.add(startBlockPos);
            state.treeBreakers.put(startBlockPos, breaker);
            state.treeLogsBreaked.putAll(startBlockPos, breakedLogs.stream().map(e ->
                    BlockPos.fromLong(((NbtLong) e).longValue())).toList());
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var trees = new NbtList();

        for (BlockPos startPos : treePositions) {
            var tree = new NbtCompound();

            tree.putLong("StartPos", startPos.asLong());
            tree.putUuid("Breaker", treeBreakers.get(startPos));

            var logsBreaked = new NbtList();

            logsBreaked.addAll(treeLogsBreaked.get(startPos).stream()
                    .map(pos -> NbtLong.of(pos.asLong())).toList());

            tree.put("BreakedLogs", logsBreaked);

            trees.add(tree);
        }

        nbt.put("Trees", trees);

        return nbt;
    }
}
