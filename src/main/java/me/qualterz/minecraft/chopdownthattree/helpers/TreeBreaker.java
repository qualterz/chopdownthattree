package me.qualterz.minecraft.chopdownthattree.helpers;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.qualterz.minecraft.chopdownthattree.utils.BlockUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.TreeUtil.*;

/**
 * A class that allows to break tree structures
 */
@Accessors(fluent = true)
public class TreeBreaker {
    /**
     * Position of the tree branch or trunk
     */
    @Setter @Getter
    private BlockPos breakPos;

    /**
     * World where the tree is located
     */
    @Setter @Getter
    private World world;

    /**
     * Tree breaker entity
     */
    @Setter @Getter
    private Entity breakerEntity = null;

    /**
     * Break the whole tree or just the top part
     */
    @Setter @Getter
    private boolean whole = true;

    /**
     * Drop tree blocks as items after break
     */
    @Setter @Getter
    private boolean drop = false;

    /**
     * This method is used to break the tree
     */
    public void breakTree() {
        if (!isTreeBranchBlock(blockAt(breakPos, world)))
            return;

        var blocks = TreeParser.setup()
                .world(world).pos(breakPos).direction(GrowDirection.BOTH)
                .apply().parse().blocks();

        var blocksToBreak = blocks.stream();
        if (!whole) blocksToBreak = blocks.stream().filter(blockPos -> blockPos.getY() >= breakPos.getY());

        blocksToBreak.forEach(block -> world.breakBlock(block, drop, breakerEntity));
    }

    public TreeBreaker(BlockPos pos, World world) {
        this.breakPos = pos;
        this.world = world;
    }
}
