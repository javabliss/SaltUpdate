package dev.saltupdate;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.PushReaction;
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

    public static final DeferredBlock<LizardEggBlock> LIZARD_EGG_BLOCK = block("lizard_egg",
            () -> new LizardEggBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.5F)
                    .sound(SoundType.METAL).randomTicks().noOcclusion().pushReaction(PushReaction.DESTROY)), false);

    private static BlockBehaviour.Properties clusterProps(int light) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER).mapColor(MapColor.SNOW).lightLevel(s -> light);
    }
    public static final DeferredBlock<AmethystClusterBlock> SALT_CLUSTER = block("salt_cluster", () -> new AmethystClusterBlock(7, 3, clusterProps(5)), false);
    public static final DeferredBlock<AmethystClusterBlock> LARGE_SALT_BUD = block("large_salt_bud", () -> new AmethystClusterBlock(5, 3, clusterProps(4)), false);
    public static final DeferredBlock<AmethystClusterBlock> MEDIUM_SALT_BUD = block("medium_salt_bud", () -> new AmethystClusterBlock(4, 3, clusterProps(2)), false);
    public static final DeferredBlock<AmethystClusterBlock> SMALL_SALT_BUD = block("small_salt_bud", () -> new AmethystClusterBlock(3, 4, clusterProps(1)), false);

    // ---------- Предметы ----------
    public static final DeferredItem<Item> SALT_CRYSTAL = ITEMS.registerSimpleItem("salt_crystal");
    /** Правый клик по снегу или льду плавит их в области. */
    public static final DeferredItem<SaltDustItem> SALT_DUST = ITEMS.register("salt_dust", () -> new SaltDustItem(new Item.Properties()));

    private static Item.Properties food(int nutrition, float sat) {
        return new Item.Properties().food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(sat).build());
    }

    public static final DeferredItem<Item> JERKY = ITEMS.registerSimpleItem("jerky", food(4, 0.8F));
    public static final DeferredItem<Item> PRETZEL = ITEMS.registerSimpleItem("pretzel", food(5, 0.6F));
    public static final DeferredItem<Item> SALTED_POTATO = ITEMS.registerSimpleItem("salted_potato", food(6, 0.7F));
    public static final DeferredItem<Item> FRIED_LIZARD_EGG = ITEMS.registerSimpleItem("fried_lizard_egg", food(5, 0.6F));

    // ---------- Генерация ----------
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MOD_ID);
    public static final DeferredHolder<Feature<?>, TravertineTerracesFeature> TERRACES =
            FEATURES.register("travertine_terraces", TravertineTerracesFeature::new);

    // ---------- Структура: дом повара ----------
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MOD_ID);
    public static final DeferredHolder<StructureType<?>, StructureType<CookHouseStructure>> COOK_HOUSE_TYPE =
            STRUCTURE_TYPES.register("cook_house", () -> (StructureType<CookHouseStructure>) () -> CookHouseStructure.CODEC);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, MOD_ID);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> COOK_HOUSE_PIECE =
            STRUCTURE_PIECES.register("cook_house", () -> (StructurePieceType.StructureTemplateType) CookHouseStructure.Piece::new);

    // ---------- Ящерица ----------
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<LizardEntity>> LIZARD = ENTITIES.register("lizard",
            () -> EntityType.Builder.of(LizardEntity::new, MobCategory.CREATURE)
                    .sized(0.7F, 0.4F).eyeHeight(0.3F).clientTrackingRange(8).build("lizard"));
    public static final DeferredItem<DeferredSpawnEggItem> LIZARD_EGG = ITEMS.register("lizard_spawn_egg",
            () -> new DeferredSpawnEggItem(LIZARD, 0x7a8a3e, 0x4e5c26, new Item.Properties()));

    // ---------- Торговец солью ----------
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MOD_ID);
    public static final DeferredHolder<PoiType, PoiType> SALT_POI = POI_TYPES.register("cook",
            () -> new PoiType(ImmutableSet.copyOf(CHISELED_SALT.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, MOD_ID);
    public static final DeferredHolder<VillagerProfession, VillagerProfession> COOK = PROFESSIONS.register("cook",
            () -> new VillagerProfession("cook", h -> h.is(SALT_POI.getId()), h -> h.is(SALT_POI.getId()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_BUTCHER));

    private static void trades(VillagerTradesEvent e) {
        if (e.getType() != COOK.get()) return;
        var t = e.getTrades();
        t.get(1).add(new BasicItemListing(new ItemStack(Items.POTATO, 20), new ItemStack(Items.EMERALD), 16, 2, 0.05F));
        t.get(1).add(new BasicItemListing(1, new ItemStack(Items.BREAD, 4), 16, 1, 0.05F));
        t.get(1).add(new BasicItemListing(1, new ItemStack(JERKY.get(), 4), 12, 1, 0.05F));
        t.get(1).add(new BasicItemListing(1, new ItemStack(SALT_DUST.get(), 6), 16, 1, 0.05F));
        t.get(2).add(new BasicItemListing(new ItemStack(SALT_CRYSTAL.get(), 12), new ItemStack(Items.EMERALD), 16, 5, 0.05F));
        t.get(2).add(new BasicItemListing(2, new ItemStack(Items.COOKED_BEEF, 4), 12, 5, 0.05F));
        t.get(2).add(new BasicItemListing(1, new ItemStack(Items.COOKED_CHICKEN, 4), 12, 5, 0.05F));
        t.get(2).add(new BasicItemListing(1, new ItemStack(PRETZEL.get(), 4), 12, 5, 0.05F));
        t.get(3).add(new BasicItemListing(2, new ItemStack(SALTED_POTATO.get(), 4), 12, 10, 0.05F));
        t.get(3).add(new BasicItemListing(1, new ItemStack(Items.PUMPKIN_PIE, 2), 12, 10, 0.05F));
        t.get(3).add(new BasicItemListing(2, new ItemStack(Items.COOKIE, 12), 12, 10, 0.05F));
        t.get(4).add(new BasicItemListing(3, new ItemStack(Items.CAKE), 6, 15, 0.05F));
        t.get(4).add(new BasicItemListing(3, new ItemStack(Items.GOLDEN_CARROT, 3), 8, 15, 0.05F));
        t.get(5).add(new BasicItemListing(4, new ItemStack(Items.HONEY_BOTTLE, 3), 8, 30, 0.05F));
        t.get(5).add(new BasicItemListing(6, new ItemStack(SALTED_POTATO.get(), 12), 8, 30, 0.05F));
    }

    public SaltUpdate(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        FEATURES.register(modBus);
        ENTITIES.register(modBus);
        STRUCTURE_TYPES.register(modBus);
        STRUCTURE_PIECES.register(modBus);
        POI_TYPES.register(modBus);
        PROFESSIONS.register(modBus);
        modBus.addListener((EntityAttributeCreationEvent e) -> e.put(LIZARD.get(), LizardEntity.createAttributes().build()));
        modBus.addListener((RegisterSpawnPlacementsEvent e) -> e.register(LIZARD.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LizardEntity::checkSpawn, RegisterSpawnPlacementsEvent.Operation.OR));
        NeoForge.EVENT_BUS.addListener(SaltUpdate::trades);
        modBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(SaltWorldgen::register));
        modBus.addListener(SaltUpdate::addToVanillaTabs);
    }

    private static void addToVanillaTabs(BuildCreativeModeTabContentsEvent e) {
        var tab = e.getTabKey();
        if (tab == CreativeModeTabs.NATURAL_BLOCKS) {
            e.accept(SALT_ORE);
            e.accept(DEEPSLATE_SALT_ORE);
            e.accept(LIZARD_EGG_BLOCK);
            e.accept(SMALL_SALT_BUD);
            e.accept(MEDIUM_SALT_BUD);
            e.accept(LARGE_SALT_BUD);
            e.accept(SALT_CLUSTER);
        } else if (tab == CreativeModeTabs.BUILDING_BLOCKS) {
            BUILDING.forEach(e::accept);
        } else if (tab == CreativeModeTabs.INGREDIENTS) {
            e.accept(SALT_CRYSTAL);
            e.accept(SALT_DUST);
        } else if (tab == CreativeModeTabs.SPAWN_EGGS) {
            e.accept(LIZARD_EGG);
        } else if (tab == CreativeModeTabs.FOOD_AND_DRINKS) {
            e.accept(JERKY);
            e.accept(PRETZEL);
            e.accept(SALTED_POTATO);
            e.accept(FRIED_LIZARD_EGG);
        }
    }
}
