package com.May2Beez.commands;

import com.May2Beez.utils.LogUtils;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UseCooldown implements ICommand {
    public static HashMap<String, Integer> RCitems = new HashMap<>();
    public static HashMap<String, Integer> LCitems = new HashMap<>();

    @Override
    public String getCommandName() {
        return "usecooldown";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName();
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            for (String i : RCitems.keySet()) {
                LogUtils.addMessage("UseCooldown - Right click macro set on " + i + " with cooldown of " + RCitems.get(i) + " ms.", EnumChatFormatting.AQUA);
            }
            for (String i : LCitems.keySet()) {
                LogUtils.addMessage("UseCooldown - Left click macro set on " + i + " with cooldown of " + LCitems.get(i) + " ms.", EnumChatFormatting.AQUA);
            }
            saveMacros();
            return;
        }
        if (args.length == 1 && isNumeric(args[0])) {
            InventoryPlayer inv = Minecraft.getMinecraft().thePlayer.inventory;

            ItemStack curStack = inv.getStackInSlot(Minecraft.getMinecraft().thePlayer.inventory.currentItem);
            if (curStack != null) {
                int cd = Integer.parseInt(args[0]);
                if (cd == 0) {
                    RCitems.remove(curStack.getDisplayName());
                    LogUtils.addMessage("UseCooldown - Removed " + curStack.getDisplayName() + " from right click macro list.", EnumChatFormatting.AQUA);
                    saveMacros();
                    return;
                }
                if (cd < 100) {
                    LogUtils.addMessage("UseCooldown - Invalid Miliseconds, Minimum delay 100 Milisecond.", EnumChatFormatting.RED);
                    saveMacros();
                    return;
                }
                RCitems.put(curStack.getDisplayName(), cd);
                LogUtils.addMessage("UseCooldown - Added " + curStack.getDisplayName() + " to right click macro list with cooldown of " + cd + " ms.", EnumChatFormatting.AQUA);
                saveMacros();
            } else {
                LogUtils.addMessage("UseCooldown - Error getting current held item.", EnumChatFormatting.RED);
            }
        } else if (args.length == 2 && isNumeric(args[0]) && args[1].equalsIgnoreCase("left")) {
            InventoryPlayer inv = Minecraft.getMinecraft().thePlayer.inventory;

            ItemStack curStack = inv.getStackInSlot(Minecraft.getMinecraft().thePlayer.inventory.currentItem);
            if (curStack != null) {
                int cd = Integer.parseInt(args[0]);
                if (cd == 0) {
                    LCitems.remove(curStack.getDisplayName());
                    LogUtils.addMessage("UseCooldown - Removed " + curStack.getDisplayName() + " from left click macro list.", EnumChatFormatting.AQUA);
                    saveMacros();
                    return;
                }
                if (cd < 100) {
                    LogUtils.addMessage("UseCooldown - Invalid Miliseconds, Minimum delay 100 Milisecond.", EnumChatFormatting.RED);
                    saveMacros();
                    return;
                }
                LogUtils.addMessage("UseCooldown - Added " + curStack.getDisplayName() + " to left click macro list with cooldown of " + cd + " ms.", EnumChatFormatting.AQUA);
                LCitems.put(curStack.getDisplayName(), cd);
                saveMacros();
            } else {
                LogUtils.addMessage("UseCooldown - Error getting current held item.", EnumChatFormatting.RED);
            }
        } else {
            LogUtils.addMessage("UseCooldown - Invalid Arguments.", EnumChatFormatting.RED);
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void saveMacros() {
        try {
            String rcjson = new Gson().toJson(RCitems);
            Files.write(Paths.get("./config/may2beez/rcmacros.json"), rcjson.getBytes(StandardCharsets.UTF_8));
            String lcjson = new Gson().toJson(LCitems);
            Files.write(Paths.get("./config/may2beez/lcmacros.json"), lcjson.getBytes(StandardCharsets.UTF_8));
        } catch (Exception error) {
            System.out.println("Error saving config file");
            error.printStackTrace();
        }
    }

    @Override
    public int compareTo(@NotNull ICommand o) {
        return 0;
    }
}
