package com.example.alieninvasion.logic;

import com.example.alieninvasion.registry.ModBlocks;
import com.example.alieninvasion.registry.BloodyVariantRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public final class ContaminationRules {
    private ContaminationRules() {
    }

    private static final java.util.Set<net.minecraft.world.level.block.Block> CONCRETE = java.util.Set.of(
            Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE,
            Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE,
            Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE,
            Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE);

    private static final java.util.Set<net.minecraft.world.level.block.Block> CONCRETE_POWDER = java.util.Set.of(
            Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER,
            Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER,
            Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER,
            Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER,
            Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER,
            Blocks.BLACK_CONCRETE_POWDER);

    /** Small surface vegetation that withers when the ground under it rots. */
    public static boolean isSurfaceVegetation(BlockState state) {
        return state.is(BlockTags.FLOWERS) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.DEAD_BUSH)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(BlockTags.SAPLINGS);
    }

    public static boolean isProtectedBlock(BlockState state) {
        if (isOreOrValuable(state)) return true;
        // NOTE: the crafting table + village work stations (barrel, cartography/
        // smithing/fletching tables, loom, stonecutter, grindstone) are NOT protected
        // anymore - they rot into their infested twins (look like vanilla, but consumed
        // by the biomass), so a contaminated village turns cohesive. Storage/smelting/
        // utility tile-entities below stay protected so their contents aren't erased.
        return state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE)
                || state.is(Blocks.SMOKER) || state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST)
                || state.is(Blocks.ENDER_CHEST) || state.is(Blocks.ANVIL)
                || state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)
                || state.is(Blocks.ENCHANTING_TABLE) || state.is(Blocks.BREWING_STAND)
                || state.is(Blocks.COMPOSTER) || state.is(Blocks.BEACON)
                || state.is(Blocks.HOPPER) || state.is(Blocks.DISPENSER) || state.is(Blocks.DROPPER)
                || state.is(Blocks.CRAFTER);
    }

    public static boolean isOreOrValuable(BlockState state) {
        return state.is(BlockTags.COAL_ORES) || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.DIAMOND_ORES)
                || state.is(Blocks.COAL_BLOCK) || state.is(Blocks.COPPER_BLOCK) || state.is(Blocks.IRON_BLOCK)
                || state.is(Blocks.GOLD_BLOCK) || state.is(Blocks.REDSTONE_BLOCK) || state.is(Blocks.LAPIS_BLOCK)
                || state.is(Blocks.EMERALD_BLOCK) || state.is(Blocks.DIAMOND_BLOCK)
                || state.is(Blocks.NETHERITE_BLOCK) || state.is(Blocks.NETHER_QUARTZ_ORE)
                || state.is(Blocks.ANCIENT_DEBRIS);
    }

    public static boolean canContaminate(LevelAccessor level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.getBlock() instanceof LiquidBlock
                || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        // Tile-entity blocks are normally left alone, but the barrel is a work station
        // we DO consume (its infested twin is decorative, contents are forfeited).
        // Bloody variants have a tiny BE only to remember their original state, and
        // must still be allowed to become bloody+infested.
        if (level.getBlockEntity(pos) != null && !state.is(Blocks.BARREL)
                && BloodyVariantRegistry.infestedForBloody(state.getBlock()) == null) return false;
        if (isProtectedBlock(state)) return false;
        // Purifier-claimed chunks are reclaimed territory: no spread of any kind.
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && ChunkContaminationData.get(serverLevel).isProtectedChunk(new net.minecraft.world.level.ChunkPos(pos))) {
            return false;
        }
        return contaminatedStateFor(state) != null;
    }

    public static BlockState contaminatedStateFor(BlockState state) {
        if (state.isAir() || state.getBlock() instanceof LiquidBlock || isProtectedBlock(state)) {
            return null;
        }
        // Never classify an already contaminated block by its material again.
        // In particular, infested ice uses glass sounds and used to degrade into
        // infested glass when a later spread tick targeted it.
        if (isContaminated(state)) {
            return null;
        }
        BlockState bloodyInfested = bloodyInfestedStateFor(state);
        if (bloodyInfested != null) {
            return bloodyInfested;
        }
        if (state.hasBlockEntity() && !state.is(Blocks.BARREL)) {
            return null;
        }
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM) || state.is(Blocks.MOSS_BLOCK)
                || state.is(BlockTags.NYLIUM)) {
            return ModBlocks.INFESTED_GRASS.defaultBlockState();
        }
        if (state.is(BlockTags.DIRT) || state.is(Blocks.MUD) || state.is(Blocks.PACKED_MUD)
                || state.is(Blocks.MUD_BRICKS)) {
            return ModBlocks.INFESTED_DIRT.defaultBlockState();
        }
        if (state.is(Blocks.SANDSTONE) || state.is(Blocks.SMOOTH_SANDSTONE) || state.is(Blocks.CUT_SANDSTONE)
                || state.is(Blocks.CHISELED_SANDSTONE) || state.is(Blocks.RED_SANDSTONE)
                || state.is(Blocks.SMOOTH_RED_SANDSTONE) || state.is(Blocks.CUT_RED_SANDSTONE)) {
            return ModBlocks.INFESTED_SANDSTONE.defaultBlockState();
        }
        if (state.is(BlockTags.TERRACOTTA)
                || state.getBlock() instanceof net.minecraft.world.level.block.GlazedTerracottaBlock) {
            return ModBlocks.INFESTED_TERRACOTTA.defaultBlockState();
        }
        if (state.is(BlockTags.SNOW) || state.is(Blocks.POWDER_SNOW)) {
            return ModBlocks.INFESTED_SNOW.defaultBlockState();
        }
        if (state.is(BlockTags.ICE) || state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE) || state.is(Blocks.FROSTED_ICE)) {
            return ModBlocks.INFESTED_ICE.defaultBlockState();
        }
        if (state.is(BlockTags.SAND)) return ModBlocks.INFESTED_SAND.defaultBlockState();
        if (state.is(Blocks.GRAVEL)) return ModBlocks.INFESTED_GRAVEL.defaultBlockState();
        if (state.is(Blocks.CLAY)) return ModBlocks.INFESTED_CLAY.defaultBlockState();
        if (state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.COBBLESTONE) || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE) || state.is(Blocks.TUFF)
                || state.is(Blocks.CALCITE) || state.is(Blocks.SMOOTH_STONE)
                || state.is(Blocks.POLISHED_ANDESITE) || state.is(Blocks.POLISHED_DIORITE)
                || state.is(Blocks.POLISHED_GRANITE) || state.is(Blocks.DRIPSTONE_BLOCK)) {
            return ModBlocks.INFESTED_STONE.defaultBlockState();
        }
        if (CONCRETE.contains(state.getBlock())) return ModBlocks.INFESTED_TERRACOTTA.defaultBlockState();
        if (CONCRETE_POWDER.contains(state.getBlock())) return ModBlocks.INFESTED_SAND.defaultBlockState();
        if (state.is(Blocks.RED_MUSHROOM_BLOCK) || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                || state.is(BlockTags.WART_BLOCKS)
                || state.is(Blocks.CACTUS)) {
            return ModBlocks.ALIEN_FLESH.defaultBlockState();
        }
        if (state.is(Blocks.MUSHROOM_STEM)) return ModBlocks.INFESTED_LOG.defaultBlockState();
        if (state.is(Blocks.SUGAR_CANE) || state.is(Blocks.BAMBOO)) {
            return ModBlocks.ALIEN_TENDRILS.defaultBlockState();
        }
        if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.POLISHED_DEEPSLATE) || state.is(Blocks.DEEPSLATE_BRICKS)
                || state.is(Blocks.CRACKED_DEEPSLATE_BRICKS) || state.is(Blocks.DEEPSLATE_TILES)
                || state.is(Blocks.CRACKED_DEEPSLATE_TILES) || state.is(Blocks.CHISELED_DEEPSLATE)) {
            return ModBlocks.INFESTED_DEEPSLATE.defaultBlockState();
        }
        if (state.is(Blocks.NETHERRACK)) return ModBlocks.INFESTED_NETHERRACK.defaultBlockState();
        if (state.is(BlockTags.BASE_STONE_NETHER)) return ModBlocks.INFESTED_STONE.defaultBlockState();
        if (state.is(BlockTags.LOGS)) {
            return ModBlocks.INFESTED_LOG.defaultBlockState();
        }
        if (state.is(BlockTags.PLANKS)) return ModBlocks.INFESTED_PLANKS.defaultBlockState();
        if (state.is(BlockTags.LEAVES)) return ModBlocks.INFESTED_LEAVES.defaultBlockState();
        // Built structures rot wholesale: doors and trapdoors get true infested
        // counterparts (orientation preserved via copyProperties); carved shapes
        // (stairs/slabs/fences) are swallowed into formless infested mass.
        if (state.is(Blocks.CRAFTING_TABLE)) return ModBlocks.INFESTED_CRAFTING_TABLE.defaultBlockState();
        // Village work stations -> infested twins (look like vanilla, but rotted).
        if (state.is(Blocks.BARREL)) return ModBlocks.INFESTED_BARREL.defaultBlockState();
        if (state.is(Blocks.CARTOGRAPHY_TABLE)) return ModBlocks.INFESTED_CARTOGRAPHY_TABLE.defaultBlockState();
        if (state.is(Blocks.SMITHING_TABLE)) return ModBlocks.INFESTED_SMITHING_TABLE.defaultBlockState();
        if (state.is(Blocks.FLETCHING_TABLE)) return ModBlocks.INFESTED_FLETCHING_TABLE.defaultBlockState();
        if (state.is(Blocks.LOOM)) return ModBlocks.INFESTED_LOOM.defaultBlockState();
        if (state.is(Blocks.STONECUTTER)) return ModBlocks.INFESTED_STONECUTTER.defaultBlockState();
        if (state.is(Blocks.GRINDSTONE)) return ModBlocks.INFESTED_GRINDSTONE.defaultBlockState();
        if (state.is(BlockTags.DOORS)) return ModBlocks.INFESTED_DOOR.defaultBlockState();
        if (state.is(BlockTags.TRAPDOORS)) return ModBlocks.INFESTED_TRAPDOOR.defaultBlockState();
        if (state.is(BlockTags.WOODEN_STAIRS) || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_FENCES) || state.is(BlockTags.FENCE_GATES)
                || state.is(Blocks.BOOKSHELF)) {
            return ModBlocks.INFESTED_PLANKS.defaultBlockState();
        }
        if (state.is(BlockTags.FENCES)) return materialFallback(state);
        if (state.is(Blocks.STONE_BRICKS) || state.is(Blocks.MOSSY_STONE_BRICKS)
                || state.is(Blocks.CRACKED_STONE_BRICKS) || state.is(Blocks.CHISELED_STONE_BRICKS)
                || state.is(Blocks.BRICKS) || state.is(Blocks.MOSSY_COBBLESTONE)) {
            return ModBlocks.INFESTED_STONE_BRICKS.defaultBlockState();
        }
        if (state.is(BlockTags.STAIRS) || state.is(BlockTags.SLABS) || state.is(BlockTags.WALLS)) {
            return ModBlocks.INFESTED_STONE.defaultBlockState(); // stone shapes (wooden matched above)
        }
        if (state.is(BlockTags.WOOL)) return ModBlocks.INFESTED_WOOL.defaultBlockState();
        if (state.is(BlockTags.WOOL_CARPETS)) return ModBlocks.INFESTED_WOOL.defaultBlockState();
        // IMPERMEABLE = glass + all 16 stained glasses; panes (incl. stained) by class.
        if (state.is(BlockTags.IMPERMEABLE) || state.is(Blocks.GLASS_PANE)
                || state.getBlock() instanceof net.minecraft.world.level.block.StainedGlassPaneBlock) {
            return ModBlocks.INFESTED_GLASS.defaultBlockState();
        }
        if (state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH)) {
            return ModBlocks.INFESTED_DIRT.defaultBlockState();
        }
        if (state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.MELON)
                || state.is(Blocks.HAY_BLOCK)) {
            return ModBlocks.ALIEN_FLESH.defaultBlockState();
        }
        if (state.is(BlockTags.CROPS) || state.is(Blocks.WHEAT) || state.is(Blocks.CARROTS)
                || state.is(Blocks.POTATOES) || state.is(Blocks.BEETROOTS)) {
            return ModBlocks.ALIEN_TENDRILS.defaultBlockState();
        }
        if (isSurfaceVegetation(state) || isAdditionalVegetation(state)) {
            return ModBlocks.ALIEN_TENDRILS.defaultBlockState();
        }
        if (state.is(BlockTags.BUTTONS) || state.is(BlockTags.PRESSURE_PLATES)
                || state.is(BlockTags.RAILS)) {
            return materialFallback(state);
        }
        // Final coverage rule: every remaining breakable vanilla block without a block
        // entity is consumed too. Thin/mechanical shapes collapse into the closest of
        // the existing infected material families instead of silently resisting spread.
        if (state.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) >= 0.0F) {
            return materialFallback(state);
        }
        return null;
    }

    private static boolean isAdditionalVegetation(BlockState state) {
        return state.is(Blocks.VINE) || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)
                || state.is(Blocks.TWISTING_VINES) || state.is(Blocks.TWISTING_VINES_PLANT)
                || state.is(Blocks.WEEPING_VINES) || state.is(Blocks.WEEPING_VINES_PLANT)
                || state.is(Blocks.CRIMSON_ROOTS) || state.is(Blocks.WARPED_ROOTS)
                || state.is(Blocks.NETHER_SPROUTS) || state.is(Blocks.HANGING_ROOTS)
                || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS)
                || state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT)
                || state.is(Blocks.LILY_PAD) || state.is(Blocks.GLOW_LICHEN)
                || state.is(Blocks.SPORE_BLOSSOM) || state.is(Blocks.SMALL_DRIPLEAF)
                || state.is(Blocks.BIG_DRIPLEAF) || state.is(Blocks.BIG_DRIPLEAF_STEM)
                || state.is(Blocks.PINK_PETALS) || state.is(Blocks.MOSS_CARPET);
    }

    private static BlockState materialFallback(BlockState state) {
        SoundType sound = state.getSoundType();
        if (sound == SoundType.WOOD || sound == SoundType.NETHER_WOOD
                || sound == SoundType.BAMBOO || sound == SoundType.BAMBOO_WOOD
                || sound == SoundType.CHERRY_WOOD) {
            return ModBlocks.INFESTED_PLANKS.defaultBlockState();
        }
        if (sound == SoundType.WOOL) {
            return ModBlocks.INFESTED_WOOL.defaultBlockState();
        }
        if (sound == SoundType.SAND) {
            return ModBlocks.INFESTED_SAND.defaultBlockState();
        }
        if (sound == SoundType.GRAVEL || sound == SoundType.GRASS || sound == SoundType.ROOTED_DIRT
                || sound == SoundType.MUD || sound == SoundType.MOSS
                || sound == SoundType.WET_GRASS || sound == SoundType.SOUL_SAND
                || sound == SoundType.SOUL_SOIL || sound == SoundType.CROP) {
            return ModBlocks.INFESTED_DIRT.defaultBlockState();
        }
        if (sound == SoundType.SNOW || sound == SoundType.POWDER_SNOW) {
            return ModBlocks.INFESTED_SNOW.defaultBlockState();
        }
        if (sound == SoundType.GLASS) {
            return ModBlocks.INFESTED_GLASS.defaultBlockState();
        }
        return ModBlocks.INFESTED_STONE.defaultBlockState();
    }

    /**
     * Day-gated ore corruption. As the infestation spreads it transmutes the ores it
     * reaches (mirrors the "world infects from the inside" patch): day 2 turns coal ->
     * platinum and copper -> palladium; day 3 turns gold into pure radiation; day 4
     * turns lapis into cosmic crystal ore and the gem ores into their infected
     * variants. Iron is left untouched. Returns null if the block isn't a convertible
     * ore yet.
     */
    public static BlockState oreConversionFor(BlockState state, int day) {
        return null;
    }

    public static BlockState cleanStateFor(BlockState state) {
        BlockState bloodyClean = cleanBloodyInfestedStateFor(state);
        if (bloodyClean != null) {
            return bloodyClean;
        }
        if (state.is(ModBlocks.ALIEN_RESIDUE) || state.is(ModBlocks.INFESTED_DIRT)
                || state.is(ModBlocks.INFESTED_GRASS)) {
            return Blocks.DIRT.defaultBlockState();
        }
        if (state.is(ModBlocks.INFESTED_STONE)) return Blocks.STONE.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_DEEPSLATE)) return Blocks.DEEPSLATE.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_SAND)) return Blocks.SAND.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_GRAVEL)) return Blocks.GRAVEL.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_CLAY)) return Blocks.CLAY.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_NETHERRACK)) return Blocks.NETHERRACK.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_SANDSTONE)) return Blocks.SANDSTONE.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_TERRACOTTA)) return Blocks.TERRACOTTA.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_SNOW)) return Blocks.SNOW_BLOCK.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_ICE)) return Blocks.ICE.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_LOG)) return Blocks.OAK_LOG.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_PLANKS)) return Blocks.OAK_PLANKS.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_CRAFTING_TABLE)) return Blocks.CRAFTING_TABLE.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_BARREL)) return Blocks.BARREL.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_CARTOGRAPHY_TABLE)) return Blocks.CARTOGRAPHY_TABLE.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_SMITHING_TABLE)) return Blocks.SMITHING_TABLE.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_FLETCHING_TABLE)) return Blocks.FLETCHING_TABLE.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_LOOM)) return Blocks.LOOM.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_STONECUTTER)) return Blocks.STONECUTTER.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_GRINDSTONE)) return Blocks.GRINDSTONE.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_DOOR)) return Blocks.OAK_DOOR.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_TRAPDOOR)) return Blocks.OAK_TRAPDOOR.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_STONE_BRICKS)) return Blocks.STONE_BRICKS.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_WOOL)) return Blocks.WHITE_WOOL.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_GLASS)) return Blocks.GLASS.defaultBlockState();
        if (state.is(ModBlocks.INFESTED_LEAVES) || state.is(ModBlocks.ALIEN_TENDRILS)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.is(ModBlocks.TOXIC_WATER) || state.is(ModBlocks.INFECTED_WATER)) {
            return Blocks.WATER.defaultBlockState();
        }
        return null;
    }

    public static boolean isContaminated(BlockState state) {
        return cleanStateFor(state) != null || state.is(ModBlocks.ALIEN_HIVE) || state.is(ModBlocks.ALIEN_STASH);
    }

    private static BlockState bloodyInfestedStateFor(BlockState state) {
        net.minecraft.world.level.block.Block combined = BloodyVariantRegistry.infestedForBloody(state.getBlock());
        return combined != null ? copyProperties(state, combined.defaultBlockState()) : null;
    }

    private static BlockState cleanBloodyInfestedStateFor(BlockState state) {
        net.minecraft.world.level.block.Block bloodOnly = BloodyVariantRegistry.cleanForBloodyInfested(state.getBlock());
        return bloodOnly != null ? copyProperties(state, bloodOnly.defaultBlockState()) : null;
    }

    /**
     * Copies every shared blockstate property (facing, axis, door half/hinge/open,
     * stairs shape...) from the original block onto its converted counterpart, so
     * doors stay doors and logs keep their orientation through the conversion.
     */
    public static BlockState copyProperties(BlockState from, BlockState to) {
        for (net.minecraft.world.level.block.state.properties.Property<?> property : from.getProperties()) {
            if (to.hasProperty(property)) {
                to = copyProperty(from, to, property);
            }
        }
        return to;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to,
            net.minecraft.world.level.block.state.properties.Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
