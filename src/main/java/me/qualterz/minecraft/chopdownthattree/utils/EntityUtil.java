package me.qualterz.minecraft.chopdownthattree.utils;

import lombok.experimental.UtilityClass;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@UtilityClass
public class EntityUtil {
    public static void damageMainHandItem(LivingEntity entity, int amount) {
        var item = entity.getMainHandStack();

        item.damage(amount, entity, e ->
                entity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
    }

    public static void damageMainHandItem(LivingEntity entity) {
        damageMainHandItem(entity, 1);
    }
    
    public static ItemStack getMainHandItem(LivingEntity entity) {
        return entity.getMainHandStack();
    }

    public static boolean usesAxe(LivingEntity entity) {
        return ItemUtil.isAxe(getMainHandItem(entity));
    }
}
