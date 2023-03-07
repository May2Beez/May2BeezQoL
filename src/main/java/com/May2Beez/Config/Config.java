package com.May2Beez.Config;

import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import com.May2Beez.May2BeezQoL;
import com.May2Beez.gui.AOTVWaypointsPage;
import com.May2Beez.hud.ForagingHUD;
import com.May2Beez.hud.GemstoneProfitHUD;
import com.May2Beez.hud.MobKillerHUD;
import com.May2Beez.hud.NextVisitorHUD;

public class Config extends cc.polyfrost.oneconfig.config.Config {

    private transient static final String MITHRIL_MINER = "Mithril Miner";
    private transient static final String GHOST_GRINDER = "Ghost Grinder";
    private transient static final String FARMING_MACRO = "Farming Macro";
    private transient static final String FORAGING_MACRO = "Foraging Macro";
    private transient static final String FISHING = "Fishing Macro";
    private transient static final String MINING = "Mining";
    private transient static final String AOTV_MACRO = "AOTV Macro";
    private transient static final String MOB_KILLER = "Mob Killer";
    private transient static final String ESP = "ESP";
    private transient static final String FAILSAFES = "FailSafes";
    private transient static final String CUSTOM_COMMAND = "Custom Command";
    private transient static final String OTHER_OPTIONS = "Other Options";
    private transient static final String VISITORS_MACRO = "Visitors Macro";
    private transient static final String GARDEN = "Garden";

    private transient static final int maxCameraSpeedMS = 500;


    //region Mining
    @Switch(name = "Powder chest solver server rotation", subcategory = "Powder Chests", category = MINING)
    public boolean solvePowderChestServerRotation = false;

    @Switch(name = "Rotate only when treasure chest under mouse", subcategory = "Powder Chests", category = MINING)
    public boolean onlyRotateIfTreasureChest = false;

    @Switch(name = "Use mining speed in mining macros", subcategory = "General", category = MINING)
    public boolean useMiningSpeed = false;

    @Switch(name = "Turn all glass panes into full block", subcategory = "General", category = MINING)
    public boolean turnGlassPanesIntoFullBlock = false;

    @Slider(name = "Camera speed in ms", subcategory = "General", category = MINING, max = maxCameraSpeedMS, min = 0.0F)
    public int cameraSpeed = 120;

    @Slider(name = "Hold server side rotation for X ms", subcategory = "General", category = MINING, max = 1500, min = 0.0F)
    public int holdRotationMS = 750;

    @Slider( name = "Space from edge to the center", subcategory = "Targeting", description = "Lower value means that macro will check closes to the block's edge if the block is visible", category = MINING, min = 0f, max = 0.5f)
    public float miningAccuracy = 0.1f;

    @Slider(name = "Accuracy checks per dimension", subcategory = "Targeting", description = "Higher value means that macro will check more times if the block is visible", category = MINING, min = 1, max = 16)
    public int miningAccuracyChecks = 8;

    @Switch(name = "Refuel with abiphone", subcategory = "Refill fuel", category = MINING)
    public boolean refuelWithAbiphone = false;

    @Slider(name = "Fuel threshold", subcategory = "Refill fuel", category = MINING, min = 100, max = 5000, step = 100)
    public int fuelThreshold = 2000;

    @Dropdown(name = "Type of fuel", subcategory = "Refill fuel", category = MINING, options = {"Goblin Egg", "Biofuel", "Volta", "Oil Barrel"})
    public int fuelType = 3;

    @HUD(name = "Gemstone Profit Calculator", category = MINING)
    public GemstoneProfitHUD gemstoneMoney = new GemstoneProfitHUD();

    @Switch(name = "Debug", category = MINING)
    public boolean debug = false;

    //endregion

    //region Mithril Miner

    @Switch(name = "Prioritize Titanium", category = MITHRIL_MINER)
    public boolean prioTitanium = false;

    @Slider(name = "Max Break Time", category = MITHRIL_MINER, max = 5000, min = 0.0F, step = 100)
    public int maxBreakTime = 2000;

    @Switch(name = "Clay", category = MITHRIL_MINER, subcategory = "Filter")
    public boolean filterClay = true;

    @Switch(name = "Gray wool", category = MITHRIL_MINER, subcategory = "Filter")
    public boolean filterGrayWool = true;

    @Switch(name = "Prismarine", category = MITHRIL_MINER, subcategory = "Filter")
    public boolean filterPrismarine = true;

    @Switch(name = "Blue wool", category = MITHRIL_MINER, subcategory = "Filter")
    public boolean filterBlueWool = true;

    @Slider( name = "Scan range", category = MITHRIL_MINER, min = 0, max = 5.5f)
    public float scanRange = 4.5f;

    //endregion

    //region Ghost Grinder
//    @Slider(name = "Line width", category = GHOST_GRINDER, min = 1, max = 10)
//    public int lineWidth = 2;
//
//    @Slider(name = "Radius", category = GHOST_GRINDER, max = 100, min = 0.0F)
//    public int radius = 15;
//
//    @Slider(name = "Click delay", category = GHOST_GRINDER, min = 1, max = 20)
//    public int clickDelay = 5;

    //endregion

    //region Farming Macro
    @Text(name = "Left coordinates", category = FARMING_MACRO)
    public String leftWall = "0.0";

    @Text(name = "Right coordinates", category = FARMING_MACRO)
    public String rightWall = "0.0";

    @Slider(name = "Amount of ms to move forward", description = "Set 0 to move only left/right", category = FARMING_MACRO, max = 5000, step = 100, min = 0.0F)
    public int forwardMs = 0;

    @Dropdown(name = "Check X or Z", category = FARMING_MACRO, options = {"X", "Z"})
    public int XorZindex = 0;

    @Dropdown(name = "Set yaw rotation", category = FARMING_MACRO, options = {"Disable", "0", "45", "90", "135", "180", "-45", "-90", "-135", "-180"})
    public int lookingDirection = 0;

    @Checkbox(name = "Use BACK instead of RIGHT", category = FARMING_MACRO)
    public boolean backInsteadOfRight = false;

    //endregion

    //region Foraging Macro

    @Switch(name = "Use Fishing Rod", category = FORAGING_MACRO)
    public boolean foragingUseRod = false;

    @Switch(name = "Use normal Bone Meal, instead of Enchanted", category = FORAGING_MACRO)
    public boolean normalBoneMeal = false;

    @Number(name = "Monkey Pet LVL", category = FORAGING_MACRO, size = 2, min = 0, max = 100)
    public int monkeyLVL = 0;

    @Slider(name = "Foraging Macro Delay", category = FORAGING_MACRO, max = 500, min = 0.0F)
    public int foragingDelay = 0;

    @Slider(name = "Foraging Macro Pre Rod Delay", category = FORAGING_MACRO, max = 500, min = 0.0F)
    public int preRodDelay = 0;

    @Slider(name = "Max idle", category = FORAGING_MACRO, max = 60, min = 0.0F)
    public int maxIdleTicks = 0;

    @HUD(name = "Foraging Macro Info", category = FORAGING_MACRO)
    public ForagingHUD foragingMacroInfo = new ForagingHUD();


    //endregion

    //region Fishing Macro
    @Switch(name = "AntiAfk", category = FISHING)
    public boolean antiAfk = false;

    @Slider(name = "SC Scan Range", category = FISHING, max = 20, min = 0.0F)
    public int scScanRange = 0;

    @Switch(name = "Sneak while fishing", category = FISHING)
    public boolean sneakWhileFishing = true;

    //endregion

    //region AOTV Macro

    @Page(name = "List of waypoints", location = PageLocation.TOP, category = AOTV_MACRO, subcategory = "Waypoints")
    public AOTVWaypointsPage aotvWaypointsPage = new AOTVWaypointsPage();

    @Slider(name = "Camera speed in ms", category = AOTV_MACRO, max = maxCameraSpeedMS, subcategory = "Mechanics", min = 0.0F)
    public int aotvCameraSpeed = 120;

    @Slider(name = "Waypoint targeting camera speed in ms", category = AOTV_MACRO, max = maxCameraSpeedMS, subcategory = "Mechanics", min = 0.0F)
    public int aotvWaypointTargetingTime = 100;

    @Slider( name = "TP between threshold", description = "If there is no veins on the spot, macro will teleport to the next and to the next etc", category = AOTV_MACRO, subcategory = "Mechanics", min = 0f, max = 3f)
    public float teleportThreshold = 1.5f;

    @Slider(name = "Stuck time threshold in ms", category = AOTV_MACRO, min = 500, max = 5000, subcategory = "Mechanics")
    public int aotvStuckTimeThreshold = 2000;

    @Slider( name = "AOTV Waypoint targeting accuracy", description = "Smaller == closer to center", min = 0.02f, max = 0.5f, category = AOTV_MACRO, subcategory = "Targeting")
    public float aotvTargetingWaypointAccuracy = 0.2f;

    @Dropdown(name = "Type of block to mine", category = AOTV_MACRO, subcategory = "Mining", options = {"Any", "Ruby", "Amethyst", "Jade", "Sapphire", "Amber", "Topaz", "Jasper", "Mithril"})
    public int aotvGemstoneType = 0;

    @Slider(min = 1, max = 6, name = "Scan radius", category = AOTV_MACRO, subcategory = "Mining")
    public float scanRadius = 4.5f;

    @Switch(name = "Stop macro if cobblestone on route has been destroyed", subcategory = "Mining", category = AOTV_MACRO)
    public boolean stopIfCobblestoneDestroyed = true;

    @Switch(name = "Draw blocks blocking AOTV vision", category = AOTV_MACRO, subcategory = "Drawing")
    public boolean drawBlocksBlockingAOTV = true;

    @Color(name = "AOTV Vision blocks color", category = AOTV_MACRO, subcategory = "Drawing")
    public OneColor aotvVisionBlocksColor = new OneColor(255, 0, 0, 120);

    @Switch(name = "Show highlighted route blocks", category = AOTV_MACRO, subcategory = "Drawing")
    public boolean showRouteBlocks = true;

    @Color(name = "Route block color", category = AOTV_MACRO, subcategory = "Drawing")
    public OneColor routeBlockColor = new OneColor(0, 255, 0, 120);

    @Switch(name = "Show highlighted route lines", category = AOTV_MACRO, subcategory = "Drawing")
    public boolean showRouteLines = true;

    @Color(name = "Route line color", category = AOTV_MACRO, subcategory = "Drawing")
    public OneColor routeLineColor = new OneColor(0, 255, 0, 50);

    @Slider( name = "AOTV Vision blocks accuracy", description = "Smaller == more checks per line between waypoints", category = AOTV_MACRO, min = 0.1f, max = 1f, subcategory = "Drawing")
    public float aotvVisionBlocksAccuracy = 0.1f;

    @Slider( name = "AOTV Vision blocks width of sight", description = "Smaller == less blocks will check for vision", category = AOTV_MACRO, min = 0.1f, max = 1f, subcategory = "Drawing")
    public float aotvVisionBlocksWidthOfSight = 0.15f;

    @Switch(name = "Yog killer", subcategory = "Additions", category = AOTV_MACRO)
    public boolean yogKiller = false;

    //endregion

    //region MobKiller

    @Slider(min = 1, max = 30, name = "Mob Killer scan range", category = MOB_KILLER)
    public int mobKillerScanRange = 10;

    @Slider(name = "Mob Killer camera speed in ms", category = MOB_KILLER, max = maxCameraSpeedMS, min = 0.0F)
    public int mobKillerCameraSpeed = 120;

    @Slider(name = "Delay between attacks", category = MOB_KILLER, min = 100, max = 500)
    public int mobKillerAttackDelay = 250;

    @Switch(name = "Use Hyperion under player", category = MOB_KILLER)
    public boolean useHyperionUnderPlayer = false;

    @Dropdown(name = "Button to attack with", description = "Doesn't override 'Use Hyperion under player'", category = MOB_KILLER, options = {"Left", "Right"})
    public int attackButton = 0;

    @Text(name = "Custom item to kill", description = "Leave empty for default weapons", category = MOB_KILLER, size = 2)
    public String customItemToKill = "";

    @Text(name = "Mob's names to kill", category = MOB_KILLER, size = 2)
    public String mobsNames = "";

    @HUD(name = "Target info", category = MOB_KILLER)
    public MobKillerHUD mobKillerHUD = new MobKillerHUD();

    //endregion

    //region ESP

    @Switch(name = "Draw ESP around mobs", category = ESP)
    public boolean mobEsp = true;

    @Color(name = "Mob ESP color", category = ESP)
    public OneColor espColor = new OneColor(255, 0, 0, 120);

    @Switch(name = "Draw ESP mob names", category = ESP, size = 2)
    public boolean drawMobNames = true;

    @Switch(name = "Draw ESP around chests", category = ESP)
    public boolean chestEsp = true;

    @Color(name = "Chest ESP color", category = ESP)
    public OneColor chestEspColor = new OneColor(208, 104, 0, 120);

    @Switch(name = "Draw ESP around gemstones", category = ESP)
    public boolean gemstoneEsp = true;

    @Switch(name = "Show ghosts in The Mist", category = ESP)
    public boolean showGhosts = false;

    @Slider(name = "Gemstone ESP alpha", category = ESP, max = 255, min = 0.0F)
    public int gemstoneEspAlpha = 80;

    @Slider(name = "ESP range", category = ESP, max = 40, min = 0.0F)
    public int espRange = 10;


    //endregion

    //region FAILSAFES

    @Switch(name = "World Change failsafe", category = FAILSAFES, subcategory = "World Change")
    public boolean stopMacrosOnWorldChange = true;

    @Switch(name = "Rotation Check failsafe", category = FAILSAFES, subcategory = "Rotation Check")
    public boolean stopMacrosOnRotationCheck = true;

    @Switch(name = "Fake camera move after rotation check", category = FAILSAFES, subcategory = "Rotation Check")
    public boolean fakeMoveAfterRotationCheck = false;

    @Switch(name = "Swap item check failsafe", category = FAILSAFES, subcategory = "Swap Item Check")
    public boolean stopMacrosOnSwapItemCheck = true;

    //endregion

    //region GARDEN

    @Switch(name = "Highlight desk position", subcategory = VISITORS_MACRO, category = GARDEN)
    public boolean highlightDeskPosition = true;

    @HUD(name = "Visitor info", subcategory = VISITORS_MACRO, category = GARDEN)
    public NextVisitorHUD visitorHUD = new NextVisitorHUD();

    @Switch(name = "Plot Cleaner ESP", subcategory = "Plot Cleaner", category = GARDEN)
    public boolean plotCleanerEsp = true;

    @Color(name = "Plot Cleaner ESP color", subcategory = "Plot Cleaner", category = GARDEN)
    public OneColor plotCleanerEspColor = new OneColor(255, 0, 0, 120);

    @Switch(name = "Grass ESP", subcategory = "Plot Cleaner FILTER", category = GARDEN)
    public boolean grassEsp = true;

    @Switch(name = "Leaves ESP", subcategory = "Plot Cleaner FILTER", category = GARDEN)
    public boolean leavesEsp = true;

    @Switch(name = "Flowers ESP", subcategory = "Plot Cleaner FILTER", category = GARDEN)
    public boolean flowersEsp = true;


    //endregion

    //region CUSTOM_COMMAND

    @Text(name = "Custom command to execute", category = CUSTOM_COMMAND, size = 2, placeholder = "/say Hello World")
    public String customCommand = "";

    //endregion

    //region OTHER_OPTIONS

    @Switch(name = "Auto Enchanting", category = OTHER_OPTIONS, subcategory = "Enchanting")
    public boolean autoEnchantingTable = true;

    @Slider(name = "Auto Enchanting Delay", category = OTHER_OPTIONS, max = 1000, min = 0, step = 10, subcategory = "Enchanting")
    public int autoEnchantingTableDelay = 300;

    @Switch(name = "Auto Enchanting Auto Close at 3 clicks", category = OTHER_OPTIONS, subcategory = "Enchanting")
    public boolean autoEnchantingTableAutoClose = true;

    @Switch(name = "Auto Join Skyblock", category = OTHER_OPTIONS, subcategory = "Skyblock")
    public boolean autoJoinSkyblock = true;

    @Switch(name = "Auto Sprint", category = OTHER_OPTIONS, subcategory = "Skyblock")
    public boolean autoSprint = true;

//    @HUD(name = "Looking at mob HUD", category = OTHER_OPTIONS, subcategory = "Skyblock")
//    public LookingAtMobHUD lookingAtMobHUD = new LookingAtMobHUD();

    //endregion

    public Config() {
        super(new Mod(May2BeezQoL.MODID, ModType.HYPIXEL), "/May2BeezQoL/config.json");
        initialize();
        addDependency("aotvVisionBlocksColor", "drawBlocksBlockingAOTV");
        addDependency("aotvVisionBlocksAccuracy", "drawBlocksBlockingAOTV");
        addDependency("routeBlockColor", "showRouteBlocks");
        addDependency("routeLineColor", "showRouteLines");
        addDependency("espColor", "mobEsp");
        addDependency("drawMobNames", "mobEsp");
        addDependency("chestEspColor", "chestEsp");
        addDependency("gemstoneEspAlpha", "gemstoneEsp");
        addDependency("autoEnchantingTableDelay", "autoEnchantingTable");
        addDependency("autoEnchantingTableAutoClose", "autoEnchantingTable");
        addDependency("holdRotationMS", "solvePowderChestServerRotation");
        addDependency("fuelThreshold", "refuelWithAbiphone");
        addDependency("fuelType", "refuelWithAbiphone");
    }
}
