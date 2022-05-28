package me.qualterz.minecraft.chopdownthattree.utils;

import lombok.experimental.UtilityClass;

import net.minecraft.item.ItemStack;

@UtilityClass
public class ItemUtil {
    public static boolean isAxe(ItemStack item) {
        return item.getName().getString().endsWith("Axe");
    }
}
