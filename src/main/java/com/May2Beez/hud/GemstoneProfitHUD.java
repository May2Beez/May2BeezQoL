package com.May2Beez.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.May2Beez.modules.features.GemstoneMoney;
import net.minecraft.util.EnumChatFormatting;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GemstoneProfitHUD extends TextHud {

    public GemstoneProfitHUD() {
        super(true, 0, 0, 1.0f, true, true, 10, 8, 8, new OneColor(0, 0, 0, 150), true, 2, new OneColor(0, 0, 0, 240));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (example) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            Duration duration = Duration.ofMillis(2300000);
            String[] text = new String[] {
                    "§r§lTotal earned $: §r" + EnumChatFormatting.GOLD + formatter.format(123456),
                    "§r§l$ / hour: §r" + EnumChatFormatting.GOLD + formatter.format((123456L * 3600000 / 2300000)),
                    "§r§lMining for: §r" + EnumChatFormatting.GOLD + String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutes() % 60, duration.getSeconds() % 60)
            };
            lines.addAll(Arrays.asList(text));
        } else if (GemstoneMoney.shouldShow()) {
            String[] mobKillerLines = GemstoneMoney.drawInfo();
            lines.addAll(Arrays.asList(mobKillerLines));
        }
    }
}
