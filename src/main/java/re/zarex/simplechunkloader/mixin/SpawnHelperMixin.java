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
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import re.zarex.simplechunkloader.SimpleChunkLoader;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {


    @Invoker("pickRandomSpawnEntry")
    static Optional<SpawnSettings.SpawnEntry> pickRandomSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, Random random, BlockPos pos) {
        throw new AssertionError();
    }

    private static boolean canSpawn(ServerWorld world, SpawnGroup group, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable pos, double squaredDistance) {
        EntityType<?> entityType = spawnEntry.type;

        if (entityType.getSpawnGroup() == SpawnGroup.MISC) {
            return false;
        }

        if (!entityType.isSpawnableFarFromPlayer() && squaredDistance > (double)(entityType.getSpawnGroup().getImmediateDespawnRange() * entityType.getSpawnGroup().getImmediateDespawnRange())) {
            return false;
        }

        if (!entityType.isSummonable()) {
            return false;
        }

        SpawnRestriction.Location location = SpawnRestriction.getLocation(entityType);
        if (!SpawnHelper.canSpawn(location, world, pos, entityType)) {
            return false;
        }


        if (!SpawnRestriction.canSpawn(entityType, world, SpawnReason.NATURAL, pos, world.random)) {
            return false;
        }

        return world.isSpaceEmpty(entityType.createSimpleBoundingBox((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5));
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
            int i = pos.getY();
            BlockState blockState = chunk.getBlockState(pos);
            if (blockState.isSolidBlock(chunk, pos)) {
                return;
            }
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            int j = 0;
            block0: for (int k = 0; k < 3; ++k) {
                int l = pos.getX();
                int m = pos.getZ();
                int n = 6;
                SpawnSettings.SpawnEntry spawnEntry = null;
                EntityData entityData = null;
                int o = MathHelper.ceil(world.random.nextFloat() * 4.0f);
                int p = 0;
                for (int q = 0; q < o; ++q) {
                    double f = 1;
                    mutable.set(l += world.random.nextInt(6) - world.random.nextInt(6), i, m += world.random.nextInt(6) - world.random.nextInt(6));
                    double d = (double)l + 0.5;
                    double e = (double)m + 0.5;

                    PlayerEntity playerEntity = world.getClosestPlayer(d, (double)i, e, -1.0, false);
                    if (playerEntity != null)
                    {
                        if (playerEntity.squaredDistanceTo(d, i, e) <= 576.0)
                            continue;
                    }

                    if (world.getSpawnPos().isWithinDistance(new Vec3d((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5), 24.0)) {
                        continue;
                    }


                    if (spawnEntry == null) {
                        Optional<SpawnSettings.SpawnEntry> optional = pickRandomSpawnEntry(world, structureAccessor, chunkGenerator, group, world.random, mutable);
                        if (optional.isEmpty()) continue block0;
                        spawnEntry = optional.get();
                        o = spawnEntry.minGroupSize + world.random.nextInt(1 + spawnEntry.maxGroupSize - spawnEntry.minGroupSize);
                    }

                    if (!canSpawn(world, group, structureAccessor, chunkGenerator, spawnEntry, mutable, 1))
                        continue;
                    if (!checker.test(spawnEntry.type, mutable, chunk))
                        continue;

                    MobEntity mobEntity = createMob(world, spawnEntry.type);
                    if (mobEntity == null) {
                        return;
                    }
                    if (mobEntity instanceof BatEntity)
                    {
                        return;
                    }



                    mobEntity.refreshPositionAndAngles(d, i, e, world.random.nextFloat() * 360.0f, 0.0f);
                    if (!isValidSpawn(world, mobEntity, f))
                        continue;
                    entityData = mobEntity.initialize(world, world.getLocalDifficulty(mobEntity.getBlockPos()), SpawnReason.NATURAL, entityData, null);
                    ++p;
                    world.spawnEntityAndPassengers(mobEntity);
                    runner.run(mobEntity, chunk);
                    if (++j >= mobEntity.getLimitPerChunk()) {
                        return;
                    }
                    if (mobEntity.spawnsTooManyForEachTry(p)) continue block0;
                }
            }
            cir.cancel();
        }
    }


}
