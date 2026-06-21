package com.example.alieninvasion.item;

import com.example.alieninvasion.AlienInvasionMod;
import com.example.alieninvasion.registry.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class EmeradiumArmorMaterial {
    public static final int BASE_DURABILITY = 43;

    public static final Holder<ArmorMaterial> EMERADIUM = Holder.direct(new ArmorMaterial(
            createDefenseMap(4, 9, 8, 4),
            15,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(ItemRegistry.EMERADIUM_INGOT),
            List.of(new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath(AlienInvasionMod.MODID, "emeradium"))),
            3.5F,
            0.05F
    ));

    private static Map<ArmorItem.Type, Integer> createDefenseMap(int helmet, int chest, int legs, int boots) {
        EnumMap<ArmorItem.Type, Integer> m = new EnumMap<>(ArmorItem.Type.class);
        m.put(ArmorItem.Type.HELMET, helmet);
        m.put(ArmorItem.Type.CHESTPLATE, chest);
        m.put(ArmorItem.Type.LEGGINGS, legs);
        m.put(ArmorItem.Type.BOOTS, boots);
        return m;
    }

    private EmeradiumArmorMaterial() {
    }
}
