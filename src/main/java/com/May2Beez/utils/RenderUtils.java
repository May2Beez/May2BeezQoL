package com.May2Beez.utils;


import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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

    public static void enableGlCap(final int cap) {
        setGlCap(cap, true);
    }

    public static void enableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, true);
    }

    public static void disableGlCap(final int cap) {
        setGlCap(cap, true);
    }

    public static void disableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, false);
    }

    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GL11.glColor4f(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public static void glColor(final Color color) {
        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private static void glColor(final int hex) {
        glColor(hex >> 16 & 0xFF, hex >> 8 & 0xFF, hex & 0xFF, hex >> 24 & 0xFF);
    }

    public static void resetCaps() {
        glCapMap.forEach(RenderUtils::setGlState);
    }

    public static void drawLineBetweenPoints(BlockPos pos1, BlockPos pos2) {
        drawLineBetweenPoints(pos1, pos2, null);
    }

    public static void drawLineBetweenPoints(BlockPos pos1, BlockPos pos2, RenderWorldLastEvent event) {
        drawLineBetweenPoints(new Vec3(pos1), pos2, event);
    }

    public static void drawLineBetweenPoints(Vec3 pos1, BlockPos pos2, RenderWorldLastEvent event) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(5f);
        Minecraft mc = Minecraft.getMinecraft();
        double x = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * (double)event.partialTicks;
        double y = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * (double)event.partialTicks;
        double z = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (double)event.partialTicks;
        Vec3 pos = new Vec3(x, y, z);
        GL11.glTranslated(-pos.xCoord, -pos.yCoord, -pos.zCoord);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glColor4f(Color.green.getRed() / 255f, Color.green.getGreen() / 255f, Color.green.getBlue() / 255f, 120 / 255f);

        GL11.glVertex3d(pos1.xCoord + 0.5,pos1.yCoord + 0.5,pos1.zCoord + 0.5);
        GL11.glVertex3d(pos2.getX() + 0.5,pos2.getY() + 0.5,pos2.getZ() + 0.5);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnd();
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void drawText(String text, BlockPos pos) {
        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        double x = ((pos.getX() - player.posX) + 0.5) ;
        double y = ((pos.getY() - player.posY) + 0.5);
        double z = ((pos.getZ() - player.posZ) + 0.5);
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        float f = 1.6F;
        float f1 = 0.016666668F * f;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x + 0.0F, (float)y + 1, (float)z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-f1, -f1, f1);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int i = 0;

        int j = fontrenderer.getStringWidth(text) / 2;
        GlStateManager.disableTexture2D();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)(-j - 1), (double)(-1 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(-j - 1), (double)(8 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(j + 1), (double)(8 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(j + 1), (double)(-1 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        fontrenderer.drawString(text, -fontrenderer.getStringWidth(text) / 2, i, 553648127);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawEntityBox(final Entity entity, final Color color, final int width, float partialTicks) {
        if(width == 0) return;
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
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
        enableGlCap(GL_LINE_SMOOTH);
        glColor(color.getRed(), color.getGreen(), color.getBlue(), 95);
        drawSelectionBoundingBox(axisAlignedBB);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), 26);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glDepthMask(true);
        resetCaps();
    }

    public static void drawSolidBox(AxisAlignedBB bb, Color color, RenderWorldLastEvent event) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(5f);
        Minecraft mc = Minecraft.getMinecraft();
        double x = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * (double)event.partialTicks;
        double y = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * (double)event.partialTicks;
        double z = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (double)event.partialTicks;
        Vec3 pos = new Vec3(x, y, z);
        GL11.glTranslated(-pos.xCoord, -pos.yCoord, -pos.zCoord);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
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
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnd();
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void blockBox(TileEntity block, Color color) {
        glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        setColor(color);
        drawSelectionBoundingBox(new AxisAlignedBB(

                (block.getRenderBoundingBox()).minX -

                        (Minecraft.getMinecraft().getRenderManager()).viewerPosX,
                (block.getRenderBoundingBox()).minY -

                        (Minecraft.getMinecraft().getRenderManager()).viewerPosY,
                (block.getRenderBoundingBox()).minY -

                        (Minecraft.getMinecraft().getRenderManager()).viewerPosZ,
                (block.getRenderBoundingBox()).maxX -

                        (Minecraft.getMinecraft().getRenderManager()).viewerPosX,
                (block.getRenderBoundingBox()).maxY -

                        (Minecraft.getMinecraft().getRenderManager()).viewerPosY,
                (block.getRenderBoundingBox()).maxZ -

                        (Minecraft.getMinecraft().getRenderManager()).viewerPosZ));
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public static void fullBlockBox(BlockPos block, Color color) {
        glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        setColor(color);

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

    public static void drawBlockBox(final BlockPos blockPos, final Color color, final int width, float partialTicks) {
        if (width == 0) return;
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        final double x = blockPos.getX() - renderManager.viewerPosX;
        final double y = blockPos.getY() - renderManager.viewerPosY;
        final double z = blockPos.getZ() - renderManager.viewerPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        final Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();

        if (block != null) {
            final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            final double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            final double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            final double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

            block.setBlockBoundsBasedOnState(Minecraft.getMinecraft().theWorld, blockPos);

            axisAlignedBB = block.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, blockPos)
                    .expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
                    .offset(-posX, -posY, -posZ);
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

        glLineWidth((float) width);
        enableGlCap(GL_LINE_SMOOTH);

        drawSelectionBoundingBox(axisAlignedBB);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glDepthMask(true);
        resetCaps();
    }
}

