package com.May2Beez;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Comparator;

public class Config extends Vigilant {

    private final String TITANIUM_ALERT = "Mithril Miner";
    private final String GHOST_ALERT = "Ghost Grinder";
    private final String HARDSTONE_COUNTER = "Hardstone Nuker";
    private final String FARMING_COUNTER = "Farming Macro";
    private final String FARMING_ALERT = "Farming Nuker";
    private final String AUTO_PLANT_CROPS = "Auto Plant Crops";
    private final String FORAGING_ALERT = "Foraging Macro";

    private final String MINING = "Mining";

    @Property(type = PropertyType.SLIDER, name = "Max Break Time", category = TITANIUM_ALERT,  min = 0, max = 500)
    public int maxBreakTime = 80;

    @Property(type = PropertyType.SWITCH, name = "Prioritize Titanium", category = TITANIUM_ALERT)
    public boolean prioTitanium = false;

    @Property(type = PropertyType.SLIDER, name = "Accuracy checks", category = TITANIUM_ALERT, min = 0, max = 20)
    public int accuracyChecks = 5;

    @Property(type = PropertyType.SLIDER, name = "Panic alert", category = TITANIUM_ALERT, min = 0, max = 300)
    public int panic = 100;

    @Property(type = PropertyType.SWITCH, name = "Check under", category = TITANIUM_ALERT)
    public boolean under = false;

    @Property(type = PropertyType.SLIDER, name = "Walking frequency &", category = TITANIUM_ALERT, min = 0, max = 100)
    public int walking = 1;

    @Property(type = PropertyType.SLIDER, name = "Walking time", category = TITANIUM_ALERT, min = 0, max = 20)
    public int walkingTime = 1;

    @Property(type = PropertyType.SWITCH, name = "Use sneak key", category = TITANIUM_ALERT)
    public boolean sneak = true;

    @Property(type = PropertyType.SELECTOR, name = "Mode", category = TITANIUM_ALERT, options = {"Wool", "Clay", "Prismarine", "Gold", "Blue"})
    public int mode = 0;

    @Property(type = PropertyType.SLIDER, name = "Camera speed", description = "Smaller number == faster", category = "General", min = 1, max = 20)
    public int cameraSpeed = 5;

    @Property(type = PropertyType.SLIDER, name = "Line width", category = GHOST_ALERT, min = 1, max = 10)
    public int lineWidth = 2;

    @Property(type = PropertyType.SLIDER, name = "Radius", category = GHOST_ALERT, min = 0, max = 100)
    public int radius = 15;

    @Property(type = PropertyType.SLIDER, name = "Click delay", category = GHOST_ALERT, min = 1, max = 20)
    public int clickDelay = 5;

    @Property(type = PropertyType.SELECTOR, name = "Hardstone Nuker Shape", description = "Choose which pattern hardstone nuker will follow",
            category = HARDSTONE_COUNTER, options = {"Closest Block", "Facing Axis"})
    public int hardIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Hardstone Nuker Height", description = "Range to break above the player",
            category = HARDSTONE_COUNTER, max = 5)
    public int hardrange = 0;

    @Property(type = PropertyType.SLIDER, name = "Hardstone Nuker Depth", description = "Range to break below the player",
            category = HARDSTONE_COUNTER, max = 3)
    public int hardrangeDown = 0;

    @Property(type = PropertyType.SWITCH, name = "Include Sand & Gravel", description = "Hardstone Nuker will also target sand and gravel",
            category = HARDSTONE_COUNTER)
    public boolean includeExcavatable = false;

    @Property(type = PropertyType.SWITCH, name = "Include Ores", description = "Hardstone Nuker, Mithril Nuker and Mithril Macro will also target ores",
            category = HARDSTONE_COUNTER)
    public boolean includeOres = false;

    @Property(type = PropertyType.TEXT, name = "Left coordinates", category = FARMING_COUNTER)
    public String leftWall = "0.0";

    @Property(type = PropertyType.TEXT, name = "Right coordinates", category = FARMING_COUNTER)
    public String rightWall = "0.0";

    @Property(type = PropertyType.SLIDER, name = "Amount of ms to move forward", description = "Set 0 to move only left/right", category = FARMING_COUNTER, min = 0, max = 5000, increment = 100)
    public int forwardMs = 0;

    @Property(type = PropertyType.SELECTOR, name = "Check X or Z", category = FARMING_COUNTER, options = {"X", "Z"})
    public int XorZindex = 0;

    @Property(type = PropertyType.SELECTOR, name = "Set yaw rotation", category = FARMING_COUNTER, options = {"Disable", "0", "45", "90", "135", "180", "-45", "-90", "-135", "-180"})
    public int lookingDirection = 0;

    @Property(type = PropertyType.CHECKBOX, name = "Use BACK instead of RIGHT", category = FARMING_COUNTER)
    public boolean backInsteadOfRight = false;

    @Property(type = PropertyType.SELECTOR, name = "Farming speed", category = FARMING_ALERT, options = {"40 BPS", "80 BPS"})
    public int farmingSpeedIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Farming radius", category = FARMING_ALERT, min = 1, max = 10)
    public int farmingRadius = 3;

    @Property(type = PropertyType.SELECTOR, name = "Farming Shape", description = "Choose which pattern crop nuker will follow",
            category = FARMING_ALERT, options = {"Closest Block", "Facing Axis"})
    public int farmShapeIndex = 0;

    @Property(type = PropertyType.SELECTOR, name = "Nuker Crop Type", description = "Select the type of crop you want to nuke",
            category = FARMING_ALERT, options = {"Any Crop Except Cane or Cactus", "Cane or Cactus", "Nether Wart", "Wheat", "Carrot", "Potato", "Pumpkin", "Melon", "Mushroom", "Cocoa"})
    public int farmNukeIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Down Up Range", category = FARMING_ALERT, min = 0, max = 4)
    public int downUpRange = 0;

    @Property(type = PropertyType.SELECTOR, name = "Crop Type", category = AUTO_PLANT_CROPS, options = {"Cocoa Beans", "Cactus"})
    public int cropTypeIndex = 0;

    @Property(type = PropertyType.SLIDER, name = "Auto Crop Range", category = AUTO_PLANT_CROPS, min = 1, max = 8)
    public int autoCropRange = 0;

    @Property(type = PropertyType.SLIDER, name = "Foraging Macro Delay", category = FORAGING_ALERT, min = 0, max = 500)
    public int foragingDelay = 0;

    @Property(type = PropertyType.CHECKBOX, name = "Use Fishing Rod", category = FORAGING_ALERT)
    public boolean foragingUseRod = false;

    @Property(type = PropertyType.TEXT, name = "Monkey Pet LVL", category = FORAGING_ALERT)
    public String monkeyLVL = "0";

    @Property(type = PropertyType.SLIDER, name = "Foraging Macro Pre Rod Delay", category = FORAGING_ALERT, min = 0, max = 500)
    public int preRodDelay = 0;

    @Property(type = PropertyType.CHECKBOX, name = "Use normal Bone Meal, instead of Enchanted", category = FORAGING_ALERT)
    public boolean normalBoneMeal = false;

    @Property(type = PropertyType.SLIDER, name = "Max idle", category = FORAGING_ALERT, min = 0, max = 60)
    public int maxIdleTicks = 0;

    @Property(type = PropertyType.SWITCH, name = "Powder chest solver server rotation", category = MINING)
    public boolean solvePowderChestServerRotation = false;

    @Property(type = PropertyType.SWITCH, name = "Draw lines to every powder chest", category = MINING)
    public boolean drawLinesToPowderChests = true;


    public Config() {
        super(new File("./config/may2beez/config.toml"), "May2Beez QoL", (PropertyCollector)new JVMAnnotationPropertyCollector(), new ConfigSorting());
        initialize();
    }

    public static class ConfigSorting extends SortingBehavior {
        @NotNull
        public Comparator<Category> getCategoryComparator() {
            return (o1, o2) -> o1.getName().equals("May2Beez QoL") ? -1 : (o2.getName().equals("May2Beez QoL") ? 1 : o1.getName().compareTo(o2.getName()));
        }
    }
}
