package com.May2Beez.modules.player;

import com.May2Beez.Module;
import com.May2Beez.May2BeezQoL;
import com.May2Beez.utils.SkyblockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class AutoMelody extends Module {
    public AutoMelody() {
        super("Auto Melody", new KeyBinding("Auto Melody", Keyboard.KEY_NONE, May2BeezQoL.MODID + " - Player"));
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!isToggled()) return;

        if(event.gui instanceof GuiChest) {
            if(SkyblockUtils.getGuiName(event.gui).startsWith("Harp -")) {
                for(Slot slot : ((GuiChest) event.gui).inventorySlots.inventorySlots) {
                    if(slot.getStack() != null && slot.getStack().getItem() instanceof ItemBlock && ((ItemBlock) slot.getStack().getItem()).getBlock() == Blocks.quartz_block) {
                        Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().thePlayer.openContainer.windowId, slot.slotNumber, 2, 3, Minecraft.getMinecraft().thePlayer);
                        break;
                    }
                }
            }
        }
    }
}
