package me.qualterz.minecraft.chopdownthattree.helpers;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

    @Getter
    private TreeParser parser;

    public TreeBreaker(BlockPos pos, World world) {
        this.breakPos = pos;
        this.world = world;
        this.parser = TreeParser.setup()
                .blockPos(pos).world(world).apply();
    }

    public TreeBreaker(TreeParser parser) {
        this.breakPos = parser.blockPos();
        this.world = parser.world();
        this.parser = parser;
    }

    /**
     * This method is used to break the tree
     */
    public void breakTree() {
        var blocksToBreak = parser.parse().blocks().stream();
        if (!whole) blocksToBreak = blocksToBreak.filter(blockPos -> blockPos.getY() >= breakPos.getY());
        blocksToBreak.forEach(block -> world.breakBlock(block, drop, breakerEntity));
    }
}
