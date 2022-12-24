package com.May2Beez.Config;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.Comparator;

public class Config extends Vigilant {

    private static final String MITHRIL_MINER = "Mithril Miner";
    private static final String GHOST_GRINDER = "Ghost Grinder";
    private static final String NUKER = "Nuker";
    private static final String FARMING_MACRO = "Farming Macro";
    private static final String FARMING_NUKER = "Farming Nuker";
    private static final String AUTO_PLANT_CROPS = "Auto Plant Crops";
    private static final String FORAGING_MACRO = "Foraging Macro";
    private static final String FISHING = "Fishing Macro";
    private static final String MINING = "Mining";
    private static final String AOTV_MACRO = "AOTV Macro";
    private static final String MOB_KILLER = "Mob Killer";
    private static final String ESP = "ESP";


    //region Mithril Miner
    @Property(type = PropertyType.SLIDER, name = "Max Break Time", category = MITHRIL_MINER,  min = 0, max = 5000)
    public int maxBreakTime = 2000;

    @Property(type = PropertyType.SWITCH, name = "Prioritize Titanium", category = MITHRIL_MINER)
    public boolean prioTitanium = false;

    @Property(type = PropertyType.SWITCH, name = "Clay", category = MITHRIL_MINER, subcategory = "Filter")
    public boolean filterClay = true;

    @Property(type = PropertyType.SWITCH, name = "Gray wool", category = MITHRIL_MINER, subcategory = "Filter")
    public boolean filterGrayWool = true;

    @Property(type = PropertyType.SWITCH, name = "Prismarine", category = MITHRIL_MINER, subcategory = "Filter")
    public boolean filterPrismarine = true;

    @Property(type = PropertyType.SWITCH, name = "Blue wool", category = MITHRIL_MINER, subcategory = "Filter")
    public boolean filterBlueWool = true;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Scan range", category = MITHRIL_MINER, minF = 0, maxF = 5.5f)
    public float scanRange = 4.5f;

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
    @Property(type = PropertyType.SELECTOR, name = "Nuker Shape", description = "Choose which pattern hardstone nuker will follow",
            category = NUKER, options = {"Sphere", "Facing Axis", "Axis Tunnels"})
    public int nukerShape = 0;

    @Property(type = PropertyType.SLIDER, name = "Speed", category = NUKER, min = 10, max = 80, increment = 10)
    public int nukerSpeed = 50;

    @Property(type = PropertyType.SLIDER, name = "Pingless reset cutoff", category = NUKER, min = 0, max = 20)
    public int nukerPinglessCutoff = 10;

    @Property(type = PropertyType.SWITCH, name = "Mine Blocks In Front", description = "Mine all blocks in the way of the player", category = NUKER)
    public boolean mineBlocksInFront = false;

    @Property(type = PropertyType.SWITCH, name = "Smooth Server Side Rotation", description = "Smoothly rotate the player server side", category = NUKER)
    public boolean smoothServerSideRotations = false;

    @Property(type = PropertyType.SELECTOR, name = "Pause nuker when solving", description = "Choose which mode hardstone nuker will use", category = NUKER, options = {"Don't", "Pause Nuker", "Pause Nuker Rotations"})
    public int powderChestPauseNukerMode = 0;

    @Property(type = PropertyType.SLIDER, name = "Field of View", description = "Change fov of sphere shape nuker", min = 0, max = 360, increment = 20, category = NUKER)
    public int nukerFieldOfView = 180;

    @Property(type = PropertyType.SELECTOR, name = "Algorithm", category = NUKER, options = {"Closest Block (Classic)", "Smallest Rotation (NEW!)"})
    public int nukerAlgorithm = 0;

    @Property(type = PropertyType.SLIDER, name = "Hardstone Nuker Height", description = "Range to break above the player",
            category = NUKER, max = 5)
    public int nukerHeight = 0;

    @Property(type = PropertyType.SLIDER, name = "Hardstone Nuker Depth", description = "Range to break below the player",
            category = NUKER, max = 3)
    public int nukerDepth = 0;

    @Property(type = PropertyType.SWITCH, name = "Server Side Rotation", category = NUKER)
    public boolean serverSideNukerRotations = false;

    @Property(type = PropertyType.SWITCH, name = "Hardstone", category = NUKER, subcategory = "FILTER")
    public boolean filterHardstone = true;

    @Property(type = PropertyType.SWITCH, name = "Gemstones", category = NUKER, subcategory = "FILTER")
    public boolean filterGemstones = true;

    @Property(type = PropertyType.SWITCH, name = "Mithril", category = NUKER, subcategory = "FILTER")
    public boolean filterMithril = true;

    @Property(type = PropertyType.SWITCH, name = "Stone", category = NUKER, subcategory = "FILTER")
    public boolean filterStone = true;

    @Property(type = PropertyType.SWITCH, name = "Excavatable", category = NUKER, subcategory = "FILTER")
    public boolean filterExcavatable = true;

    @Property(type = PropertyType.SWITCH, name = "Gold", category = NUKER, subcategory = "FILTER")
    public boolean filterGold = true;

    @Property(type = PropertyType.SWITCH, name = "Ores", category = NUKER, subcategory = "FILTER")
    public boolean filterOres = true;

    @Property(type = PropertyType.SWITCH, name = "Sand", category = NUKER, subcategory = "FILTER")
    public boolean filterSand = true;

    @Property(type = PropertyType.SWITCH, name = "Wood", category = NUKER, subcategory = "FILTER")
    public boolean filterWood = true;

    @Property(type = PropertyType.SWITCH, name = "Crops", category = NUKER, subcategory = "FILTER")
    public boolean filterCrops = true;

    @Property(type = PropertyType.SWITCH, name = "Glowstone", category = NUKER, subcategory = "FILTER")
    public boolean filterGlowstone = true;

    @Property(type = PropertyType.SWITCH, name = "Ice", category = NUKER, subcategory = "FILTER")
    public boolean filterIce = true;

    @Property(type = PropertyType.SWITCH, name = "Netherrack", category = NUKER, subcategory = "FILTER")
    public boolean filterNetherrack = true;

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

    @Property(type = PropertyType.SLIDER, name = "Powder chest server side rotation speed", category = MINING, min = 1, max = 10)
    public int powderChestServerRotationSpeed = 3;

    @Property(type = PropertyType.SWITCH, name = "Use mining speed in mining macros", category = MINING)
    public boolean useMiningSpeed = false;

    @Property(type = PropertyType.SLIDER, name = "Camera speed in ms", category = MINING, min = 0, max = 1000)
    public int cameraSpeed = 120;

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

    @Property(type = PropertyType.SLIDER, name = "Camera speed in ms", category = AOTV_MACRO, min = 0, max = 1000, subcategory = "Timers")
    public int aotvCameraSpeed = 120;

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

    @Property(type = PropertyType.SLIDER, name = "AOTV Waypoint targeting camera speed in ms", category = AOTV_MACRO, min = 0, max = 1000, subcategory = "Timers")
    public int aotvWaypointTargetingTime = 100;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "AOTV Waypoint targeting accuracy", description = "Smaller == closer to center", decimalPlaces = 2, minF = 0.02f, maxF = 0.5f, category = AOTV_MACRO)
    public float aotvTargetingWaypointAccuracy = 0.2f;

    //endregion


    //region MobKiller

    @Property(type = PropertyType.SLIDER, min = 1, max = 30, name = "Mob Killer scan range", category = MOB_KILLER)
    public int mobKillerScanRange = 10;

    @Property(type = PropertyType.TEXT, name = "Mob's names to kill", category = MOB_KILLER)
    public String mobsNames = "";

    @Property(type = PropertyType.SLIDER, name = "Mob Killer camera speed in ms", category = MOB_KILLER, min = 0, max = 1000)
    public int mobKillerCameraSpeed = 120;

    //endregion

    //region ESP

    @Property(type = PropertyType.SWITCH, name = "Draw ESP around mobs", category = ESP)
    public boolean mobEsp = true;

    @Property(type = PropertyType.COLOR, name = "ESP color", category = ESP)
    public Color espColor = new Color(255, 0, 0, 120);

    @Property(type = PropertyType.SWITCH, name = "Draw ESP mob names", category = ESP)
    public boolean drawMobNames = true;

    @Property(type = PropertyType.SWITCH, name = "Draw ESP around chests", category = ESP)
    public boolean chestEsp = true;

    @Property(type = PropertyType.COLOR, name = "Chest ESP color", category = ESP)
    public Color chestEspColor = new Color(208, 104, 0, 120);

    @Property(type = PropertyType.SWITCH, name = "Draw ESP around gemstones", category = ESP)
    public boolean gemstoneEsp = true;

    @Property(type = PropertyType.SLIDER, name = "Gemstone ESP alpha", category = ESP, min = 0, max = 255)
    public int gemstoneEspAlpha = 80;

    @Property(type = PropertyType.SLIDER, name = "ESP range", category = ESP, min = 0, max = 40)
    public int espRange = 10;


    //endregion


    //region DEBUG

    @Property(type = PropertyType.SWITCH, name = "Debug", category = "DEBUG")
    public boolean debug = false;

    //endregion

    public Config() {
        super(new File("./config/may2beez/config.toml"), "May2Beez QoL", new JVMAnnotationPropertyCollector(), new ConfigSorting());
        initialize();
        addDependency("aotvVisionBlocksColor", "drawBlocksBlockingAOTV");
        addDependency("aotvVisionBlocksAccuracy", "drawBlocksBlockingAOTV");
        addDependency("routeBlockColor", "showRouteBlocks");
        addDependency("routeLineColor", "showRouteLines");
        addDependency("espColor", "mobEsp");
        addDependency("drawMobNames", "mobEsp");
        addDependency("chestEspColor", "chestEsp");
        addDependency("gemstoneEspAlpha", "gemstoneEsp");
    }

    public static class ConfigSorting extends SortingBehavior {
        @NotNull
        public Comparator<Category> getCategoryComparator() {
            return (o1, o2) -> o1.getName().equals("May2Beez QoL") ? -1 : (o2.getName().equals("May2Beez QoL") ? 1 : o1.getName().compareTo(o2.getName()));
        }
    }
}
