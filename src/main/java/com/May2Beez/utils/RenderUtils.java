package com.May2Beez.utils;


import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;

import com.May2Beez.mixins.accessors.RenderManagerAccessor;
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

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawEntityBox(final Entity entity, final Color color, final int lineWidth, float partialTicks) {
        RenderManagerAccessor rm = (RenderManagerAccessor) Minecraft.getMinecraft().getRenderManager();

        double renderPosX = rm.getRenderPosX();
        double renderPosY = rm.getRenderPosY();
        double renderPosZ = rm.getRenderPosZ();

        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - renderPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - renderPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - renderPosZ;

        AxisAlignedBB bbox = entity.getEntityBoundingBox();
        AxisAlignedBB aabb = new AxisAlignedBB(
                bbox.minX - entity.posX + x,
                bbox.minY - entity.posY + y,
                bbox.minZ - entity.posZ + z,
                bbox.maxX - entity.posX + x,
                bbox.maxY - entity.posY + y,
                bbox.maxZ - entity.posZ + z
        ).expand(0.1, 0.1, 0.1);

        drawFilledBoundingBox(aabb, color, 0.7f, lineWidth);
    }

    public static void miniBlockBox(Vec3 vec, Color color, float lineWidth) {
        RenderManagerAccessor renderManager = (RenderManagerAccessor) Minecraft.getMinecraft().getRenderManager();

        double renderPosX = renderManager.getRenderPosX();
        double renderPosY = renderManager.getRenderPosY();
        double renderPosZ = renderManager.getRenderPosZ();

        double x = vec.xCoord - renderPosX;
        double y = vec.yCoord - renderPosY;
        double z = vec.zCoord - renderPosZ;

        AxisAlignedBB aabb = new AxisAlignedBB(
                x - 0.05,
                y - 0.05,
                z - 0.05,
                x + 0.05,
                y + 0.05,
                z + 0.05
        );

        drawFilledBoundingBox(aabb, color, 0.7f, lineWidth);
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

    public static void drawText(String str, double X, double Y, double Z, float partialTicks, boolean showDist) {
        GlStateManager.alphaFunc(516, 0.1F);

        GlStateManager.pushMatrix();

        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

        double x = X - viewerX;
        double y = Y - viewerY - viewer.getEyeHeight();
        double z = Z - viewerZ;

        double distSq = x * x + y * y + z * z;
        double dist = Math.sqrt(distSq);
        if (distSq > 144) {
            x *= 12 / dist;
            y *= 12 / dist;
            z *= 12 / dist;
        }
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0, viewer.getEyeHeight(), 0);

        drawNametag(str);

        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0, -0.25f, 0);
        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

        if (showDist) {
            drawNametag("§e" + Math.round(dist * 10) / 10 + " blocks");
        }

        GlStateManager.popMatrix();

        GlStateManager.disableLighting();
    }

    public static void drawNametag(String str) {
        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
        float f = 1.6F;
        float f1 = 0.016666668F * f;
        GlStateManager.pushMatrix();
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-f1, -f1, f1);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferBuilder = tessellator.getWorldRenderer();

        int textWidth = fontrenderer.getStringWidth(StringUtils.stripControlCodes(str));

        float j = textWidth / 2f;
        GlStateManager.disableTexture2D();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(-j - 1, -1, 0).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferBuilder.pos(-j - 1, 8, 0).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferBuilder.pos(j + 1, 8, 0).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferBuilder.pos(j + 1, -1, 0).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        fontrenderer.drawString(str, (int) -j, 0, 553648127);
        GlStateManager.depthMask(true);

        fontrenderer.drawString(str, (int) -j, 0, -1);

        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawOutline(BlockPos blockPos, Color color, float lineWidth, float partialTicks) {
        if (blockPos != null) {
            IBlockState blockState = mc.theWorld.getBlockState(blockPos);

            if (blockState != null) {
                Block block = blockState.getBlock();
                block.setBlockBoundsBasedOnState(mc.theWorld, blockPos);
                double d0 = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * (double) partialTicks;
                double d1 = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * (double) partialTicks;
                double d2 = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * (double) partialTicks;
                drawFilledBoundingBox(block.getSelectedBoundingBox(mc.theWorld, blockPos).expand(0.002D, 0.002D, 0.002D).offset(-d0, -d1, -d2), color, 0f, lineWidth);
            }
        }
    }

    public static void drawDoubleChestBlockBox(BlockPos blockPos1, BlockPos blockPos2, Color color, float lineWidth, float partialTicks) {
        if (blockPos1 != null && blockPos2 != null) {
            // Get the IBlockState and Block objects for both BlockPos objects
            IBlockState blockState1 = mc.theWorld.getBlockState(blockPos1);
            IBlockState blockState2 = mc.theWorld.getBlockState(blockPos2);
            Block block1 = blockState1.getBlock();
            Block block2 = blockState2.getBlock();

            if (block1 != null && block2 != null) {
                // Set the block bounds based on the block state
                block1.setBlockBoundsBasedOnState(mc.theWorld, blockPos1);
                block2.setBlockBoundsBasedOnState(mc.theWorld, blockPos2);

                // Calculate the bounding box for the double chest by combining the bounding boxes of both blocks
                AxisAlignedBB boundingBox1 = block1.getSelectedBoundingBox(mc.theWorld, blockPos1).expand(0.002D, 0.002D, 0.002D);
                AxisAlignedBB boundingBox2 = block2.getSelectedBoundingBox(mc.theWorld, blockPos2).expand(0.002D, 0.002D, 0.002D);
                AxisAlignedBB boundingBox = boundingBox1.union(boundingBox2);

                // Calculate the player's position for the given partial ticks
                double d0 = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * (double) partialTicks;
                double d1 = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * (double) partialTicks;
                double d2 = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * (double) partialTicks;

                // Offset the bounding box and draw it
                drawFilledBoundingBox(boundingBox.offset(-d0, -d1, -d2), color, 0.7f, lineWidth);
            }
        }
    }

    public static void drawBlockBox(BlockPos blockPos, Color color, float lineWidth, float partialTicks) {
        if (blockPos != null) {
            IBlockState blockState = mc.theWorld.getBlockState(blockPos);

            if (blockState != null) {
                Block block = blockState.getBlock();
                block.setBlockBoundsBasedOnState(mc.theWorld, blockPos);
                double d0 = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * (double) partialTicks;
                double d1 = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * (double) partialTicks;
                double d2 = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * (double) partialTicks;
                drawFilledBoundingBox(block.getSelectedBoundingBox(mc.theWorld, blockPos).expand(0.002D, 0.002D, 0.002D).offset(-d0, -d1, -d2), color, 0.7f, lineWidth);
            }
        }
    }

    public static void drawBlockBox(AxisAlignedBB bb, Color color, float lineWidth, float partialTicks) {
        double d0 = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * (double) partialTicks;
        double d1 = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * (double) partialTicks;
        double d2 = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * (double) partialTicks;
        drawFilledBoundingBox(bb.expand(0.002D, 0.002D, 0.002D).offset(-d0, -d1, -d2), color, 0.7f, lineWidth);
    }

    public static void drawFilledBoundingBox(AxisAlignedBB aabb, Color color, float opacity, float lineWidth) {
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        float a = color.getAlpha() / 255.0F;
        float r = color.getRed() / 255.0F;
        float g = color.getGreen() / 255.0F;
        float b = color.getBlue() / 255.0F;

        GlStateManager.color(r, g, b, a * opacity);
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
        GlStateManager.color(r, g, b, a * opacity);
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
        GlStateManager.color(r, g, b, a * opacity);
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
        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(lineWidth);
        RenderGlobal.drawSelectionBoundingBox(aabb);
        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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

    public static void drawLineBetweenPoints(Vec3 pos1, Vec3 pos2, Color color, float partialTicks, float thickness) {
        final Entity render = mc.getRenderViewEntity();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferBuilder = tessellator.getWorldRenderer();
        final double realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks;
        final double realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks;
        final double realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks;
        GlStateManager.pushMatrix();
        GlStateManager.translate(-realX, -realY, -realZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glDisable(3553);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GL11.glLineWidth(thickness);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

        bufferBuilder.pos(pos1.xCoord + 0.5, pos1.yCoord + 0.5, pos1.zCoord + 0.5).color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f).endVertex();
        bufferBuilder.pos(pos2.xCoord + 0.5, pos2.yCoord + 0.5, pos2.zCoord + 0.5).color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f).endVertex();

        Tessellator.getInstance().draw();
        GlStateManager.translate(realX, realY, realZ);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }
}

