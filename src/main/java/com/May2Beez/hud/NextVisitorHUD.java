package com.May2Beez.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.May2Beez.modules.farming.VisitorsMacro;
import com.May2Beez.utils.LocationUtils;
import com.May2Beez.utils.TablistUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class NextVisitorHUD extends TextHud {

    public NextVisitorHUD() {
        super(true, 0, 0, 1.0f, true, true, 10, 8, 8, new OneColor(0, 0, 0, 150), true, 2, new OneColor(0, 0, 0, 240));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;

        if (example) {
            lines.addAll(new ArrayList<String>() {{
                add("§r§lState: §f" + "TURNED_OFF");
                add("§r§lNext Visitor in: §f" + "0m" + "56s");
            }});
            return;
        }

        if (LocationUtils.currentIsland != LocationUtils.Island.GARDEN) return;
        List<String> tablist = TablistUtils.getTabList();

        for (String line : tablist) {
            String strippedLine = StringUtils.stripControlCodes(line).trim();
            if (strippedLine.contains("Next Visitor:")) {
                lines.add("§r§lState: §f" + VisitorsMacro.currentState);
                lines.add("§r§lNext Visitor in: §f" + strippedLine.replace("Next Visitor: ", ""));
                return;
            }
        }
    }
}
