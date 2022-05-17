package re.zarex.simplechunkloader.mixin;

import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import re.zarex.simplechunkloader.SimpleChunkLoader;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {
    @Inject(method = "shouldTick", at = @At("HEAD"), cancellable = true)
    private void shouldTick(ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) {
        if (SimpleChunkLoader.FORCELOADEDCHUNKS.contains(chunkPos.toLong())) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
