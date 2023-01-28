package com.May2Beez.commands;

import com.May2Beez.gui.AOTVWaypointsGUI;
import com.May2Beez.May2BeezQoL;
import com.May2Beez.utils.BlockUtils;
import com.May2Beez.utils.LogUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class AOTVWaypoints implements ICommand {

    private final KeyBinding keyBinding = new KeyBinding("Open Waypoints Settings", Keyboard.KEY_NEXT, May2BeezQoL.MODID + " - General");
    private final KeyBinding keyBinding2 = new KeyBinding("Add current position to selected waypoint list", Keyboard.KEY_EQUALS, May2BeezQoL.MODID + " - General");
    private final KeyBinding keyBinding3 = new KeyBinding("Delete current position from selected waypoint list", Keyboard.KEY_MINUS, May2BeezQoL.MODID + " - General");


    public AOTVWaypoints() {
        ClientRegistry.registerKeyBinding(keyBinding);
        ClientRegistry.registerKeyBinding(keyBinding2);
        ClientRegistry.registerKeyBinding(keyBinding3);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (keyBinding.isPressed()) {
            May2BeezQoL.display = new AOTVWaypointsGUI();
        }
        if (keyBinding2.isPressed()) {
            if (May2BeezQoL.coordsConfig.getSelectedRoute() == null) return;
            boolean added = May2BeezQoL.coordsConfig.addCoord(May2BeezQoL.coordsConfig.getSelectedRoute(), new AOTVWaypointsGUI.Waypoint(String.valueOf(May2BeezQoL.coordsConfig.getSelectedRoute().waypoints.size()), BlockUtils.getPlayerLoc().down()));
            if (added)
                LogUtils.addMessage("Added current position (" + BlockUtils.getPlayerLoc().getX() + ", " + BlockUtils.getPlayerLoc().getY() + ", " + BlockUtils.getPlayerLoc().getZ() + ") to " + May2BeezQoL.coordsConfig.getSelectedRoute().name + " list", EnumChatFormatting.GREEN);
            AOTVWaypointsGUI.SaveWaypoints();
        }
        if (keyBinding3.isPressed()) {
            AOTVWaypointsGUI.Waypoint waypointToDelete = null;
            if (May2BeezQoL.coordsConfig.getSelectedRoute() == null) return;
            for (AOTVWaypointsGUI.Waypoint waypoint : May2BeezQoL.coordsConfig.getSelectedRoute().waypoints) {
                if (BlockUtils.getPlayerLoc().down().equals(new BlockPos(waypoint.x, waypoint.y, waypoint.z))) {
                    waypointToDelete = waypoint;
                }
            }
            if (waypointToDelete != null) {
                May2BeezQoL.coordsConfig.removeCoord(May2BeezQoL.coordsConfig.getSelectedRoute(), waypointToDelete);
                LogUtils.addMessage("Removed current position (" + BlockUtils.getPlayerLoc().getX() + ", " + BlockUtils.getPlayerLoc().getY() + ", " + BlockUtils.getPlayerLoc().getZ() + ") from selected waypoint list", EnumChatFormatting.RED);
                AOTVWaypointsGUI.SaveWaypoints();
            } else {
                LogUtils.addMessage("AOTV Waypoints - No waypoint found at your current position", EnumChatFormatting.RED);
            }
        }
    }

    @Override
    public String getCommandName() {
        return "waypoints";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<String>() {
            {
                add("wp");
            }
        };
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        May2BeezQoL.display = new AOTVWaypointsGUI();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(@NotNull ICommand o) {
        return 0;
    }
}
