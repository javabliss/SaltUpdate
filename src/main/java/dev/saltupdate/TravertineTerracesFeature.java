package dev.saltupdate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

/**
 * Белые ступенчатые озёра, как в Памуккале: несколько бассейнов, спускающихся уступами.
 * Верхний бассейн самый высокий, каждый следующий ниже на блок и чуть шире.
 */
public class TravertineTerracesFeature extends Feature<NoneFeatureConfiguration> {
    public TravertineTerracesFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos origin = ctx.origin();

        int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        if (!level.getFluidState(new BlockPos(origin.getX(), ground - 1, origin.getZ())).isEmpty()) return false;

        int tiers = 2 + rand.nextInt(2);        // 2..3 бассейна
        int radius = 4 + rand.nextInt(2);       // 4..5 блоков
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
        int topWater = ground + tiers;

        BlockState salt = SaltUpdate.SALT_BLOCK.get().defaultBlockState();
        BlockState polished = SaltUpdate.POLISHED_SALT.get().defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        List<BlockPos> waters = new ArrayList<>();
        List<BlockPos> rims = new ArrayList<>();

        // от нижнего к верхнему, чтобы верхний бассейн не затирался
        for (int t = tiers - 1; t >= 0; t--) {
            int rad = radius + t;
            int cx = origin.getX() + dir.getStepX() * t * (radius - 1);
            int cz = origin.getZ() + dir.getStepZ() * t * (radius - 1);
            int waterY = topWater - t;

            for (int dx = -rad; dx <= rad; dx++) {
                for (int dz = -rad; dz <= rad; dz++) {
                    double d = Math.sqrt((double) (dx * dx + dz * dz)) / (rad + 0.5);
                    if (d > 1.0) continue;
                    int x = cx + dx, z = cz + dz;
                    int g = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                    boolean rim = d > 0.78;

                    // фундамент от земли до уровня воды
                    for (int y = Math.min(g, waterY) - 3; y < waterY; y++) {
                        set(level, new BlockPos(x, y, z), (y == waterY - 1 && !rim) ? polished : salt);
                    }
                    if (rim) {
                        set(level, new BlockPos(x, waterY, z), (rand.nextInt(3) == 0) ? polished : salt);
                        rims.add(new BlockPos(x, waterY + 1, z));
                    } else {
                        BlockPos wp = new BlockPos(x, waterY, z);
                        if (set(level, wp, water)) { level.scheduleTick(wp, Fluids.WATER, 0); waters.add(wp); }
                    }
                    // расчистка над бассейном
                    for (int y = waterY + 1; y <= Math.max(g, waterY + 1) + 2; y++) {
                        BlockPos p = new BlockPos(x, y, z);
                        if (!level.isEmptyBlock(p)) set(level, p, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        if (!waters.isEmpty()) {
            for (int i = 0; i < 2; i++) {
                BlockPos above = waters.get(rand.nextInt(waters.size())).above();
                if (level.isEmptyBlock(above)) set(level, above, Blocks.FROGSPAWN.defaultBlockState());
            }
            spawn(level, EntityType.TADPOLE, waters, rand, 2 + rand.nextInt(3));
            if (!rims.isEmpty()) spawn(level, EntityType.FROG, rims, rand, 1 + rand.nextInt(2));
        }
        return true;
    }

    private static void spawn(WorldGenLevel level, EntityType<? extends Mob> type, List<BlockPos> spots, RandomSource rand, int count) {
        for (int i = 0; i < count; i++) {
            BlockPos p = spots.get(rand.nextInt(spots.size()));
            if (!level.ensureCanWrite(p)) continue;
            Mob mob = type.create(level.getLevel());
            if (mob == null) continue;
            mob.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, rand.nextFloat() * 360F, 0F);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(p), MobSpawnType.CHUNK_GENERATION, null);
            mob.setPersistenceRequired();
            level.addFreshEntityWithPassengers(mob);
        }
    }

    private static boolean set(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!level.ensureCanWrite(pos)) return false;
        return level.setBlock(pos, state, 2);
    }
}
