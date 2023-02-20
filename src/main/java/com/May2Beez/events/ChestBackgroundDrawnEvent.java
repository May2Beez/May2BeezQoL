package com.May2Beez.events;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;


public class ChestBackgroundDrawnEvent extends Event
{
    public final Container chest;
    public final String displayName;
    public final int chestSize;
    public final List<Slot> slots;
    public final IInventory chestInv;

    public ChestBackgroundDrawnEvent(final Container chest, final String displayName, final int chestSize, final List<Slot> slots, final IInventory chestInv) {
        this.chest = chest;
        this.displayName = displayName;
        this.chestSize = chestSize;
        this.slots = slots;
        this.chestInv = chestInv;
    }
}
