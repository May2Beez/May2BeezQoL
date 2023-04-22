package com.May2Beez.modules.farming;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.modules.Module;
import com.May2Beez.utils.InventoryUtils;
import com.May2Beez.utils.LogUtils;
import com.May2Beez.utils.SkyblockUtils;
import com.May2Beez.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FillChestWithSaplingMacro extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public enum States {
        IDLE,
        ABIPHONE,
        BUILDER,
        OPEN_CHEST,
        FILL_CHEST
    }

    private static States state = States.IDLE;
    private static final Timer waitTimer = new Timer();

    public FillChestWithSaplingMacro(){
        super("Fill Chest With Sapling Macro");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        state = States.IDLE;
        waitTimer.reset();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isToggled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (!waitTimer.hasReached(state == States.BUILDER ? 80 : 300)) return;

        switch (state) {
            case IDLE:
                if (InventoryUtils.hasItemInInventory(getSaplingName())) {
                    state = States.OPEN_CHEST;
                    waitTimer.reset();
                    return;
                }

                if (SkyblockUtils.hasOpenContainer()) {
                    mc.thePlayer.closeScreen();
                    waitTimer.reset();
                    return;
                }

                if (InventoryUtils.findItemInHotbar("Abiphone") == -1) {
                    LogUtils.addMessage("You need an Abiphone to use this macro!", EnumChatFormatting.RED);
                    toggle();
                    return;
                }

                if (mc.thePlayer.inventory.getCurrentItem() == null || !mc.thePlayer.inventory.getCurrentItem().getDisplayName().contains("Abiphone")) {
                    mc.thePlayer.inventory.currentItem = InventoryUtils.findItemInHotbar("Abiphone");
                } else {
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem());
                    state = States.ABIPHONE;
                }
                waitTimer.reset();
                break;
            case ABIPHONE:
                if (!SkyblockUtils.hasOpenContainer()) return;

                if (InventoryUtils.clickItemInContainer("Builder")) {
                    state = States.BUILDER;
                    waitTimer.reset();
                    return;
                }

                if (!InventoryUtils.getContainerName().contains("Builder")) {
                    mc.thePlayer.closeScreen();
                    waitTimer.reset();
                    LogUtils.addMessage("You need a Builder's contact to use this macro!", EnumChatFormatting.RED);
                    toggle();
                    return;
                }
                break;
            case BUILDER:
                if (!SkyblockUtils.hasOpenContainer()) return;

                if (InventoryUtils.getContainerName().contains("Builder")) {
                    InventoryUtils.clickItemInContainer("Green Thumb");
                    waitTimer.reset();
                    return;
                }

                if (InventoryUtils.getContainerName().contains("Green Thumb")) {
                    InventoryUtils.clickItemInContainer(getSaplingName(), true);
                    waitTimer.reset();
                    return;
                }

                if (InventoryUtils.getContainerName().contains("Shop Trading Options")) {
                    if (InventoryUtils.hasFreeSlots()) {
                        InventoryUtils.clickItemInContainer(getSaplingName(), false, 64);
                        waitTimer.reset();
                    } else {
                        mc.thePlayer.closeScreen();
                        waitTimer.reset();
                        state = States.OPEN_CHEST;
                        return;
                    }
                }
                break;
            case OPEN_CHEST:
                if (SkyblockUtils.hasOpenContainer()) {
                    mc.thePlayer.closeScreen();
                    waitTimer.reset();
                    return;
                }

                MovingObjectPosition objectMouseOver = mc.objectMouseOver;

                if (objectMouseOver == null || objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !mc.theWorld.getBlockState(objectMouseOver.getBlockPos()).getBlock().equals(Blocks.chest)) {
                    LogUtils.addMessage("You need to be looking at a chest to use this macro!", EnumChatFormatting.RED);
                    toggle();
                    return;
                }

                SkyblockUtils.rightClick();
                state = States.FILL_CHEST;
                waitTimer.reset();
                break;
            case FILL_CHEST:
                if (!SkyblockUtils.hasOpenContainer()) return;

                if (InventoryUtils.hasItemInInventory(getSaplingName()) && InventoryUtils.hasFreeSlotsInContainer()) {
                    InventoryUtils.moveEveryItemToContainer(getSaplingName());
                    waitTimer.reset();
                } else if (InventoryUtils.hasItemInInventory(getSaplingName()) && !InventoryUtils.hasFreeSlotsInContainer()) {
                    mc.thePlayer.closeScreen();
                    toggle();
                    LogUtils.addMessage("The chest is full!", EnumChatFormatting.RED);
                } else {
                    mc.thePlayer.closeScreen();
                    waitTimer.reset();
                    LogUtils.addMessage("Every sapling has been put into the chest", EnumChatFormatting.GREEN);
                    state = States.IDLE;
                }
                break;
        }
    }

    private static String getSaplingName() {
        switch (May2BeezQoL.config.fillChestSaplingType) {
            case 0:
                return "Spruce Sapling";
            case 1:
                return "Jungle Sapling";
            case 2:
                return "Dark Oak Sapling";
            default:
                throw new IllegalStateException("Unexpected value: " + May2BeezQoL.config.fillChestSaplingType);
        }
    }
}
