package com.May2Beez.utils;

import dorkbox.notify.Notify;
import dorkbox.notify.Pos;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.awt.image.BufferedImage;

public class LogUtils {
    static Minecraft mc = Minecraft.getMinecraft();

    public static void addMessage(String message, EnumChatFormatting color) {
        String prefix = EnumChatFormatting.BOLD.toString() + EnumChatFormatting.LIGHT_PURPLE + "[QoL]";
        String mess = prefix + EnumChatFormatting.RESET + color + " " + message;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(mess));

    }

    public static void createNotification(String text, SystemTray tray, TrayIcon.MessageType messageType) {

        new Thread(() -> {
            if(Minecraft.isRunningOnMac) {
                Notify.create()
                        .title("May2BeezQoL Failsafes") // not enough space
                        .position(Pos.TOP_RIGHT)
                        .text(text)
                        .darkStyle()
                        .showWarning();
            } else {
                TrayIcon trayIcon = new TrayIcon(new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR), "May2BeezQoL Failsafe Notification");
                trayIcon.setToolTip("May2BeezQoL Failsafe Notification");
                try {
                    tray.add(trayIcon);
                } catch (AWTException e) {
                    throw new RuntimeException(e);
                }

                trayIcon.displayMessage("May2BeezQoL - Failsafes", text, messageType);

                tray.remove(trayIcon);
            }

        }).start();

    }
}
