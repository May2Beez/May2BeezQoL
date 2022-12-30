package com.May2Beez.Config;

import com.May2Beez.AOTVWaypointsGUI;
import com.May2Beez.modules.mining.AOTVMacro;
import com.May2Beez.utils.LogUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
public class CoordsConfig {

    private final ArrayList<AOTVWaypointsGUI.WaypointList> WaypointLists = new ArrayList<AOTVWaypointsGUI.WaypointList>() {{
        add(new AOTVWaypointsGUI.WaypointList("Default", true, true, new ArrayList<>()));
    }};

    private int selectedRoute = 0;

    public void setSelectedRoute(int selectedRoute) {
        this.selectedRoute = selectedRoute;
        AOTVMacro.getAOTVWaypoints();
    }

    public AOTVWaypointsGUI.WaypointList getSelectedRoute() {
        if (WaypointLists.isEmpty())
            return new AOTVWaypointsGUI.WaypointList("Default", true, true, new ArrayList<>());
        return WaypointLists.get(selectedRoute);
    }

    public String getSelectedRouteName() {
        return WaypointLists.get(selectedRoute).name;
    }

    public ArrayList<AOTVWaypointsGUI.WaypointList> getRoutes() {
        return WaypointLists;
    }

    public AOTVWaypointsGUI.WaypointList getRoute(int index) {
        return WaypointLists.get(index);
    }

    public void addRoute(String name) {
        WaypointLists.add(new AOTVWaypointsGUI.WaypointList(name));
        AOTVWaypointsGUI.SaveWaypoints();
    }

    public void addCoord(AOTVWaypointsGUI.WaypointList wp, AOTVWaypointsGUI.Waypoint pos) {
        int index = WaypointLists.indexOf(wp);
        AOTVWaypointsGUI.WaypointList inner = WaypointLists.get(index);
        if (wp.waypoints.stream().anyMatch(p -> p.x == pos.x && p.y == pos.y && p.z == pos.z)) {
            LogUtils.addMessage("AOTV Waypoints - This waypoint already exists!", EnumChatFormatting.RED);
            return;
        }
        inner.waypoints.add(pos);
        WaypointLists.set(index, inner);
        AOTVWaypointsGUI.SaveWaypoints();
    }

    public void removeCoord(AOTVWaypointsGUI.WaypointList wp, AOTVWaypointsGUI.Waypoint waypoint) {
        int index = WaypointLists.indexOf(wp);
        AOTVWaypointsGUI.WaypointList inner = WaypointLists.get(index);
        inner.waypoints.remove(waypoint);
        WaypointLists.set(index, inner);
        AOTVWaypointsGUI.SaveWaypoints();
    }

    public void removeRoute(AOTVWaypointsGUI.WaypointList index) {
        WaypointLists.remove(index);
        AOTVWaypointsGUI.SaveWaypoints();
    }
}
