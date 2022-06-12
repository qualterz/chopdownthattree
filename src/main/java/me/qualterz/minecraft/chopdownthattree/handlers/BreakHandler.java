package me.qualterz.minecraft.chopdownthattree.handlers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BreakHandler {
    protected final BlockPos breakPos;
    protected final World world;

    public abstract boolean handleBreak();

    public BreakHandler(BlockPos breakPos, World world) {
        this.breakPos = breakPos;
        this.world = world;
    }
}
