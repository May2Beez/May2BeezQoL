package com.May2Beez.modules.features;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.events.ReceivePacketEvent;
import com.May2Beez.modules.Module;
import com.May2Beez.utils.LogUtils;
import com.May2Beez.utils.RotationUtils;
import com.May2Beez.utils.SkyblockUtils;
import com.May2Beez.utils.structs.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.Random;

public class FailSafes {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String[] teleportItems = new String[] {"Void", "Hyperion", "Aspect"};

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        if (!May2BeezQoL.config.stopMacrosOnWorldChange) return;

        if (May2BeezQoL.modules.stream().anyMatch(Module::isToggled)) {
            LogUtils.addMessage("Detected World Change, Stopping All Macros", EnumChatFormatting.DARK_RED);
        }
        for (Module m : May2BeezQoL.modules) {
            if (m.isToggled()) m.toggle();
        }
    }

    @SubscribeEvent
    public void onPacket2(ReceivePacketEvent event) {
        if (!May2BeezQoL.config.stopMacrosOnSwapItemCheck) return;
        if (May2BeezQoL.modules.stream().noneMatch(m -> m.isToggled() && m.disableOnFailsafe)) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!(event.packet instanceof S09PacketHeldItemChange)) return;

        May2BeezQoL.modules.forEach(m -> {
            if (m.isToggled() && m.disableOnFailsafe) m.toggle();
        });

        LogUtils.addMessage("Swap item check?", EnumChatFormatting.GOLD);
        SkyblockUtils.sendPingAlert();
    }

    @SubscribeEvent
    public void onPacket(ReceivePacketEvent event) {
        if (!May2BeezQoL.config.stopMacrosOnRotationCheck) return;
        if (May2BeezQoL.modules.stream().noneMatch(m -> m.isToggled() && m.disableOnFailsafe)) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!(event.packet instanceof S08PacketPlayerPosLook)) return;
        if (mc.thePlayer.getHeldItem() != null && Arrays.stream(teleportItems).anyMatch(i -> mc.thePlayer.getHeldItem().getDisplayName().contains(i))) return;

        May2BeezQoL.modules.forEach(m -> {
            if (m.isToggled() && m.disableOnFailsafe) m.toggle();
        });

        LogUtils.addMessage("Rotation check?", EnumChatFormatting.GOLD);
        SkyblockUtils.sendPingAlert();

        if (May2BeezQoL.config.fakeMoveAfterRotationCheck) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                RotationUtils.smoothLook(new Rotation(mc.thePlayer.rotationYaw - (new Random().nextInt(80) - 40), mc.thePlayer.rotationPitch - (new Random().nextInt(50) - 25)), 340);
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                RotationUtils.smoothLook(new Rotation(mc.thePlayer.rotationYaw - (new Random().nextInt(80) - 40), mc.thePlayer.rotationPitch - (new Random().nextInt(50) - 25)), 270);
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                RotationUtils.smoothLook(new Rotation(mc.thePlayer.rotationYaw - (new Random().nextInt(80) - 40), mc.thePlayer.rotationPitch - (new Random().nextInt(50) - 25)), 500);
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                RotationUtils.smoothLook(new Rotation(mc.thePlayer.rotationYaw - (new Random().nextInt(30) - 15), 60 - (new Random().nextInt(10) - 5)), 450);
            }).start();
        }
    }
}
