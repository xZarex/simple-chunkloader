package re.zarex.simplechunkloader;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.zarex.simplechunkloader.blocks.ChunkLoader;
import re.zarex.simplechunkloader.blocks.entities.ChunkLoaderEntity;
import re.zarex.simplechunkloader.gui.ChunkLoaderGuiDescription;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SimpleChunkLoader implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("simplechunkloader");
	public static final ChunkTicketType<ChunkPos> TICKET_TYPE = ChunkTicketType.create("scl_forced", Comparator.comparingLong(ChunkPos::toLong));;
	public static final HashMap<String, LongArraySet> FORCELOADEDCHUNKS = new HashMap<>();
	public static final HashMap<String, LongArraySet> chunkloaderToChunks = new HashMap<>();
	public static final Identifier CHUNK_SIZE_PACKET = new Identifier("simplechunkloader", "sizepacket");


	public static void addChunk(ServerWorld serverWorld, ChunkPos chunk, BlockPos chunkloaderPos)
	{
		String world = serverWorld.getRegistryKey().getValue().toString();
		if (!FORCELOADEDCHUNKS.containsKey(world))
			FORCELOADEDCHUNKS.put(world, new LongArraySet());
		FORCELOADEDCHUNKS.get(world).add(chunk.toLong());

		String chunkloader = world+chunkloaderPos.toShortString();
		if (!chunkloaderToChunks.containsKey(chunkloader))
			chunkloaderToChunks.put(chunkloader, new LongArraySet());

		chunkloaderToChunks.get(chunkloader).add(chunk.toLong());

		serverWorld.getChunkManager().threadedAnvilChunkStorage.getTicketManager().addTicket(SimpleChunkLoader.TICKET_TYPE, chunk, 2, chunk);
		SaveHashmap(serverWorld.getServer(), "simplechunkloader.chunks", FORCELOADEDCHUNKS);
		SaveHashmap(serverWorld.getServer(), "simplechunkloader.chunkloaders", chunkloaderToChunks);
	}

	public static void refreshChunkLoader(ServerWorld world, BlockPos chunkloaderPos, int size)
	{
		removeChunkLoader(world, chunkloaderPos);

		int startX = ChunkSectionPos.getSectionCoord(chunkloaderPos.getX());
		int startY = ChunkSectionPos.getSectionCoord(chunkloaderPos.getZ());
		// 2 = 3x3, 3 = 5x5
		for (int i = (-1*size)+1; i < size; i++)
		{
			for (int y = (-1*size)+1; y < size; y++)
			{
				addChunk(world, new ChunkPos(startX+i, startY+y), chunkloaderPos);
			}
		}
	}

	public static void removeChunkLoader(ServerWorld serverWorld, BlockPos chunkloaderPos)
	{

		String world = serverWorld.getRegistryKey().getValue().toString();
		String chunkloader = world+chunkloaderPos.toShortString();

		if (!chunkloaderToChunks.containsKey(chunkloader))
			return;

		LongArraySet chunksToRemove = new LongArraySet();
		chunksToRemove.addAll(chunkloaderToChunks.get(chunkloader));

		for (Map.Entry<String, LongArraySet> entry : chunkloaderToChunks.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(chunkloader))
				continue;

			for (long chunk : entry.getValue()) {
				if (chunksToRemove.contains(chunk))
					chunksToRemove.remove(chunk);
			}
		}

		chunkloaderToChunks.get(chunkloader).clear();
		chunkloaderToChunks.remove(chunkloader);

		for (long chunk : chunksToRemove) {
			ChunkPos chunkPos = new ChunkPos(chunk);
			serverWorld.getChunkManager().threadedAnvilChunkStorage.getTicketManager().removeTicket(SimpleChunkLoader.TICKET_TYPE, chunkPos, 2, chunkPos);
			FORCELOADEDCHUNKS.get(world).remove(chunk);
		}

		SaveHashmap(serverWorld.getServer(), "simplechunkloader.chunks", FORCELOADEDCHUNKS);
		SaveHashmap(serverWorld.getServer(), "simplechunkloader.chunkloaders", FORCELOADEDCHUNKS);
	}

	public static boolean containsChunks(String world, long chunk)
	{
		try {
			return FORCELOADEDCHUNKS.get(world).contains(chunk);
		}
		catch (Exception e)
		{

		}
		return false;
	}

	public static void SaveHashmap(MinecraftServer server, String filename, HashMap<String, LongArraySet> hashMap)
	{
		Path path = server.getSavePath(WorldSavePath.ROOT);
		Path configPath = path.resolve( filename ).toAbsolutePath();
		try {
			Files.deleteIfExists(configPath);
			Files.createFile(configPath);
			ObjectOutputStream oos = null;
			FileOutputStream fout = null;
			try {
				File file = new File(configPath.toString());
				fout = new FileOutputStream(file);
				oos = new ObjectOutputStream(fout);

				oos.writeObject(hashMap);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if(oos != null){
					oos.close();
				}
			}

		} catch (Exception e) {
			LOGGER.info("Could not save data file! "+e.toString());
		}
	}

	public static void loadChunks(MinecraftServer server)
	{
		loadHashmap(server, "simplechunkloader.chunkloaders", chunkloaderToChunks);
		loadHashmap(server, "simplechunkloader.chunks", FORCELOADEDCHUNKS);
		for (ServerWorld world : server.getWorlds()) {
			if (FORCELOADEDCHUNKS.containsKey(world.getRegistryKey().getValue().toString()))
			{
				for (long chunk : FORCELOADEDCHUNKS.get(world.getRegistryKey().getValue().toString())) {
					ChunkPos pos = new ChunkPos(chunk);
					world.getChunkManager().threadedAnvilChunkStorage.getTicketManager().addTicket(SimpleChunkLoader.TICKET_TYPE, pos, 2, pos);
				}
			}
		}
	}

	public static void loadHashmap(MinecraftServer server, String filename, HashMap<String, LongArraySet> hashMap)
	{
		Path path = server.getSavePath(WorldSavePath.ROOT);
		Path configPath = path.resolve( filename ).toAbsolutePath();
		try {
			ObjectInputStream objectinputstream = null;
			try {
				File file = new File(configPath.toString());
				FileInputStream streamIn = new FileInputStream(file);
				objectinputstream = new ObjectInputStream(streamIn);
				hashMap.putAll ((HashMap<String, LongArraySet>)objectinputstream.readObject());
			} catch (Exception e) {
				LOGGER.info("Could not load config file! "+e.toString());
			} finally {
				if(objectinputstream != null){
					objectinputstream .close();
				}
			}
		} catch (Exception ex2) {
			LOGGER.info("Could not generate config file! "+ex2.toString());
		}


	}
	@Override
	public void onInitialize() {
		ChunkLoader.Register();

		ServerPlayNetworking.registerGlobalReceiver(CHUNK_SIZE_PACKET, (server, player, handler, buf, sender) -> {
			BlockPos pos = buf.readBlockPos();
			if (pos == null)
				return;
			int i = buf.readInt();
			if (i > 3)
				return;
			server.execute(() -> {
				BlockState state = player.getWorld().getBlockState(pos);
				if (state == null)
					return;
				if (player.getWorld().canPlayerModifyAt(player, pos) && state.getBlock() instanceof ChunkLoader)
				{

					if (player.getWorld().getBlockEntity(pos) instanceof ChunkLoaderEntity)
					{

						ChunkLoaderEntity block = (ChunkLoaderEntity)player.getWorld().getBlockEntity(pos);
						block.setSize(i);
					}
				}
			});
		});
	}




}
