package re.zarex.simplechunkloader.blocks.entities.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import re.zarex.simplechunkloader.blocks.ChunkLoader;
import re.zarex.simplechunkloader.blocks.entities.ChunkLoaderEntity;

@Environment(EnvType.CLIENT)
public class ChunkLoaderEntityRenderer implements BlockEntityRenderer<ChunkLoaderEntity> {

    private static ItemStack stack = new ItemStack(ChunkLoader.WORLDITEM, 1);

    public ChunkLoaderEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(ChunkLoaderEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        double offset = Math.sin((entity.getWorld().getTime() + tickDelta) / 8.0) / 8.0;
        matrices.translate(0.5, 1.0 + offset, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((entity.getWorld().getTime() + tickDelta) * 2));

        int lightAbove = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.GROUND, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);

        matrices.pop();
    }
}
