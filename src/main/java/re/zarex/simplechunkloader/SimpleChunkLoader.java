package re.zarex.simplechunkloader;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.zarex.simplechunkloader.blocks.ChunkLoader;

public class SimpleChunkLoader implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("simplechunkloader");
	public static LongSet FORCELOADEDCHUNKS;
	@Override
	public void onInitialize() {
		ChunkLoader.Register();
		ServerTickEvents.START_WORLD_TICK.register(world -> {
			SimpleChunkLoader.FORCELOADEDCHUNKS = world.getForcedChunks();
		});
	}


}
