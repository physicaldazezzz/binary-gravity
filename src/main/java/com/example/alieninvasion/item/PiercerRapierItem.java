package com.example.alieninvasion.item;

import com.example.alieninvasion.registry.ModEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PiercerRapierItem extends SwordItem {
    private static final ThreadLocal<Boolean> APPLYING_BONUS = ThreadLocal.withInitial(() -> false);

    public PiercerRapierItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            if (!APPLYING_BONUS.get()) {
                APPLYING_BONUS.set(true);
                try {
                    float dose = com.example.alieninvasion.logic.RadiationManager.getDose(player);
                    if (dose > 0) {
                        float extraDamage = dose * 0.08F; // Up to +8.0 damage at 100% radiation
                        target.hurt(attacker.damageSources().playerAttack(player), extraDamage);
                    }
                } finally {
                    APPLYING_BONUS.set(false);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        player.getCooldowns().addCooldown(this, 120); // 6 seconds cooldown

        // Push player forward (dash)
        Vec3 look = player.getLookAngle();
        Vec3 dashDir = new Vec3(look.x, 0, look.z).normalize().scale(1.5D).add(0, 0.2D, 0);
        player.setDeltaMovement(dashDir);
        player.hurtMarked = true;

        if (!level.isClientSide) {
            ServerLevel sl = (ServerLevel) level;
            AABB area = player.getBoundingBox().expandTowards(look.scale(6.0D)).inflate(1.5D);
            java.util.List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && e.isAlive());

            int hits = 0;
            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().playerAttack(player), 7.0F);

                // Drain max health (reduce by 2.0 per hit)
                target.addEffect(new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PLATINUM_HEALTH_DRAIN),
                        300, 0)); // 15 seconds

                // Weakness II for 4 seconds (80 ticks)
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                // Slowness III for 4 seconds (80 ticks)
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));

                hits++;
            }

            if (hits > 0) {
                // Grant health boost to the player
                int currentAmp = -1;
                var boostHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PLATINUM_HEALTH_BOOST);
                if (player.hasEffect(boostHolder)) {
                    var effect = player.getEffect(boostHolder);
                    if (effect != null) {
                        currentAmp = effect.getAmplifier();
                    }
                }
                int newAmp = Math.min(9, currentAmp + hits); // Cap at level 10 (+20 Max Health)
                player.addEffect(new MobEffectInstance(boostHolder, 300, newAmp, false, true));

                // Heal the player for 2.0F (1 heart) per hit target
                player.heal(hits * 2.0F);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.4F);
            }

            // Sounds
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WIND_CHARGE_THROW, SoundSource.PLAYERS, 1.0F, 1.5F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);
        }

        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
