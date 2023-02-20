package com.May2Beez.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.May2Beez.modules.combat.MobKiller;
import com.May2Beez.modules.farming.ForagingMacro;

import java.util.Arrays;
import java.util.List;

public class ForagingHUD extends TextHud {

    public ForagingHUD() {
        super(true, 0, 0, 1.0f, true, true, 10, 8, 8, new OneColor(0, 0, 0, 150), true, 2, new OneColor(0, 0, 0, 240));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (example) {
            String[] textToDraw = new String[3];
            long timeToNextAxe = 1234;
            textToDraw[0] = "§r§lState: §f" + "TURNED_OFF";
            textToDraw[1] = "§r§lIdle time: §f" + "5";
            textToDraw[2] = "§r§lAxe ready in: §f" + String.format("%.2f", (double) timeToNextAxe / 1000) + "s";
            lines.addAll(Arrays.asList(textToDraw));
        } else if (ForagingMacro.isRunning()) {
            String[] mobKillerLines = ForagingMacro.drawFunction();
            lines.addAll(Arrays.asList(mobKillerLines));
        }
    }
}
