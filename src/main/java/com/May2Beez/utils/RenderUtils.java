package com.May2Beez.utils;


import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import static net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox;
import static org.lwjgl.opengl.GL11.*;

public class RenderUtils {

    private static final Map<Integer, Boolean> glCapMap = new HashMap<>();

    public static void setColor(Color c) {
        GL11.glColor4f(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, c
                .getAlpha() / 255.0F);
    }

    public static void setGlCap(final int cap, final boolean state) {
        glCapMap.put(cap, glGetBoolean(cap));
        setGlState(cap, state);
    }

    public static void setGlState(final int cap, final boolean state) {
        if (state)
            glEnable(cap);
        else
            glDisable(cap);
    }

    public static void disableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, false);
    }

    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GL11.glColor4f(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public static void resetCaps() {
        glCapMap.forEach(RenderUtils::setGlState);
    }




    public static void drawEntityBox(final Entity entity, final Color color, final int width, float partialTicks) {
        if(width == 0) return;
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
                - renderManager.viewerPosX;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
                - renderManager.viewerPosY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
                - renderManager.viewerPosZ;

        final AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - entity.posX + x - 0.05D,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z - 0.05D,
                entityBox.maxX - entity.posX + x + 0.05D,
                entityBox.maxY - entity.posY + y + 0.15D,
                entityBox.maxZ - entity.posZ + z + 0.05D
        );

        glLineWidth((float) width);
        glEnable(GL_LINE_SMOOTH);
        glColor(color.getRed(), color.getGreen(), color.getBlue(), 95);
        drawSelectionBoundingBox(axisAlignedBB);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), 26);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glDepthMask(true);
        resetCaps();
    }

    public static void blockBox(BlockPos block, Color color) {
        glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        setColor(color);
        drawSelectionBoundingBox(new AxisAlignedBB(block.getX() - (Minecraft.getMinecraft().getRenderManager()).viewerPosX,
                block.getY() - (Minecraft.getMinecraft().getRenderManager()).viewerPosY,
                block.getZ() - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ,
                (block.getX() + 1) - (Minecraft.getMinecraft().getRenderManager()).viewerPosX,
                (block.getY() + 1) - (Minecraft.getMinecraft().getRenderManager()).viewerPosY,
                (block.getZ() + 1) - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ));
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public static void miniBlockBox(Vec3 block, Color color) {
        glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        setColor(color);
        drawSelectionBoundingBox(new AxisAlignedBB(block.xCoord - 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosX,
                block.yCoord - 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosY,
                block.zCoord - 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ,
                block.xCoord + 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosX,
                block.yCoord + 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosY,
                block.zCoord + 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ));
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public static void renderText(String text, int x, int y) {
        renderText(text, x, y, 1.0, 0xFFFFFF);
    }

    public static void renderText(String text, int x, int y, Double scale, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        GlStateManager.scale(scale, scale, scale);
        int yOffset = y - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        String[] textArray = text.split("\n");
        for (String s : textArray) {
            yOffset += (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * scale);
            Minecraft.getMinecraft().fontRendererObj.drawString(s, Math.round(x / scale), Math.round(yOffset / scale), color, true);
        }
        GlStateManager.popMatrix();
    }

    public static void renderBoxedText(String[] text, int x, int y) { renderBoxedText(text, x, y, 1.0, 0xFFFFFF);}

    public static void renderBoxedText(String[] text, int x, int y, Double scale, int color) {
        String longestString = Arrays.stream(text).max(Comparator.comparingInt(String::length)).get();
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.color(0, 0, 0, 1f);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        Gui.drawRect(x - 5, y - 5, x + fontRenderer.getStringWidth(longestString) + 5, y + (text.length * 9) + 5, new Color(0, 0, 0, 153).getRGB());

        for (int i = 0; i < text.length; i++) {
            int yOffset = (y + (i * 9));
            String[] textArray = text[i].split("\n");
            for (String s : textArray) {
                Minecraft.getMinecraft().fontRendererObj.drawString(s, Math.round(x / scale), Math.round(yOffset / scale), color, true);
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void preDraw() {
//        glEnable(GL_BLEND);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        glEnable(GL_LINE_SMOOTH);
//        glDisable(GL_TEXTURE_2D);
//        glEnable(GL_CULL_FACE);
//        glDisable(GL_DEPTH_TEST);
        glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        double renderPosX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        glPushMatrix();
        glTranslated(-renderPosX, -renderPosY, -renderPosZ);
    }

    public static void postDraw() {
        glPopMatrix();

        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public static void drawText(String text, double x, double y, double z, Color color) {
        drawText(text, x, y, z, color);
    }

    public static void drawText(String text, double x, double y, double z, Color color, boolean renderBlackBox) {
        drawText(text, x, y, z, color, renderBlackBox, 1f);
    }

    public static void drawText(String text, double x, double y, double z, Color color, boolean renderBlackBox, float scale) {
        drawText(text, x, y, z, color, renderBlackBox, scale, false);
    }

    public static void drawText(String text, double x, double y, double z, Color color, boolean renderBlackBox, float scale, boolean increase) {
        float lScale = scale;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        double renderPosX = x - Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = y - Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = z - Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        if (increase) {
            double distance = Math.sqrt(renderPosX * renderPosX + renderPosY * renderPosY + renderPosZ * renderPosZ);
            double multiplier = Math.max(distance / 150f, 0.1f);
            lScale *= 0.45f * multiplier;
        }

        float xMultiplier = Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1 : 1;

        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        GlStateManager.pushMatrix();
        GlStateManager.translate(renderPosX, renderPosY, renderPosZ);
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        GlStateManager.rotate(-renderManager.playerViewY, 0, 1, 0);
        GlStateManager.rotate(renderManager.playerViewX * xMultiplier, 1, 0, 0);
        GlStateManager.scale(-lScale, -lScale, lScale);
        GlStateManager.disableLighting();
        glDepthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);

        int textWidth = fontRenderer.getStringWidth(text);

        if (renderBlackBox) {
            float j = textWidth / 2f;
            GlStateManager.disableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.color(0, 0, 0, 0.25f);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION);
            worldrenderer.pos(-j - 1, -1, 0).endVertex();
            worldrenderer.pos(-j - 1, 8, 0).endVertex();
            worldrenderer.pos(j + 1, 8, 0).endVertex();
            worldrenderer.pos(j + 1, -1, 0).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        fontRenderer.drawString(text, -textWidth / 2, 0, color.getRGB());
        GlStateManager.enableDepth();
        glDepthMask(true);
        glPopMatrix();
    }

    public static void drawBlockBox(BlockPos blockPos, Color color, float lineWidth) {
        glLineWidth(lineWidth);

        AxisAlignedBB bb;

        if (Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock() != null) {
            bb = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, blockPos);
        } else {
            bb = new AxisAlignedBB(blockPos, blockPos.add(1, 1, 1));
        }

        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (Math.max(color.getAlpha() - 100, 75)) / 255f);
        drawSolidBox(bb);

        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (Math.max(color.getAlpha(), 175)) / 255f);
        drawOutlinedBox(bb);
    }

    public static void drawLineBetweenPoints(BlockPos pos1, BlockPos pos2, Color color) {
        GL11.glLineWidth(4f);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glColor4f(Color.green.getRed() / 255f, Color.green.getGreen() / 255f, Color.green.getBlue() / 255f, 120 / 255f);

        GL11.glVertex3d(pos1.getX() + 0.5,pos1.getY() + 0.5,pos1.getZ() + 0.5);
        GL11.glVertex3d(pos2.getX() + 0.5,pos2.getY() + 0.5,pos2.getZ() + 0.5);
        GL11.glEnd();
    }


    public static void drawSolidBox(AxisAlignedBB bb) {

        glBegin(GL_QUADS);
        {
            glVertex3d(bb.minX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.minY, bb.maxZ);
            glVertex3d(bb.minX, bb.minY, bb.maxZ);

            glVertex3d(bb.minX, bb.maxY, bb.minZ);
            glVertex3d(bb.minX, bb.maxY, bb.maxZ);
            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
            glVertex3d(bb.maxX, bb.maxY, bb.minZ);

            glVertex3d(bb.minX, bb.minY, bb.minZ);
            glVertex3d(bb.minX, bb.maxY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.minZ);
            glVertex3d(bb.maxX, bb.minY, bb.minZ);

            glVertex3d(bb.maxX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
            glVertex3d(bb.maxX, bb.minY, bb.maxZ);

            glVertex3d(bb.minX, bb.minY, bb.maxZ);
            glVertex3d(bb.maxX, bb.minY, bb.maxZ);
            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.maxZ);

            glVertex3d(bb.minX, bb.minY, bb.minZ);
            glVertex3d(bb.minX, bb.minY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.minZ);
        }
        glEnd();
    }

    public static void drawOutlinedBox(AxisAlignedBB bb) {

        glBegin(GL_LINES);
        {
            glVertex3d(bb.minX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.minY, bb.minZ);

            glVertex3d(bb.maxX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.minY, bb.maxZ);

            glVertex3d(bb.maxX, bb.minY, bb.maxZ);
            glVertex3d(bb.minX, bb.minY, bb.maxZ);

            glVertex3d(bb.minX, bb.minY, bb.maxZ);
            glVertex3d(bb.minX, bb.minY, bb.minZ);

            glVertex3d(bb.minX, bb.minY, bb.minZ);
            glVertex3d(bb.minX, bb.maxY, bb.minZ);

            glVertex3d(bb.maxX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.minZ);

            glVertex3d(bb.maxX, bb.minY, bb.maxZ);
            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

            glVertex3d(bb.minX, bb.minY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.maxZ);

            glVertex3d(bb.minX, bb.maxY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.minZ);

            glVertex3d(bb.maxX, bb.maxY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.maxZ);

            glVertex3d(bb.minX, bb.maxY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.minZ);
        }
        glEnd();
    }
}

