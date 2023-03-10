package com.May2Beez.modules.garden;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.modules.Module;
import com.May2Beez.utils.Timer;
import com.May2Beez.utils.*;
import com.May2Beez.utils.structs.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Slot;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.May2Beez.utils.SkyblockUtils.hasOpenContainer;
import static com.May2Beez.utils.SkyblockUtils.rightClick;

public class VisitorsMacro extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    enum States {
        TURNED_OFF,
        WAITING_FOR_VISITOR,
        OPEN_MENU,
        CHECK_REQUIREMENTS,
        CLOSE_MENU,
        OPEN_BZ,
        CLICK_SEARCH,
        INPUT_SEARCH_VALUE,
        CONFIRM_SEARCH_VALUE,
        SELECT_PRODUCT,
        CLICK_BIN,
        CLICK_CUSTOM_AMOUNT,
        INPUT_CUSTOM_AMOUNT,
        CONFIRM_CUSTOM_AMOUNT,
        CLICK_BUY,
        WAITING_FOR_BUY,
        CHECK_IF_MORE_ITEMS,
        GET_ITEMS,
        BUYING,
        FINISHING_VISITOR_1,
        FINISHING_VISITOR_2,
        FINISHING_VISITOR_3
    }

    private final Timer waitTimer = new Timer();
    private final Timer stuckTimer = new Timer();
    private boolean waitForNextVisitor = false;

    public static States currentState = States.TURNED_OFF;
    private final BlockPos deskPosition = new BlockPos(5, 71, -21);
    private final ArrayList<Tuple<String, Integer>> requiredItems = new ArrayList<>();
    private Thread buyingThread;

    private int findingItemTries = 0;


    public VisitorsMacro() {
        super("Visitors Macro", new KeyBinding("Visitors Macro", Keyboard.KEY_NONE, "May2BeezQoL - Garden"));
    }

    @Override
    public void onEnable() {
        currentState = States.WAITING_FOR_VISITOR;
        if (!BlockUtils.getPlayerLoc().down().equals(deskPosition)) {
            LogUtils.addMessage("You are not standing at the desk!", EnumChatFormatting.RED);
            this.toggle();
            return;
        }
        findingItemTries = 0;
        RotationUtils.smoothLook(new Rotation(0, 5), 300);
        waitTimer.reset();
        stuckTimer.reset();
        waitForNextVisitor = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        currentState = States.TURNED_OFF;
        requiredItems.clear();
        if (buyingThread != null) {
            buyingThread.interrupt();
            buyingThread = null;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (LocationUtils.currentIsland != LocationUtils.Island.GARDEN) return;
        if (!isToggled()) return;

        switch (currentState) {
            case WAITING_FOR_VISITOR:
                if (!RotationUtils.done) return;
                if (!waitTimer.hasReached(waitForNextVisitor ? 4000 : 400)) return;
                MovingObjectPosition objectMouseOver = mc.objectMouseOver;
                if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    rightClick();
                    waitTimer.reset();
                    stuckTimer.reset();
                    currentState = States.OPEN_MENU;
                }
                break;
            case OPEN_MENU:
                if (!waitTimer.hasReached(400)) return;
                if (hasOpenContainer()) {
                    currentState = States.CHECK_REQUIREMENTS;
                    waitTimer.reset();
                    break;
                }
                rightClick();
                waitTimer.reset();
                currentState = States.CHECK_REQUIREMENTS;
                break;
            case CHECK_REQUIREMENTS:
                if (!waitTimer.hasReached(400)) return;
                requiredItems.clear();
                if (stuckTimer.hasReached(6_000)) {
                    LogUtils.addMessage("I'm stuck, restarting", EnumChatFormatting.RED);
                    mc.thePlayer.closeScreen();
                    waitTimer.reset();
                    stuckTimer.reset();
                    currentState = States.WAITING_FOR_VISITOR;
                    return;
                }
                if (!hasOpenContainer()) return;
                Slot acceptOffer = mc.thePlayer.openContainer.getSlot(29);
                ArrayList<String> lore = SkyblockUtils.getItemLore(acceptOffer.getStack());
                boolean foundRequirements = false;
                for (String line : lore) {
                    if (line.contains("Required:")) {
                        foundRequirements = true;
                        continue;
                    }
                    if (line.trim().contains("Rewards:") || line.trim().isEmpty()) {
                        currentState = States.CLOSE_MENU;
                        break;
                    }
                    if (foundRequirements) {
                        String[] split = StringUtils.stripControlCodes(line.replace(",", "")).trim().split(" ");
                        String itemName = split[split.length-1].startsWith("x") ? String.join(" ", split).replace(" " + split[split.length-1], "") : String.join(" ", split);
                        requiredItems.add(new Tuple<>(itemName, Integer.parseInt(!split[split.length-1].startsWith("x") ? "1" : split[split.length-1].replace("x", ""))));
                    }
                }
                waitTimer.reset();
                break;
            case CLOSE_MENU:
                if (!waitTimer.hasReached(400)) return;
                mc.thePlayer.closeScreen();
                waitTimer.reset();
                System.out.println("Required items: " + requiredItems);
                LogUtils.addMessage("Required items:", EnumChatFormatting.GOLD);
                for (Tuple<String, Integer> requiredItem : requiredItems) {
                    LogUtils.addMessage("* " + requiredItem.getFirst() + " x" + requiredItem.getSecond(), EnumChatFormatting.GOLD);
                }
                currentState = States.OPEN_BZ;
                break;
            case OPEN_BZ:
                if (!waitTimer.hasReached(400)) return;
                mc.thePlayer.sendChatMessage("/bz");
                currentState = States.CLICK_SEARCH;
                break;
            case CLICK_SEARCH:
                if (!waitTimer.hasReached(400)) return;
                if (!(mc.currentScreen instanceof GuiChest)) return;
                if (mc.thePlayer.openContainer == null ||
                    mc.thePlayer.openContainer.getSlot(45) == null ||
                    mc.thePlayer.openContainer.getSlot(45).getStack() == null ||
                    !mc.thePlayer.openContainer.getSlot(45).getStack().getDisplayName().contains("Search")) return;

                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 45, 0, 0, mc.thePlayer);
                currentState = States.INPUT_SEARCH_VALUE;
                waitTimer.reset();
                break;
            case INPUT_SEARCH_VALUE:
                if (!waitTimer.hasReached(400)) return;
                if (!(mc.currentScreen instanceof GuiEditSign)) return;

                Method m;
                try {
                    m = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredMethod("func_73869_a", char.class, int.class);
                } catch (NoSuchMethodException e) {
                    try {
                        m = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredMethod("keyTyped", char.class, int.class);
                    } catch (NoSuchMethodException e1) {
                        throw new RuntimeException(e1);
                    }
                }
                try {
                    m.setAccessible(true);
                    m.invoke(mc.currentScreen, '\r', 14);
                    m.invoke(mc.currentScreen, '\r', 14);
                    m.invoke(mc.currentScreen, '\r', 14);
                    int i = 0;
                    for (char c : requiredItems.get(0).getFirst().toCharArray()) {
                        if (i > 14) break;
                        m.invoke(mc.currentScreen, c, 16);
                        i++;
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                currentState = States.CONFIRM_SEARCH_VALUE;
                waitTimer.reset();
                break;
            case CONFIRM_SEARCH_VALUE:
                if (!waitTimer.hasReached(400)) return;
                if (!(mc.currentScreen instanceof GuiEditSign)) return;

                try {
                    m = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredMethod("func_73869_a", char.class, int.class);
                } catch (NoSuchMethodException e) {
                    try {
                        m = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredMethod("keyTyped", char.class, int.class);
                    } catch (NoSuchMethodException e1) {
                        throw new RuntimeException(e1);
                    }
                }

                try {
                    m.setAccessible(true);
                    m.invoke(mc.currentScreen, '\r', 1);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                currentState = States.SELECT_PRODUCT;
                waitTimer.reset();
                break;
            case SELECT_PRODUCT:
                if (!waitTimer.hasReached(500)) return;
                if (!(mc.currentScreen instanceof GuiChest)) return;
                if (mc.thePlayer.openContainer == null ||
                    mc.thePlayer.openContainer.getSlot(11) == null ||
                    mc.thePlayer.openContainer.getSlot(11).getStack() == null ||
                    mc.thePlayer.openContainer.getSlot(11).getStack().getDisplayName().isEmpty()) return;

                boolean found = false;


                for (Slot item : mc.thePlayer.openContainer.inventorySlots) {
                    if (item == null) continue;
                    if (item.getStack() == null) continue;
                    if (StringUtils.stripControlCodes(item.getStack().getDisplayName()).equals(requiredItems.get(0).getFirst())) {
                        mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, item.slotNumber, 0, 0, mc.thePlayer);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (findingItemTries > 5) {
                        LogUtils.addMessage("Could not find " + requiredItems.get(0).getFirst() + "!", EnumChatFormatting.RED);
                        this.toggle();
                    } else {
                        findingItemTries++;
                        waitTimer.reset();
                    }
                    return;
                }
                currentState = States.CLICK_BIN;
                waitTimer.reset();
                break;
            case CLICK_BIN:
                if (!waitTimer.hasReached(400)) return;
                if (!(mc.currentScreen instanceof GuiChest)) return;
                if (mc.thePlayer.openContainer == null ||
                    mc.thePlayer.openContainer.getSlot(10) == null ||
                    mc.thePlayer.openContainer.getSlot(10).getStack() == null ||
                    !mc.thePlayer.openContainer.getSlot(10).getStack().getDisplayName().contains("Buy Instantly")) return;

                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 10, 0, 0, mc.thePlayer);
                currentState = States.CLICK_CUSTOM_AMOUNT;
                waitTimer.reset();
                break;
            case CLICK_CUSTOM_AMOUNT:
                if (!waitTimer.hasReached(400)) return;
                if (!(mc.currentScreen instanceof GuiChest)) return;
                if (mc.thePlayer.openContainer == null ||
                    mc.thePlayer.openContainer.getSlot(16) == null ||
                    mc.thePlayer.openContainer.getSlot(16).getStack() == null ||
                    !mc.thePlayer.openContainer.getSlot(16).getStack().getDisplayName().contains("Custom Amount")) return;

                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 16, 0, 0, mc.thePlayer);
                currentState = States.INPUT_CUSTOM_AMOUNT;
                waitTimer.reset();
                break;
            case INPUT_CUSTOM_AMOUNT:
                if (!waitTimer.hasReached(400)) return;
                if (!(mc.currentScreen instanceof GuiEditSign)) return;

                try {
                    m = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredMethod("func_73869_a", char.class, int.class);
                } catch (NoSuchMethodException e) {
                    try {
                        m = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredMethod("keyTyped", char.class, int.class);
                    } catch (NoSuchMethodException e1) {
                        throw new RuntimeException(e1);
                    }
                }
                try {
                    m.setAccessible(true);
                    m.invoke(mc.currentScreen, '\r', 14);
                    m.invoke(mc.currentScreen, '\r', 14);
                    m.invoke(mc.currentScreen, '\r', 14);
                    int i = 0;
                    for (char c : String.valueOf(requiredItems.get(0).getSecond()).toCharArray()) {
                        if (i > 14) break;
                        m.invoke(mc.currentScreen, c, 16);
                        i++;
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                currentState = States.CONFIRM_CUSTOM_AMOUNT;
                waitTimer.reset();
                break;
            case CONFIRM_CUSTOM_AMOUNT:
                if (!waitTimer.hasReached(400)) return;
                if (!(mc.currentScreen instanceof GuiEditSign)) return;

                try {
                    m = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredMethod("func_73869_a", char.class, int.class);
                } catch (NoSuchMethodException e) {
                    try {
                        m = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredMethod("keyTyped", char.class, int.class);
                    } catch (NoSuchMethodException e1) {
                        throw new RuntimeException(e1);
                    }
                }

                try {
                    m.setAccessible(true);
                    m.invoke(mc.currentScreen, '\r', 1);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                currentState = States.CLICK_BUY;
                waitTimer.reset();
                break;
            case CLICK_BUY:
                if (!waitTimer.hasReached(400)) return;
                if (!(mc.currentScreen instanceof GuiChest)) return;
                if (mc.thePlayer.openContainer == null ||
                    mc.thePlayer.openContainer.getSlot(13) == null ||
                    mc.thePlayer.openContainer.getSlot(13).getStack() == null ||
                    !mc.thePlayer.openContainer.getSlot(13).getStack().getDisplayName().contains("Custom Amount")) return;

                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 13, 0, 0, mc.thePlayer);
                currentState = States.WAITING_FOR_BUY;
                waitTimer.reset();
                break;
            case WAITING_FOR_BUY:
                if (!waitTimer.hasReached(400)) return;
                if (hasOpenContainer()) return;

                requiredItems.remove(0);

                if (requiredItems.isEmpty()) {
                    currentState = States.FINISHING_VISITOR_1;
                    return;
                } else {
                    currentState = States.OPEN_BZ;
                }
                waitTimer.reset();
                break;
            case FINISHING_VISITOR_1:
                if (!waitTimer.hasReached(400)) return;
                MovingObjectPosition mop = mc.objectMouseOver;
                if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    rightClick();
                    waitTimer.reset();
                    currentState = States.FINISHING_VISITOR_2;
                }
                break;
            case FINISHING_VISITOR_2:
                if (!waitTimer.hasReached(400)) return;
                rightClick();
                waitTimer.reset();
                currentState = States.FINISHING_VISITOR_3;
                break;
            case FINISHING_VISITOR_3:
                if (!waitTimer.hasReached(250)) return;
                Slot slot = mc.thePlayer.openContainer.getSlot(29);
                if (slot.getStack() == null || !slot.getStack().getDisplayName().contains("Accept Offer")) return;
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 29, 0, 0, mc.thePlayer);
                waitTimer.reset();
                currentState = States.WAITING_FOR_VISITOR;
                waitForNextVisitor = true;
                break;
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isToggled()) return;
        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (message.contains("[Bazaar] Bought ")) {
            mc.thePlayer.closeScreen();
        }
    }

    @SubscribeEvent
    public void onLastWorldRender(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (LocationUtils.currentIsland != LocationUtils.Island.GARDEN) return;
        if (!May2BeezQoL.config.highlightDeskPosition) return;
        if (deskPosition == null) return;
        RenderUtils.drawBlockBox(deskPosition, Color.RED, 1);
    }
}
