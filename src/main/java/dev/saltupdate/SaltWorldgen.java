package dev.saltupdate;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

/** Регистрация биома «Соляные равнины» через TerraBlender. */
public class SaltWorldgen {
    public static final ResourceKey<Biome> SALT_FLATS = ResourceKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(SaltUpdate.MOD_ID, "salt_flats"));

    /** Регион заменяет часть пустынь соляными равнинами. */
    public static class SaltRegion extends Region {
        public SaltRegion(ResourceLocation name, int weight) {
            super(name, RegionType.OVERWORLD, weight);
        }

        @Override
        public void addBiomes(net.minecraft.core.Registry<Biome> registry,
                              java.util.function.Consumer<com.mojang.datafixers.util.Pair<net.minecraft.world.level.biome.Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
            this.addModifiedVanillaOverworldBiomes(mapper, builder -> builder.replaceBiome(Biomes.DESERT, SALT_FLATS));
        }
    }

    public static void register() {
        Regions.register(new SaltRegion(ResourceLocation.fromNamespaceAndPath(SaltUpdate.MOD_ID, "overworld"), 3));

        SurfaceRules.RuleSource salt = SurfaceRules.state(SaltUpdate.SALT_BLOCK.get().defaultBlockState());
        SurfaceRules.RuleSource rule = SurfaceRules.ifTrue(SurfaceRules.isBiome(SALT_FLATS),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, salt),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, salt)));
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, SaltUpdate.MOD_ID, rule);
    }
}
