package me.qualterz.minecraft.chopdownthattree.handlers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DummyBreakHandler extends BreakHandler {
    public DummyBreakHandler(BlockPos pos, World world) {
        super(pos, world);
    }

    @Override
    public boolean handleBreak() {
        return true;
    }
}
