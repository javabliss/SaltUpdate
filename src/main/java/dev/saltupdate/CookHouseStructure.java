package dev.saltupdate;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

/** Дом повара. Не появляется на воде и на неровной земле. */
public class CookHouseStructure extends Structure {
    public static final MapCodec<CookHouseStructure> CODEC = simpleCodec(CookHouseStructure::new);
    private static final ResourceLocation TEMPLATE = ResourceLocation.fromNamespaceAndPath(SaltUpdate.MOD_ID, "cook_house");

    public CookHouseStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        ChunkPos cp = ctx.chunkPos();
        int cx = cp.getMiddleBlockX(), cz = cp.getMiddleBlockZ();
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int dx = -8; dx <= 8; dx += 4) {
            for (int dz = -8; dz <= 8; dz += 4) {
                int x = cx + dx, z = cz + dz;
                int h = ctx.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, ctx.heightAccessor(), ctx.randomState());
                NoiseColumn column = ctx.chunkGenerator().getBaseColumn(x, z, ctx.heightAccessor(), ctx.randomState());
                BlockState top = column.getBlock(h - 1);
                if (!top.getFluidState().isEmpty()) return Optional.empty(); // вода: дом не ставим
                min = Math.min(min, h);
                max = Math.max(max, h);
            }
        }
        if (max - min > 4) return Optional.empty();
        Rotation rot = Rotation.getRandom(ctx.random());
        BlockPos pos = new BlockPos(cx - 7, min - 5, cz - 6);
        return Optional.of(new GenerationStub(pos, builder ->
                builder.addPiece(new Piece(ctx.structureTemplateManager(), pos, rot))));
    }

    @Override
    public StructureType<?> type() {
        return SaltUpdate.COOK_HOUSE_TYPE.get();
    }

    public static class Piece extends TemplateStructurePiece {
        public Piece(StructureTemplateManager manager, BlockPos pos, Rotation rot) {
            super(SaltUpdate.COOK_HOUSE_PIECE.get(), 0, manager, TEMPLATE, TEMPLATE.toString(), settings(rot), pos);
        }

        public Piece(StructureTemplateManager manager, CompoundTag tag) {
            super(SaltUpdate.COOK_HOUSE_PIECE.get(), tag, manager, id -> settings(Rotation.valueOf(tag.getString("Rot"))));
        }

        private static StructurePlaceSettings settings(Rotation rot) {
            return new StructurePlaceSettings().setRotation(rot).setMirror(Mirror.NONE).setRotationPivot(new BlockPos(7, 0, 6));
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
            super.addAdditionalSaveData(ctx, tag);
            tag.putString("Rot", placeSettings.getRotation().name());
        }

        @Override
        protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
        }
    }
}
