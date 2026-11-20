package dev.saltupdate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Небольшая пустынная ящерица (игуана). Спокойная, убегает от опасности, разводится пауками-глазами. */
public class LizardEntity extends Animal {
    private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(LizardEntity.class, EntityDataSerializers.BOOLEAN);

    public LizardEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.22);
    }

    public static boolean checkSpawn(EntityType<LizardEntity> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        var below = level.getBlockState(pos.below());
        return (below.is(SaltUpdate.SALT_BLOCK.get()) || below.is(BlockTags.SAND)) && level.getRawBrightness(pos, 0) > 8;
    }

    public boolean hasEgg() {
        return entityData.get(HAS_EGG);
    }

    public void setHasEgg(boolean value) {
        entityData.set(HAS_EGG, value);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_EGG, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HasEgg", hasEgg());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setHasEgg(tag.getBoolean("HasEgg"));
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && !hasEgg();
    }

    /** Вместо детёныша самка носит яйцо и затем откладывает кладку. */
    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        setHasEgg(true);
        resetLove();
        mate.resetLove();
        level.addFreshEntity(new ExperienceOrb(level, getX(), getY(), getZ(), getRandom().nextInt(7) + 1));
    }

    private static class LayEggGoal extends MoveToBlockGoal {
        private final LizardEntity lizard;

        LayEggGoal(LizardEntity lizard, double speed) {
            super(lizard, speed, 16);
            this.lizard = lizard;
        }

        @Override
        public boolean canUse() {
            return lizard.hasEgg() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return lizard.hasEgg() && super.canContinueToUse();
        }

        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            BlockState below = level.getBlockState(pos);
            return level.isEmptyBlock(pos.above()) && (below.is(SaltUpdate.SALT_BLOCK.get()) || below.is(BlockTags.SAND));
        }

        @Override
        public void tick() {
            super.tick();
            if (lizard.isInWater() || !isReachedTarget()) return;
            Level level = lizard.level();
            BlockPos above = blockPos.above();
            if (!level.isEmptyBlock(above)) return;
            level.playSound(null, blockPos, net.minecraft.sounds.SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F);
            level.setBlock(above, SaltUpdate.LIZARD_EGG_BLOCK.get().defaultBlockState()
                    .setValue(LizardEggBlock.EGGS, 1 + lizard.getRandom().nextInt(3)), 3);
            lizard.setHasEgg(false);
            lizard.setAge(6000);
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        goalSelector.addGoal(2, new LayEggGoal(this, 1.0));
        goalSelector.addGoal(3, new TemptGoal(this, 1.1, Ingredient.of(Items.SPIDER_EYE), false));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SPIDER_EYE);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return SaltUpdate.LIZARD.get().create(level);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CAT_HISS;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FROG_DEATH;
    }
}
