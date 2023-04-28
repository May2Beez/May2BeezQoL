package com.May2Beez.commands;

import cc.polyfrost.oneconfig.gui.OneConfigGui;
import com.May2Beez.Config.AOTVWaypointsStructs;
import com.May2Beez.May2BeezQoL;
import com.May2Beez.gui.AOTVWaypointsPage;
import com.May2Beez.utils.BlockUtils;
import com.May2Beez.utils.LogUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class AOTVWaypoints {

    private final KeyBinding keyBinding = new KeyBinding("Open Waypoints Settings", Keyboard.KEY_NEXT, "May2BeezQoL - Waypoints");
    private final KeyBinding keyBinding2 = new KeyBinding("Add current position to selected waypoint list", Keyboard.KEY_EQUALS, "May2BeezQoL - Waypoints");
    private final KeyBinding keyBinding3 = new KeyBinding("Delete current position from selected waypoint list", Keyboard.KEY_MINUS, "May2BeezQoL - Waypoints");


    public AOTVWaypoints() {
        ClientRegistry.registerKeyBinding(keyBinding);
        ClientRegistry.registerKeyBinding(keyBinding2);
        ClientRegistry.registerKeyBinding(keyBinding3);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (keyBinding.isPressed()) {
            May2BeezQoL.config.openGui();
            try {
                AOTVWaypointsPage.redrawRoutes();
                OneConfigGui.INSTANCE.openPage(May2BeezQoL.config.aotvWaypointsPage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (keyBinding2.isPressed()) {
            if (May2BeezQoL.coordsConfig.getSelectedRoute() == null) return;
            boolean added = May2BeezQoL.coordsConfig.addCoord(May2BeezQoL.coordsConfig.getSelectedRoute(), new AOTVWaypointsStructs.Waypoint(String.valueOf(May2BeezQoL.coordsConfig.getSelectedRoute().waypoints.size()), BlockUtils.getPlayerLoc().down()));
            if (added)
                LogUtils.addMessage("AOTV Waypoints - Added current position (" + BlockUtils.getPlayerLoc().getX() + ", " + BlockUtils.getPlayerLoc().getY() + ", " + BlockUtils.getPlayerLoc().getZ() + ") to selected waypoint list", EnumChatFormatting.GREEN);
            else
                LogUtils.addMessage("AOTV Waypoints - This waypoint already exists!", EnumChatFormatting.RED);
            AOTVWaypointsStructs.SaveWaypoints();
            AOTVWaypointsPage.redrawRoutes();
        }
        if (keyBinding3.isPressed()) {
            AOTVWaypointsStructs.Waypoint waypointToDelete = null;
            if (May2BeezQoL.coordsConfig.getSelectedRoute() == null) return;
            for (AOTVWaypointsStructs.Waypoint waypoint : May2BeezQoL.coordsConfig.getSelectedRoute().waypoints) {
                if (BlockUtils.getPlayerLoc().down().equals(new BlockPos(waypoint.x, waypoint.y, waypoint.z))) {
                    waypointToDelete = waypoint;
                }
            }
            if (waypointToDelete != null) {
                May2BeezQoL.coordsConfig.removeCoord(May2BeezQoL.coordsConfig.getSelectedRoute(), waypointToDelete);
                LogUtils.addMessage("AOTV Waypoints - Removed current position (" + BlockUtils.getPlayerLoc().getX() + ", " + BlockUtils.getPlayerLoc().getY() + ", " + BlockUtils.getPlayerLoc().getZ() + ") from selected waypoint list", EnumChatFormatting.GREEN);
                AOTVWaypointsStructs.SaveWaypoints();
                AOTVWaypointsPage.redrawRoutes();
            } else {
                LogUtils.addMessage("AOTV Waypoints - No waypoint found at your current position", EnumChatFormatting.RED);
            }
        }
    }
}
