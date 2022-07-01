package me.qualterz.minecraft.chopdownthattree.helpers;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

    public TreeBreaker(BlockPos breakPos, World world) {
        this.breakPos = breakPos;
        this.world = world;
    }

    public TreeBreaker(TreeParser parser) {
        this.breakPos = parser.blockPos();
        this.world = parser.world();
    }

    /**
     * This method is used to break the tree
     */
    public void breakTree() {
        var direction = whole ? GrowDirection.BOTH : GrowDirection.UPWARDS;
        var parser = TreeParser.setup().blockPos(breakPos).world(world).direction(direction).apply();
        var blocks = parser.blocks().isEmpty() ? parser.parse().blocks() : parser.blocks();

        blocks.forEach(block -> world.breakBlock(block, drop, breakerEntity));
    }
}
