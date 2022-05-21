package re.zarex.simplechunkloader.gui;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class ChunkLoaderBlockScreen extends CottonInventoryScreen<ChunkLoaderGuiDescription> {
    public ChunkLoaderBlockScreen(ChunkLoaderGuiDescription gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }
}