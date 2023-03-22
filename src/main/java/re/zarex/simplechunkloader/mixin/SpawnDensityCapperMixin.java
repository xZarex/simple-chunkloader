package re.zarex.simplechunkloader.mixin;


import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.SpawnDensityCapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import re.zarex.simplechunkloader.SimpleChunkLoader;
import re.zarex.simplechunkloader.ThreadedAnvilChunkStorageAccessor;

import java.util.HashMap;
import java.util.Map;

@Mixin(SpawnDensityCapper.class)
public class SpawnDensityCapperMixin {

    @Shadow
    private final ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

    public SpawnDensityCapperMixin()
    {
        threadedAnvilChunkStorage = null;
    }
    static class DensityCap {
        private final Object2IntMap<SpawnGroup> spawnGroupsToDensity = new Object2IntOpenHashMap<SpawnGroup>(SpawnGroup.values().length);

        DensityCap() {
        }

        public void increaseDensity(SpawnGroup spawnGroup) {
            this.spawnGroupsToDensity.computeInt(spawnGroup, (group, density) -> density == null ? 1 : density + 1);
        }

        public boolean canSpawn(SpawnGroup spawnGroup) {
            return this.spawnGroupsToDensity.getOrDefault((Object)spawnGroup, 0) < spawnGroup.getCapacity();
        }
    }

    private Map<ChunkPos, DensityCap> chunkPosDensityCapMap;


    @Inject(method = "Lnet/minecraft/world/SpawnDensityCapper;<init>(Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;)V", at = @At(value = "TAIL"))
    private void SpawnDensityCapper(ThreadedAnvilChunkStorage threadedAnvilChunkStorage, CallbackInfo cir) {
        chunkPosDensityCapMap = new HashMap<>();
    }

    @Inject(method = "increaseDensity", at = @At(value = "HEAD"), cancellable = true)
    private void increaseDensity(ChunkPos chunkPos, SpawnGroup spawnGroup, CallbackInfo cir) {
        if (SimpleChunkLoader.containsChunks(((ThreadedAnvilChunkStorageAccessor)(threadedAnvilChunkStorage)).getHookedWorld().getRegistryKey().getValue().toString(), chunkPos.toLong())) {
            this.chunkPosDensityCapMap.computeIfAbsent(chunkPos, pos -> new DensityCap()).increaseDensity(spawnGroup);
            cir.cancel();
        }
    }

    @Inject(method = "canSpawn", at = @At(value = "HEAD"), cancellable = true)
    private void canSpawn(SpawnGroup spawnGroup, ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) {
        if (SimpleChunkLoader.containsChunks(((ThreadedAnvilChunkStorageAccessor)(Object)threadedAnvilChunkStorage).getHookedWorld().getRegistryKey().getValue().toString(), chunkPos.toLong())) {
            DensityCap densityCap = this.chunkPosDensityCapMap.get(chunkPos);
            if (densityCap != null && densityCap.canSpawn(spawnGroup))
                cir.setReturnValue(true);
            else
                cir.setReturnValue(false);
            cir.cancel();
        }
    }


}
