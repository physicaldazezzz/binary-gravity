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

public class AlienHazmatArmorMaterial {
    public static final int BASE_DURABILITY = 5; // leather-level durability

    public static final Holder<ArmorMaterial> ALIEN_HAZMAT = Holder.direct(new ArmorMaterial(
            Map.of(
                    net.minecraft.world.item.ArmorItem.Type.HELMET,     1,
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, 3,
                    net.minecraft.world.item.ArmorItem.Type.LEGGINGS,   2,
                    net.minecraft.world.item.ArmorItem.Type.BOOTS,      1
            ),
            5,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(ItemRegistry.ALIEN_SKIN),
            List.of(new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath(AlienInvasionMod.MODID, "alien_hazmat")
            )),
            3.0F,
            0.0F
    ));
}
