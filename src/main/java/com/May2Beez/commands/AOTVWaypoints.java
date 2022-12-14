package com.May2Beez.commands;

import com.May2Beez.AOTVWaypointsGUI;
import com.May2Beez.SkyblockMod;
import com.May2Beez.utils.BlockUtils;
import com.May2Beez.utils.SkyblockUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class AOTVWaypoints implements ICommand {

    private final KeyBinding keyBinding = new KeyBinding("Open Waypoints Settings", Keyboard.KEY_HOME, SkyblockMod.MODID + " - General");
    private final KeyBinding keyBinding2 = new KeyBinding("Add current position to selected waypoint list", Keyboard.KEY_EQUALS, SkyblockMod.MODID + " - General");
    private final KeyBinding keyBinding3 = new KeyBinding("Delete current position from selected waypoint list", Keyboard.KEY_MINUS, SkyblockMod.MODID + " - General");


    public AOTVWaypoints() {
        ClientRegistry.registerKeyBinding(keyBinding);
        ClientRegistry.registerKeyBinding(keyBinding2);
        ClientRegistry.registerKeyBinding(keyBinding3);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (keyBinding.isPressed()) {
            SkyblockMod.display = new AOTVWaypointsGUI();
        }
        if (keyBinding2.isPressed()) {
            SkyblockMod.coordsConfig.addCoord(SkyblockMod.coordsConfig.getSelectedRoute(), new AOTVWaypointsGUI.Waypoint(String.valueOf(SkyblockMod.coordsConfig.getSelectedRoute().waypoints.size()), BlockUtils.getPlayerLoc().down()));
        }
        if (keyBinding3.isPressed()) {
            AOTVWaypointsGUI.Waypoint waypointToDelete = null;
            for (AOTVWaypointsGUI.Waypoint waypoint : SkyblockMod.coordsConfig.getSelectedRoute().waypoints) {
                if (BlockUtils.getPlayerLoc().down().equals(new BlockPos(waypoint.x, waypoint.y, waypoint.z))) {
                    waypointToDelete = waypoint;
                }
            }
            if (waypointToDelete != null)
                SkyblockMod.coordsConfig.removeCoord(SkyblockMod.coordsConfig.getSelectedRoute(), waypointToDelete);
            else {
                SkyblockUtils.SendInfo("No waypoint found at your current position", false, "AOTV Waypoints - ");
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
        SkyblockMod.display = new AOTVWaypointsGUI();
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
