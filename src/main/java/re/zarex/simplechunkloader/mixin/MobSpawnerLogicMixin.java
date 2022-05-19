package re.zarex.simplechunkloader.mixin;


import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import re.zarex.simplechunkloader.SimpleChunkLoader;

import java.util.Optional;
import java.util.Random;

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
