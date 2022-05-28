package me.qualterz.minecraft.chopdownthattree.setups;

import net.minecraft.entity.player.PlayerEntity;

import me.qualterz.minecraft.chopdownthattree.helpers.TreeBreaker;

public class TreeBreakerSetup {
    private final TreeBreaker treeBreaker;

    private TreeBreakerSetup(TreeBreaker treeBreaker) {
        this.treeBreaker = treeBreaker;
    }

    public static TreeBreakerSetup initialize(TreeBreaker treeBreaker) {
        return new TreeBreakerSetup(treeBreaker);
    }

    public TreeBreaker forPlayer(PlayerEntity player) {
        return treeBreaker.breakerEntity(player).whole(player.isCreative() && player.isSneaking()).drop(!player.isCreative());
    }
}
