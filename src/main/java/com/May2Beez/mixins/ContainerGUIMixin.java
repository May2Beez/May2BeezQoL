package com.May2Beez.mixins;

import com.May2Beez.events.ChestBackgroundDrawnEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = { GuiContainer.class }, priority = 999)
public class ContainerGUIMixin extends GuiScreen {
    @Shadow
    public Container inventorySlots;

    @Inject(method = { "drawScreen" }, at = { @At("HEAD") })
    private void backgroundDrawn(final CallbackInfo ci) {
        if (this.inventorySlots instanceof ContainerChest) {
            final IInventory chest = ((ContainerChest)this.inventorySlots).getLowerChestInventory();
            MinecraftForge.EVENT_BUS.post((Event)new ChestBackgroundDrawnEvent(this.inventorySlots, StringUtils.stripControlCodes(chest.getDisplayName().getUnformattedText().trim()), this.inventorySlots.inventorySlots.size(), this.inventorySlots.inventorySlots, chest));
        }
    }
}
