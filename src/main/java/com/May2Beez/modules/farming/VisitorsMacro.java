package com.May2Beez.modules.farming;

import cc.polyfrost.oneconfig.events.event.ChatReceiveEvent;
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
        GET_ITEMS,
        CONFIRM_SIGN,
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

    public VisitorsMacro() {
        super("Visitors Macro", new KeyBinding("Visitors Macro", Keyboard.KEY_NONE, "May2Beez - Farming"));
    }

    @Override
    public void onEnable() {
        currentState = States.WAITING_FOR_VISITOR;
        if (!BlockUtils.getPlayerLoc().down().equals(deskPosition)) {
            LogUtils.addMessage("You are not standing at the desk!", EnumChatFormatting.RED);
            this.toggle();
            return;
        }

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
                currentState = States.GET_ITEMS;
                break;
            case GET_ITEMS:
                if (!waitTimer.hasReached(400)) return;
                buyingThread = new Thread(() -> {

                    if (requiredItems.isEmpty()) {
                        LogUtils.addMessage("No required items, can't finish the visitor", EnumChatFormatting.RED);
                        this.toggle();
                        return;
                    }

                    for (Tuple<String, Integer> requiredItem : requiredItems) {
                        try {
                            Thread.sleep(400);
                            mc.thePlayer.sendChatMessage("/bz");
                            while (!(mc.currentScreen instanceof GuiChest)) {
                                Thread.sleep(50);
                            }
                            Thread.sleep(100);
                            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 45, 0, 0, mc.thePlayer);
                            while (!(mc.currentScreen instanceof GuiEditSign)) {
                                Thread.sleep(50);
                            }
                            Thread.sleep(100);
                            Method m = ((GuiEditSign) mc.currentScreen).getClass().getDeclaredMethod("func_73869_a", char.class, int.class);
                            m.setAccessible(true);
                            m.invoke(mc.currentScreen, '\r', 14);
                            m.invoke(mc.currentScreen, '\r', 14);
                            m.invoke(mc.currentScreen, '\r', 14);
                            int i = 0;
                            for (char c : requiredItem.getFirst().toCharArray()) {
                                if (i > 14) break;
                                m.invoke(mc.currentScreen, c, 16);
                                i++;
                            }
                            Thread.sleep(250);
                            currentState = States.CONFIRM_SIGN;
                            m.invoke(mc.currentScreen, '\r', 1);
                            Thread.sleep(250);

                            boolean found = false;

                            for (Slot item : mc.thePlayer.openContainer.inventorySlots) {
                                if (item == null) continue;
                                if (item.getStack() == null) continue;
                                if (StringUtils.stripControlCodes(item.getStack().getDisplayName()).equals(requiredItem.getFirst())) {
                                    mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, item.slotNumber, 0, 0, mc.thePlayer);
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                LogUtils.addMessage("Could not find " + requiredItem.getFirst() + "!", EnumChatFormatting.RED);
                                this.toggle();
                                return;
                            }

                            Thread.sleep(300);

                            //click slot 10

                            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 10, 0, 0, mc.thePlayer);

                            Thread.sleep(300);
                            //click slot 16

                            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 16, 0, 0, mc.thePlayer);
                            Thread.sleep(300);

                            // write number and accept
                            m.setAccessible(true);
                            m.invoke(mc.currentScreen, '\r', 14);
                            m.invoke(mc.currentScreen, '\r', 14);
                            m.invoke(mc.currentScreen, '\r', 14);
                            for (char c : requiredItem.getSecond().toString().toCharArray()) {
                                m.invoke(mc.currentScreen, c, 16);
                            }
                            Thread.sleep(300);
                            currentState = States.CONFIRM_SIGN;
                            m.invoke(mc.currentScreen, '\r', 1);
                            Thread.sleep(300);

                            // click slot 13

                            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 13, 0, 0, mc.thePlayer);

                            while (hasOpenContainer()) {
                                Thread.sleep(50);
                            }
                        } catch (InterruptedException
                                 | InvocationTargetException | NoSuchMethodException |
                                 IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        // give it to visitor
                    }
                    waitTimer.reset();
                    currentState = States.FINISHING_VISITOR_1;
                    buyingThread = null;
                });
                buyingThread.start();
                currentState = States.BUYING;
                break;
            case BUYING:
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
                if (!waitTimer.hasReached(400)) return;
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
