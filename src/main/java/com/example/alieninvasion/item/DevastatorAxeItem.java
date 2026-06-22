package com.example.alieninvasion.item;

import com.example.alieninvasion.logic.BleedManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DevastatorAxeItem extends AxeItem {
    private static final ThreadLocal<Boolean> APPLYING_BONUS = ThreadLocal.withInitial(() -> false);

    public DevastatorAxeItem(Tier tier, Properties properties) {
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

            // Critical strike check
            if (player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater()) {
                BleedManager.wound(target, player.damageSources().playerAttack(player), 8.0F);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0D, target.getZ(), 15, 0.2D, 0.2D, 0.2D, 0.1D);
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

        if (!level.isClientSide) {
            ServerLevel sl = (ServerLevel) level;
            AABB area = player.getBoundingBox().inflate(4.0D);
            java.util.List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && e.isAlive());

            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().mobAttack(player), 6.0F);
                BleedManager.wound(target, player.damageSources().playerAttack(player), 6.0F);

                // Knock back away from player
                double dx = target.getX() - player.getX();
                double dz = target.getZ() - player.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0.1) {
                    target.setDeltaMovement(dx / dist * 0.8D, 0.4D, dz / dist * 0.8D);
                    target.hurtMarked = true;
                }
            }

            // Ground smash particles
            BlockPos below = player.blockPosition().below();
            var belowState = level.getBlockState(below);
            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, belowState),
                    player.getX(), player.getY(), player.getZ(), 40, 2.0D, 0.2D, 2.0D, 0.1D);

            // Sounds
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 1.4F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 1.0F, 0.8F);
        }

        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
