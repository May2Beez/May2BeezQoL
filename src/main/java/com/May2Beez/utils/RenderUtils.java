package com.May2Beez.utils;


import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtils {

    private static final ResourceLocation beaconBeam = new ResourceLocation("textures/entity/beacon_beam.png");

    public static void drawEntityBox(final Entity entity, final Color color, final int width, float partialTicks) {
        if(width == 0) return;
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
                - renderManager.viewerPosX;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
                - renderManager.viewerPosY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
                - renderManager.viewerPosZ;

        final AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - entity.posX + x - 0.1D,
                entityBox.minY - entity.posY + y - 0.1D,
                entityBox.minZ - entity.posZ + z - 0.1D,
                entityBox.maxX - entity.posX + x + 0.1D,
                entityBox.maxY - entity.posY + y + 0.1D,
                entityBox.maxZ - entity.posZ + z + 0.1D
        );

        drawBlockBox(axisAlignedBB, color, width);
    }

    public static void miniBlockBox(Vec3 block, Color color, float lineWidth) {
        drawBlockBox(new AxisAlignedBB(block.xCoord - 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosX,
                block.yCoord - 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosY,
                block.zCoord - 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ,
                block.xCoord + 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosX,
                block.yCoord + 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosY,
                block.zCoord + 0.05D - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ), color, lineWidth);
    }

    public static void renderBoxedText(String[] text, int x, int y, Double scale) {
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
                Minecraft.getMinecraft().fontRendererObj.drawString(s, Math.round(x / scale), Math.round(yOffset / scale), Color.white.getRGB(), true);
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
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

    public static void drawOutline(BlockPos blockPos, Color color, float lineWidth) {
        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);

        if (blockState == null) return;

        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(lineWidth);

        AxisAlignedBB bb;
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        Block block = blockState.getBlock();
        block.setBlockBoundsBasedOnState(Minecraft.getMinecraft().theWorld, blockPos);
        bb = block.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, blockPos);

        bb = bb.offset(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (color.getAlpha()) / 255f);
        RenderGlobal.drawSelectionBoundingBox(bb);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GL11.glLineWidth(1);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawBlockBox(BlockPos blockPos, Color color, float lineWidth) {
        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);

        if (blockState == null) return;

        AxisAlignedBB bb;
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        Block block = blockState.getBlock();
        block.setBlockBoundsBasedOnState(Minecraft.getMinecraft().theWorld, blockPos);
        bb = block.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, blockPos);

        bb = bb.offset(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

        drawBlockBox(bb, color, lineWidth);
    }

    public static void drawBlockBox(AxisAlignedBB bb, Color color, float lineWidth) {
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();

        GL11.glLineWidth(lineWidth);
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (Math.max(color.getAlpha() - 100, 30)) / 255f);
        drawSolidBox(bb);

        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (Math.max(color.getAlpha(), 130)) / 255f);
        RenderGlobal.drawSelectionBoundingBox(bb);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GL11.glLineWidth(1);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawLineBetweenPoints(Vec3 pos1, Vec3 pos2, Color color) {
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
        worldRenderer.pos(pos1.xCoord, pos1.yCoord, pos1.zCoord).endVertex();
        worldRenderer.pos(pos2.xCoord, pos2.yCoord, pos2.zCoord).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GL11.glLineWidth(1);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }


    public static void drawSolidBox(AxisAlignedBB aabb) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex();
        worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex();
        tessellator.draw();
    }

    public static void renderBeacon(Vec3 location, Color color, float partialTicks) {
        int height = 300;
        int bottomOffset = 0;
        int topOffset = bottomOffset + height;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.translate(-Minecraft.getMinecraft().getRenderManager().viewerPosX, -Minecraft.getMinecraft().getRenderManager().viewerPosY, -Minecraft.getMinecraft().getRenderManager().viewerPosZ);
        Minecraft.getMinecraft().getTextureManager().bindTexture(beaconBeam);

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.disableLighting();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        double time = Minecraft.getMinecraft().theWorld.getWorldTime() + partialTicks;

        double d1 = MathHelper.func_181162_h(-time * 0.2 - MathHelper.floor_double(-time * 0.1));

        double d2 = time * 0.025 * -1.5;
        double d4 = 0.5 + Math.cos(d2 + 2.356194490192345) * 0.2;
        double d5 = 0.5 + Math.sin(d2 + 2.356194490192345) * 0.2;
        double d6 = 0.5 + Math.cos(d2 + (Math.PI / 4)) * 0.2;
        double d7 = 0.5 + Math.sin(d2 + (Math.PI / 4)) * 0.2;
        double d8 = 0.5 + Math.cos(d2 + 3.9269908169872414) * 0.2;
        double d9 = 0.5 + Math.sin(d2 + 3.9269908169872414) * 0.2;
        double d10 = 0.5 + Math.cos(d2 + 5.497787143782138) * 0.2;
        double d11 = 0.5 + Math.sin(d2 + 5.497787143782138) * 0.2;
        double d14 = -1 + d1;
        double d15 = height * 2.5 + d14;

        double x = location.xCoord;
        double y = location.yCoord;
        double z = location.zCoord;

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0D, d15).color(color.getRed(), color.getGreen(), color.getBlue(),1.0F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0D, d14).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0D, d14).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0D, d15).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(1.0D, d15).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(1.0D, d14).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(0.0D, d14).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(0.0D, d15).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(1.0D, d15).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(1.0D, d14).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(0.0D, d14).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(0.0D, d15).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(1.0D, d15).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(1.0D, d14).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(0.0D, d14).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(0.0D, d15).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F * color.getAlpha()).endVertex();
        tessellator.draw();

        GlStateManager.disableCull();
        double d12 = -1.0D + d1;
        double d13 = height + d12;

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.2D).tex(1.0D, d13).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.2D).tex(1.0D, d12).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.2D).tex(0.0D, d12).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.2D).tex(0.0D, d13).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.8D).tex(1.0D, d13).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.8D).tex(1.0D, d12).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.8D).tex(0.0D, d12).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.8D).tex(0.0D, d13).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.2D).tex(1.0D, d13).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.2D).tex(1.0D, d12).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.8D).tex(0.0D, d12).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.8D).tex(0.0D, d13).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.8D).tex(1.0D, d13).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F * color.getAlpha()).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.8D).tex(1.0D, d12).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.2D).tex(0.0D, d12).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.2D).tex(0.0D, d13).color(color.getRed(), color.getGreen(), color.getBlue(), 0.25F * color.getAlpha()).endVertex();
        tessellator.draw();

        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();

        GlStateManager.enableDepth();

        GlStateManager.popMatrix();
    }
}

