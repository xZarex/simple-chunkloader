package re.zarex.simplechunkloader.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import re.zarex.simplechunkloader.SimpleChunkLoader;

public class ChunkLoaderItem extends BlockItem {
    public ChunkLoaderItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient)
            return super.useOnBlock(context);

        if (SimpleChunkLoader.containsChunks(context.getWorld().getRegistryKey().toString(), context.getWorld().getChunk(context.getBlockPos()).getPos().toLong()))
        {
            context.getPlayer().sendMessage(Text.of("This chunk is already force loaded!"), false);
            return ActionResult.FAIL;
        }
        return super.useOnBlock(context);
    }
}
