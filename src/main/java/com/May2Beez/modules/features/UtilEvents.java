package com.May2Beez.modules.features;

import com.May2Beez.May2BeezQoL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class UtilEvents {

    private boolean joinSkyblock = false;
    private long lastWorldChange = 0;
    private final Minecraft mc = Minecraft.getMinecraft();
    public static GuiScreen display = null;


    int ticks = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        ticks++;
        if (event.phase != TickEvent.Phase.START) return;
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;

        if (display != null) {
            try {
                Minecraft.getMinecraft().displayGuiScreen(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
            display = null;
        }

        if (May2BeezQoL.customCommand.isPressed()) {
            if (May2BeezQoL.config.customCommand.startsWith("/")) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage(May2BeezQoL.config.customCommand);
            }
        }

        if (mc.currentScreen == null) {
            final ItemStack held = mc.thePlayer.inventory.getCurrentItem();
            if (held != null) {
                final String displayName = held.getDisplayName();
                if (May2BeezQoL.config.autoJoinSkyblock && joinSkyblock && displayName.equals("§aGame Menu §7(Right Click)")) {
                    joinSkyblock = false;
                    mc.thePlayer.sendChatMessage("/play sb");
                }
            }
        }

        if (May2BeezQoL.config.autoSprint) {
            if (mc.thePlayer.motionX == 0 && mc.thePlayer.motionZ == 0) return;
            KeyBinding sprint = mc.gameSettings.keyBindSprint;
            if (sprint.isKeyDown()) return;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        try {
            String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
            if (message.contains(":") || message.contains(">")) return;
            if(message.startsWith("You used your Mining Speed Boost")) {
                May2BeezQoL.miningSpeedReady = false;
                May2BeezQoL.miningSpeedActive = true;
            } else if(message.endsWith("is now available!")) {
                May2BeezQoL.miningSpeedReady = true;
            }
            if (message.endsWith("Speed Boost has expired!")) {
                May2BeezQoL.miningSpeedActive = false;
            }
        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (System.currentTimeMillis() - lastWorldChange < 1400L) return;
        lastWorldChange = System.currentTimeMillis();
        joinSkyblock = true;
    }
}
