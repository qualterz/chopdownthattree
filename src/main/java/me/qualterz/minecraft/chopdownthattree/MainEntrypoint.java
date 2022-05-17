package me.qualterz.minecraft.chopdownthattree;

import lombok.extern.log4j.Log4j2;

import me.qualterz.minecraft.chopdownthattree.setups.TreeBreakHandlerSetup;
import me.qualterz.minecraft.chopdownthattree.utils.TreeUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.stream.Collectors;

import static me.qualterz.minecraft.chopdownthattree.utils.BlockUtil.*;
import static me.qualterz.minecraft.chopdownthattree.utils.EntityUtil.damageMainHandItem;
import static me.qualterz.minecraft.chopdownthattree.utils.TreeUtil.getTreeBranchParts;

// TODO: implement branch break feature

@Log4j2
public class MainEntrypoint implements ModInitializer {
	public static final String MOD_ID = "chopdownthattree";

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register(this::beforeBlockBreak);
	}

	public boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState block, BlockEntity blockEntity) {
		return TreeBreakHandlerSetup.initialize(pos, world).forPlayer(player).handleBreak();
	}
}
