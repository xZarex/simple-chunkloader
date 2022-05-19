package re.zarex.simplechunkloader;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.zarex.simplechunkloader.blocks.ChunkLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SimpleChunkLoader implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("simplechunkloader");
	public static final ChunkTicketType<ChunkPos> TICKET_TYPE = ChunkTicketType.create("scl_forced", Comparator.comparingLong(ChunkPos::toLong));;
	public static final HashMap<String, LongArraySet> FORCELOADEDCHUNKS = new HashMap<>();

	public static void addChunk(String world, long chunk)
	{
		if (!FORCELOADEDCHUNKS.containsKey(world))
			FORCELOADEDCHUNKS.put(world, new LongArraySet());
		FORCELOADEDCHUNKS.get(world).add(chunk);
		SaveChunks();
	}

	public static void removeChunk(String world, long chunk)
	{
		FORCELOADEDCHUNKS.get(world).remove(chunk);
		SaveChunks();
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

	public static void SaveChunks()
	{
		Path path = FabricLoader.getInstance().getConfigDir();
		Path configPath = path.resolve( "simplechunkloader.chunks" ).toAbsolutePath();
		try {
			Files.deleteIfExists(configPath);
			Files.createFile(configPath);
			ObjectOutputStream oos = null;
			FileOutputStream fout = null;
			try {
				File file = new File(configPath.toString());
				fout = new FileOutputStream(file);
				oos = new ObjectOutputStream(fout);

				oos.writeObject(FORCELOADEDCHUNKS);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if(oos != null){
					oos.close();
				}
			}

		} catch (Exception e) {
			LOGGER.info("Could not save config file! "+e.toString());
		}
	}
	@Override
	public void onInitialize() {
		Path path = FabricLoader.getInstance().getConfigDir();
		Path configPath = path.resolve( "simplechunkloader.chunks" ).toAbsolutePath();
		try {
			Files.createFile(configPath);
		}
		catch (Exception e) {
			// Not an error, no chunks were saved.
		};
		try {
			ObjectInputStream objectinputstream = null;
			try {
				File file = new File(configPath.toString());
				FileInputStream streamIn = new FileInputStream(file);
				objectinputstream = new ObjectInputStream(streamIn);
				FORCELOADEDCHUNKS.putAll ((HashMap<String, LongArraySet>)objectinputstream.readObject());
				LOGGER.info("loaded chunks! "+FORCELOADEDCHUNKS.size());
				LOGGER.info("loaded chunks! "+FORCELOADEDCHUNKS.get("minecraft:overworld").size());
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



		ChunkLoader.Register();
	}

	public static void loadChunks(MinecraftServer server)
	{
		LOGGER.info("loaded chunks! "+FORCELOADEDCHUNKS.size());
		LOGGER.info("loaded chunks! "+FORCELOADEDCHUNKS.get("minecraft:overworld").size());
		for (ServerWorld world : server.getWorlds()) {
			if (FORCELOADEDCHUNKS.containsKey(world.getRegistryKey().getValue().toString()))
			{
				for (long chunk : FORCELOADEDCHUNKS.get(world.getRegistryKey().getValue().toString())) {
					LOGGER.info("loading chunk: "+chunk);
					ChunkPos pos = new ChunkPos(chunk);
					world.getChunkManager().threadedAnvilChunkStorage.getTicketManager().addTicket(SimpleChunkLoader.TICKET_TYPE, pos, 2, pos);
				}
			}
		}
	}


}
