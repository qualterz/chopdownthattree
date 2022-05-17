package me.qualterz.minecraft.chopdownthattree.handlers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BreakHandler {
    protected final BlockPos pos;
    protected final World world;

    public abstract boolean handleBreak();

    public BreakHandler(BlockPos pos, World world) {
        this.pos = pos;
        this.world = world;
    }
}
