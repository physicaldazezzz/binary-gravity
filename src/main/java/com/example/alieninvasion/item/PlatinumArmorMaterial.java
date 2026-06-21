package com.example.alieninvasion.item;

import com.example.alieninvasion.AlienInvasionMod;
import com.example.alieninvasion.registry.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

public class PlatinumArmorMaterial {
    public static final int BASE_DURABILITY = 33; // slightly above iron (15)

    public static final Holder<ArmorMaterial> PLATINUM = Holder.direct(new ArmorMaterial(
            Map.of(
                    net.minecraft.world.item.ArmorItem.Type.HELMET,     3,
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, 8,
                    net.minecraft.world.item.ArmorItem.Type.LEGGINGS,   6,
                    net.minecraft.world.item.ArmorItem.Type.BOOTS,      3
            ),
            14,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(ItemRegistry.PLATINUM_INGOT),
            List.of(new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath(AlienInvasionMod.MODID, "platinum")
            )),
            0.0F,
            0.0F
    ));
}
