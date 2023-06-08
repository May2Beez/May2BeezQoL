package com.May2Beez.modules;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.utils.BlockUtils;
import com.May2Beez.utils.RenderUtils;
import com.May2Beez.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class Debug extends Module {

    private BlockPos testBlock = null;
    private final Minecraft mc = Minecraft.getMinecraft();

    public Debug() {
        super("Debug", new KeyBinding("Debug Key", Keyboard.KEY_H, "May2BeezQoL - Debug"));
    }
    private final static Frustum frustrum = new Frustum();

    @Override
    public void onEnable() {
        testBlock = null;
        super.onEnable();
    }

    @Override
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (this.keyBinding != null && this.keyBinding.isPressed()) {
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                testBlock = mc.objectMouseOver.getBlockPos();
            } else {
                testBlock = null;
            }
        }
    }

    @SubscribeEvent
    public void onWorldLastRenderEvent(RenderWorldLastEvent event) {
        if (testBlock != null) {
            RenderUtils.drawOutline(testBlock, Color.orange, 2f);
        }
    }


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (testBlock == null) return;

        frustrum.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        System.out.println("visible: " + frustrum.isBoundingBoxInFrustum(new AxisAlignedBB(testBlock.getX(), testBlock.getY(), testBlock.getZ(), testBlock.getX() + 1, testBlock.getY() + 1, testBlock.getZ() + 1)));
    }

    public static boolean isUngrabbed = false;
    private static MouseHelper oldMouseHelper;
    private static boolean doesGameWantUngrabbed;
    public static void ungrabMouse() {
        Minecraft m = Minecraft.getMinecraft();
        if (isUngrabbed) return;
        m.gameSettings.pauseOnLostFocus = false;
        if (oldMouseHelper == null) oldMouseHelper = m.mouseHelper;
        doesGameWantUngrabbed = !Mouse.isGrabbed();
        oldMouseHelper.ungrabMouseCursor();
        m.inGameHasFocus = true;
        m.mouseHelper = new MouseHelper() {
            @Override
            public void mouseXYChange() {
            }
            @Override
            public void grabMouseCursor() {
                doesGameWantUngrabbed = false;
            }
            @Override
            public void ungrabMouseCursor() {
                doesGameWantUngrabbed = true;
            }
        };
        isUngrabbed = true;
    }

    /**
     * This function performs all the steps required to regrab the mouse.
     */
    public static void regrabMouse() {
        if (!isUngrabbed) return;
        Minecraft m = Minecraft.getMinecraft();
        m.mouseHelper = oldMouseHelper;
        if (!doesGameWantUngrabbed) m.mouseHelper.grabMouseCursor();
        oldMouseHelper = null;
        isUngrabbed = false;
    }

    public static ArrayList<Vec3> getBoundingPoints(ArrayList<Vec3> points) {
        ArrayList<Vec3> boundingPoints = new ArrayList<>();

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;

        for (Vec3 point : points) {
            minX = Math.min(minX, point.xCoord);
            minY = Math.min(minY, point.yCoord);
            minZ = Math.min(minZ, point.zCoord);
            maxX = Math.max(maxX, point.xCoord);
            maxY = Math.max(maxY, point.yCoord);
            maxZ = Math.max(maxZ, point.zCoord);
        }

        // add the bounding points by connecting min and max coordinates
        boundingPoints.add(new Vec3(minX, minY, minZ));
        boundingPoints.add(new Vec3(minX, minY, maxZ));
        boundingPoints.add(new Vec3(minX, maxY, minZ));
        boundingPoints.add(new Vec3(minX, maxY, maxZ));
        boundingPoints.add(new Vec3(maxX, minY, minZ));
        boundingPoints.add(new Vec3(maxX, minY, maxZ));
        boundingPoints.add(new Vec3(maxX, maxY, minZ));
        boundingPoints.add(new Vec3(maxX, maxY, maxZ));

        return boundingPoints;
    }
}
