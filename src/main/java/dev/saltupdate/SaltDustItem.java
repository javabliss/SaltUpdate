package dev.saltupdate;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Соляная пыль: правый клик по снегу или льду плавит их в области 5x5. */
public class SaltDustItem extends Item {
    private static final int RADIUS = 2;

    public SaltDustItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos center = ctx.getClickedPos();
        if (melt(level.getBlockState(center)) == null) return InteractionResult.PASS;

        if (!level.isClientSide) {
            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (dx * dx + dz * dz > 6) continue; // скруглённая область
                    for (int dy = -2; dy <= 2; dy++) {
                        BlockPos p = center.offset(dx, dy, dz);
                        BlockState result = melt(level.getBlockState(p));
                        if (result != null) level.setBlock(p, result, 3);
                    }
                }
            }
            level.playSound(null, center, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.4F);
            Player player = ctx.getPlayer();
            if (player != null) {
                player.getCooldowns().addCooldown(this, 10);
                if (!player.getAbilities().instabuild) ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Соль вредна слизням: правый клик по слайму или магмовому кубу наносит урон. */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Slime)) return InteractionResult.PASS;
        if (!player.level().isClientSide) {
            target.hurt(player.damageSources().playerAttack(player), 4.0F);
            player.level().playSound(null, target.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6F, 1.6F);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    /** Во что превращается блок, либо null, если соль на него не действует. */
    private static BlockState melt(BlockState s) {
        Block b = s.getBlock();
        if (b instanceof SnowLayerBlock || b == Blocks.SNOW_BLOCK || b == Blocks.POWDER_SNOW) return Blocks.AIR.defaultBlockState();
        if (b == Blocks.ICE) return Blocks.WATER.defaultBlockState();
        if (b == Blocks.PACKED_ICE) return Blocks.ICE.defaultBlockState();
        if (b == Blocks.BLUE_ICE) return Blocks.PACKED_ICE.defaultBlockState();
        return null;
    }
}
