package re.zarex.simplechunkloader.blocks.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import re.zarex.simplechunkloader.SimpleChunkLoader;
import re.zarex.simplechunkloader.blocks.ChunkLoader;

import java.util.Objects;

public class ChunkLoaderEntity extends BlockEntity {
    public ChunkLoaderEntity(BlockPos pos, BlockState state) {
        super(ChunkLoader.ENTITY_TYPE, pos, state);
    }

    public void onPlaced(World world, BlockPos pos, BlockState state)
    {
        ServerWorld serverWorld = (ServerWorld)world;   //Objects.requireNonNull(world.getServer()).getWorld(world.getRegistryKey());
        ChunkPos chunkPos = serverWorld.getChunk(pos).getPos();
        serverWorld.getChunkManager().threadedAnvilChunkStorage.getTicketManager().addTicket(SimpleChunkLoader.TICKET_TYPE, chunkPos, 2, chunkPos);
        SimpleChunkLoader.addChunk(serverWorld.getRegistryKey().getValue().toString(), chunkPos.toLong());
    }

    public void onBreak(World world, BlockPos pos, BlockState state)
    {
        ServerWorld serverWorld = (ServerWorld)world;   //Objects.requireNonNull(world.getServer()).getWorld(world.getRegistryKey());
        ChunkPos chunkPos = serverWorld.getChunk(pos).getPos();
        serverWorld.getChunkManager().threadedAnvilChunkStorage.getTicketManager().removeTicket(SimpleChunkLoader.TICKET_TYPE, chunkPos, 2, chunkPos);
        SimpleChunkLoader.removeChunk(serverWorld.getRegistryKey().getValue().toString(), chunkPos.toLong());
    }
}
