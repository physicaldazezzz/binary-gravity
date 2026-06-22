package com.example.alieninvasion.registry;

import com.example.alieninvasion.AlienInvasionMod;
import com.example.alieninvasion.item.InfestedFleshItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import com.example.alieninvasion.item.ModToolTiers;
import com.example.alieninvasion.item.AlienBlasterItem;
import com.example.alieninvasion.item.BlasterIIItem;
import com.example.alieninvasion.item.GravityBootsItem;
import com.example.alieninvasion.item.InvasionTrackerItem;
import com.example.alieninvasion.item.PalladiumSwordItem;
import com.example.alieninvasion.item.PlatinumSwordItem;
import com.example.alieninvasion.item.NibiriumSwordItem;
import com.example.alieninvasion.item.EmeradiumArmorMaterial;
import com.example.alieninvasion.item.GreenRayBlasterItem;
import com.example.alieninvasion.item.AlienHazmatArmorMaterial;
import com.example.alieninvasion.item.AlienChemArmorMaterial;
import com.example.alieninvasion.item.PlatinumArmorMaterial;
import com.example.alieninvasion.item.PalladiumArmorMaterial;
import com.example.alieninvasion.item.PiercerRapierItem;
import com.example.alieninvasion.item.DevastatorAxeItem;
import com.example.alieninvasion.item.ReaperScytheItem;

public class ItemRegistry {

    public static final Item ALIEN_GRUNT_SPAWN_EGG = registerItem("alien_grunt_spawn_egg",
            new SpawnEggItem(EntityRegistry.ALIEN_GRUNT, 0x00FF00, 0x000000, new Item.Properties()));

    public static final Item ALIEN_WORKER_SPAWN_EGG = registerItem("alien_worker_spawn_egg",
            new com.example.alieninvasion.item.CustomEntitySpawnerItem(() -> EntityRegistry.ALIEN_GRUNT,
                    new Item.Properties().rarity(Rarity.UNCOMMON), entity -> {
                if (entity instanceof com.example.alieninvasion.entity.AlienGruntEntity grunt) {
                    grunt.setScavenger(true);
                    grunt.setCustomName(net.minecraft.network.chat.Component.literal("§aПришелец-рабочий"));
                    grunt.setCustomNameVisible(true);
                }
            }));

    public static final Item TELEKINETIC_ALIEN_SPAWN_EGG = registerItem("telekinetic_alien_spawn_egg",
            new SpawnEggItem(EntityRegistry.TELEKINETIC_ALIEN, 0x800080, 0x000000, new Item.Properties()));

    public static final Item UFO_SPAWN_EGG = registerItem("ufo_spawn_egg",
            new SpawnEggItem(EntityRegistry.UFO, 0x808080, 0xFF0000, new Item.Properties()));

    public static final Item ALIEN_BRUTE_SPAWN_EGG = registerItem("alien_brute_spawn_egg",
            new SpawnEggItem(EntityRegistry.ALIEN_BRUTE, 0x555555, 0xFF0000, new Item.Properties()));

    public static final Item ALIEN_CHICKEN_SPAWN_EGG = registerItem("alien_chicken_spawn_egg",
            new SpawnEggItem(EntityRegistry.ALIEN_CHICKEN, 0x3FA000, 0xCC2020, new Item.Properties()));

    public static final Item HIVE_TYRANT_SPAWN_EGG = registerItem("hive_tyrant_spawn_egg",
            new SpawnEggItem(EntityRegistry.HIVE_TYRANT, 0x2B3340, 0x21D8C8, new Item.Properties()));

    public static final Item ALIEN_TROLL_SPAWN_EGG = registerItem("alien_troll_spawn_egg",
            new SpawnEggItem(EntityRegistry.ALIEN_TROLL, 0x4CAF20, 0x103000, new Item.Properties()));

    public static final Item INFESTED_PLAYER_CLONE_SPAWN_EGG = registerItem("infested_player_clone_spawn_egg",
            new SpawnEggItem(EntityRegistry.INFESTED_PLAYER_CLONE, 0x3FA000, 0x00FF00, new Item.Properties()));

    public static final Item INFESTED_CREEPER_SPAWN_EGG = registerItem("infested_creeper_spawn_egg",
            new SpawnEggItem(EntityRegistry.INFESTED_CREEPER, 0x0DA70D, 0x5D8A00, new Item.Properties()));

    public static final Item INFESTED_SKELETON_SPAWN_EGG = registerItem("infested_skeleton_spawn_egg",
            new SpawnEggItem(EntityRegistry.INFESTED_SKELETON, 0xC1C1C1, 0x5D8A00, new Item.Properties()));

    public static final Item INFESTED_ZOMBIE_SPAWN_EGG = registerItem("infested_zombie_spawn_egg",
            new SpawnEggItem(EntityRegistry.INFESTED_ZOMBIE, 0x00A0A0, 0x5D8A00, new Item.Properties()));

    public static final Item SWARM_MOTHER_SPAWN_EGG = registerItem("swarm_mother_spawn_egg",
            new SpawnEggItem(EntityRegistry.SWARM_MOTHER, 0x8A008A, 0x000000, new Item.Properties().rarity(Rarity.EPIC)));

    public static final Item HUNTER_SPAWN_EGG = registerItem("hunter_spawn_egg",
            new SpawnEggItem(EntityRegistry.HUNTER, 0x1A1A1A, 0xCC2222, new Item.Properties().rarity(Rarity.EPIC)));

    public static final Item HUNTER_TOKEN = registerItem("hunter_token",
            new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant().stacksTo(1)));

    public static final Item HIVE_CORE = registerItem("hive_core",
            new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final Item INFESTED_FLESH = registerItem("infested_flesh",
            new InfestedFleshItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(4).saturationModifier(0.1F).alwaysEdible().build())));

    public static final Item COSMIC_SHARD = registerItem("cosmic_shard",
            new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item COSMIC_INGOT = registerItem("cosmic_ingot",
            new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item ASTRAL_PRISM_INGOT = registerItem("astral_prism_ingot",
            new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final Item BIO_SERUM = registerItem("bio_serum",
            new com.example.alieninvasion.item.BioSerumItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));

    public static final Item WEAK_ANTIDOTE = registerItem("weak_antidote",
            new com.example.alieninvasion.item.AntidoteItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));

    public static final Item HERBAL_BREW = registerItem("herbal_brew",
            new com.example.alieninvasion.item.HerbalBrewItem(new Item.Properties().stacksTo(16)));

    public static final Item COSMIC_STIMULANT = registerItem("cosmic_stimulant",
            new com.example.alieninvasion.item.CosmicStimulantItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(16)));

    public static final Item ALIEN_BATTERY = registerItem("alien_battery",
            new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item GRAVITY_GRENADE = registerItem("gravity_grenade",
            new com.example.alieninvasion.item.GravityGrenadeItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(16)));

    // Cosmic Armor: full set grants Infection immunity and lets you walk over alien blocks.
    public static final Item COSMIC_HELMET = registerItem("cosmic_helmet",
            new net.minecraft.world.item.ArmorItem(com.example.alieninvasion.item.CosmicArmorMaterial.COSMIC,
                    net.minecraft.world.item.ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.RARE)
                    .durability(net.minecraft.world.item.ArmorItem.Type.HELMET.getDurability(com.example.alieninvasion.item.CosmicArmorMaterial.BASE_DURABILITY))));

    public static final Item COSMIC_CHESTPLATE = registerItem("cosmic_chestplate",
            new net.minecraft.world.item.ArmorItem(com.example.alieninvasion.item.CosmicArmorMaterial.COSMIC,
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.RARE)
                    .durability(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(com.example.alieninvasion.item.CosmicArmorMaterial.BASE_DURABILITY))));

    public static final Item COSMIC_LEGGINGS = registerItem("cosmic_leggings",
            new net.minecraft.world.item.ArmorItem(com.example.alieninvasion.item.CosmicArmorMaterial.COSMIC,
                    net.minecraft.world.item.ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.RARE)
                    .durability(net.minecraft.world.item.ArmorItem.Type.LEGGINGS.getDurability(com.example.alieninvasion.item.CosmicArmorMaterial.BASE_DURABILITY))));

    public static final Item COSMIC_BOOTS = registerItem("cosmic_boots",
            new net.minecraft.world.item.ArmorItem(com.example.alieninvasion.item.CosmicArmorMaterial.COSMIC,
                    net.minecraft.world.item.ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.RARE)
                    .durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(com.example.alieninvasion.item.CosmicArmorMaterial.BASE_DURABILITY))));

    public static final Item GRAVITY_GUN = registerItem("gravity_gun",
            new com.example.alieninvasion.item.GravityGunItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

    public static final Item PURIFIER_WAND = registerItem("purifier_wand",
            new com.example.alieninvasion.item.PurifierItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(250)));

    public static final Item ALIEN_BLASTER = registerItem("alien_blaster",
            new AlienBlasterItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));
    public static final Item BLASTER_II = registerItem("blaster_ii",
            new BlasterIIItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));
    public static final Item GREEN_RAY_BLASTER = registerItem("green_ray_blaster",
            new GreenRayBlasterItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));
    public static final Item EMERADIUM_SHIELD = registerItem("emeradium_shield",
            new net.minecraft.world.item.ShieldItem(new Item.Properties().rarity(Rarity.RARE).durability(1000)));

    public static final Item PLASMA_BOLT_ITEM = registerItem("plasma_bolt",
            new Item(new Item.Properties()));

    public static final Item PLASMA_CELL = registerItem("plasma_cell",
            new Item(new Item.Properties()));

    public static final Item GRAVITY_BOOTS = registerItem("gravity_boots",
            new GravityBootsItem(com.example.alieninvasion.item.CosmicArmorMaterial.COSMIC,
                    new Item.Properties().rarity(Rarity.RARE)
                    .durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(com.example.alieninvasion.item.CosmicArmorMaterial.BASE_DURABILITY))));

    public static final Item INVASION_TRACKER = registerItem("invasion_tracker",
            new InvasionTrackerItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));

    public static final Item ALIEN_BREACHER_SPAWN_EGG = registerItem("alien_breacher_spawn_egg",
            new SpawnEggItem(EntityRegistry.ALIEN_BREACHER, 0x473B25, 0xFF0000, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item DRILL_SPAWN_EGG = registerItem("drill_spawn_egg",
            new com.example.alieninvasion.item.CustomEntitySpawnerItem(() -> EntityRegistry.DRILL, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item METEOR_SPAWN_EGG = registerItem("meteor_spawn_egg",
            new com.example.alieninvasion.item.CustomEntitySpawnerItem(() -> EntityRegistry.METEOR, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item PARASITE_ITEM = registerItem("parasite_item",
            new com.example.alieninvasion.item.ParasiteItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

    public static final Item INFESTED_WORM_SPAWN_EGG = registerItem("infested_worm_spawn_egg",
            new SpawnEggItem(EntityRegistry.INFESTED_WORM, 0xB04467, 0x4E0E2C, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item ALIEN_RAPTOR_SPAWN_EGG = registerItem("alien_raptor_spawn_egg",
            new SpawnEggItem(EntityRegistry.ALIEN_RAPTOR, 0x69784F, 0x2C381F, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item PARASITE_SPAWN_EGG = registerItem("parasite_spawn_egg",
            new SpawnEggItem(EntityRegistry.PARASITE, 0x5D8A00, 0x8A008A, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item ALIEN_STALKER_SPAWN_EGG = registerItem("alien_stalker_spawn_egg",
            new SpawnEggItem(EntityRegistry.ALIEN_STALKER, 0x1A2230, 0x55FF99, new Item.Properties().rarity(Rarity.RARE)));

    public static final Item PLASMA_CASTER_SPAWN_EGG = registerItem("plasma_caster_spawn_egg",
            new SpawnEggItem(EntityRegistry.PLASMA_CASTER, 0x223A2A, 0xFF6622, new Item.Properties().rarity(Rarity.RARE)));

    public static final Item HIVE_SHAMAN_SPAWN_EGG = registerItem("hive_shaman_spawn_egg",
            new SpawnEggItem(EntityRegistry.HIVE_SHAMAN, 0x3A2A4A, 0xBB55FF, new Item.Properties().rarity(Rarity.RARE)));

    public static final Item SKY_DRONE_SPAWN_EGG = registerItem("sky_drone_spawn_egg",
            new SpawnEggItem(EntityRegistry.SKY_DRONE, 0x224455, 0x66FFCC, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item CAVE_LURKER_SPAWN_EGG = registerItem("cave_lurker_spawn_egg",
            new SpawnEggItem(EntityRegistry.CAVE_LURKER, 0x1A2418, 0x88CC55, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item ACID_SPITTER_SPAWN_EGG = registerItem("acid_spitter_spawn_egg",
            new SpawnEggItem(EntityRegistry.ACID_SPITTER, 0x3A4A22, 0xCCFF44, new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Item ROGUE_SCAVENGER_SPAWN_EGG = registerItem("rogue_scavenger_spawn_egg",
            new SpawnEggItem(EntityRegistry.ROGUE_SCAVENGER, 0x6A5A44, 0x4A6E4A, new Item.Properties().rarity(Rarity.RARE)));

    public static final Item BLINK_CORE = registerItem("blink_core",
            new com.example.alieninvasion.item.BlinkCoreItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));

    public static final Item COMMS_BEACON = registerItem("comms_beacon",
            new com.example.alieninvasion.item.CommsBeaconItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));

    public static final Item EMP_GRENADE = registerItem("emp_grenade",
            new com.example.alieninvasion.item.EmpGrenadeItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(16)));

    public static final Item ASTRAL_PRISM_GUN = registerItem("astral_prism_gun",
            new com.example.alieninvasion.item.AstralPrismGunItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant().stacksTo(1)));

    public static final Item ASTRAL_RESONANCE_GRENADE = registerItem("astral_resonance_grenade",
            new com.example.alieninvasion.item.AstralResonanceGrenadeItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant().stacksTo(16)));

    public static final Item HERBAL_SALVE = registerItem("herbal_salve",
            new com.example.alieninvasion.item.HerbalSalveItem(new Item.Properties().rarity(Rarity.COMMON).stacksTo(64)));

    public static final Item DARK_MATTER_SHARD = registerItem("dark_matter_shard",
            new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final Item RADIATION_CRYSTAL = registerItem("radiation_crystal",
            new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    // Nibirium economy: platinum/palladium chunks → raw ore → ingots → nibirium alloy
    public static final Item PLATINUM_CHUNK = registerItem("platinum_chunk", new Item(new Item.Properties()));
    public static final Item PALLADIUM_CHUNK = registerItem("palladium_chunk", new Item(new Item.Properties()));
    public static final Item RAW_PLATINUM = registerItem("raw_platinum", new Item(new Item.Properties()));
    public static final Item RAW_PALLADIUM = registerItem("raw_palladium", new Item(new Item.Properties()));
    public static final Item PLATINUM_INGOT = registerItem("platinum_ingot", new Item(new Item.Properties()));
    public static final Item PALLADIUM_INGOT = registerItem("palladium_ingot", new Item(new Item.Properties()));

    public static final Item NIBIRIUM_INGOT = registerItem("nibirium_ingot",
            new Item(new Item.Properties().rarity(Rarity.RARE).fireResistant()));
    public static final Item EMERADIUM_INGOT = registerItem("emeradium_ingot",
            new Item(new Item.Properties().rarity(Rarity.RARE).fireResistant()));
    public static final Item EMERADIUM_RESONATOR = registerItem("emeradium_resonator",
            new Item(new Item.Properties().rarity(Rarity.RARE).fireResistant().stacksTo(1)));
    public static final Item ALIEN_SKIN = registerItem("alien_skin", new Item(new Item.Properties()));

    // Platinum tools — iron speed, diamond durability
    public static final Item PLATINUM_SWORD = registerItem("platinum_sword",
            new PlatinumSwordItem(ModToolTiers.PLATINUM,
                    new Item.Properties().rarity(Rarity.UNCOMMON).attributes(SwordItem.createAttributes(ModToolTiers.PLATINUM, 3, -2.4F))));
    public static final Item PLATINUM_PICKAXE = registerItem("platinum_pickaxe",
            new PickaxeItem(ModToolTiers.PLATINUM,
                    new Item.Properties().rarity(Rarity.UNCOMMON).attributes(PickaxeItem.createAttributes(ModToolTiers.PLATINUM, 1.0F, -2.8F))));
    public static final Item PLATINUM_AXE = registerItem("platinum_axe",
            new AxeItem(ModToolTiers.PLATINUM,
                    new Item.Properties().rarity(Rarity.UNCOMMON).attributes(AxeItem.createAttributes(ModToolTiers.PLATINUM, 5.0F, -3.0F))));
    public static final Item PLATINUM_SHOVEL = registerItem("platinum_shovel",
            new ShovelItem(ModToolTiers.PLATINUM,
                    new Item.Properties().rarity(Rarity.UNCOMMON).attributes(ShovelItem.createAttributes(ModToolTiers.PLATINUM, 1.5F, -3.0F))));
    public static final Item PLATINUM_HOE = registerItem("platinum_hoe",
            new HoeItem(ModToolTiers.PLATINUM,
                    new Item.Properties().rarity(Rarity.UNCOMMON).attributes(HoeItem.createAttributes(ModToolTiers.PLATINUM, -2.0F, 0.0F))));

    // Palladium tools — diamond speed, iron durability
    public static final Item PALLADIUM_SWORD = registerItem("palladium_sword",
            new PalladiumSwordItem(ModToolTiers.PALLADIUM,
                    new Item.Properties().rarity(Rarity.UNCOMMON).attributes(SwordItem.createAttributes(ModToolTiers.PALLADIUM, 4, -2.35F))));
    public static final Item PALLADIUM_PICKAXE = registerItem("palladium_pickaxe",
            new PickaxeItem(ModToolTiers.PALLADIUM,
                    new Item.Properties().rarity(Rarity.UNCOMMON).attributes(PickaxeItem.createAttributes(ModToolTiers.PALLADIUM, 1.5F, -2.7F))));
    public static final Item PALLADIUM_AXE = registerItem("palladium_axe",
            new AxeItem(ModToolTiers.PALLADIUM,
                    new Item.Properties().rarity(Rarity.UNCOMMON).attributes(AxeItem.createAttributes(ModToolTiers.PALLADIUM, 5.0F, -3.0F))));
    public static final Item PALLADIUM_SHOVEL = registerItem("palladium_shovel",
            new ShovelItem(ModToolTiers.PALLADIUM,
                    new Item.Properties().rarity(Rarity.UNCOMMON).attributes(ShovelItem.createAttributes(ModToolTiers.PALLADIUM, 1.5F, -3.0F))));
    public static final Item PALLADIUM_HOE = registerItem("palladium_hoe",
            new HoeItem(ModToolTiers.PALLADIUM,
                    new Item.Properties().rarity(Rarity.UNCOMMON).attributes(HoeItem.createAttributes(ModToolTiers.PALLADIUM, -3.0F, 0.0F))));


    // Химзащита armor — early-game alien-skin suit (leather-level protection)
    public static final Item ALIEN_HAZMAT_HELMET = registerItem("alien_hazmat_helmet",
            new net.minecraft.world.item.ArmorItem(AlienHazmatArmorMaterial.ALIEN_HAZMAT,
                    net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().durability(net.minecraft.world.item.ArmorItem.Type.HELMET.getDurability(AlienHazmatArmorMaterial.BASE_DURABILITY))));
    public static final Item ALIEN_HAZMAT_CHESTPLATE = registerItem("alien_hazmat_chestplate",
            new net.minecraft.world.item.ArmorItem(AlienHazmatArmorMaterial.ALIEN_HAZMAT,
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(AlienHazmatArmorMaterial.BASE_DURABILITY))));
    public static final Item ALIEN_HAZMAT_LEGGINGS = registerItem("alien_hazmat_leggings",
            new net.minecraft.world.item.ArmorItem(AlienHazmatArmorMaterial.ALIEN_HAZMAT,
                    net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(net.minecraft.world.item.ArmorItem.Type.LEGGINGS.getDurability(AlienHazmatArmorMaterial.BASE_DURABILITY))));
    public static final Item ALIEN_HAZMAT_BOOTS = registerItem("alien_hazmat_boots",
            new net.minecraft.world.item.ArmorItem(AlienHazmatArmorMaterial.ALIEN_HAZMAT,
                    net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(AlienHazmatArmorMaterial.BASE_DURABILITY))));

    // Химдоспех armor — mid-game nibirium suit (iron-level protection), smithing upgrade from Химзащита
    public static final Item ALIEN_CHEM_HELMET = registerItem("alien_chem_helmet",
            new net.minecraft.world.item.ArmorItem(AlienChemArmorMaterial.ALIEN_CHEM,
                    net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.HELMET.getDurability(AlienChemArmorMaterial.BASE_DURABILITY))));
    public static final Item ALIEN_CHEM_CHESTPLATE = registerItem("alien_chem_chestplate",
            new net.minecraft.world.item.ArmorItem(AlienChemArmorMaterial.ALIEN_CHEM,
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(AlienChemArmorMaterial.BASE_DURABILITY))));
    public static final Item ALIEN_CHEM_LEGGINGS = registerItem("alien_chem_leggings",
            new net.minecraft.world.item.ArmorItem(AlienChemArmorMaterial.ALIEN_CHEM,
                    net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.LEGGINGS.getDurability(AlienChemArmorMaterial.BASE_DURABILITY))));
    public static final Item ALIEN_CHEM_BOOTS = registerItem("alien_chem_boots",
            new net.minecraft.world.item.ArmorItem(AlienChemArmorMaterial.ALIEN_CHEM,
                    net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(AlienChemArmorMaterial.BASE_DURABILITY))));

    // Nibirium tools — netherite-grade. Pickaxe & shovel break 3x3 (ModEvents).
    public static final Item NIBIRIUM_SWORD = registerItem("nibirium_sword",
            new NibiriumSwordItem(ModToolTiers.NIBIRIUM,
                    new Item.Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(ModToolTiers.NIBIRIUM, 5, -2.4F))));
    public static final Item NIBIRIUM_PICKAXE = registerItem("nibirium_pickaxe",
            new PickaxeItem(ModToolTiers.NIBIRIUM,
                    new Item.Properties().rarity(Rarity.RARE).attributes(PickaxeItem.createAttributes(ModToolTiers.NIBIRIUM, 1.5F, -2.8F))));
    public static final Item NIBIRIUM_AXE = registerItem("nibirium_axe",
            new AxeItem(ModToolTiers.NIBIRIUM,
                    new Item.Properties().rarity(Rarity.RARE).attributes(AxeItem.createAttributes(ModToolTiers.NIBIRIUM, 5.5F, -3.0F))));
    public static final Item NIBIRIUM_SHOVEL = registerItem("nibirium_shovel",
            new ShovelItem(ModToolTiers.NIBIRIUM,
                    new Item.Properties().rarity(Rarity.RARE).attributes(ShovelItem.createAttributes(ModToolTiers.NIBIRIUM, 1.5F, -3.0F))));
    public static final Item NIBIRIUM_HOE = registerItem("nibirium_hoe",
            new HoeItem(ModToolTiers.NIBIRIUM,
                    new Item.Properties().rarity(Rarity.RARE).attributes(HoeItem.createAttributes(ModToolTiers.NIBIRIUM, -3.0F, 0.0F))));

    // Platinum armor — diamond-level protection, set bonus: cap radiation at 70%
    public static final Item PLATINUM_HELMET = registerItem("platinum_helmet",
            new net.minecraft.world.item.ArmorItem(PlatinumArmorMaterial.PLATINUM,
                    net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.HELMET.getDurability(PlatinumArmorMaterial.BASE_DURABILITY))));
    public static final Item PLATINUM_CHESTPLATE = registerItem("platinum_chestplate",
            new net.minecraft.world.item.ArmorItem(PlatinumArmorMaterial.PLATINUM,
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(PlatinumArmorMaterial.BASE_DURABILITY))));
    public static final Item PLATINUM_LEGGINGS = registerItem("platinum_leggings",
            new net.minecraft.world.item.ArmorItem(PlatinumArmorMaterial.PLATINUM,
                    net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.LEGGINGS.getDurability(PlatinumArmorMaterial.BASE_DURABILITY))));
    public static final Item PLATINUM_BOOTS = registerItem("platinum_boots",
            new net.minecraft.world.item.ArmorItem(PlatinumArmorMaterial.PLATINUM,
                    net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(PlatinumArmorMaterial.BASE_DURABILITY))));

    // Emeradium armor — upgraded platinum armor
    public static final Item EMERADIUM_HELMET = registerItem("emeradium_helmet",
            new net.minecraft.world.item.ArmorItem(EmeradiumArmorMaterial.EMERADIUM,
                    net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.RARE).durability(net.minecraft.world.item.ArmorItem.Type.HELMET.getDurability(EmeradiumArmorMaterial.BASE_DURABILITY))));
    public static final Item EMERADIUM_CHESTPLATE = registerItem("emeradium_chestplate",
            new net.minecraft.world.item.ArmorItem(EmeradiumArmorMaterial.EMERADIUM,
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.RARE).durability(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(EmeradiumArmorMaterial.BASE_DURABILITY))));
    public static final Item EMERADIUM_LEGGINGS = registerItem("emeradium_leggings",
            new net.minecraft.world.item.ArmorItem(EmeradiumArmorMaterial.EMERADIUM,
                    net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.RARE).durability(net.minecraft.world.item.ArmorItem.Type.LEGGINGS.getDurability(EmeradiumArmorMaterial.BASE_DURABILITY))));
    public static final Item EMERADIUM_BOOTS = registerItem("emeradium_boots",
            new net.minecraft.world.item.ArmorItem(EmeradiumArmorMaterial.EMERADIUM,
                    net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(Rarity.RARE).durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(EmeradiumArmorMaterial.BASE_DURABILITY))));

    // Palladium armor — between iron and diamond, set bonus: cap radiation at 70%
    public static final Item PALLADIUM_HELMET = registerItem("palladium_helmet",
            new net.minecraft.world.item.ArmorItem(PalladiumArmorMaterial.PALLADIUM,
                    net.minecraft.world.item.ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.HELMET.getDurability(PalladiumArmorMaterial.BASE_DURABILITY))));
    public static final Item PALLADIUM_CHESTPLATE = registerItem("palladium_chestplate",
            new net.minecraft.world.item.ArmorItem(PalladiumArmorMaterial.PALLADIUM,
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(PalladiumArmorMaterial.BASE_DURABILITY))));
    public static final Item PALLADIUM_LEGGINGS = registerItem("palladium_leggings",
            new net.minecraft.world.item.ArmorItem(PalladiumArmorMaterial.PALLADIUM,
                    net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.LEGGINGS.getDurability(PalladiumArmorMaterial.BASE_DURABILITY))));
    public static final Item PALLADIUM_BOOTS = registerItem("palladium_boots",
            new net.minecraft.world.item.ArmorItem(PalladiumArmorMaterial.PALLADIUM,
                    net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(PalladiumArmorMaterial.BASE_DURABILITY))));

    // Astral Prism armor — upgraded palladium armor
    public static final Item ASTRAL_PRISM_HELMET = registerItem("astral_prism_helmet",
            new net.minecraft.world.item.ArmorItem(com.example.alieninvasion.item.AstralPrismArmorMaterial.ASTRAL_PRISM,
                    net.minecraft.world.item.ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                    .durability(net.minecraft.world.item.ArmorItem.Type.HELMET.getDurability(com.example.alieninvasion.item.AstralPrismArmorMaterial.BASE_DURABILITY))));
    public static final Item ASTRAL_PRISM_CHESTPLATE = registerItem("astral_prism_chestplate",
            new net.minecraft.world.item.ArmorItem(com.example.alieninvasion.item.AstralPrismArmorMaterial.ASTRAL_PRISM,
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                    .durability(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(com.example.alieninvasion.item.AstralPrismArmorMaterial.BASE_DURABILITY))));
    public static final Item ASTRAL_PRISM_LEGGINGS = registerItem("astral_prism_leggings",
            new net.minecraft.world.item.ArmorItem(com.example.alieninvasion.item.AstralPrismArmorMaterial.ASTRAL_PRISM,
                    net.minecraft.world.item.ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                    .durability(net.minecraft.world.item.ArmorItem.Type.LEGGINGS.getDurability(com.example.alieninvasion.item.AstralPrismArmorMaterial.BASE_DURABILITY))));
    public static final Item ASTRAL_PRISM_BOOTS = registerItem("astral_prism_boots",
            new net.minecraft.world.item.ArmorItem(com.example.alieninvasion.item.AstralPrismArmorMaterial.ASTRAL_PRISM,
                    net.minecraft.world.item.ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                    .durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(com.example.alieninvasion.item.AstralPrismArmorMaterial.BASE_DURABILITY))));

    public static final Item TOXIC_WATER_BUCKET = registerItem("toxic_water_bucket",
            new BucketItem(com.example.alieninvasion.registry.ModFluids.TOXIC_WATER_STILL,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final Item INFECTED_WATER_BUCKET = registerItem("infected_water_bucket",
            new BucketItem(com.example.alieninvasion.registry.ModFluids.INFECTED_WATER_STILL,
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final Item GEIGER_COUNTER = registerItem("geiger_counter",
            new com.example.alieninvasion.item.GeigerCounterItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1)));

    public static final Item RAD_PILLS = registerItem("rad_pills",
            new com.example.alieninvasion.item.RadPillItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));

    public static final Item INFECTION_PILLS = registerItem("infection_pills",
            new com.example.alieninvasion.item.InfectionPillItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(16)));

    // Тканевый респиратор — начальная маска. Фильтрует споры/радиацию (слабее), но
    // НЕ герметична: в ядовитом газе и кислотном дожде не спасает. Дёшев.
    public static final Item CLOTH_RESPIRATOR = registerItem("cloth_respirator",
            new com.example.alieninvasion.item.BioFilterMaskItem(
                    new Item.Properties().durability(128)));

    public static final Item BIO_FILTER_MASK = registerItem("bio_filter_mask",
            new com.example.alieninvasion.item.BioFilterMaskItem(
                    new Item.Properties().rarity(Rarity.UNCOMMON).durability(256)));

    // Боевой противогаз — высший тир. Герметичен (дышит в газе/кислоте), сильнейший
    // фильтр, в тёмных заражённых зонах даёт ночное зрение. Собирается из био-фильтра.
    public static final Item GAS_MASK = registerItem("gas_mask",
            new com.example.alieninvasion.item.BioFilterMaskItem(
                    new Item.Properties().rarity(Rarity.RARE).durability(512)));

    // Баллон воздуха: ПКМ заправляет запас воздуха герметичной маски (для ядовитых зон).
    public static final Item AIR_CANISTER = registerItem("air_canister",
            new com.example.alieninvasion.item.AirCanisterItem(new Item.Properties().stacksTo(16)));

    public static final Item PIERCER_RAPIER = registerItem("piercer_rapier",
            new PiercerRapierItem(ModToolTiers.EMERAD_RAD,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().attributes(SwordItem.createAttributes(ModToolTiers.EMERAD_RAD, 3, -2.2F))));

    public static final Item DEVASTATOR_AXE = registerItem("devastator_axe",
            new DevastatorAxeItem(ModToolTiers.EMERAD_RAD,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().attributes(AxeItem.createAttributes(ModToolTiers.EMERAD_RAD, 5.5F, -3.0F))));

    public static final Item REAPER_SCYTHE = registerItem("reaper_scythe",
            new ReaperScytheItem(ModToolTiers.EMERAD_RAD,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant().attributes(HoeItem.createAttributes(ModToolTiers.EMERAD_RAD, 4.0F, -2.8F))));

    public static final Item CONTAMINATED_FOOD = registerItem("contaminated_food",
            new com.example.alieninvasion.item.ContaminatedFoodItem(new Item.Properties().stacksTo(16)
                    .food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.1F).alwaysEdible().build())));

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(AlienInvasionMod.MODID, name), item);
    }

    public static void registerItems() {
        // Just triggering class loading
    }
}
