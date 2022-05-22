package re.zarex.simplechunkloader.blocks;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import re.zarex.simplechunkloader.blocks.entities.ChunkLoaderEntity;
import re.zarex.simplechunkloader.blocks.entities.renderer.ChunkLoaderEntityRenderer;
import re.zarex.simplechunkloader.gui.ChunkLoaderBlockScreen;
import re.zarex.simplechunkloader.gui.ChunkLoaderGuiDescription;

public class ChunkLoader extends BlockWithEntity {
    public static Block BLOCK;
    public static Item WORLDITEM;
    public static BlockEntityType<ChunkLoaderEntity> ENTITY_TYPE;
    public static ScreenHandlerType<ChunkLoaderGuiDescription> SCREEN_HANDLER_TYPE;

    protected ChunkLoader(Settings settings) {
        super(settings);
    }

    public static void Register() {
        WORLDITEM = Registry.register(Registry.ITEM, new Identifier("simplechunkloader", "world"), new Item(new FabricItemSettings()));
        BLOCK = new ChunkLoader(FabricBlockSettings.of(Material.METAL).strength(1.0f).requiresTool().nonOpaque());
        Identifier ID = new Identifier("simplechunkloader", "chunkloader");
        Registry.register(Registry.BLOCK, ID, BLOCK);
        Registry.register(Registry.ITEM, ID, new ChunkLoaderItem(BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, "simplechunkloader:chunkloader_entity", FabricBlockEntityTypeBuilder.create(ChunkLoaderEntity::new, BLOCK).build(null));
        SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(ID, (syncId, inventory, buf) -> new ChunkLoaderGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY, buf.readBlockPos(), buf.readInt()));
    }


    public static void RegisterClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(BLOCK, RenderLayer.getCutout());
        ScreenRegistry.<ChunkLoaderGuiDescription, ChunkLoaderBlockScreen>register(SCREEN_HANDLER_TYPE, (gui, inventory, title) -> new ChunkLoaderBlockScreen(gui, inventory.player, title));
        BlockEntityRendererRegistry.INSTANCE.register(ENTITY_TYPE, ChunkLoaderEntityRenderer::new);
    }



    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need to change that!
        return BlockRenderType.MODEL;
    }


    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChunkLoaderEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.isClient)
            return;

        BlockEntity be = world.getBlockEntity(pos);
        if (be == null)
            return;

        ((ChunkLoaderEntity)be).onPlaced(world, pos, state);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player)
    {
        if (world.isClient)
        {
            super.onBreak(world, pos, state, player);
            return;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (be == null)
            return;

        ((ChunkLoaderEntity)be).onBreak(world, pos, state);
        super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient())
            return ActionResult.PASS;
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        return ActionResult.SUCCESS;
    }
}
