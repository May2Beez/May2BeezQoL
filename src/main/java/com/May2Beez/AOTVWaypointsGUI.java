package com.May2Beez;

import com.May2Beez.Config.CoordsConfig;
import com.May2Beez.utils.BlockUtils;
import gg.essential.elementa.ElementaVersion;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.WindowScreen;
import gg.essential.elementa.components.ScrollComponent;
import gg.essential.elementa.components.UIContainer;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.components.input.UITextInput;
import gg.essential.elementa.constraints.*;
import gg.essential.elementa.effects.OutlineEffect;
import gg.essential.vigilance.gui.settings.ButtonComponent;
import gg.essential.vigilance.gui.settings.CheckboxComponent;
import kotlin.Unit;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class AOTVWaypointsGUI extends WindowScreen {

    public static class Waypoint {
        public String name;
        public int x;
        public int y;
        public int z;

        public Waypoint(String name, int x, int y, int z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Waypoint(String name, BlockPos pos) {
            this.name = name;
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }
    }

    private UIComponent scrollComponent;

    public static class WaypointList {
        public boolean enabled;
        public String name;
        public boolean showCoords;
        public ArrayList<Waypoint> waypoints;

        public WaypointList(String name) {
            this.enabled = false;
            this.showCoords = false;
            this.waypoints = new ArrayList<>();
            this.name = name;
        }

        public WaypointList(String name, boolean enabled, boolean showCoords, ArrayList<Waypoint> waypoints) {
            this.enabled = enabled;
            this.showCoords = showCoords;
            this.waypoints = waypoints;
            this.name = name;
        }
    }

    public AOTVWaypointsGUI() {
        super(ElementaVersion.V2, false, true, true);
        getWindow().clearChildren();
        LoadWaypoints();
        ShowGUI();
    }

    public static void SaveWaypoints() {
        // Save waypoints to config file
        String json = May2BeezQoL.gson.toJson(May2BeezQoL.coordsConfig);
        try {
            Files.write(Paths.get("./config/may2beez/aotv_coords.json"), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void LoadWaypoints() {
        // Load waypoints from config file
        try {
            String json = new String(Files.readAllBytes(Paths.get("./config/may2beez/aotv_coords.json")), StandardCharsets.UTF_8);
            May2BeezQoL.coordsConfig = May2BeezQoL.gson.fromJson(json, CoordsConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onScreenClose() {
        SaveWaypoints();
        super.onScreenClose();
    }

    private void ShowGUI() {
        UIComponent topContainer = new UIContainer()
                .setWidth(new PixelConstraint(getWindow().getWidth()))
                .setHeight(new PixelConstraint(getWindow().getHeight() * 0.15f))
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(0))
                .setChildOf(getWindow());

        new UIText("AOTV Waypoints")
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(10))
                .setChildOf(topContainer);

        new ButtonComponent("Add New Waypoint List", this::AddNewWaypointListHandler)
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(40))
                .setChildOf(topContainer);


        scrollComponent = new ScrollComponent("", 10f)
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(topContainer.getHeight() + 20))
                .setHeight(new PixelConstraint(getWindow().getHeight() * 0.75f))
                .setWidth(new PixelConstraint(getWindow().getWidth() * 0.9f))
                .setColor(new Color(0, 0, 0, 0.8f))
                .enableEffect(new OutlineEffect(new Color(0, 0, 0, 0.8f), 1f))
                .setChildOf(getWindow());


        for (WaypointList wl : May2BeezQoL.coordsConfig.getRoutes()) {
            addNewWaypointList(wl);
            if (wl.showCoords) {
                for (Waypoint w : wl.waypoints) {
                    AddNewWaypoint(wl, w);
                }
            }
        }

    }

    private void addNewWaypointList(WaypointList wp) {
        UIComponent container = new UIContainer()
                .setChildOf(scrollComponent)
                .setX(new CenterConstraint())
                .setY(new SiblingConstraint(5f))
                .setWidth(new PixelConstraint(scrollComponent.getWidth() * 0.75f))
                .setHeight(new ChildBasedSizeConstraint())
                .enableEffect(new OutlineEffect(new Color(1f, 1f, 1f, 0.4f), 1f));

        UIComponent enableButton = new CheckboxComponent(wp.enabled)
                .setX(new PixelConstraint(5f))
                .setY(new PixelConstraint(10f))
                .setChildOf(container);

        enableButton.onMouseClick((mouseButton, clickType) -> {
            if (clickType.getMouseButton() == 0) {
                May2BeezQoL.coordsConfig.getRoutes().forEach(wl -> wl.enabled = false);
                int index = May2BeezQoL.coordsConfig.getRoutes().indexOf(wp);
                wp.enabled = !wp.enabled;
                May2BeezQoL.coordsConfig.setSelectedRoute(index);
                UpdateGUI();
            }
            return Unit.INSTANCE;
        });

        new UITextInput(wp.name + " (" + wp.waypoints.size() + " waypoints)")
                .setTextScale(new PixelConstraint(1))
                .setX(new SiblingConstraint(12.5f))
                .setY(new PixelConstraint(10f))
                .setHeight(new PixelConstraint(24f))
                .setWidth(new PixelConstraint(scrollComponent.getWidth() * 0.55f))
                .onMouseClick((component, clickType) -> {
                    if (clickType.getMouseButton() == 1) {
                        component.grabWindowFocus();
                    }
                    return Unit.INSTANCE;
                })
                .onKeyType((component, character, integer) -> {
                    wp.name = ((UITextInput) component).getText();
                    return Unit.INSTANCE;
                })
                .onFocusLost((component) -> {
                    setText(wp.name, true);
                    return Unit.INSTANCE;
                })
                .setChildOf(container);


        UIComponent expandButton = new ButtonComponent("Expand", () -> Unit.INSTANCE)
                .setX(new PixelConstraint(5f, true))
                .setY(new PixelConstraint(10f))
                .setChildOf(container);

        expandButton.onMouseClick((mouseButton, clickType) -> {
            if (clickType.getMouseButton() == 0) {
                May2BeezQoL.coordsConfig.getRoutes().forEach(wl -> {
                    if (wl == wp) {
                        return;
                    }
                    if (wl.showCoords) {
                        wl.showCoords = false;
                    }
                });
                wp.showCoords = !wp.showCoords;
                UpdateGUI();
            }
            return Unit.INSTANCE;
        });

        UIComponent deleteButton = new ButtonComponent("Delete", () -> Unit.INSTANCE)
                .setX(new SiblingConstraint(5f, true))
                .setY(new PixelConstraint(10f))
                .setChildOf(container);

        deleteButton.onMouseClick((mouseButton, clickType) -> {
            if (clickType.getMouseButton() == 0) {
                May2BeezQoL.coordsConfig.removeRoute(wp);
                UpdateGUI();
            }
            return Unit.INSTANCE;
        });

        UIComponent addWaypointButton = new ButtonComponent("Add Waypoint", () -> Unit.INSTANCE)
                .setX(new SiblingConstraint(5, true))
                .setY(new PixelConstraint(10f))
                .setChildOf(container);

        addWaypointButton.onMouseClick((mouseButton, clickType) -> {
            May2BeezQoL.coordsConfig.getRoutes().forEach(wl -> wl.showCoords = false);
            wp.showCoords = true;
            if (clickType.getMouseButton() == 0) {
                May2BeezQoL.coordsConfig.addCoord(wp, new Waypoint(String.valueOf(wp.waypoints.size()), BlockUtils.getPlayerLoc().down()));
            }
            UpdateGUI();
            return Unit.INSTANCE;
        });

        scrollComponent.addChild(container);
    }

    private void AddNewWaypoint(WaypointList wl, Waypoint w) {
        UIComponent container = new UIContainer()
                .setChildOf(scrollComponent)
                .setX(new CenterConstraint())
                .setY(new SiblingConstraint(5f))
                .setWidth(new PixelConstraint(scrollComponent.getWidth() * 0.5f))
                .setHeight(new ChildBasedRangeConstraint())
                .enableEffect(new OutlineEffect(new Color(1f, 1f, 1f, 0.4f), 1f));

        new UITextInput(w.name)
                .setTextScale(new PixelConstraint(1.5f))
                .setX(new PixelConstraint(5f))
                .setY(new PixelConstraint(10f))
                .setHeight(new PixelConstraint(24f))
                .setWidth(new PixelConstraint(scrollComponent.getWidth() * 0.35f))
                .onMouseClick((component, clickType) -> {
                    if (clickType.getMouseButton() == 1) {
                        component.grabWindowFocus();
                    }
                    return Unit.INSTANCE;
                })
                .onKeyType((component, character, integer) -> {
                    w.name = ((UITextInput) component).getText();
                    return Unit.INSTANCE;
                })
                .onFocusLost((component) -> {
                    setText(w.name, true);
                    return Unit.INSTANCE;
                })
                .setChildOf(container);

        new UITextInput(String.valueOf(w.x))
                .setTextScale(new PixelConstraint(1))
                .setX(new PixelConstraint(5f))
                .setY(new SiblingConstraint(5f))
                .setHeight(new PixelConstraint(24f))
                .setWidth(new PixelConstraint(scrollComponent.getWidth() * 0.55f))
                .onMouseClick((component, clickType) -> {
                    if (clickType.getMouseButton() == 1) {
                        component.grabWindowFocus();
                    }
                    return Unit.INSTANCE;
                })
                .onKeyType((component, character, integer) -> {
                    try {
                        w.x = Integer.parseInt(((UITextInput) component).getText());
                    } catch (NumberFormatException e) {
                        w.x = 0;
                    }
                    return Unit.INSTANCE;
                })
                .onFocusLost((component) -> {
                    setText(String.valueOf(w.x), true);
                    return Unit.INSTANCE;
                })
                .setChildOf(container);

        new UITextInput(String.valueOf(w.y))
                .setTextScale(new PixelConstraint(1))
                .setX(new PixelConstraint(5f))
                .setY(new SiblingConstraint(5f))
                .setHeight(new PixelConstraint(24f))
                .setWidth(new PixelConstraint(scrollComponent.getWidth() * 0.55f))
                .onMouseClick((component, clickType) -> {
                    if (clickType.getMouseButton() == 1) {
                        component.grabWindowFocus();
                    }
                    return Unit.INSTANCE;
                })
                .onKeyType((component, character, integer) -> {
                    try {
                        w.y = Integer.parseInt(((UITextInput) component).getText());
                    } catch (NumberFormatException e) {
                        w.y = 0;
                    }
                    return Unit.INSTANCE;
                })
                .onFocusLost((component) -> {
                    setText(String.valueOf(w.y), true);
                    return Unit.INSTANCE;
                })
                .setChildOf(container);

        new UITextInput(String.valueOf(w.z))
                .setTextScale(new PixelConstraint(1))
                .setX(new PixelConstraint(5f))
                .setY(new SiblingConstraint(5f))
                .setHeight(new PixelConstraint(24f))
                .setWidth(new PixelConstraint(scrollComponent.getWidth() * 0.55f))
                .onMouseClick((component, clickType) -> {
                    if (clickType.getMouseButton() == 1) {
                        component.grabWindowFocus();
                    }
                    return Unit.INSTANCE;
                })
                .onKeyType((component, character, integer) -> {
                    try {
                        w.z = Integer.parseInt(((UITextInput) component).getText());
                    } catch (NumberFormatException e) {
                        w.z = 0;
                    }
                    return Unit.INSTANCE;
                })
                .onFocusLost((component) -> {
                    setText(String.valueOf(w.z), true);
                    return Unit.INSTANCE;
                })
                .setChildOf(container);


        UIComponent deleteButton = new ButtonComponent("Delete", () -> Unit.INSTANCE)
                .setX(new PixelConstraint(5f, true))
                .setY(new PixelConstraint(10f))
                .setChildOf(container);

        deleteButton.onMouseClick((mouseButton, clickType) -> {
            if (clickType.getMouseButton() == 0) {
                May2BeezQoL.coordsConfig.removeCoord(wl, w);
                UpdateGUI();
            }
            return Unit.INSTANCE;
        });

        scrollComponent.addChild(container);
    }

    private void UpdateGUI() {
        getWindow().clearChildren();
        ShowGUI();
    }

    private Unit AddNewWaypointListHandler() {
        System.out.println("Add New Waypoint List");
        May2BeezQoL.coordsConfig.addRoute("Waypoint List " + (May2BeezQoL.coordsConfig.getSelectedRoute() != null ? May2BeezQoL.coordsConfig.getSelectedRoute().waypoints.size() : 1));
        UpdateGUI();
        return Unit.INSTANCE;
    }
}
