package com.example.alieninvasion.item;

import com.example.alieninvasion.AlienInvasionMod;
import com.example.alieninvasion.registry.ItemRegistry;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum ModToolTiers implements Tier {
    PLATINUM(1561, 6.0F, 2.0F, 14),
    PALLADIUM(250,  8.0F, 3.0F, 10),
    NIBIRIUM(2031,  9.0F, 4.0F, 15);

    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;

    ModToolTiers(int uses, float speed, float attackDamageBonus, int enchantmentValue) {
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
    }

    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return attackDamageBonus; }
    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return switch (this) {
            case PLATINUM -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            case PALLADIUM -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            case NIBIRIUM -> TagKey.create(
                    net.minecraft.core.registries.Registries.BLOCK,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(AlienInvasionMod.MODID, "incorrect_for_nibirium_tool"));
        };
    }
    @Override public int getEnchantmentValue() { return enchantmentValue; }

    @Override
    public Ingredient getRepairIngredient() {
        return switch (this) {
            case PLATINUM  -> Ingredient.of(ItemRegistry.PLATINUM_INGOT);
            case PALLADIUM -> Ingredient.of(ItemRegistry.PALLADIUM_INGOT);
            case NIBIRIUM  -> Ingredient.of(ItemRegistry.NIBIRIUM_INGOT);
        };
    }
}
