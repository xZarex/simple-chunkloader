package re.zarex.simplechunkloader.gui;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WLabeledSlider;
import io.github.cottonmc.cotton.gui.widget.WSlider;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import re.zarex.simplechunkloader.SimpleChunkLoader;
import re.zarex.simplechunkloader.blocks.ChunkLoader;

public class ChunkLoaderGuiDescription extends SyncedGuiDescription {
    public ChunkLoaderGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, BlockPos pos, int size) {
        super(ChunkLoader.SCREEN_HANDLER_TYPE, syncId, playerInventory, null, new ArrayPropertyDelegate(1));

        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        //root.setSize(300, 200);
        root.setInsets(Insets.ROOT_PANEL);

        WLabeledSlider slider = new WLabeledSlider(1, 3, Axis.HORIZONTAL);
        //slider.

        slider.setLabelUpdater(value -> Text.of((value+(value-1))+" x "+ (value+(value-1))));

        slider.setValue(size);
        slider.setLabel(Text.of((size+(size-1))+" x "+(size+(size-1))));

        slider.setDraggingFinishedListener((int i) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeInt(i);
            ClientPlayNetworking.send(SimpleChunkLoader.CHUNK_SIZE_PACKET, buf);
        });
        root.add(slider, 0, 1, 5, 2);


        root.validate(this);
    }
}