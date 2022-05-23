package re.zarex.simplechunkloader.blocks.entities;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import re.zarex.simplechunkloader.SimpleChunkLoader;
import re.zarex.simplechunkloader.blocks.ChunkLoader;
import re.zarex.simplechunkloader.gui.ChunkLoaderGuiDescription;

public class ChunkLoaderEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    // Store the current value of the number
    private int size = 1;
    public ChunkLoaderEntity(BlockPos pos, BlockState state) {
        super(ChunkLoader.ENTITY_TYPE, pos, state);
    }

    public void onPlaced(World world, BlockPos pos, BlockState state)
    {
        ServerWorld serverWorld = (ServerWorld)world;   //Objects.requireNonNull(world.getServer()).getWorld(world.getRegistryKey());
        ChunkPos chunkPos = serverWorld.getChunk(pos).getPos();
        SimpleChunkLoader.addChunk(serverWorld, chunkPos, getPos());
    }

    public void onBreak(World world, BlockPos pos, BlockState state)
    {
        ServerWorld serverWorld = (ServerWorld)world;   //Objects.requireNonNull(world.getServer()).getWorld(world.getRegistryKey());
        SimpleChunkLoader.removeChunkLoader(serverWorld, getPos());
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putInt("chunkloadersize", size);
        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        size = tag.getInt("chunkloadersize");
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
        return new ChunkLoaderGuiDescription(syncId, inventory, ScreenHandlerContext.create(world, pos), getPos(), size);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(getPos());
        buf.writeInt(size);
    }


    public void setSize(int i)
    {
        size = i;
        SimpleChunkLoader.refreshChunkLoader((ServerWorld) getWorld(), getPos(), i);
        markDirty();
    }
}
