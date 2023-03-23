package re.zarex.simplechunkloader.mixin;


import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import re.zarex.simplechunkloader.SimpleChunkLoader;

import java.util.Optional;
import net.minecraft.util.math.random.Random;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {


    @Invoker("pickRandomSpawnEntry")
    static Optional<SpawnSettings.SpawnEntry> pickRandomSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, Random random, BlockPos pos) {
        throw new AssertionError();
    }


    @Invoker("canSpawn")
    private static boolean canSpawn(ServerWorld world, SpawnGroup group, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable pos, double squaredDistance)  {
        throw new AssertionError();
    }

    @Invoker("createMob")
    static MobEntity createMob(ServerWorld world, EntityType<?> type) {
        throw new AssertionError();
    }

    @Invoker("isValidSpawn")
    static boolean isValidSpawn(ServerWorld world, MobEntity entity, double squaredDistance) {
        throw new AssertionError();
    }

    @Inject(method = "Lnet/minecraft/world/SpawnHelper;spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V", at = @At(value = "HEAD"), cancellable = true)
    private static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo cir) {
        if (SimpleChunkLoader.containsChunks(world.getRegistryKey().getValue().toString(), chunk.getPos().toLong())) {
            StructureAccessor structureAccessor = world.getStructureAccessor();
            ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
            int posY = pos.getY();
            BlockState blockState = chunk.getBlockState(pos);
            if (blockState.isSolidBlock(chunk, pos)) {
                cir.cancel();
                return;
            }
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            int mobCount = 0;
            block0: for (int i = 0; i < 3; ++i) {
                int posX = pos.getX();
                int posZ = pos.getZ();
                SpawnSettings.SpawnEntry spawnEntry = null;
                EntityData entityData = null;
                int spawnCount = MathHelper.ceil(world.random.nextFloat() * 4.0f);
                int spawnedCount = 0;
                for (int q = 0; q < spawnCount; ++q) {
                    double fakeDistance = 1;
                    mutable.set(posX += world.random.nextInt(6) - world.random.nextInt(6), posY, posZ += world.random.nextInt(6) - world.random.nextInt(6));
                    double playerPosX = (double)posX + 0.5;
                    double playerPosY = (double)posZ + 0.5;

                    PlayerEntity playerEntity = world.getClosestPlayer(playerPosX, (double)posY, playerPosY, -1.0, false);
                    if (playerEntity != null)
                    {
                        if (playerEntity.squaredDistanceTo(playerPosX, posY, playerPosY) <= 576.0)
                            continue;
                    }

                    if (world.getSpawnPos().isWithinDistance(new Vec3d((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5), 24.0)) {
                        continue;
                    }

                    if (spawnEntry == null) {
                        Optional<SpawnSettings.SpawnEntry> optional = pickRandomSpawnEntry(world, structureAccessor, chunkGenerator, group, world.random, mutable);
                        if (optional.isEmpty()) continue block0;
                        spawnEntry = optional.get();
                        spawnCount = spawnEntry.minGroupSize + world.random.nextInt(1 + spawnEntry.maxGroupSize - spawnEntry.minGroupSize);
                    }

                    if (!canSpawn(world, group, structureAccessor, chunkGenerator, spawnEntry, mutable, 1))
                        continue;

                    if (!checker.test(spawnEntry.type, mutable, chunk))
                        continue;

                    MobEntity mobEntity = createMob(world, spawnEntry.type);
                    if (mobEntity == null) {
                        cir.cancel();
                        return;
                    }

                    if (mobEntity instanceof BatEntity) {
                        cir.cancel();
                        return;
                    }

                    mobEntity.refreshPositionAndAngles(playerPosX, posY, playerPosY, world.random.nextFloat() * 360.0f, 0.0f);
                    if (!isValidSpawn(world, mobEntity, fakeDistance))
                        continue;
                    entityData = mobEntity.initialize(world, world.getLocalDifficulty(mobEntity.getBlockPos()), SpawnReason.NATURAL, entityData, null);
                    ++spawnedCount;
                    world.spawnEntityAndPassengers(mobEntity);
                    runner.run(mobEntity, chunk);
                    if (++mobCount >= mobEntity.getLimitPerChunk()) {
                        cir.cancel();
                        return;
                    }
                    if (mobEntity.spawnsTooManyForEachTry(spawnedCount)) continue block0;
                }
            }
            cir.cancel();
        }
    }

}
