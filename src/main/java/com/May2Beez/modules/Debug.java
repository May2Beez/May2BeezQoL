package com.May2Beez.modules;

import com.May2Beez.Module;
import com.May2Beez.utils.BlockUtils;
import com.May2Beez.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Debug extends Module {

    private BlockPos testBlock = null;
    private final Minecraft mc = Minecraft.getMinecraft();

    public Debug() {
        super("Debug", new KeyBinding("Debug", 0, "May2Beez"));
    }

    @Override
    public void onEnable() {
        testBlock = null;
        super.onEnable();
    }

    @Override
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (this.keyBinding != null && this.keyBinding.isPressed()) {
            MovingObjectPosition objectMouseOver = mc.objectMouseOver;
            if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                testBlock = objectMouseOver.getBlockPos();
            }
        }
    }

    @SubscribeEvent
    public void onWorldLastRenderEvent(RenderWorldLastEvent event) {
        if (testBlock != null) {
            RenderUtils.drawOutline(testBlock, Color.orange, 2f);
        }
        if (!testPoints.isEmpty()) {
            for (Vec3 pos : testPoints) {
                RenderUtils.miniBlockBox(pos, Color.lightGray, 2f);
            }
        }
    }

    private final CopyOnWriteArrayList<Vec3> testPoints = new CopyOnWriteArrayList<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        testPoints.clear();
        if (testBlock == null) return;

        testPoints.addAll(BlockUtils.getAllVisibilityLines(testBlock));
    }
}
