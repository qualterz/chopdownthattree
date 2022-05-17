package me.qualterz.minecraft.chopdownthattree.helpers;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.qualterz.minecraft.chopdownthattree.utils.TreeUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.qualterz.minecraft.chopdownthattree.utils.BlockUtil.blockAt;
import static me.qualterz.minecraft.chopdownthattree.utils.TreeUtil.*;

/**
 * A class that allows to parse tree structures
 */
@Accessors(fluent = true)
@Builder(builderMethodName = "setup", buildMethodName = "apply", builderClassName = "Config")
public class TreeParser {
    /**
     * Position of the tree branch or trunk
     */
    @Getter
    private BlockPos pos;

    /**
     * World where the tree is located
     */
    @Getter
    private World world;

    /**
     * Parse direction
     */
    @Getter @Builder.Default
    private TreeUtil.GrowDirection direction = TreeUtil.GrowDirection.BOTH;

    @Getter private final Set<BlockPos> branchBlocks = new LinkedHashSet<>();
    @Getter private final SetMultimap<BlockPos, BlockPos> branchAttachmentBlocks = LinkedHashMultimap.create();

    public Set<BlockPos> attachmentBlocks() {
        return new HashSet<>(branchAttachmentBlocks.values());
    }

    public Set<BlockPos> blocks() {
        return Stream.concat(branchBlocks.stream(), branchAttachmentBlocks.values().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * This method is used to parse the tree
     */
    public TreeParser parse() {
        Set<BlockPos> processedBranchBlocks = new HashSet<>();

        if (isTreeBranchBlock(blockAt(pos, world)))
            processedBranchBlocks.add(pos);

        while (processedBranchBlocks.size() > 0) {
            processedBranchBlocks = processedBranchBlocks.stream()
                    // Tree branch parts
                    .flatMap(pos -> getTreeBranchParts(pos, world, direction).stream())

                    // Do not process tree branch parts infinitely
                    .filter(block -> !branchBlocks.contains(block))

                    .collect(Collectors.toUnmodifiableSet());

            branchBlocks.addAll(processedBranchBlocks);
        }

        branchBlocks.forEach(block -> {
            var attachments = getTreeBranchPartAttachments(block, world);

            if (!attachments.isEmpty())
                branchAttachmentBlocks.putAll(block, attachments);
        });

        return this;
    }
}
