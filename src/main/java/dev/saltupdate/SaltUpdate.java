package dev.saltupdate;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Соляное обновление (Salt Update). */
@Mod(SaltUpdate.MOD_ID)
public class SaltUpdate {
    public static final String MOD_ID = "saltupdate";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    private static final List<DeferredItem<BlockItem>> BUILDING = new ArrayList<>();

    private static BlockBehaviour.Properties saltProps() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .mapColor(MapColor.SNOW).sound(SoundType.CALCITE).strength(1.5F, 6.0F).requiresCorrectToolForDrops();
    }

    private static <T extends Block> DeferredBlock<T> block(String name, Supplier<T> s, boolean building) {
        DeferredBlock<T> b = BLOCKS.register(name, s);
        DeferredItem<BlockItem> i = ITEMS.registerSimpleBlockItem(b);
        if (building) BUILDING.add(i);
        return b;
    }

    // ---------- Руды ----------
    public static final DeferredBlock<DropExperienceBlock> SALT_ORE = block("salt_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE)), false);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_SALT_ORE = block("deepslate_salt_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COAL_ORE)), false);

    // ---------- Строительные блоки ----------
    public static final DeferredBlock<Block> SALT_BLOCK = block("salt_block", () -> new Block(saltProps()), true);
    public static final DeferredBlock<StairBlock> SALT_STAIRS = block("salt_stairs",
            () -> new StairBlock(SALT_BLOCK.get().defaultBlockState(), saltProps()), true);
    public static final DeferredBlock<SlabBlock> SALT_SLAB = block("salt_slab", () -> new SlabBlock(saltProps()), true);
    public static final DeferredBlock<WallBlock> SALT_WALL = block("salt_wall", () -> new WallBlock(saltProps()), true);

    public static final DeferredBlock<Block> POLISHED_SALT = block("polished_salt", () -> new Block(saltProps()), true);
    public static final DeferredBlock<StairBlock> POLISHED_SALT_STAIRS = block("polished_salt_stairs",
            () -> new StairBlock(POLISHED_SALT.get().defaultBlockState(), saltProps()), true);
    public static final DeferredBlock<SlabBlock> POLISHED_SALT_SLAB = block("polished_salt_slab", () -> new SlabBlock(saltProps()), true);
    public static final DeferredBlock<WallBlock> POLISHED_SALT_WALL = block("polished_salt_wall", () -> new WallBlock(saltProps()), true);

    public static final DeferredBlock<Block> SALT_BRICKS = block("salt_bricks", () -> new Block(saltProps()), true);
    public static final DeferredBlock<StairBlock> SALT_BRICK_STAIRS = block("salt_brick_stairs",
            () -> new StairBlock(SALT_BRICKS.get().defaultBlockState(), saltProps()), true);
    public static final DeferredBlock<SlabBlock> SALT_BRICK_SLAB = block("salt_brick_slab", () -> new SlabBlock(saltProps()), true);
    public static final DeferredBlock<WallBlock> SALT_BRICK_WALL = block("salt_brick_wall", () -> new WallBlock(saltProps()), true);

    public static final DeferredBlock<Block> CHISELED_SALT = block("chiseled_salt", () -> new Block(saltProps()), true);

    // ---------- Предметы ----------
    public static final DeferredItem<Item> SALT_CRYSTAL = ITEMS.registerSimpleItem("salt_crystal");
    /** Правый клик по снегу или льду плавит их в области. */
    public static final DeferredItem<SaltDustItem> SALT_DUST = ITEMS.register("salt_dust", () -> new SaltDustItem(new Item.Properties()));

    private static Item.Properties food(int nutrition, float sat) {
        return new Item.Properties().food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(sat).build());
    }

    public static final DeferredItem<Item> JERKY = ITEMS.registerSimpleItem("jerky", food(4, 0.8F));
    public static final DeferredItem<Item> PRETZEL = ITEMS.registerSimpleItem("pretzel", food(5, 0.6F));
    public static final DeferredItem<Item> SALTED_FISH = ITEMS.registerSimpleItem("salted_fish", food(5, 0.8F));
    public static final DeferredItem<Item> SALTED_POTATO = ITEMS.registerSimpleItem("salted_potato", food(6, 0.7F));

    // ---------- Генерация ----------
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MOD_ID);
    public static final DeferredHolder<Feature<?>, TravertineTerracesFeature> TERRACES =
            FEATURES.register("travertine_terraces", TravertineTerracesFeature::new);

    public SaltUpdate(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        FEATURES.register(modBus);
        modBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(SaltWorldgen::register));
        modBus.addListener(SaltUpdate::addToVanillaTabs);
    }

    private static void addToVanillaTabs(BuildCreativeModeTabContentsEvent e) {
        var tab = e.getTabKey();
        if (tab == CreativeModeTabs.NATURAL_BLOCKS) {
            e.accept(SALT_ORE);
            e.accept(DEEPSLATE_SALT_ORE);
        } else if (tab == CreativeModeTabs.BUILDING_BLOCKS) {
            BUILDING.forEach(e::accept);
        } else if (tab == CreativeModeTabs.INGREDIENTS) {
            e.accept(SALT_CRYSTAL);
            e.accept(SALT_DUST);
        } else if (tab == CreativeModeTabs.FOOD_AND_DRINKS) {
            e.accept(JERKY);
            e.accept(PRETZEL);
            e.accept(SALTED_FISH);
            e.accept(SALTED_POTATO);
        }
    }
}
