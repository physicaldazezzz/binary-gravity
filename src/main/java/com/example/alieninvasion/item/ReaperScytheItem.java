package com.example.alieninvasion.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ReaperScytheItem extends HoeItem {
    private static final ThreadLocal<Boolean> APPLYING_BONUS = ThreadLocal.withInitial(() -> false);

    public ReaperScytheItem(Tier tier, Properties properties) {
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

        player.getCooldowns().addCooldown(this, 80); // 4 seconds cooldown

        if (!level.isClientSide) {
            ServerLevel sl = (ServerLevel) level;
            Vec3 look = player.getLookAngle().normalize();
            AABB area = player.getBoundingBox().inflate(3.5D);
            java.util.List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && e.isAlive());

            int hitCount = 0;
            for (LivingEntity target : targets) {
                Vec3 toTarget = target.position().subtract(player.position()).normalize();
                double dot = look.dot(toTarget);

                // Cone check (~120 degrees)
                if (dot > 0.2) {
                    target.hurt(level.damageSources().playerAttack(player), 5.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0)); // Wither I for 5 seconds

                    // Pull slightly towards player
                    Vec3 pull = player.position().subtract(target.position()).normalize().scale(0.4D);
                    target.setDeltaMovement(pull.x, 0.1D, pull.z);
                    target.hurtMarked = true;

                    hitCount++;
                }
            }

            if (hitCount > 0) {
                player.heal(hitCount * 1.0F); // Restore 0.5 heart per target
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.0F, 0.8F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 0.7F);

                // Spawn sweeping particles in front
                for (int i = 0; i < 5; i++) {
                    double angle = (i - 2) * 0.25D;
                    double cos = Math.cos(angle);
                    double sin = Math.sin(angle);
                    Vec3 rotated = new Vec3(look.x * cos - look.z * sin, look.y, look.x * sin + look.z * cos);
                    Vec3 pPos = player.getEyePosition(1.0F).add(rotated.scale(2.0D));
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, pPos.x, pPos.y, pPos.z, 1, 0.1D, 0.1D, 0.1D, 0.0D);
                }
            }
        }

        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, net.minecraft.world.level.block.state.BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide && miner instanceof Player player && state.getBlock() instanceof net.minecraft.world.level.block.CropBlock) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    BlockPos targetPos = pos.offset(x, 0, z);
                    net.minecraft.world.level.block.state.BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && crop.isMaxAge(targetState)) {
                        level.destroyBlock(targetPos, true, player);
                    }
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, miner);
    }
}
