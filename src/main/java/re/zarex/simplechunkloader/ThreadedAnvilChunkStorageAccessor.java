package re.zarex.simplechunkloader;

import net.minecraft.server.world.ServerWorld;

public interface ThreadedAnvilChunkStorageAccessor {
    public ServerWorld getHookedWorld();
}
