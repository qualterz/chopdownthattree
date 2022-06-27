package me.qualterz.minecraft.chopdownthattree;

import java.util.*;

import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class TreeState extends PersistentState {
    public Set<BlockPos> choppedLogs = new LinkedHashSet<>();

    public static PersistentState fromNbt(NbtCompound nbt) {
        var state = new TreeState();
        {
            var choppedLogs = nbt.getList("ChoppedLogs", NbtByte.LONG_TYPE);

            state.choppedLogs.addAll(choppedLogs.stream().map(element ->
                    BlockPos.fromLong(((NbtLong) element).longValue())).toList());
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var choppedLogs = new NbtList();
        choppedLogs.addAll(this.choppedLogs.stream()
                .map(pos -> NbtLong.of(pos.asLong())).toList());

        nbt.put("ChoppedLogs", choppedLogs);
        return nbt;
    }

    public static TreeState getState(World world) {
        return (TreeState) world.getServer().getWorld(world.getRegistryKey())
                .getPersistentStateManager().getOrCreate(TreeState::fromNbt, TreeState::new, MainEntrypoint.MOD_ID);
    }
}
