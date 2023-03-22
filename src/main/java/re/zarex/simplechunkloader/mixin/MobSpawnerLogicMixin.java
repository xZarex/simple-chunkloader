package re.zarex.simplechunkloader.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import re.zarex.simplechunkloader.SimpleChunkLoader;

@Mixin(MobSpawnerLogic.class)
public class MobSpawnerLogicMixin {


    @Inject(method = "isPlayerInRange", at = @At(value = "HEAD"), cancellable = true)
    private void isPlayerInRange(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (world instanceof ServerWorld)
        {
            if (SimpleChunkLoader.containsChunks(((ServerWorld)world).getRegistryKey().getValue().toString(), ((ServerWorld)world).getChunk(pos).getPos().toLong())) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }


}
