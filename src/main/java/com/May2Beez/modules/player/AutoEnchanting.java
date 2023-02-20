package com.May2Beez.modules.player;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.events.ChestBackgroundDrawnEvent;
import com.May2Beez.utils.LocationUtils;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.Sys;

import java.util.Arrays;
import java.util.List;

public class AutoEnchanting {
    private long lastInteractTime;
    private boolean getNextChronomatronClick;
    private int[] pattern;
    private int clickCount;
    private int nextSlot;

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderGui(final ChestBackgroundDrawnEvent event) {
        if (!May2BeezQoL.config.autoEnchantingTable || LocationUtils.currentIsland != LocationUtils.Island.PRIVATE_ISLAND || event.chest.inventorySlots.get(49).getStack() == null) {
            return;
        }
        final List<Slot> invSlots = event.chest.inventorySlots;
        if (event.displayName.startsWith("Ultrasequencer (")) {
            if (invSlots.get(49).getStack().getItem() == Items.clock) {
                if (this.pattern[this.clickCount] != 0 && System.currentTimeMillis() - this.lastInteractTime >= May2BeezQoL.config.autoEnchantingTableDelay) {
                    mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, this.pattern[this.clickCount], 1, 0, mc.thePlayer);
                    this.lastInteractTime = System.currentTimeMillis();
                    this.pattern[this.clickCount] = 0;
                    ++this.clickCount;
                }
            }
            else if (invSlots.get(49).getStack().getItem() == Item.getItemFromBlock(Blocks.glowstone)) {
                for (int i = 9; i <= 44; ++i) {
                    if (invSlots.get(i).getStack() != null) {
                        if (this.pattern[invSlots.get(i).getStack().stackSize - 1] == 0 && !invSlots.get(i).getStack().getDisplayName().startsWith(" ")) {
                            this.pattern[invSlots.get(i).getStack().stackSize - 1] = i;
                        }
                    }
                }
                if (May2BeezQoL.config.autoEnchantingTableAutoClose && this.pattern[9] != 0) {
                    mc.thePlayer.closeScreen();
                    return;
                }
                this.clickCount = 0;
            }
        }
        else if (event.displayName.startsWith("Chronomatron (")) {
            if (May2BeezQoL.config.autoEnchantingTableAutoClose && invSlots.get(4).getStack().stackSize >= 13) {
                mc.thePlayer.closeScreen();
                return;
            }
            if (invSlots.get(49).getStack().getItem() == Items.clock) {
                if (this.getNextChronomatronClick) {
                    for (int i = 11; i <= 33; ++i) {
                        if (invSlots.get(i).getStack() != null) {
                            if (invSlots.get(i).getStack().getItem() == Item.getItemFromBlock(Blocks.stained_hardened_clay) && this.pattern[this.nextSlot] == 0) {
                                this.getNextChronomatronClick = false;
                                this.pattern[this.nextSlot] = i;
                                ++this.nextSlot;
                                break;
                            }
                        }
                    }
                }
                if (this.pattern[this.clickCount] != 0 && System.currentTimeMillis() - this.lastInteractTime >= May2BeezQoL.config.autoEnchantingTableDelay) {
                    mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, this.pattern[this.clickCount], 1, 0, (EntityPlayer)mc.thePlayer);
                    this.lastInteractTime = System.currentTimeMillis();
                    ++this.clickCount;
                }
            }
            else if (invSlots.get(49).getStack().getItem() == Item.getItemFromBlock(Blocks.glowstone)) {
                this.clickCount = 0;
                this.getNextChronomatronClick = true;
            }
        }
    }

    @SubscribeEvent
    public void onOpenGui(final GuiOpenEvent event) {
        this.clickCount = 0;
        this.pattern = new int[66];
        this.nextSlot = 0;
    }
}
