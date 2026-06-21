package com.example.alieninvasion.mixin;

import com.example.alieninvasion.registry.ModEffects;
import com.example.alieninvasion.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin implements com.example.alieninvasion.logic.InfectionTrackedPlayer {

    @Unique
    private int alien_infectionTicks = 0;
    @Unique
    private int alien_prevInfectionStage = 0; // 0 = none, 1 = stage 1, 2 = stage 2, 3 = stage 3
    @Unique
    private net.minecraft.core.BlockPos alien_placedLightPos = null;

    @Unique
    private static final net.minecraft.resources.ResourceLocation ALIEN_HEALTH_MOD_ID = 
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("alien-invasion", "infection_health");

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeInfectionData(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        tag.putInt("AlienInfectionTicks", this.alien_infectionTicks);
        tag.putInt("AlienPrevInfectionStage", this.alien_prevInfectionStage);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readInfectionData(net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("AlienInfectionTicks")) {
            this.alien_infectionTicks = tag.getInt("AlienInfectionTicks");
        }
        if (tag.contains("AlienPrevInfectionStage")) {
            this.alien_prevInfectionStage = tag.getInt("AlienPrevInfectionStage");
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInfection(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        Level level = player.level();
        if (level.isClientSide) return;

        boolean hasEffect = player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION));

        if (hasEffect) {
            var effectInstance = player.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION));
            if (effectInstance != null) {
                int currentStage = effectInstance.getAmplifier() + 1; // 1, 2, 3
                this.alien_prevInfectionStage = currentStage;
                this.alien_infectionTicks++;

                // Stage 1 -> Stage 2 progression (after 1200 ticks / 1 minute)
                if (currentStage == 1) {
                    // Apply mild slowness (Slowness I)
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false));

                    if (this.alien_infectionTicks >= 6000) {
                        player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION), 72000, 1, false, true));
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[!] Инкубационный период завершен. Ваша ДНК начинает мутировать..."));
                        this.alien_infectionTicks = 6000; // Reset tick count for next stage check
                    }
                }
                // Stage 2 -> Stage 3 progression (after 2400 ticks / 2 minutes)
                else if (currentStage == 2) {
                    // Apply glowing
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.GLOWING, 40, 0, false, false));

                    // Reduce max health
                    alien_applyHealthModifier(player);

                    // Attract aliens
                    if (player.tickCount % 40 == 0) {
                        java.util.List<net.minecraft.world.entity.Mob> nearbyAliens = level.getEntitiesOfClass(
                                net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(32.0D),
                                e -> com.example.alieninvasion.entity.AlienUtils.isAlliedTo(null, e) && e.getTarget() == null);
                        for (net.minecraft.world.entity.Mob alien : nearbyAliens) {
                            alien.setTarget(player);
                        }
                    }

                    if (this.alien_infectionTicks >= 14000) { // much slower: 6000 + 8000
                        player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION), 72000, 2, false, true));
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4[!] Ваши клетки полностью ассимилированы роем!"));
                    }
                }
                // Stage 3 -> Death and Spawn Clone (after 100 ticks)
                else if (currentStage == 3) {
                    if (this.alien_infectionTicks >= 14400) { // 14000 + 400, slower final stage
                        alien_triggerAssimilation((net.minecraft.server.level.ServerLevel) level, (net.minecraft.server.level.ServerPlayer) player);
                    }
                }
            }
        } else {
            // No infection effect present
            if (this.alien_prevInfectionStage > 0) {
                boolean curedTag = player.getTags().contains("CuredByAntidote");
                boolean immuneArmor = alien_hasInfectionImmuneArmor(player);
                if (curedTag || immuneArmor) {
                    // Explicit cure (antidote/serum) OR the full Chitin/Cosmic set's
                    // infection immunity. The armor path MUST reset the stage machine
                    // too - otherwise this branch re-applied Stage 2/3 every tick and
                    // silently defeated the documented "shrugs off Infection" set bonus.
                    this.alien_infectionTicks = 0;
                    this.alien_prevInfectionStage = 0;
                    alien_removeHealthModifier(player);
                    if (curedTag) {
                        player.removeTag("CuredByAntidote");
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[!] Вы успешно вылечились от заражения!"));
                    }
                } else if (this.alien_prevInfectionStage == 1) {
                    // Stage 1 washes out (milk, or simply leaving the source).
                    this.alien_infectionTicks = 0;
                    this.alien_prevInfectionStage = 0;
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[!] Молоко вывело патоген из вашего организма."));
                } else {
                    // Stage 2/3 is a real mutation - needs antidote/serum, not milk.
                    int amp = this.alien_prevInfectionStage - 1;
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION), 72000, amp, false, true));
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[!] Обычное молоко не способно вылечить мутацию пришельцев! Вам нужно слабое противоядие или био-сыворотка."));
                }
            }
        }
    }

    @Inject(method = "eat", at = @At("TAIL"))
    private void adjustNutrition(Level level, ItemStack itemStack, FoodProperties foodProperties, CallbackInfoReturnable<ItemStack> cir) {
        Player player = (Player) (Object) this;

        if (!level.isClientSide) {
            net.minecraft.world.item.Item item = itemStack.getItem();
            if (item == net.minecraft.world.item.Items.GOLDEN_APPLE ||
                item == net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE ||
                item == net.minecraft.world.item.Items.GOLDEN_CARROT) {
                
                float reducePercent = 0.0F;
                if (item == net.minecraft.world.item.Items.GOLDEN_APPLE) {
                    reducePercent = 50.0F;
                } else if (item == net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE) {
                    reducePercent = 100.0F;
                } else if (item == net.minecraft.world.item.Items.GOLDEN_CARROT) {
                    reducePercent = 8.0F;
                }

                // Apply Radiation reduction
                if (reducePercent >= 100.0F) {
                    com.example.alieninvasion.logic.RadiationManager.clearDose(player);
                } else {
                    com.example.alieninvasion.logic.RadiationManager.reduceDose(player, reducePercent);
                    com.example.alieninvasion.logic.RadiationManager.reduceHealthDrain(player, 16.0F * (reducePercent / 100.0F));
                }

                // Apply Infection reduction
                float currentMeter = com.example.alieninvasion.logic.InfectionManager.getMeter(player);
                float newMeter = Math.max(0.0F, currentMeter - reducePercent);

                if (newMeter <= 0.01F) {
                    player.addTag("CuredByAntidote");
                    com.example.alieninvasion.logic.InfectionManager.clear(player);
                    player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION));
                } else {
                    com.example.alieninvasion.logic.InfectionManager.reduceMeter(player, reducePercent);
                    
                    int newStage = newMeter >= 75.0F ? 3 : newMeter >= 50.0F ? 2 : newMeter >= 25.0F ? 1 : 0;
                    if (newStage == 0) {
                        player.addTag("CuredByAntidote");
                        com.example.alieninvasion.logic.InfectionManager.clear(player);
                        player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION));
                    } else {
                        int amp = newStage - 1;
                        player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION), 72000, amp, false, true));
                        
                        this.alien_prevInfectionStage = newStage;
                        this.alien_infectionTicks = 0;
                        
                        if (newStage < 2) {
                            alien_removeHealthModifier(player);
                        }
                    }
                }
            }
        }

        if (player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION))) {
            var effect = player.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION));
            if (effect != null && effect.getAmplifier() >= 1) { // Stage 2 Mutation or Stage 3 Assimilation
                boolean isRaw = alien_isRawFood(itemStack);
                boolean isCooked = alien_isCookedFood(itemStack);

                int baseNutrition = foodProperties.nutrition();
                float baseSaturation = foodProperties.saturation();

                if (isRaw) {
                    // Double nutrition: vanilla already added baseNutrition, we add another baseNutrition
                    player.getFoodData().eat(baseNutrition, baseSaturation);
                } else if (isCooked) {
                    // Zero nutrition: we subtract what vanilla just added
                    int currentFood = player.getFoodData().getFoodLevel();
                    float currentSat = player.getFoodData().getSaturationLevel();

                    int newFood = Math.max(0, currentFood - baseNutrition);
                    float addedSat = (float) baseNutrition * baseSaturation * 2.0F;
                    float newSat = Math.max(0.0F, currentSat - addedSat);

                    player.getFoodData().setFoodLevel(newFood);
                    player.getFoodData().setSaturation(newSat);
                }
            }
        }

        // Once the world is fully infected (day 4+), plant food is tainted - eating
        // it inflicts a random ailment, pushing players toward meat/clean supplies.
        if (!level.isClientSide && com.example.alieninvasion.logic.SurvivalManager.getDay(level) >= 4
                && alien_isPlantFood(itemStack)) {
            switch (player.getRandom().nextInt(4)) {
                case 0 -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 100, 0));
                case 1 -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER, 200, 0));
                case 2 -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 120, 0));
                default -> player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 160, 0));
            }
        }
    }

    @Unique
    private boolean alien_isPlantFood(ItemStack stack) {
        String p = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return p.equals("bread") || p.equals("apple") || p.equals("carrot") || p.equals("potato")
                || p.equals("baked_potato") || p.equals("beetroot") || p.equals("beetroot_soup")
                || p.equals("melon_slice") || p.equals("pumpkin_pie") || p.equals("cookie")
                || p.equals("mushroom_stew") || p.equals("dried_kelp") || p.equals("chorus_fruit")
                || p.contains("berries");
    }

    @Unique
    private boolean alien_isRawFood(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("raw_") || path.contains("rotten_") || path.contains("infested_") 
                || path.equals("cod") || path.equals("salmon") || path.equals("melon_slice") 
                || path.equals("sweet_berries") || path.equals("glow_berries") || path.equals("apple");
    }

    @Unique
    private boolean alien_isCookedFood(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("cooked_") || path.equals("bread") || path.equals("baked_potato") 
                || path.contains("stew") || path.contains("soup") || path.equals("pumpkin_pie") 
                || path.equals("cookie") || path.equals("cake");
    }

    @Unique
    private void alien_applyHealthModifier(Player player) {
        var maxHealthAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            if (maxHealthAttr.getModifier(ALIEN_HEALTH_MOD_ID) == null) {
                maxHealthAttr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        ALIEN_HEALTH_MOD_ID, -8.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
            }
        }
    }

    @Unique
    private void alien_removeHealthModifier(Player player) {
        var maxHealthAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealthAttr != null && maxHealthAttr.getModifier(ALIEN_HEALTH_MOD_ID) != null) {
            maxHealthAttr.removeModifier(ALIEN_HEALTH_MOD_ID);
        }
    }

    @Unique
    private void alien_triggerAssimilation(net.minecraft.server.level.ServerLevel level, net.minecraft.server.level.ServerPlayer player) {
        // Spawn Clone — ТОЛЬКО с 4-го дня. Раньше заражённых клонов в мире быть не должно
        // (на 0 день ассимиляция просто лечит/убивает игрока без клона).
        if (com.example.alieninvasion.logic.SurvivalManager.getDay(level) >= 4) {
            com.example.alieninvasion.entity.InfestedPlayerCloneEntity clone = com.example.alieninvasion.registry.EntityRegistry.INFESTED_PLAYER_CLONE.create(level);
            if (clone != null) {
                clone.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                clone.copyFromPlayer(player);
                level.addFreshEntity(clone);
            }
        }

        // Clear player inventory so they don't drop items on death
        player.getInventory().clearContent();

        // Cure infection before killing, so they don't respawn infected
        player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INFECTION));
        this.alien_infectionTicks = 0;
        this.alien_prevInfectionStage = 0;
        alien_removeHealthModifier(player);

        // Kill player
        player.hurt(level.damageSources().magic(), 1000.0F);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void cleanUpLightOnRemove(net.minecraft.world.entity.Entity.RemovalReason reason, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (this.alien_placedLightPos != null && !player.level().isClientSide) {
            alien_removeLight(player.level(), this.alien_placedLightPos);
            this.alien_placedLightPos = null;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickDynamicLight(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        Level level = player.level();
        if (level.isClientSide) return;

        // Clean up light if player is dead or spectator
        if (!player.isAlive() || player.isSpectator()) {
            if (this.alien_placedLightPos != null) {
                alien_removeLight(level, this.alien_placedLightPos);
                this.alien_placedLightPos = null;
            }
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        int mainLight = alien_getLightLevel(mainHand);
        int offLight = alien_getLightLevel(offHand);
        int heldLight = Math.max(mainLight, offLight);

        if (heldLight > 0) {
            BlockPos currentPos = player.blockPosition();
            BlockPos targetPos = null;

            // Check if feet position or eye position is valid for placing a light
            if (alien_isValidLightPos(level, currentPos)) {
                targetPos = currentPos;
            } else if (alien_isValidLightPos(level, currentPos.above())) {
                targetPos = currentPos.above();
            }

            if (targetPos != null) {
                if (this.alien_placedLightPos != null && !this.alien_placedLightPos.equals(targetPos)) {
                    // Player moved! Clean up old pos
                    alien_removeLight(level, this.alien_placedLightPos);
                    this.alien_placedLightPos = null;
                }

                // Place new light block
                var state = level.getBlockState(targetPos);
                boolean waterlogged = state.is(Blocks.WATER) || (state.is(Blocks.LIGHT) && state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED) && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED));
                var lightState = Blocks.LIGHT.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.LightBlock.LEVEL, heldLight)
                        .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, waterlogged);
                
                level.setBlockAndUpdate(targetPos, lightState);
                this.alien_placedLightPos = targetPos;
            } else {
                // No valid position, clean up old light
                if (this.alien_placedLightPos != null) {
                    alien_removeLight(level, this.alien_placedLightPos);
                    this.alien_placedLightPos = null;
                }
            }
        } else {
            // Not holding a light source, clean up
            if (this.alien_placedLightPos != null) {
                alien_removeLight(level, this.alien_placedLightPos);
                this.alien_placedLightPos = null;
            }
        }
    }

    @Unique
    private boolean alien_isValidLightPos(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LIGHT);
    }

    @Unique
    private void alien_removeLight(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.is(Blocks.LIGHT)) {
            boolean waterlogged = state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED) && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED);
            level.setBlockAndUpdate(pos, waterlogged ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
    }

    @Unique
    private int alien_getLightLevel(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var item = stack.getItem();
        if (item == net.minecraft.world.item.Items.TORCH) return 14;
        if (item == net.minecraft.world.item.Items.SOUL_TORCH) return 10;
        if (item == net.minecraft.world.item.Items.REDSTONE_TORCH) return 7;
        if (item == net.minecraft.world.item.Items.LANTERN) return 15;
        if (item == net.minecraft.world.item.Items.SOUL_LANTERN) return 10;
        if (item == net.minecraft.world.item.Items.GLOWSTONE) return 15;
        if (item == net.minecraft.world.item.Items.CAMPFIRE) return 15;
        if (item == net.minecraft.world.item.Items.SOUL_CAMPFIRE) return 10;
        if (item == net.minecraft.world.item.Items.JACK_O_LANTERN) return 15;
        if (item == net.minecraft.world.item.Items.LAVA_BUCKET) return 15;
        
        // Custom items
        if (item == ItemRegistry.COSMIC_SHARD) return 8;
        if (item == ItemRegistry.COSMIC_INGOT) return 10;
        if (item == ItemRegistry.COSMIC_HELMET || item == ItemRegistry.COSMIC_CHESTPLATE || item == ItemRegistry.COSMIC_LEGGINGS || item == ItemRegistry.COSMIC_BOOTS) return 10;
        
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        if (path.contains("cosmic_block")) return 15;
        if (path.contains("dark_matter_ore")) return 15;
        if (path.contains("purifier")) return 12;

        return 0;
    }

    @Override
    public void alien_resetInfectionTicks() {
        this.alien_infectionTicks = 0;
    }

    // Full Chitin or full Cosmic set = infection immunity. Mirrors the checks in
    // ModEvents; used so the stage machine treats armoured removal as a real cure
    // instead of re-applying the effect.
    @Unique
    private boolean alien_hasInfectionImmuneArmor(Player player) {
        return com.example.alieninvasion.logic.ArmorProtection.hasCompatibleSet(player,
                ItemRegistry.COSMIC_HELMET, ItemRegistry.COSMIC_CHESTPLATE,
                ItemRegistry.COSMIC_LEGGINGS, ItemRegistry.COSMIC_BOOTS);
    }
}
