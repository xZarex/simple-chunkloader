package re.zarex.simplechunkloader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import re.zarex.simplechunkloader.blocks.ChunkLoader;

@Environment(EnvType.CLIENT)
public class SimpleChunkLoaderClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ChunkLoader.RegisterClient();
	}
}
