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

/**
 * Custom armor material for the late-game Cosmic Armor set (1.21.1 API: a record
 * wrapped in a direct Holder; no registry entry required). Strong, alien-forged
 * plate repaired with Cosmic Ingots. The full-set ability (walk safely over alien
 * blocks) is implemented in ModEvents, not here.
 */
public final class CosmicArmorMaterial {

    // Fed into ArmorItem.Type.getDurability(int) per piece (iron=15, netherite=37).
    public static final int BASE_DURABILITY = 40;

    public static final Holder<ArmorMaterial> COSMIC = Holder.direct(new ArmorMaterial(
            // 1) defense points per piece
            createDefenseMap(3, 9, 7, 3),
            // 2) enchantmentValue
            12,
            // 3) equipSound (already a Holder<SoundEvent> in 1.21.1)
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            // 4) repairIngredient (Supplier<Ingredient>, resolved lazily)
            () -> Ingredient.of(ItemRegistry.COSMIC_INGOT),
            // 5) layers -> assets/alien-invasion/textures/models/armor/cosmic_layer_1.png (+ _2)
            List.of(new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath(AlienInvasionMod.MODID, "cosmic"))),
            // 6) toughness
            3.5F,
            // 7) knockbackResistance
            0.1F
    ));

    private static Map<ArmorItem.Type, Integer> createDefenseMap(int helmet, int chest, int legs, int boots) {
        EnumMap<ArmorItem.Type, Integer> m = new EnumMap<>(ArmorItem.Type.class);
        m.put(ArmorItem.Type.HELMET, helmet);
        m.put(ArmorItem.Type.CHESTPLATE, chest);
        m.put(ArmorItem.Type.LEGGINGS, legs);
        m.put(ArmorItem.Type.BOOTS, boots);
        return m;
    }

    private CosmicArmorMaterial() {
    }
}
