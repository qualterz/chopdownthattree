package me.qualterz.minecraft.chopdownthattree;

import java.util.*;

import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class TreeState extends PersistentState {
    public Set<BlockPos> breakedLogs = new LinkedHashSet<>();

    public static PersistentState fromNbt(NbtCompound nbt) {
        var state = new TreeState();
        {
            var breakedLogs = nbt.getList("BreakedLogs", NbtByte.LONG_TYPE);

            state.breakedLogs.addAll(breakedLogs.stream().map(element ->
                    BlockPos.fromLong(((NbtLong) element).longValue())).toList());
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var breakedLogs = new NbtList();
        breakedLogs.addAll(this.breakedLogs.stream()
                .map(pos -> NbtLong.of(pos.asLong())).toList());

        nbt.put("BreakedLogs", breakedLogs);
        return nbt;
    }

    public static TreeState getState(World world) {
        return (TreeState) world.getServer().getWorld(world.getRegistryKey())
                .getPersistentStateManager().getOrCreate(TreeState::fromNbt, TreeState::new, MainEntrypoint.MOD_ID);
    }
}
