package com.May2Beez.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class LogUtils {
    static Minecraft mc = Minecraft.getMinecraft();

    public static void addMessage(String message, EnumChatFormatting color) {
        String prefix = EnumChatFormatting.BOLD.toString() + EnumChatFormatting.LIGHT_PURPLE + "[QoL]";
        String mess = prefix + EnumChatFormatting.RESET + color + " " + message;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(mess));

    }
}
