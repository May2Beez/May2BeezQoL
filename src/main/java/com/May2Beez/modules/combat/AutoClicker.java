package com.May2Beez.modules.combat;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.events.ClickEvent;
import com.May2Beez.modules.Module;
import com.May2Beez.utils.SkyblockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public class AutoClicker extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private long nextLeftClick = System.currentTimeMillis();
    private long nextRightClick = System.currentTimeMillis();
    private static final Random random = new Random();

    public AutoClicker() {
        super("AutoClicker", new KeyBinding("AutoClicker", 0, "May2BeezQoL - Combat"));
        disableOnFailsafe = false;
    }

    @SubscribeEvent
    public void onLeftClick(ClickEvent.LeftClickEvent event) {
        if (!May2BeezQoL.config.leftClick) return;
        long nowMillis = System.currentTimeMillis();
        int maxCpsMillis = (int) (1000.0 / May2BeezQoL.config.leftClickMaxCPS);
        int minCpsMillis = (int) (1000.0 / May2BeezQoL.config.leftClickMinCPS);
        if (maxCpsMillis >= minCpsMillis) {
            maxCpsMillis = minCpsMillis - 1;
        }
        nextLeftClick = (long) (nowMillis + maxCpsMillis + (random.nextDouble() * (maxCpsMillis - minCpsMillis)));
    }

    @SubscribeEvent
    public void onRightClick(ClickEvent.RightClickEvent event) {
        if (!May2BeezQoL.config.rightClick) return;
        long nowMillis = System.currentTimeMillis();
        long overshoot = (nowMillis - nextRightClick) < 200 ? (nowMillis - nextRightClick) : 0L;
        nextRightClick = nowMillis + Math.max(((long) May2BeezQoL.config.rightClickDelay) - overshoot, 0L);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (isToggled() && mc.thePlayer != null && mc.currentScreen == null && !mc.thePlayer.isUsingItem()) {
            long nowMillis = System.currentTimeMillis();
            MovingObjectPosition mop = mc.objectMouseOver;
            if (mop != null && mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                // If the player is looking at an entity, don't click
                return;
            }
            if (May2BeezQoL.config.leftClick && mc.gameSettings.keyBindAttack.isKeyDown() && nowMillis >= nextLeftClick) {
                SkyblockUtils.leftClick();
                // The delay is set in onLeftClick
            }
            // Ensure that not this and the terminator clicker are both active at the same time
            if (May2BeezQoL.config.rightClick && mc.gameSettings.keyBindUseItem.isKeyDown() && nowMillis >= nextRightClick) {
                SkyblockUtils.rightClick();
                // The delay is set in onRightClick
            }
        }
    }
}
