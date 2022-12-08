package com.May2Beez.Config;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.Comparator;

public class Config extends Vigilant {

    private final String MITHRIL_MINER = "Mithril Miner";
    private final String GHOST_GRINDER = "Ghost Grinder";
    private final String HARDSTONE_NUKER = "Hardstone Nuker";
    private final String FARMING_MACRO = "Farming Macro";
    private final String FARMING_NUKER = "Farming Nuker";
    private final String AUTO_PLANT_CROPS = "Auto Plant Crops";
    private final String FORAGING_MACRO = "Foraging Macro";
    private final String FISHING = "Fishing Macro";
    private final String MINING = "Mining";
    private final String AOTV_MACRO = "AOTV Macro";


    //region Mithril Miner
    @Property(type = PropertyType.SLIDER, name = "Max Break Time", category = MITHRIL_MINER,  min = 0, max = 500)
    public int maxBreakTime = 80;

    @Property(type = PropertyType.SWITCH, name = "Prioritize Titanium", category = MITHRIL_MINER)
    public boolean prioTitanium = false;

    @Property(type = PropertyType.SLIDER, name = "Panic alert", category = MITHRIL_MINER, min = 0, max = 300)
    public int panic = 100;

    @Property(type = PropertyType.SLIDER, name = "Walking frequency &", category = MITHRIL_MINER, min = 0, max = 100)
    public int walking = 1;

    @Property(type = PropertyType.SLIDER, name = "Walking time", category = MITHRIL_MINER, min = 0, max = 20)
    public int walkingTime = 1;

    @Property(type = PropertyType.SWITCH, name = "Use sneak key", category = MITHRIL_MINER)
    public boolean sneak = true;

    @Property(type = PropertyType.SELECTOR, name = "Mode", category = MITHRIL_MINER, options = {"Wool", "Clay", "Prismarine", "Gold", "Blue"})
    public int mode = 0;

    //endregion

    //region General
    @Property(type = PropertyType.DECIMAL_SLIDER, decimalPlaces = 1, name = "Camera speed", description = "Smaller number == faster", category = "General", minF = 0, maxF = 10)
    public float cameraSpeed = 3;

    //endregion

    //region Ghost Grinder
    @Property(type = PropertyType.SLIDER, name = "Line width", category = GHOST_GRINDER, min = 1, max = 10)
    public int lineWidth = 2;

    @Property(type = PropertyType.SLIDER, name = "Radius", category = GHOST_GRINDER, min = 0, max = 100)
    public int radius = 15;

    @Property(type = PropertyType.SLIDER, name = "Click delay", category = GHOST_GRINDER, min = 1, max = 20)
    public int clickDelay = 5;

    //endregion

    //region Hardstone Nuker
    @Property(type = PropertyType.SELECTOR, name = "Hardstone Nuker Shape", description = "Choose which pattern hardstone nuker will follow",
            category = HARDSTONE_NUKER, options = {"Closest Block", "Facing Axis"})
    public int hardIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Hardstone Nuker Height", description = "Range to break above the player",
            category = HARDSTONE_NUKER, max = 5)
    public int hardrange = 0;

    @Property(type = PropertyType.SLIDER, name = "Hardstone Nuker Depth", description = "Range to break below the player",
            category = HARDSTONE_NUKER, max = 3)
    public int hardrangeDown = 0;

    @Property(type = PropertyType.SWITCH, name = "Include Sand & Gravel", description = "Hardstone Nuker will also target sand and gravel",
            category = HARDSTONE_NUKER)
    public boolean includeExcavatable = false;

    @Property(type = PropertyType.SWITCH, name = "Include Ores", description = "Hardstone Nuker, Mithril Nuker and Mithril Macro will also target ores",
            category = HARDSTONE_NUKER)
    public boolean includeOres = false;

    //endregion

    //region Farming Macro
    @Property(type = PropertyType.TEXT, name = "Left coordinates", category = FARMING_MACRO)
    public String leftWall = "0.0";

    @Property(type = PropertyType.TEXT, name = "Right coordinates", category = FARMING_MACRO)
    public String rightWall = "0.0";

    @Property(type = PropertyType.SLIDER, name = "Amount of ms to move forward", description = "Set 0 to move only left/right", category = FARMING_MACRO, min = 0, max = 5000, increment = 100)
    public int forwardMs = 0;

    @Property(type = PropertyType.SELECTOR, name = "Check X or Z", category = FARMING_MACRO, options = {"X", "Z"})
    public int XorZindex = 0;

    @Property(type = PropertyType.SELECTOR, name = "Set yaw rotation", category = FARMING_MACRO, options = {"Disable", "0", "45", "90", "135", "180", "-45", "-90", "-135", "-180"})
    public int lookingDirection = 0;

    @Property(type = PropertyType.CHECKBOX, name = "Use BACK instead of RIGHT", category = FARMING_MACRO)
    public boolean backInsteadOfRight = false;

    //endregion

    //region Farming Nuker
    @Property(type = PropertyType.SELECTOR, name = "Farming speed", category = FARMING_NUKER, options = {"40 BPS", "80 BPS"})
    public int farmingSpeedIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Farming radius", category = FARMING_NUKER, min = 1, max = 10)
    public int farmingRadius = 3;

    @Property(type = PropertyType.SELECTOR, name = "Farming Shape", description = "Choose which pattern crop nuker will follow",
            category = FARMING_NUKER, options = {"Closest Block", "Facing Axis"})
    public int farmShapeIndex = 0;

    @Property(type = PropertyType.SELECTOR, name = "Nuker Crop Type", description = "Select the type of crop you want to nuke",
            category = FARMING_NUKER, options = {"Any Crop Except Cane or Cactus", "Cane or Cactus", "Nether Wart", "Wheat", "Carrot", "Potato", "Pumpkin", "Melon", "Mushroom", "Cocoa"})
    public int farmNukeIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Down Up Range", category = FARMING_NUKER, min = 0, max = 4)
    public int downUpRange = 0;

    //endregion

    //region Auto Plant Crops
    @Property(type = PropertyType.SELECTOR, name = "Crop Type", category = AUTO_PLANT_CROPS, options = {"Cocoa Beans", "Cactus"})
    public int cropTypeIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Auto Crop Range", category = AUTO_PLANT_CROPS, min = 1, max = 8)
    public int autoCropRange = 0;

    //endregion

    //region Foraging Macro
    @Property(type = PropertyType.SLIDER, name = "Foraging Macro Delay", category = FORAGING_MACRO, min = 0, max = 500)
    public int foragingDelay = 0;

    @Property(type = PropertyType.CHECKBOX, name = "Use Fishing Rod", category = FORAGING_MACRO)
    public boolean foragingUseRod = false;

    @Property(type = PropertyType.TEXT, name = "Monkey Pet LVL", category = FORAGING_MACRO)
    public String monkeyLVL = "0";

    @Property(type = PropertyType.SLIDER, name = "Foraging Macro Pre Rod Delay", category = FORAGING_MACRO, min = 0, max = 500)
    public int preRodDelay = 0;

    @Property(type = PropertyType.CHECKBOX, name = "Use normal Bone Meal, instead of Enchanted", category = FORAGING_MACRO)
    public boolean normalBoneMeal = false;

    @Property(type = PropertyType.SLIDER, name = "Max idle", category = FORAGING_MACRO, min = 0, max = 60)
    public int maxIdleTicks = 0;

    //endregion

    //region Mining
    @Property(type = PropertyType.SWITCH, name = "Powder chest solver server rotation", category = MINING)
    public boolean solvePowderChestServerRotation = false;

    @Property(type = PropertyType.SWITCH, name = "Draw lines to every powder chest", category = MINING)
    public boolean drawLinesToPowderChests = true;

    @Property(type = PropertyType.SWITCH, name = "Use mining speed in mining macros", category = MINING)
    public boolean useMiningSpeed = false;

    //endregion

    //region Fishing Macro
    @Property(type = PropertyType.SWITCH, name = "AntiAfk", category = FISHING)
    public boolean antiAfk = false;

    @Property(type = PropertyType.SLIDER, name = "Rod Slot", category = FISHING, min = 1, max = 9)
    public int rodSlot = 1;

    @Property(type = PropertyType.SLIDER, name = "Weapon Slot", category = FISHING, min = 1, max = 9)
    public int weaponSlot = 1;

    @Property(type = PropertyType.SELECTOR, name = "Weapon Attack Mode", category = FISHING, options = {"LEFT CLICK", "RIGHT CLICK"})
    public int weaponAttackMode = 0;

    @Property(type = PropertyType.SWITCH, name = "Prioritize Killing SCs", category = FISHING)
    public boolean prioritizeSCs = false;

    @Property(type = PropertyType.SWITCH, name = "Look down when attacking", category = FISHING)
    public boolean lookDownWhenAttacking = false;

    @Property(type = PropertyType.SLIDER, name = "SC Scan Range", category = FISHING, min = 0, max = 20)
    public int scScanRange = 0;

    @Property(type = PropertyType.SWITCH, name = "Sneak while fishing", category = FISHING)
    public boolean sneakWhileFishing = true;

    //endregion

    //region AOTV Macro
    @Property(type = PropertyType.SWITCH, name = "Show highlighted route blocks", category = AOTV_MACRO, subcategory = "Drawing")
    public boolean showRouteBlocks = true;

    @Property(type = PropertyType.SWITCH, name = "Show highlighted route lines", category = AOTV_MACRO, subcategory = "Drawing")
    public boolean showRouteLines = true;

    @Property(type = PropertyType.COLOR, name = "Route block color", category = AOTV_MACRO, subcategory = "Drawing")
    public Color routeBlockColor = new Color(0, 255, 0, 120);

    @Property(type = PropertyType.COLOR, name = "Route line color", category = AOTV_MACRO, subcategory = "Drawing")
    public Color routeLineColor = new Color(0, 255, 0, 50);

    @Property(type = PropertyType.DECIMAL_SLIDER, decimalPlaces = 1, name = "Camera speed in ticks", category = AOTV_MACRO, minF = 0, maxF = 10, subcategory = "Timers")
    public float aotvCameraSpeed = 3f;

    @Property(type = PropertyType.SLIDER, name = "Stuck time threshold in ms", category = AOTV_MACRO, min = 500, max = 5000, subcategory = "Timers")
    public int aotvStuckTimeThreshold = 2000;

    @Property(type = PropertyType.SELECTOR, name = "Type of gemstone to mine", category = AOTV_MACRO, options = {"Any", "Ruby", "Amethyst", "Jade", "Sapphire", "Amber", "Topaz"})
    public int aotvGemstoneType = 0;

    @Property(type = PropertyType.SWITCH, name = "Draw blocks blocking AOTV vision", category = AOTV_MACRO, subcategory = "Drawing")
    public boolean drawBlocksBlockingAOTV = true;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "AOTV Vision blocks accuracy", decimalPlaces = 3, description = "Smaller == more checks per line between waypoints", category = AOTV_MACRO, minF = 0.01f, maxF = 0.1f, subcategory = "Drawing")
    public float aotvVisionBlocksAccuracy = 0.1f;

    @Property(type = PropertyType.COLOR, name = "AOTV Vision blocks color", category = AOTV_MACRO, subcategory = "Drawing")
    public Color aotvVisionBlocksColor = new Color(255, 0, 0, 120);

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "AOTV Waypoint targeting camera speed", decimalPlaces = 1, category = AOTV_MACRO, minF = 0, maxF = 10, subcategory = "Timers")
    public float aotvWaypointTargetingTime = 1f;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "AOTV Waypoint targeting accuracy", description = "Smaller == closer to center", decimalPlaces = 2, minF = 0.02f, maxF = 0.5f, category = AOTV_MACRO)
    public float aotvTargetingWaypointAccuracy = 0.2f;

    //endregion


    public Config() {
        super(new File("./config/may2beez/config.toml"), "May2Beez QoL", new JVMAnnotationPropertyCollector(), new ConfigSorting());
        initialize();
        addDependency("aotvVisionBlocksColor", "drawBlocksBlockingAOTV");
        addDependency("aotvVisionBlocksAccuracy", "drawBlocksBlockingAOTV");
        addDependency("routeBlockColor", "showRouteBlocks");
        addDependency("routeLineColor", "showRouteLines");
    }

    public static class ConfigSorting extends SortingBehavior {
        @NotNull
        public Comparator<Category> getCategoryComparator() {
            return (o1, o2) -> o1.getName().equals("May2Beez QoL") ? -1 : (o2.getName().equals("May2Beez QoL") ? 1 : o1.getName().compareTo(o2.getName()));
        }
    }
}
