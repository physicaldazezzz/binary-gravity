package com.example.alieninvasion.logic;

import com.example.alieninvasion.registry.ItemRegistry;
import com.example.alieninvasion.registry.ModBlocks;
import com.example.alieninvasion.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RadiationManager {
    private RadiationManager() {
    }

    public static final float MAX_DOSE = 100.0F;
    private static final float GAIN = 0.45F;

    // HP отнимается от максимума каждую секунду на активном тире
    private static final float DRAIN_SLOW = 0.2F;  // ≥50%: ~1 сердце за 10 сек
    private static final float DRAIN_FAST = 0.5F;  // ≥75%: ~1 сердце за 4 сек
    private static final float DRAIN_MAX  = 16.0F; // максимум 8 сердец убрать (минимум 2)

    private static final ResourceLocation HEALTH_DRAIN_ID =
            ResourceLocation.fromNamespaceAndPath("alien-invasion", "radiation_health_drain");

    private static final Map<UUID, Float>   DOSE           = new ConcurrentHashMap<>();
    private static final Map<UUID, Float>   DOSE_MULT      = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> LAST_DOSE_TIER = new ConcurrentHashMap<>();
    private static final Map<UUID, Float>   HEALTH_DRAIN   = new ConcurrentHashMap<>();
    public  static final Map<UUID, Integer> SCREEN_GLITCH  = new ConcurrentHashMap<>();
    /** UUID игроков, у которых следующий вызов hurtTo() не должен показывать анимацию урона. */
    public  static final java.util.Set<UUID> SUPPRESS_HURT_ANIM = java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static int stormTicks = 0;

    public static float getDose(UUID id) {
        return DOSE.getOrDefault(id, 0.0F);
    }

    public static float getDose(Player player) {
        return getDose(player.getUUID());
    }

    private static void setDose(UUID id, float dose) {
        dose = Math.max(0.0F, Math.min(MAX_DOSE, dose));
        if (dose <= 0.01F) {
            DOSE.remove(id);
        } else {
            DOSE.put(id, dose);
        }
    }

    public static void setDoseMultiplier(Player player, float mult) {
        if (mult == 1.0F) DOSE_MULT.remove(player.getUUID());
        else DOSE_MULT.put(player.getUUID(), mult);
    }

    public static void capDose(Player player, float max) {
        UUID id = player.getUUID();
        float d = DOSE.getOrDefault(id, 0.0F);
        if (d > max) DOSE.put(id, max);
    }

    public static void reduceDose(Player player, float amount) {
        setDose(player.getUUID(), getDose(player) - amount);
    }

    public static void reduceHealthDrain(Player player, float amount) {
        UUID id = player.getUUID();
        float current = HEALTH_DRAIN.getOrDefault(id, 0.0F);
        float next = Math.max(0.0F, current - amount);
        if (next <= 0.01F) {
            HEALTH_DRAIN.remove(id);
            if (player instanceof ServerPlayer sp) {
                removeHealthDrain(sp);
            }
        } else {
            HEALTH_DRAIN.put(id, next);
            if (player instanceof ServerPlayer sp) {
                applyHealthDrain(sp, id);
            }
        }
    }

    public static void clearDose(Player player) {
        UUID id = player.getUUID();
        DOSE.remove(id);
        LAST_DOSE_TIER.remove(id);
        HEALTH_DRAIN.remove(id);
        SUPPRESS_HURT_ANIM.remove(id);
        if (player instanceof ServerPlayer sp) removeHealthDrain(sp);
    }

    /** Полная очистка состояния игрока при выходе с сервера — иначе UUID-записи копятся вечно. */
    public static void onDisconnect(Player player) {
        clearDose(player);
        DOSE_MULT.remove(player.getUUID());
        SCREEN_GLITCH.remove(player.getUUID());
    }

    public static boolean isStormActive() {
        return stormTicks > 0;
    }

    public static int scanField(Level level, BlockPos center) {
        int danger = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -8; x <= 8; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -8; z <= 8; z++) {
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    danger += sourceWeight(level.getBlockState(cursor));
                }
            }
        }
        if (isStormActive() && level.canSeeSky(center)) {
            danger += 6;
        }
        return Math.min(99, danger);
    }

    private static int sourceWeight(BlockState state) {
        if (state.is(ModBlocks.PURE_RADIATION_BLOCK)) {
            return 5;
        }
        if (state.is(ModBlocks.TOXIC_WATER) || state.is(ModBlocks.TOXIC_BARREL)) {
            return 1;
        }
        return 0;
    }

    private static boolean isInfestedBlock(BlockState state) {
        return state.is(ModBlocks.INFESTED_STONE) || state.is(ModBlocks.INFESTED_DIRT)
                || state.is(ModBlocks.INFESTED_GRASS) || state.is(ModBlocks.INFESTED_SAND)
                || state.is(ModBlocks.INFESTED_GRAVEL) || state.is(ModBlocks.INFESTED_DEEPSLATE)
                || state.is(ModBlocks.ALIEN_RESIDUE);
    }

    private static float scanExposure(Level level, BlockPos center) {
        float exposure = 0.0F;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -4; x <= 4; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockState bs = level.getBlockState(cursor.set(
                            center.getX() + x, center.getY() + y, center.getZ() + z));
                    int w = sourceWeight(bs);
                    if (w == 0) continue; // ONLY real radioactive sources - infested blocks are clean
                    int distSq = x * x + y * y + z * z;
                    float falloff = distSq <= 4 ? 1.0F : distSq <= 16 ? 0.5F : 0.25F;
                    exposure += w * falloff;
                }
            }
        }
        if (isStormActive() && level.canSeeSky(center)) {
            exposure += 4.0F;
        }
        return Math.min(12.0F, exposure);
    }

    public static void addDose(Player player, float amount) {
        float mult = DOSE_MULT.getOrDefault(player.getUUID(), 1.0F);
        setDose(player.getUUID(), getDose(player) + amount * mult);
    }

    public static void removeAllDoseEffects(ServerPlayer player) {
        player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.IRRADIATION));
        player.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
        player.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
        player.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
        player.removeEffect(net.minecraft.world.effect.MobEffects.HUNGER);
        player.removeEffect(net.minecraft.world.effect.MobEffects.DARKNESS);
        removeHealthDrain(player);
    }

    private static void removeHealthDrain(ServerPlayer player) {
        var attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) attr.removeModifier(HEALTH_DRAIN_ID);
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void applyHealthDrain(ServerPlayer player, UUID id) {
        float drain = HEALTH_DRAIN.getOrDefault(id, 0.0F);
        var attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) return;
        // Подавить анимацию урона — обработается в LocalPlayerMixin.hurtTo()
        SUPPRESS_HURT_ANIM.add(id);
        attr.removeModifier(HEALTH_DRAIN_ID);
        if (drain > 0.01F) {
            attr.addTransientModifier(
                    new AttributeModifier(HEALTH_DRAIN_ID, -drain, AttributeModifier.Operation.ADD_VALUE));
            // Minecraft сам зажмёт health через onAttributeUpdated(); явный setHealth не нужен
        } else {
            SUPPRESS_HURT_ANIM.remove(id); // дрейн нулевой — пакет не пойдёт, убираем флаг сразу
        }
    }

    public static void reapplyDoseEffects(ServerPlayer player) {
        float dose = getDose(player);
        int tier = dose >= MAX_DOSE ? 4 : dose >= 75.0F ? 3 : dose >= 50.0F ? 2 : dose >= 25.0F ? 1 : 0;
        if (tier < 1) return;
        var irrH = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.IRRADIATION);
        player.addEffect(new MobEffectInstance(irrH, 100, 0, false, true));
        if (tier >= 2) player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 39, 0, false, true));
        if (tier >= 3) {
            player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,          100, 0, false, true));
            player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, true));
            player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN,      100, 0, false, true));
            player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER,            100, 0, false, true));
            player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS,          100, 0, false, true));
        }
        applyHealthDrain(player, player.getUUID());
    }

    public static void tickPlayer(ServerLevel level, ServerPlayer player) {
        UUID id = player.getUUID();
        if (player.isCreative() || player.isSpectator()) {
            clearDose(player);
            SCREEN_GLITCH.remove(id);
            removeAllDoseEffects(player);
            player.setAttached(com.example.alieninvasion.registry.ModAttachments.RADIATION_FIELD, 0);
            return;
        }

        // Снижение облучения даёт МАСКА в своём слоте — НЕ шлем брони (раньше тут стоял
        // шлемный слот, из-за чего казалось, что шлем защищает как маска).
        boolean masked = com.example.alieninvasion.logic.MaskSlot.hasMask(player);
        float exposure = scanExposure(level, player.blockPosition());
        // Фон дозиметра (синкается на клиент) — из РЕАЛЬНОЙ экспозиции (та же, что копит
        // дозу), до ослабления маской. exposure(0..12) → шкала 0..99. Так дозиметр
        // показывает именно ту радиацию, что вокруг, а не клиентскую переоценку дальних блоков.
        player.setAttached(com.example.alieninvasion.registry.ModAttachments.RADIATION_FIELD,
                Math.min(99, Math.round(exposure * 8.0F)));
        if (masked) exposure *= 0.35F;

        float dose = getDose(player);
        float doseBefore = dose;
        if (exposure > 0.0F) {
            dose += exposure * GAIN;
        } else if (dose > 0.0F) {
            // Вне источников доза всегда медленно спадает (~1%/сек) — выход
            // из смертельной спирали возможен и без таблеток, просто долгий.
            dose = Math.max(0.0F, dose - 0.2F);
        }
        setDose(id, dose);
        dose = getDose(player);
        float doseDelta = dose - doseBefore;

        int newTier  = dose >= MAX_DOSE ? 4 : dose >= 75.0F ? 3 : dose >= 50.0F ? 2 : dose >= 25.0F ? 1 : 0;
        int prevTier = LAST_DOSE_TIER.getOrDefault(id, 0);

        // Снятие эффектов при понижении тира
        if (newTier < prevTier) {
            if (newTier < 3) {
                player.removeEffect(net.minecraft.world.effect.MobEffects.DARKNESS);
                player.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
                player.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
                player.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
                player.removeEffect(net.minecraft.world.effect.MobEffects.HUNGER);
            }
            if (newTier < 2) player.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
            if (newTier < 1) player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.IRRADIATION));
            LAST_DOSE_TIER.put(id, newTier);
        } else if (newTier > prevTier) {
            LAST_DOSE_TIER.put(id, newTier);
        }

        // Применение накопленных эффектов
        var irrH = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.IRRADIATION);
        if (newTier >= 1) player.addEffect(new MobEffectInstance(irrH, 60, 0, false, true));
        // Иссушение — только визуал, duration=39 исключает авто-урон ваниллы (39%40≠0)
        if (newTier >= 2) player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 39, 0, false, true));
        if (newTier >= 3) {
            player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,          60, 0, false, true));
            player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, true));
            player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN,      60, 0, false, true));
            player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER,            60, 0, false, true));
            player.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS,          60, 0, false, true));
        }

        // Постепенное уменьшение максимального здоровья
        if (newTier >= 2) {
            float rate = newTier >= 3 ? DRAIN_FAST : DRAIN_SLOW;
            float current = HEALTH_DRAIN.getOrDefault(id, 0.0F);
            HEALTH_DRAIN.put(id, Math.min(DRAIN_MAX, current + rate));
            applyHealthDrain(player, id);
        }

        // Помехи: 2=сильные (быстрый рост ≥1.5%/с), 1=лёгкие (любой рост вблизи), 0=нет
        if      (exposure > 0.0F && (doseDelta >= 1.5F || dose >= MAX_DOSE)) SCREEN_GLITCH.put(id, 2);
        else if (exposure > 0.0F && doseDelta > 0.0F)                       SCREEN_GLITCH.put(id, 1);
        else                                                                 SCREEN_GLITCH.remove(id);

        // Звук счётчика Гейгера вблизи источников
        if (exposure > 0.0F && level.random.nextFloat() < Math.min(0.8F, 0.1F + exposure * 0.06F)) {
            level.playSound(null, player.blockPosition(), SoundEvents.STONE_BUTTON_CLICK_ON,
                    SoundSource.PLAYERS, 0.25F, 1.7F + level.random.nextFloat() * 0.6F);
        }
    }

    public static void tickStorm(ServerLevel level) {
        if (stormTicks > 0) {
            stormTicks--;
            if (stormTicks == 0) {
                for (ServerPlayer p : level.players()) {
                    p.displayClientMessage(Component.literal("§a[!] Радиоактивная буря утихла."), false);
                }
            }
            return;
        }
        if (level.isNight() && level.getGameTime() % 24000 == 15000
                && SurvivalManager.getDay(level) >= 5 && level.random.nextFloat() < 0.18F
                && !level.players().isEmpty()) {
            stormTicks = 3000;
            for (ServerPlayer p : level.players()) {
                p.displayClientMessage(Component.literal(
                        "§4[!] РАДИОАКТИВНАЯ БУРЯ! Найдите укрытие или наденьте костюм химзащиты."), false);
                level.playSound(null, p.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.AMBIENT, 1.0F, 0.45F);
            }
        }
    }
}
