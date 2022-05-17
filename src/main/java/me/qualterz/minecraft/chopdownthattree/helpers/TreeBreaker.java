package me.qualterz.minecraft.chopdownthattree.helpers;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private BlockPos pos;

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
        if (!isTreeBranchBlock(blockAt(pos, world)))
            return;

        var direction = whole ? GrowDirection.BOTH : GrowDirection.UPWARDS;

        TreeParser.setup().world(world).pos(pos).direction(direction).apply().parse().blocks()
                .forEach(block -> world.breakBlock(block, drop, breakerEntity));
    }

    public TreeBreaker(BlockPos pos, World world) {
        this.pos = pos;
        this.world = world;
    }
}
