package re.zarex.simplechunkloader.mixin;

import net.minecraft.server.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import re.zarex.simplechunkloader.SimpleChunkLoader;

@Mixin(ChunkTicketManager.class)
public class ChunkTicketManagerMixin {
    @Inject(method = "shouldTick", at = @At("HEAD"), cancellable = true)
    private void shouldTick(long chunkPos, CallbackInfoReturnable<Boolean> cir) {
        if (SimpleChunkLoader.FORCELOADEDCHUNKS.contains(chunkPos)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "shouldTickEntities", at = @At("HEAD"), cancellable = true)
    private void shouldTickEntities(long chunkPos, CallbackInfoReturnable<Boolean> cir) {
        if (SimpleChunkLoader.FORCELOADEDCHUNKS.contains(chunkPos)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "shouldTickBlocks", at = @At("HEAD"), cancellable = true)
    private void shouldTickBlocks(long chunkPos, CallbackInfoReturnable<Boolean> cir) {
        if (SimpleChunkLoader.FORCELOADEDCHUNKS.contains(chunkPos)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
