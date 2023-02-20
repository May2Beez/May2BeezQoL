package com.May2Beez.modules.features;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.utils.JsonUtils;
import com.May2Beez.utils.SkyblockUtils;
import com.May2Beez.utils.Timer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GemstoneMoney {

    private final Minecraft mc = Minecraft.getMinecraft();
    private static long startTime = -1;
    private static final HashMap<String, Integer> gemstonesMined = new HashMap<>();
    private static long lastMined = 0;
    private boolean needRefresh = false;
    private static final HashMap<String, Float> gemstonesCost = new HashMap<>();
    private final Timer refreshTimeoutTimer = new Timer();
    private static final Timer lastRefreshTimer = new Timer();
    private boolean timeout = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!May2BeezQoL.config.gemstoneMoney.isEnabled()) return;

        if (timeout && refreshTimeoutTimer.hasReached(5000)) {
            timeout = false;
            needRefresh = true;
            refreshTimeoutTimer.reset();
        } else if (timeout) {
            return;
        }

        if (lastMined != 0 && System.currentTimeMillis() - lastMined > 90_000) {
            gemstonesMined.clear();
            lastMined = 0;
            startTime = -1;
            needRefresh = false;
            return;
        }

        if (!needRefresh) return;

        new Thread(() -> {
            needRefresh = false;
            JsonObject json = JsonUtils.getContent("https://api.hypixel.net/skyblock/bazaar");
            if (json == null) {
                System.out.println("Failed to get bazaar data");
                timeout = true;
                return;
            }
            Set<Map.Entry<String, JsonElement>> entries = json.getAsJsonObject("products").entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                try {
                    String name = entry.getKey();
                    if (name.startsWith("FLAWED_")) {
                        JsonObject product = entry.getValue().getAsJsonObject();
                        JsonObject quickStatus = product.getAsJsonObject("quick_status");
                        float price = quickStatus.get("sellPrice").getAsFloat();
                        gemstonesCost.put(name.toUpperCase(), price);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    timeout = true;
                }
            }
            System.out.println("Refreshed gemstone prices");
        }).start();
    }

    public static boolean shouldShow() {
        return startTime != -1;
    }

    public static String[] drawInfo() {

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);

        long time = lastRefreshTimer.lastMS - startTime + 1;
        long money = 0;
        for (Map.Entry<String, Integer> entry : gemstonesMined.entrySet()) {
            money += entry.getValue() * gemstonesCost.getOrDefault(entry.getKey().toUpperCase(), 240.0f);
        }

        if (lastRefreshTimer.hasReached(500)) {
            lastRefreshTimer.reset();
        }

        Duration duration = Duration.ofMillis(time);

        return new String[] {
                "§r§lTotal earned $: §r" + EnumChatFormatting.GOLD + formatter.format(money),
                "§r§l$ / hour: §r" + EnumChatFormatting.GOLD + formatter.format((money * 3600000 / time)),
                "§r§lMining for: §r" + EnumChatFormatting.GOLD + String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutes() % 60, duration.getSeconds() % 60)
        };
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!May2BeezQoL.config.gemstoneMoney.isEnabled()) return;
        String message = event.message.getUnformattedText();
        Pattern pattern = Pattern.compile("PRISTINE! You found .? (\\w+) (\\w+) Gemstone x(\\d+)!");
        Matcher matcher = pattern.matcher(StringUtils.stripControlCodes(message));
        if (matcher.find()) {
            if (startTime == -1) {
                startTime = System.currentTimeMillis();
                needRefresh = true;
                lastRefreshTimer.reset();
            }
            String gemstone = matcher.group(2);
            int amount = Integer.parseInt(matcher.group(3));
            String gemstoneId = "FLAWED_" + gemstone.toUpperCase() + "_GEM";
            gemstonesMined.put(gemstoneId, gemstonesMined.getOrDefault(gemstoneId, 0) + amount);
            lastMined = System.currentTimeMillis();
        }
    }
}
