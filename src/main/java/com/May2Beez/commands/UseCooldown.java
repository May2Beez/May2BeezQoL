package com.May2Beez.commands;

import com.May2Beez.utils.SkyblockUtils;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UseCooldown implements ICommand {
    public static HashMap<String, Integer> RCitems = new HashMap<String, Integer>();
    public static HashMap<String, Integer> LCitems = new HashMap<String, Integer>();

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
                SkyblockUtils.SendInfo("§7Right click macro set on " + i + " §7with cooldown of " + RCitems.get(i) + " ms.", true, "UseCooldown -");
            }
            for (String i : LCitems.keySet()) {
                SkyblockUtils.SendInfo("§7Left click macro set on " + i + " §7with cooldown of " + LCitems.get(i) + " ms.", true, "UseCooldown -");
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
                    SkyblockUtils.SendInfo("§aSuccessfully Removed " + curStack.getDisplayName() + "§a.", true, "UseCooldown -");
                    saveMacros();
                    return;
                }
                if (cd < 100) {
                    SkyblockUtils.SendInfo("§cInvalid Miliseconds, Minimum delay 100 Milisecond.", false, "UseCooldown -");
                    saveMacros();
                    return;
                }
                RCitems.put(curStack.getDisplayName(), cd);
                SkyblockUtils.SendInfo("§aSuccessfully Added " + curStack.getDisplayName() + "§a to right click with a delay of " + cd + " ms.", true, "UseCooldown -");
                saveMacros();
            } else {
                SkyblockUtils.SendInfo("§cError getting current held item.", false, "UseCooldown -");
            }
        } else if (args.length == 2 && isNumeric(args[0]) && args[1].equalsIgnoreCase("left")) {
            InventoryPlayer inv = Minecraft.getMinecraft().thePlayer.inventory;

            ItemStack curStack = inv.getStackInSlot(Minecraft.getMinecraft().thePlayer.inventory.currentItem);
            if (curStack != null) {
                int cd = Integer.parseInt(args[0]);
                if (cd == 0) {
                    LCitems.remove(curStack.getDisplayName());
                    SkyblockUtils.SendInfo("§aSuccessfully Removed " + curStack.getDisplayName() + "§a.", true, "UseCooldown -");
                    saveMacros();
                    return;
                }
                if (cd < 100) {
                    SkyblockUtils.SendInfo("§cInvalid Miliseconds, Minimum delay 100 Milisecond.", false, "UseCooldown - ");
                    saveMacros();
                    return;
                }
                SkyblockUtils.SendInfo("§aSuccessfully Added " + curStack.getDisplayName() + "§a to left click with a delay of " + cd + " ms.", true, "UseCooldown - ");
                LCitems.put(curStack.getDisplayName(), cd);
                saveMacros();
            } else {
                SkyblockUtils.SendInfo("§cError getting current held item.", false, "UseCooldown - ");
            }
        } else {
            SkyblockUtils.SendInfo("§cInvalid Arguments.", false, "UseCooldown - ");
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
