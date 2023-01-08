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
    private static final String WORLD_SCANNER = "World Scanner";
    private static final String POWDER_MACRO = "Powder Macro";

    private static final int maxCameraSpeedMS = 500;


    //region Mithril Miner
    @Property(type = PropertyType.SLIDER, name = "Max Break Time", category = MITHRIL_MINER, max = 5000)
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
    @Property(type = PropertyType.SLIDER, name = "Line width", category = GHOST_GRINDER, min = 1, max = 10, hidden = true)
    public int lineWidth = 2;

    @Property(type = PropertyType.SLIDER, name = "Radius", category = GHOST_GRINDER, max = 100, hidden = true)
    public int radius = 15;

    @Property(type = PropertyType.SLIDER, name = "Click delay", category = GHOST_GRINDER, min = 1, max = 20, hidden = true)
    public int clickDelay = 5;

    //endregion

    //region Nuker
    @Property(type = PropertyType.SELECTOR, name = "Nuker Shape", description = "Choose which pattern hardstone nuker will follow",
            category = NUKER, options = {"Sphere", "Facing Axis", "Axis Tunnels"})
    public int nukerShape = 0;

    @Property(type = PropertyType.SLIDER, name = "Speed", category = NUKER, min = 10, max = 80, increment = 10)
    public int nukerSpeed = 20;

    @Property(type = PropertyType.SLIDER, name = "Pingless reset cutoff", category = NUKER, max = 20)
    public int nukerPinglessCutoff = 15;

    @Property(type = PropertyType.SWITCH, name = "Mine Blocks In Front", description = "Mine all blocks in the way of the player", category = NUKER)
    public boolean mineBlocksInFront = false;

    @Property(type = PropertyType.SWITCH, name = "Smooth Server Side Rotation", description = "Smoothly rotate the player server side", category = NUKER)
    public boolean smoothServerSideRotations = false;

    @Property(type = PropertyType.SELECTOR, name = "Pause nuker when solving", description = "Choose which mode hardstone nuker will use", category = NUKER, options = {"Don't", "Pause Nuker", "Pause Nuker Rotations"})
    public int powderChestPauseNukerMode = 0;

    @Property(type = PropertyType.SLIDER, name = "Field of View", description = "Change fov of sphere shape nuker", max = 360, increment = 20, category = NUKER)
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

    @Property(type = PropertyType.SLIDER, name = "Amount of ms to move forward", description = "Set 0 to move only left/right", category = FARMING_MACRO, max = 5000, increment = 100)
    public int forwardMs = 0;

    @Property(type = PropertyType.SELECTOR, name = "Check X or Z", category = FARMING_MACRO, options = {"X", "Z"})
    public int XorZindex = 0;

    @Property(type = PropertyType.SELECTOR, name = "Set yaw rotation", category = FARMING_MACRO, options = {"Disable", "0", "45", "90", "135", "180", "-45", "-90", "-135", "-180"})
    public int lookingDirection = 0;

    @Property(type = PropertyType.CHECKBOX, name = "Use BACK instead of RIGHT", category = FARMING_MACRO)
    public boolean backInsteadOfRight = false;

    //endregion

    //region Farming Nuker
    @Property(type = PropertyType.SELECTOR, name = "Farming speed", category = FARMING_NUKER, options = {"40 BPS", "80 BPS"}, hidden = true)
    public int farmingSpeedIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Farming radius", category = FARMING_NUKER, min = 1, max = 10, hidden = true)
    public int farmingRadius = 3;

    @Property(type = PropertyType.SELECTOR, name = "Farming Shape", description = "Choose which pattern crop nuker will follow",
            category = FARMING_NUKER, options = {"Closest Block", "Facing Axis"}, hidden = true)
    public int farmShapeIndex = 0;

    @Property(type = PropertyType.SELECTOR, name = "Nuker Crop Type", description = "Select the type of crop you want to nuke",
            category = FARMING_NUKER, options = {"Any Crop Except Cane or Cactus", "Cane or Cactus", "Nether Wart", "Wheat", "Carrot", "Potato", "Pumpkin", "Melon", "Mushroom", "Cocoa"}, hidden = true)
    public int farmNukeIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Down Up Range", category = FARMING_NUKER, max = 4, hidden = true)
    public int downUpRange = 0;

    //endregion

    //region Auto Plant Crops
    @Property(type = PropertyType.SELECTOR, name = "Crop Type", category = AUTO_PLANT_CROPS, options = {"Cocoa Beans", "Cactus"}, hidden = true)
    public int cropTypeIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Auto Crop Range", category = AUTO_PLANT_CROPS, min = 1, max = 8, hidden = true)
    public int autoCropRange = 0;

    //endregion

    //region Foraging Macro
    @Property(type = PropertyType.SLIDER, name = "Foraging Macro Delay", category = FORAGING_MACRO, max = 500)
    public int foragingDelay = 0;

    @Property(type = PropertyType.CHECKBOX, name = "Use Fishing Rod", category = FORAGING_MACRO)
    public boolean foragingUseRod = false;

    @Property(type = PropertyType.TEXT, name = "Monkey Pet LVL", category = FORAGING_MACRO)
    public String monkeyLVL = "0";

    @Property(type = PropertyType.SLIDER, name = "Foraging Macro Pre Rod Delay", category = FORAGING_MACRO, max = 500)
    public int preRodDelay = 0;

    @Property(type = PropertyType.CHECKBOX, name = "Use normal Bone Meal, instead of Enchanted", category = FORAGING_MACRO)
    public boolean normalBoneMeal = false;

    @Property(type = PropertyType.SLIDER, name = "Max idle", category = FORAGING_MACRO, max = 60)
    public int maxIdleTicks = 0;

    //endregion

    //region Mining
    @Property(type = PropertyType.SWITCH, name = "Powder chest solver server rotation", subcategory = "Powder Chests", category = MINING)
    public boolean solvePowderChestServerRotation = false;

    @Property(type = PropertyType.SWITCH, name = "Use mining speed in mining macros", subcategory = "General", category = MINING)
    public boolean useMiningSpeed = false;

    @Property(type = PropertyType.SLIDER, name = "Camera speed in ms", subcategory = "General", category = MINING, max = maxCameraSpeedMS)
    public int cameraSpeed = 120;

    @Property(type = PropertyType.SLIDER, name = "Hold server side rotation for X ms", subcategory = "General", category = MINING, max = 1500)
    public int holdRotationMS = 750;

    @Property(type = PropertyType.SWITCH, name = "Powder chest rotation only if object under mouse is a treasure chest", subcategory = "Powder Chests", category = MINING)
    public boolean onlyRotateIfTreasureChest = false;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Space from edge block to the center for accuracy checks", subcategory = "Targetting", description = "Lower value means that macro will check closes to the block's edge if the block is visible", category = MINING, minF = 0f, maxF = 0.5f, decimalPlaces = 2)
    public float miningAccuracy = 0.1f;

    @Property(type = PropertyType.SLIDER, name = "Accuracy checks per dimension", subcategory = "Targetting", description = "Higher value means that macro will check more times if the block is visible", category = MINING, min = 1, max = 16)
    public int miningAccuracyChecks = 8;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Space from cobblestone to the center", subcategory = "Targetting", description = "Increase if macro destroys cobblestone too often", category = MINING, minF = 0f, maxF = 0.3f, decimalPlaces = 3)
    public float miningCobblestoneAccuracy = 0.05f;

    @Property(type = PropertyType.SWITCH, name = "Refuel with abiphone", subcategory = "General", category = MINING)
    public boolean refuelWithAbiphone = false;

    @Property(type = PropertyType.SLIDER, name = "Fuel threshold", subcategory = "General", category = MINING, min = 100, max = 5000)
    public int fuelThreshold = 2000;

    @Property(type = PropertyType.SELECTOR, name = "Type of fuel", subcategory = "General", category = MINING, options = {"Goblin Egg", "Biofuel", "Volta", "Oil Barrel"})
    public int fuelType = 3;

    //endregion

    //region Fishing Macro
    @Property(type = PropertyType.SWITCH, name = "AntiAfk", category = FISHING)
    public boolean antiAfk = false;

    @Property(type = PropertyType.SLIDER, name = "SC Scan Range", category = FISHING, max = 20)
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

    @Property(type = PropertyType.SLIDER, name = "Camera speed in ms", category = AOTV_MACRO, max = maxCameraSpeedMS, subcategory = "Timers")
    public int aotvCameraSpeed = 120;

    @Property(type = PropertyType.SLIDER, name = "Stuck time threshold in ms", category = AOTV_MACRO, min = 500, max = 5000, subcategory = "Timers")
    public int aotvStuckTimeThreshold = 2000;

    @Property(type = PropertyType.SELECTOR, name = "Type of block to mine", category = AOTV_MACRO, options = {"Any", "Ruby", "Amethyst", "Jade", "Sapphire", "Amber", "Topaz", "Jasper", "Mithril"})
    public int aotvGemstoneType = 0;

    @Property(type = PropertyType.SWITCH, name = "Draw blocks blocking AOTV vision", category = AOTV_MACRO, subcategory = "Drawing")
    public boolean drawBlocksBlockingAOTV = true;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "AOTV Vision blocks accuracy", decimalPlaces = 1, description = "Smaller == more checks per line between waypoints", category = AOTV_MACRO, minF = 0.1f, maxF = 1f, subcategory = "Drawing")
    public float aotvVisionBlocksAccuracy = 0.1f;

    @Property(type = PropertyType.COLOR, name = "AOTV Vision blocks color", category = AOTV_MACRO, subcategory = "Drawing")
    public Color aotvVisionBlocksColor = new Color(255, 0, 0, 120);

    @Property(type = PropertyType.SLIDER, name = "AOTV Waypoint targeting camera speed in ms", category = AOTV_MACRO, max = maxCameraSpeedMS, subcategory = "Timers")
    public int aotvWaypointTargetingTime = 100;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "AOTV Waypoint targeting accuracy", description = "Smaller == closer to center", decimalPlaces = 2, minF = 0.02f, maxF = 0.5f, category = AOTV_MACRO)
    public float aotvTargetingWaypointAccuracy = 0.2f;

    @Property(type = PropertyType.SWITCH, name = "Yog killer", subcategory = "Additions", category = AOTV_MACRO)
    public boolean yogKiller = false;

    @Property(type = PropertyType.DECIMAL_SLIDER, minF = 1, maxF = 6, name = "Scan radius", category = AOTV_MACRO)
    public float scanRadius = 4.5f;

    @Property(type = PropertyType.SWITCH, name = "Stop macro if cobblestone on route has been destroyed", subcategory = "Additions", category = AOTV_MACRO)
    public boolean stopIfCobblestoneDestroyed = true;

    //endregion

    //region MobKiller

    @Property(type = PropertyType.SLIDER, min = 1, max = 30, name = "Mob Killer scan range", category = MOB_KILLER)
    public int mobKillerScanRange = 10;

    @Property(type = PropertyType.TEXT, name = "Mob's names to kill", category = MOB_KILLER)
    public String mobsNames = "";

    @Property(type = PropertyType.SLIDER, name = "Mob Killer camera speed in ms", category = MOB_KILLER, max = maxCameraSpeedMS)
    public int mobKillerCameraSpeed = 120;

    @Property(type = PropertyType.SWITCH, name = "Use Hyperion under player", category = MOB_KILLER)
    public boolean useHyperionUnderPlayer = false;

    @Property(type = PropertyType.SLIDER, name = "Delay between attacks", category = MOB_KILLER, min = 100, max = 500)
    public int mobKillerAttackDelay = 250;

    @Property(type = PropertyType.TEXT, name = "Custom item to kill", description = "Leave empty for default weapons", category = MOB_KILLER)
    public String customItemToKill = "";

    @Property(type = PropertyType.SELECTOR, name = "Button to attack with", description = "Doesn't override 'Use Hyperion under player'", category = MOB_KILLER, options = {"Left", "Right"})
    public int attackButton = 0;

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

    @Property(type = PropertyType.SLIDER, name = "Gemstone ESP alpha", category = ESP, max = 255)
    public int gemstoneEspAlpha = 80;

    @Property(type = PropertyType.SLIDER, name = "ESP range", category = ESP, max = 40)
    public int espRange = 10;

    @Property(type = PropertyType.SWITCH, name = "Show ghosts in The Mist", category = ESP)
    public boolean showGhosts = false;


    //endregion

    //region DEBUG

    @Property(type = PropertyType.SWITCH, name = "Debug", category = "DEBUG")
    public boolean debug = false;

    //endregion

    //region WORLD_SCANNER

    @Property(type = PropertyType.SWITCH, name = "Enable world scanner", category = WORLD_SCANNER)
    public boolean worldScanner = false;

    @Property(type = PropertyType.SELECTOR, name = "Scanner mode", category = WORLD_SCANNER, options = {"When chunk is loaded", "Timer", "Both"})
    public int worldScannerScanMode = 0;

    @Property(type = PropertyType.SLIDER, name = "World Scan Timer", category = WORLD_SCANNER, max = 20)
    public int worldScannerScanFrequency = 10;

    @Property(type = PropertyType.SWITCH, name = "ESP Waypoint highlight", category = WORLD_SCANNER, subcategory = "Drawing")
    public boolean espHighlight = true;

    @Property(type = PropertyType.SWITCH, name = "ESP Waypoint text", category = WORLD_SCANNER, subcategory = "Drawing")
    public boolean espWaypointText = true;

    @Property(type = PropertyType.SWITCH, name = "ESP Waypoint Beacon", category = WORLD_SCANNER, subcategory = "Drawing")
    public boolean espBeacon = true;

    @Property(type = PropertyType.SWITCH, category = WORLD_SCANNER, subcategory = "FILTER", name = "Crystal Hollows crystals")
    public boolean worldScannerCHCrystals = true;

    @Property(type = PropertyType.SWITCH, category = WORLD_SCANNER, subcategory = "FILTER", name = "Crystal Hollows mob spots")
    public boolean worldScannerCHMobSpots = true;

    @Property(type = PropertyType.SWITCH, category = WORLD_SCANNER, subcategory = "FILTER", name = "Crystal Hollows fairy grottos")
    public boolean worldScannerCHFairyGrottos = true;

    @Property(type = PropertyType.SWITCH, category = WORLD_SCANNER, subcategory = "FILTER", name = "Crystal Hollows worm fishing spots")
    public boolean worldScannerCHWormFishing = true;

    //endregion

    //region POWDER_MACRO

    @Property(type = PropertyType.SWITCH, name = "Kill Scathas and worms", category = POWDER_MACRO)
    public boolean killScathasAndWorms = false;

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
