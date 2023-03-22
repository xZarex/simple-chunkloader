package re.zarex.simplechunkloader.mixin;

import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import re.zarex.simplechunkloader.SimpleChunkLoader;
import re.zarex.simplechunkloader.ThreadedAnvilChunkStorageAccessor;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin implements ThreadedAnvilChunkStorageAccessor {



    private ServerWorld hookedWorld;


    @Inject(method = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/structure/StructureTemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/util/thread/ThreadExecutor;Lnet/minecraft/world/chunk/ChunkProvider;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/server/WorldGenerationProgressListener;Lnet/minecraft/world/chunk/ChunkStatusChangeListener;Ljava/util/function/Supplier;IZ)V", at = @At(value = "TAIL"))
    private void ThreadedAnvilChunkStorage(ServerWorld world, LevelStorage.Session session, com.mojang.datafixers.DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, ThreadExecutor<Runnable> mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier<PersistentStateManager> persistentStateManagerFactory, int viewDistance, boolean dsync, CallbackInfo cir) {
        hookedWorld = world;
    }

    @Override
    public ServerWorld getHookedWorld() {
        return hookedWorld;
    }

    @Inject(method = "shouldTick", at = @At("HEAD"), cancellable = true)
    private void shouldTick(ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) {
        if (SimpleChunkLoader.containsChunks(hookedWorld.getRegistryKey().getValue().toString(), chunkPos.toLong())) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
