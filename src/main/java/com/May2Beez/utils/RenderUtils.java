package com.May2Beez.utils;


import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;

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

    public static void drawEntityBox(final Entity entity, final Color color, final int width, float partialTicks) {
        if(width == 0) return;
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

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

        GL11.glLineWidth(width);
        GL11.glEnable(GL_LINE_SMOOTH);
        GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), 95);
        drawSelectionBoundingBox(axisAlignedBB);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.depthMask(true);
    }

    public static void miniBlockBox(Vec3 block, Color color) {
        drawBlockBox(new AxisAlignedBB(block.xCoord - 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosX,
                block.yCoord - 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosY,
                block.zCoord - 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ,
                block.xCoord + 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosX,
                block.yCoord + 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosY,
                block.zCoord + 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ), color);
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

    public static void drawText(String text, double x, double y, double z, Color color) {
        drawText(text, x, y, z, color, false);
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

    private static void drawBlockBox(AxisAlignedBB bb, Color color) {
        GlStateManager.pushMatrix();
        glLineWidth(2f);
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (Math.max(color.getAlpha() - 100, 75)) / 255f);
        drawSolidBox(bb);

        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (Math.max(color.getAlpha(), 175)) / 255f);
        drawOutlinedBox(bb);
        GlStateManager.popMatrix();
    }

    public static void drawBlockBox(BlockPos blockPos, Color color, float lineWidth) {
        GlStateManager.pushMatrix();
        GL11.glLineWidth(lineWidth);

        AxisAlignedBB bb;
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        if (Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock() != null) {
            bb = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, blockPos);
        } else {
            bb = new AxisAlignedBB(blockPos, blockPos.add(1, 1, 1));
        }

        bb = bb.offset(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (Math.max(color.getAlpha() - 100, 75)) / 255f);
        drawSolidBox(bb);

        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (Math.max(color.getAlpha(), 175)) / 255f);
        drawOutlinedBox(bb);
        GlStateManager.popMatrix();
    }

    public static void drawLineBetweenPoints(BlockPos pos1, BlockPos pos2, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

        GL11.glLineWidth(10f);
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        worldRenderer.pos(pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5).endVertex();
        worldRenderer.pos(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }


    public static void drawSolidBox(AxisAlignedBB bb) {
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();

        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();

        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();

        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();

        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();

        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawOutlinedBox(AxisAlignedBB bb) {
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        // draw bottom face
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        tessellator.draw();

        // draw top face
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        tessellator.draw();

        // draw front face
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        tessellator.draw();

        // draw back face
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        tessellator.draw();

        // draw left face
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        tessellator.draw();

        // draw right face
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}

