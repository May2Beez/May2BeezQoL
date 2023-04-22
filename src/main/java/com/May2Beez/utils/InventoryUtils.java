package com.May2Beez.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;

import java.util.Arrays;

public class InventoryUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean hasItemInInventory(String name) {
        for (int i = 0; i < 36; i++) {
            if (mc.thePlayer.inventory.getStackInSlot(i) != null) {
                if (mc.thePlayer.inventory.getStackInSlot(i).getDisplayName().contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int findItemInHotbar(String ...name) {
        InventoryPlayer inv = mc.thePlayer.inventory;
        for (int i = 0; i < 9; i++) {
            ItemStack curStack = inv.getStackInSlot(i);
            if (curStack != null) {
                if (Arrays.stream(name).anyMatch(curStack.getDisplayName()::contains)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static boolean clickItemInContainer(String name) {
        return clickItemInContainer(name, false);
    }

    public static boolean clickItemInContainer(String name, boolean rightClick) {
        for (int i = 0; i < 36; i++) {
            if (mc.thePlayer.openContainer.getSlot(i).getStack() != null) {
                if (mc.thePlayer.openContainer.getSlot(i).getStack().getDisplayName().contains(name)) {
                    mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, i, rightClick ? 1 : 0, 0, mc.thePlayer);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean clickItemInContainer(String name, boolean rightClick, int minStackSize) {
        for (int i = 0; i < 36; i++) {
            if (mc.thePlayer.openContainer.getSlot(i).getStack() != null) {
                if (mc.thePlayer.openContainer.getSlot(i).getStack().getDisplayName().contains(name) && mc.thePlayer.openContainer.getSlot(i).getStack().stackSize >= minStackSize) {
                    mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, i, rightClick ? 1 : 0, 0, mc.thePlayer);
                    return true;
                }
            }
        }
        return false;
    }

    public static String getContainerName() {
        return mc.thePlayer.openContainer.getSlot(0).inventory.getName();
    }

    public static boolean hasFreeSlots() {
        for (int i = 0; i < 36; i++) {
            if (mc.thePlayer.inventory.getStackInSlot(i) == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasFreeSlotsInContainer() {
        if (mc.currentScreen instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
            for (Slot slot : chest.inventorySlots) {
                if (slot.getStack() == null && slot.slotNumber < chest.getLowerChestInventory().getSizeInventory()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void moveEveryItemToContainer(String name) {
        if (mc.currentScreen instanceof GuiChest) {
            final ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
            for (Slot slot : chest.inventorySlots) {
                if (slot != null && slot.getHasStack() && slot.slotNumber >= chest.getLowerChestInventory().getSizeInventory()) {
                    if (StringUtils.stripControlCodes(slot.getStack().getDisplayName()).equalsIgnoreCase(name)) {
                        mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot.slotNumber, 0, 1, mc.thePlayer);
                    }
                }
            }
        }
    }
}
