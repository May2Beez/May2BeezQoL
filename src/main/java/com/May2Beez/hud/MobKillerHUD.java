package com.May2Beez.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.May2Beez.modules.combat.MobKiller;

import java.util.Arrays;
import java.util.List;

public class MobKillerHUD extends TextHud {

    public MobKillerHUD() {
        super(true, 0, 0, 1.0f, true, true, 10, 8, 8, new OneColor(0, 0, 0, 150), true, 2, new OneColor(0, 0, 0, 240));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (example) {
            String[] exampleLines = new String[]{
                "§lTarget:",
                        "§rName: §f" + "None",
                        "§rDistance: §f" + "No target",
                        "§rHealth: §f" + "No target",
                        "§rState: §f" + "TURNED_OFF"
            };
            lines.addAll(Arrays.asList(exampleLines));
        } else if (MobKiller.ShouldScan) {
            String[] mobKillerLines = MobKiller.drawInfo();
            lines.addAll(Arrays.asList(mobKillerLines));
        }
    }
}
