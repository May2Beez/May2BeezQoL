package com.May2Beez;

import com.May2Beez.Config.Config;
import com.May2Beez.Config.CoordsConfig;
import com.May2Beez.commands.AOTVWaypoints;
import com.May2Beez.commands.OpenSettings;
import com.May2Beez.commands.UseCooldown;
import com.May2Beez.events.MillisecondEvent;
import com.May2Beez.events.SecondEvent;
import com.May2Beez.modules.combat.MobKiller;
import com.May2Beez.modules.farming.FarmingMacro;
import com.May2Beez.modules.farming.ForagingMacro;
import com.May2Beez.modules.mining.AOTVMacro;
import com.May2Beez.modules.mining.MithrilMiner;
import com.May2Beez.modules.mining.Nuker;
import com.May2Beez.modules.mining.PowderMacro;
import com.May2Beez.modules.player.CustomItemMacro;
import com.May2Beez.modules.player.FishingMacro;
import com.May2Beez.modules.player.ESP;
import com.May2Beez.modules.player.PowderChest;
import com.May2Beez.modules.singeplayer.AspectOfTheVoid;
import com.May2Beez.modules.world.WorldScanner;
import com.May2Beez.utils.LocationUtils;
import com.May2Beez.utils.LogUtils;
import com.May2Beez.utils.RotationUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod(modid = May2BeezQoL.MODID, version = May2BeezQoL.VERSION)
public class May2BeezQoL
{
    public static final String MODID = "May2BeezQoL";
    public static final String VERSION = "1.0.0";
    public static final PowderChest powderChestMacro = new PowderChest();
    public static Config config = new Config();
    public static GuiScreen display = null;
    public static CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<>();
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static CoordsConfig coordsConfig;

    public static final MobKiller mobKiller = new MobKiller();

    public static boolean miningSpeedReady = true;

    private void initConfigs(FMLPreInitializationEvent event) {
        File directory = new File(event.getModConfigurationDirectory(), "may2beez");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File coordsFile = new File(directory, "aotv_coords.json");
        File rcmacrosFile = new File(directory, "rcmacros.json");
        File lcmacrosFile = new File(directory, "lcmacros.json");

        if (!coordsFile.exists()) {
            try {
                Files.createFile(Paths.get(coordsFile.getPath()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!rcmacrosFile.exists()) {
            try {
                Files.createFile(Paths.get(rcmacrosFile.getPath()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!lcmacrosFile.exists()) {
            try {
                Files.createFile(Paths.get(lcmacrosFile.getPath()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Reader reader = Files.newBufferedReader(Paths.get("./config/may2beez/aotv_coords.json"));
            coordsConfig = gson.fromJson(reader, CoordsConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (coordsConfig != null) {
            System.out.println(coordsConfig.getRoutes());
        } else {
            System.out.println("coordsConfig is null");
            System.out.println("Creating new CoordsConfig");
            coordsConfig = new CoordsConfig();
            AOTVWaypointsGUI.SaveWaypoints();
            System.out.println(coordsConfig.getRoutes());
        }
    }

    private void LoadCooldownMacros() {
        try {
            Reader reader = Files.newBufferedReader(Paths.get("./config/may2beez/rcmacros.json"));
            int data = reader.read();
            StringBuilder str = new StringBuilder();
            while(data != -1) {
                str.append((char) data);
                data = reader.read();
            }
            str = new StringBuilder(str.toString().replace("\"", "").replace("{", "").replace("}", ""));
            String[] arr = str.toString().split(",");
            for (String value : arr) {
                String[] arr2 = value.split(":");
                if (arr2.length == 2) {
                    System.out.println(arr2[0] + " " + arr2[1]);
                    UseCooldown.RCitems.put(arr2[0], Integer.parseInt(arr2[1]));
                }
            }
            reader.close();
            Reader reader2 = Files.newBufferedReader(Paths.get("./config/may2beez/lcmacros.json"));
            int data2 = reader2.read();
            StringBuilder str2 = new StringBuilder();
            while(data2 != -1) {
                str2.append((char) data2);
                data2 = reader.read();
            }
            str2 = new StringBuilder(str2.toString().replace("\"", "").replace("{", "").replace("}", ""));
            String[] arr3 = str2.toString().split(",");
            for (String s : arr3) {
                String[] arr4 = s.split(":");
                if (arr4.length == 2) {
                    System.out.println(arr4[0] + " " + arr4[1]);
                    UseCooldown.LCitems.put(arr4[0], Integer.parseInt(arr4[1]));
                }
            }
            reader2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        AOTVWaypoints aotvWaypoints = new AOTVWaypoints();
        OpenSettings openSettings = new OpenSettings();
        initConfigs(event);
        LoadCooldownMacros();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RotationUtils());
        MinecraftForge.EVENT_BUS.register(aotvWaypoints);
        MinecraftForge.EVENT_BUS.register(openSettings);
        MinecraftForge.EVENT_BUS.register(new LocationUtils());
        modules.add(new MithrilMiner());
        modules.add(new ForagingMacro());
//        modules.add(new GhostGrinder()); // not rly working
        modules.add(new Nuker());
        modules.add(new FarmingMacro());
        modules.add(new CustomItemMacro());
//        modules.add(new CropNuker());
        modules.add(powderChestMacro);
//        modules.add(new AutoPlantCrops()); // meh
//        modules.add(new AutoMelody()); // kinda pointless
        modules.add(new FishingMacro());
        modules.add(new AOTVMacro());
        modules.add(mobKiller); // sub-module for later usage
        modules.add(new ESP());
        modules.add(new WorldScanner());
//        modules.add(new PowderMacro());
        modules.add(new AspectOfTheVoid());

        for (Module m : modules)
            MinecraftForge.EVENT_BUS.register(m);

        ClientCommandHandler.instance.registerCommand(aotvWaypoints);
        ClientCommandHandler.instance.registerCommand(openSettings);
        ClientCommandHandler.instance.registerCommand(new UseCooldown());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LocalDateTime now = LocalDateTime.now();
        Duration initialDelay = Duration.between(now, now);
        long initialDelaySeconds = initialDelay.getSeconds();

        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
        threadPool.scheduleAtFixedRate(() -> MinecraftForge.EVENT_BUS.post(new SecondEvent()), initialDelaySeconds, 1, TimeUnit.SECONDS);
        threadPool.scheduleAtFixedRate(() -> MinecraftForge.EVENT_BUS.post(new MillisecondEvent()), initialDelaySeconds, 1, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        if (modules.stream().anyMatch(Module::isToggled)) {
            LogUtils.addMessage("Detected World Change, Stopping All Macros", EnumChatFormatting.DARK_RED);
        }
        for (Module m : modules) {
            if (m.isToggled()) m.toggle();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;

        if (display != null) {
            try {
                Minecraft.getMinecraft().displayGuiScreen(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
            display = null;
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        try {
            String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
            if (message.contains(":") || message.contains(">")) return;
            if(message.startsWith("You used your")) {
                miningSpeedReady = false;
            } else if(message.endsWith("is now available!")) {
                miningSpeedReady = true;
            }
        } catch (Exception ignored) {}
    }
}
