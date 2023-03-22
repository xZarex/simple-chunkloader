package re.zarex.simplechunkloader.mixin;

import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import re.zarex.simplechunkloader.SimpleChunkLoader;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(method = "Lnet/minecraft/server/world/ServerWorld;shouldTick(Lnet/minecraft/util/math/ChunkPos;)Z", at = @At("HEAD"), cancellable = true)
    private void shouldTick(ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld serverWorld = (ServerWorld)(Object)this;
        if (SimpleChunkLoader.containsChunks(serverWorld.getRegistryKey().getValue().toString(), chunkPos.toLong())) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "Lnet/minecraft/server/world/ServerWorld;shouldTickEntity(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void shouldTickEntity(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld serverWorld = (ServerWorld)(Object)this;
        if (SimpleChunkLoader.containsChunks(serverWorld.getRegistryKey().getValue().toString(), serverWorld.getChunk(pos).getPos().toLong())) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "Lnet/minecraft/server/world/ServerWorld;shouldTick(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void shouldTick(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld serverWorld = (ServerWorld)(Object)this;
        if (SimpleChunkLoader.containsChunks(serverWorld.getRegistryKey().getValue().toString(), serverWorld.getChunk(pos).getPos().toLong())) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "Lnet/minecraft/world/World;shouldTickBlocksInChunk(J)Z", at = @At("HEAD"), cancellable = true)
    private void shouldTickBlocksInChunk(long chunkPos, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld serverWorld = (ServerWorld)(Object)this;
        if (SimpleChunkLoader.containsChunks(serverWorld.getRegistryKey().getValue().toString(), chunkPos)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Redirect(method = "Lnet/minecraft/server/world/ServerWorld;method_31420(Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager;shouldTickEntities(J)Z"))
    private boolean injectedChunkManagerCall(ChunkTicketManager chunkTicketManager, long chunkPos) {
        ServerWorld serverWorld = (ServerWorld)(Object)this;
        if (SimpleChunkLoader.containsChunks(serverWorld.getRegistryKey().getValue().toString(), chunkPos))
            return true;

        return chunkTicketManager.shouldTickEntities(chunkPos);
    }
}
