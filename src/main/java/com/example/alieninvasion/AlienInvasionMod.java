package com.example.alieninvasion;

import com.example.alieninvasion.registry.EntityRegistry;
import com.example.alieninvasion.registry.ItemRegistry;
import com.example.alieninvasion.registry.ModEffects;
import com.example.alieninvasion.registry.ModFluids;
import com.example.alieninvasion.registry.ModItemGroups;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlienInvasionMod implements ModInitializer {
	public static final String MODID = "alien-invasion"; // Must match fabric.mod.json
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> INFESTED_STONE_VEIN_KEY =
			net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE,
					net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "infested_stone_vein"));

	public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> ALIEN_RESIDUE_VEIN_KEY =
			net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE,
					net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "alien_residue_vein"));

	public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> COSMIC_CRYSTAL_VEIN_KEY =
			net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE,
					net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "cosmic_crystal_vein"));

	public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> PLATINUM_ORE_VEIN_KEY =
			net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE,
					net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "platinum_ore_vein"));

	public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> PALLADIUM_ORE_VEIN_KEY =
			net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE,
					net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "palladium_ore_vein"));

	public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> PURE_RADIATION_BLOCK_VEIN_KEY =
			net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE,
					net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "pure_radiation_block_vein"));

	public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.placement.PlacedFeature> DARK_MATTER_ORE_VEIN_KEY =
			net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.PLACED_FEATURE,
					net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, "dark_matter_ore_vein"));

	@Override
	public void onInitialize() {
		LOGGER.info("Alien Invasion Mod Initialized (Fabric)");

		ModEffects.registerEffects();
		com.example.alieninvasion.registry.ModParticles.registerParticles();
		com.example.alieninvasion.registry.ModSounds.registerSounds();
		com.example.alieninvasion.registry.ModAttachments.init();
		// Сеть слота маски: клиент шлёт тоггл-пакет, сервер надевает/снимает маску.
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(
				com.example.alieninvasion.network.ToggleMaskPayload.TYPE,
				com.example.alieninvasion.network.ToggleMaskPayload.CODEC);
		net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
				com.example.alieninvasion.network.ToggleMaskPayload.TYPE,
				(payload, context) -> context.player().server.execute(() -> {
					net.minecraft.server.level.ServerPlayer sp = context.player();
					sp.openMenu(new net.minecraft.world.SimpleMenuProvider(
							(id, inv, p) -> new com.example.alieninvasion.block.MaskMenu(
									id, inv, com.example.alieninvasion.logic.MaskSlot.get(p)),
							net.minecraft.network.chat.Component.translatable("container.alien-invasion.mask")));
				}));
		ModFluids.registerFluids();
		com.example.alieninvasion.registry.ModBlocks.registerBlocks(); // Register Blocks
		EntityRegistry.registerEntities();
		ItemRegistry.registerItems();
		ModItemGroups.registerItemGroups();

		// Сетевой пакет победы (S2C) — чтобы клиент скрыл HUD после победы.
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(
				com.example.alieninvasion.network.VictoryPayload.TYPE,
				com.example.alieninvasion.network.VictoryPayload.CODEC);
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(
				com.example.alieninvasion.network.GravityBootsTogglePayload.TYPE,
				com.example.alieninvasion.network.GravityBootsTogglePayload.CODEC);
		net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
				com.example.alieninvasion.network.GravityBootsTogglePayload.TYPE,
				(payload, context) -> context.server().execute(() ->
						com.example.alieninvasion.item.GravityBootsItem.toggleLevitation(context.player())));

		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(
				com.example.alieninvasion.network.LeftClickBlasterPayload.TYPE,
				com.example.alieninvasion.network.LeftClickBlasterPayload.CODEC);
		net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(
				com.example.alieninvasion.network.LeftClickBlasterPayload.TYPE,
				(payload, context) -> context.server().execute(() -> {
					net.minecraft.world.item.ItemStack stack = context.player().getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
					if (!stack.isEmpty() && stack.getItem() instanceof com.example.alieninvasion.item.AlienBlasterItem blaster) {
						blaster.handleAlternativeFire(context.player(), stack);
					}
				}));

		// EventHandler.registerEvents(); // Old
		com.example.alieninvasion.events.ModEvents.registerEvents();

		// Smart vanilla mobs + player-like neutral villagers.
		com.example.alieninvasion.logic.SmartMobs.register();

		// Overworld-only campaign (dimensions disabled; win = survive 8 days).
		com.example.alieninvasion.logic.CampaignRules.register();

		// Procedural structures: cave dungeons, alien cities, crashed UFOs.
		com.example.alieninvasion.worldgen.ModFeatures.register();

		// Register World Gen features
		net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
				net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
				net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
				INFESTED_STONE_VEIN_KEY
		);
		net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
				net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
				net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
				ALIEN_RESIDUE_VEIN_KEY
		);
		net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
				net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
				net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
				COSMIC_CRYSTAL_VEIN_KEY
		);
		net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
				net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
				net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
				PLATINUM_ORE_VEIN_KEY
		);
		net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
				net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
				net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
				PALLADIUM_ORE_VEIN_KEY
		);
		net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
				net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
				net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
				PURE_RADIATION_BLOCK_VEIN_KEY
		);
		net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
				net.fabricmc.fabric.api.biome.v1.BiomeSelectors.foundInOverworld(),
				net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES,
				DARK_MATTER_ORE_VEIN_KEY
		);

		net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			com.example.alieninvasion.command.InvasionCommand.register(dispatcher);
		});

		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(level -> {
			if (level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
				com.example.alieninvasion.world.InvasionManager.get(level).tick(level);
			} else if (level.dimension().equals(com.example.alieninvasion.logic.HomeworldManager.HOMEWORLD)) {
				// Родной мир Роя: очередь декорирования свежих чанков (дюны, шпили,
				// озёра, гнёзда) — плоская заготовка оживает по мере исследования.
				com.example.alieninvasion.logic.HomeworldManager.tickWorld(level);
			}
		});

		net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			if (world.dimension().equals(com.example.alieninvasion.logic.HomeworldManager.HOMEWORLD)) {
				com.example.alieninvasion.logic.HomeworldManager.onChunkLoad(world, chunk);
			}
		});
	}
}
