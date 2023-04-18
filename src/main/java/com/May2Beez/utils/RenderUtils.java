package com.May2Beez.utils;


import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;

import com.May2Beez.mixins.accessors.RenderManagerAccessor;
import com.May2Beez.mixins.accessors.RendererLivingEntityAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class RenderUtils {

    private static final ResourceLocation beaconBeam = new ResourceLocation("textures/entity/beacon_beam.png");

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawEntityBox(final Entity entity, final Color color, final int lineWidth, float partialTicks) {
        RenderManagerAccessor rm = (RenderManagerAccessor) mc.getRenderManager();

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
        );

        drawFilledBoundingBox(aabb, color, 0.7f, lineWidth);
    }

    public static void drawEntityESP(EntityLivingBase entity, ModelBase model, Color color, float partialTicks) {
        ModelData modelData = preModelDraw(entity, model, partialTicks);
        outlineEntity(
                model,
                entity,
                modelData.limbSwing,
                modelData.limbSwingAmount,
                modelData.age,
                modelData.rotationYaw,
                modelData.rotationPitch,
                0.0625f,
                partialTicks,
                color
        );
        GlStateManager.resetColor();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    public static void outlineEntity(
            ModelBase model,
            EntityLivingBase entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float headYaw,
            float headPitch,
            float scaleFactor,
            float partialTicks,
            Color color
    ) {
        RendererLivingEntityAccessor renderer = (RendererLivingEntityAccessor) mc.getRenderManager().getEntityRenderObject(entity);
        boolean fancyGraphics = mc.gameSettings.fancyGraphics;
        float gamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.fancyGraphics = false;
        mc.gameSettings.gammaSetting = Float.MAX_VALUE;
        float f3 = color.getAlpha() / 255f;
        float f = color.getRed() / 255f;
        float f1 = color.getGreen() / 255f;
        float f2 = color.getBlue() / 255f;
        GlStateManager.resetColor();
        GlStateManager.color(f, f1, f2, f3);
        renderOne();
        model.render(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scaleFactor);
        GlStateManager.color(f, f1, f2, f3);
        renderLayers(renderer, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, headYaw, headPitch, scaleFactor, f, f1, f2, f3);
        GlStateManager.color(f, f1, f2, f3);
        renderTwo();
        model.render(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scaleFactor);
        GlStateManager.color(f, f1, f2, f3);
        renderLayers(renderer, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, headYaw, headPitch, scaleFactor, f, f1, f2, f3);
        GlStateManager.color(f, f1, f2, f3);
        renderThree();
        model.render(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scaleFactor);
        renderLayers(renderer, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, headYaw, headPitch, scaleFactor, f, f1, f2, f3);
        GlStateManager.color(f, f1, f2, f3);
        GlStateManager.color(f, f1, f2, f3);
        renderFour();
        model.render(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scaleFactor);
        GlStateManager.color(f, f1, f2, f3);
        renderLayers(renderer, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, headYaw, headPitch, scaleFactor, f, f1, f2, f3);
        GlStateManager.color(f, f1, f2, f3);
        renderFive();
        mc.gameSettings.fancyGraphics = fancyGraphics;
        mc.gameSettings.gammaSetting = gamma;
    }

    static void renderLayers(
            RendererLivingEntityAccessor renderer,
            EntityLivingBase entitylivingbaseIn,
            float p_177093_2_,
            float p_177093_3_,
            float partialTicks,
            float p_177093_5_,
            float p_177093_6_,
            float p_177093_7_,
            float p_177093_8_,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (!(entitylivingbaseIn instanceof EntitySkeleton)) return;
        for (Object layerrenderer : renderer.getLayerRenderers()) {
            if (layerrenderer instanceof LayerArmorBase<?>) {
                for (int i = 1; i <= 4; i++) {
                    ItemStack itemstack = entitylivingbaseIn.getCurrentArmor(i - 1);
                    if (itemstack == null || !(itemstack.getItem() instanceof ItemArmor)) continue;

                    ModelBase armorModel = ((LayerArmorBase<?>) layerrenderer).getArmorModel(i);
                    armorModel.setLivingAnimations(entitylivingbaseIn, p_177093_2_, p_177093_3_, partialTicks);

                    GlStateManager.color(red, green, blue, alpha);
                    armorModel.render(entitylivingbaseIn, p_177093_2_, p_177093_3_, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_);
                }
            }
        }
    }

    private static void renderOne() {
        checkSetupFBO();
        GL11.glPushAttrib(1048575);
        GL11.glDisable(3008);
        GL11.glDisable(3553);
        GL11.glDisable(2896);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(5f);
        GL11.glEnable(2848);
        GL11.glEnable(2960);
        GL11.glClear(1024);
        GL11.glClearStencil(15);
        GL11.glStencilFunc(512, 1, 15);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glPolygonMode(1032, 6913);
    }

    private static void checkSetupFBO() {
        Framebuffer fbo = mc.getFramebuffer();
        if (fbo != null && fbo.depthBuffer > -1) {
            setupFBO(fbo);
            fbo.depthBuffer = -1;
        }
    }

    private static void setupFBO(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
        int stencilDepthBufferId = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(36161, stencilDepthBufferId);
        EXTFramebufferObject.glRenderbufferStorageEXT(
                36161,
                34041,
                mc.displayWidth,
                mc.displayHeight
        );
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencilDepthBufferId);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencilDepthBufferId);
    }

    private static void renderTwo() {
        GL11.glStencilFunc(512, 0, 15);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glPolygonMode(1032, 6914);
    }

    private static void renderThree() {
        GL11.glStencilFunc(514, 1, 15);
        GL11.glStencilOp(7680, 7680, 7680);
        GL11.glPolygonMode(1032, 6913);
    }

    private static void renderFour() {
        GL11.glDepthMask(false);
        GL11.glDisable(2929);
        GL11.glEnable(10754);
        GL11.glPolygonOffset(1.0f, -2000000.0f);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
    }

    private static void renderFive() {
        GL11.glPolygonOffset(1.0f, 2000000.0f);
        GL11.glDisable(10754);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(2960);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glEnable(3042);
        GL11.glEnable(2896);
        GL11.glEnable(3553);
        GL11.glEnable(3008);
        GL11.glPopAttrib();
    }

    private static ModelData preModelDraw(EntityLivingBase entity, ModelBase model, float partialTicks) {
        Render<Entity> render = mc.getRenderManager().getEntityRenderObject(entity);
        RenderManagerAccessor renderManager = (RenderManagerAccessor) mc.getRenderManager();
        RendererLivingEntityAccessor renderer = (RendererLivingEntityAccessor) render;
        float renderYaw = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        float prevYaw = interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
        float rotationYaw = prevYaw - renderYaw;
        float rotationPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        float limbSwing = entity.limbSwing - entity.limbSwingAmount * (1f - partialTicks);
        float limbSwingAmout = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
        float age = entity.ticksExisted + partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        model.swingProgress = entity.getSwingProgress(partialTicks);
        model.isChild = entity.isChild();
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        GlStateManager.translate(x - renderManager.getRenderPosX(), y - renderManager.getRenderPosY(), z - renderManager.getRenderPosZ());
        float f = interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        rotateCorpse(entity, age, f, partialTicks);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1f, -1f, 1f);
        GlStateManager.translate(0.0f, -1.5078125f, 0.0f);
        model.setLivingAnimations(entity, limbSwing, limbSwingAmout, partialTicks);
        model.setRotationAngles(limbSwing, limbSwingAmout, age, rotationYaw, rotationPitch, 0.0625f, entity);

        return new ModelData(renderer, rotationYaw, rotationPitch, limbSwing, limbSwingAmout, age);
    }


    public static float interpolateRotation(float par1, float par2, float par3) {
        float f = par2 - par1;
        while (f < -180.0f) {
            f += 360.0f;
        }
        while (f >= 180.0f) {
            f -= 360.0f;
        }
        return par1 + par3 * f;
    }

    public static void rotateCorpse(EntityLivingBase bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
        GlStateManager.rotate(180.0f - p_77043_3_, 0.0f, 1.0f, 0.0f);
        if (bat.deathTime > 0) {
            float f = (bat.deathTime + partialTicks - 1.0f) / 20.0f * 1.6f;
            f = MathHelper.sqrt_float(f);
            if (f > 1.0f) {
                f = 1.0f;
            }
            GlStateManager.rotate(f * 90.0f, 0.0f, 0.0f, 1.0f);
        } else {
            String s = EnumChatFormatting.getTextWithoutFormattingCodes(bat.getName());
            if (s != null && (s.equals("Dinnerbone") || s.equals("Grumm")) && (!(bat instanceof EntityPlayer) || ((EntityPlayer) bat).isWearing(EnumPlayerModelParts.CAPE))) {
                GlStateManager.translate(0.0f, bat.height + 0.1f, 0.0f);
                GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
            }
        }
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

    public static Rectangle renderBoxedText(String[] text, int x, int y, Double scale) {
        String longestString = Arrays.stream(text).max(Comparator.comparingInt(String::length)).get();
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.color(0, 0, 0, 1f);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        Gui.drawRect((int) (x / scale), (int) (y / scale), (int) ((x / scale) + fontRenderer.getStringWidth(longestString) + 10), (int) ((y / scale) + (text.length * 9) + 10), new Color(0, 0, 0, 125).getRGB());

        for (int i = 0; i < text.length; i++) {
            int yOffset = (int) (((y / scale) + 5 + (i * 9)) * scale);
            String s = text[i];

            fontRenderer.drawString(s, Math.round((x / scale) + 5 / scale), Math.round(yOffset / scale), Color.white.getRGB(), true);
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        return new Rectangle(x, y, (int) ((fontRenderer.getStringWidth(longestString) + 10) * scale), (int) ((text.length * 9 + 10) * scale));
    }

    public static void drawText(String str, double X, double Y, double Z) {
        drawText(str, X, Y, Z, false);
    }

    public static void drawText(String str, double X, double Y, double Z, boolean showDistance) {
        float lScale = 1.0f;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        double renderPosX = X - Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double renderPosY = Y - Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double renderPosZ = Z - Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        double distance = Math.sqrt(renderPosX * renderPosX + renderPosY * renderPosY + renderPosZ * renderPosZ);
        double multiplier = Math.max(distance / 150f, 0.1f);
        lScale *= 0.45f * multiplier;

        float xMultiplier = Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1 : 1;

        GlStateManager.pushMatrix();
        GlStateManager.translate(renderPosX, renderPosY, renderPosZ);
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        GlStateManager.rotate(-renderManager.playerViewY, 0, 1, 0);
        GlStateManager.rotate(renderManager.playerViewX * xMultiplier, 1, 0, 0);
        GlStateManager.scale(-lScale, -lScale, lScale);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int textWidth = fontRenderer.getStringWidth(StringUtils.stripControlCodes((str)));

        float j = textWidth / 2f;
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(0, 0, 0, 0.5f);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(-j - 1, -1, 0).endVertex();
        worldrenderer.pos(-j - 1, 8, 0).endVertex();
        worldrenderer.pos(j + 1, 8, 0).endVertex();
        worldrenderer.pos(j + 1, -1, 0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        fontRenderer.drawString(str, -textWidth / 2, 0, 553648127);
        GlStateManager.depthMask(true);
        fontRenderer.drawString(str, -textWidth / 2, 0, -1);

        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawOutline(BlockPos blockPos, Color color, float lineWidth) {
        if (blockPos != null) {
            IBlockState blockState = mc.theWorld.getBlockState(blockPos);

            if (blockState != null) {
                Block block = blockState.getBlock();
                block.setBlockBoundsBasedOnState(mc.theWorld, blockPos);
                double d0 = mc.getRenderManager().viewerPosX;
                double d1 = mc.getRenderManager().viewerPosY;
                double d2 = mc.getRenderManager().viewerPosZ;
                drawFilledBoundingBox(block.getSelectedBoundingBox(mc.theWorld, blockPos).expand(0.002D, 0.002D, 0.002D).offset(-d0, -d1, -d2), color, 0f, lineWidth);
            }
        }
    }

    public static void drawDoubleChestBlockBox(BlockPos blockPos1, BlockPos blockPos2, Color color, float lineWidth) {
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
                double d0 = mc.getRenderManager().viewerPosX;
                double d1 = mc.getRenderManager().viewerPosY;
                double d2 = mc.getRenderManager().viewerPosZ;

                // Offset the bounding box and draw it
                drawFilledBoundingBox(boundingBox.offset(-d0, -d1, -d2), color, 0.7f, lineWidth);
            }
        }
    }

    public static void drawBlockBox(BlockPos blockPos, Color color, float lineWidth) {
        if (blockPos != null) {
            IBlockState blockState = mc.theWorld.getBlockState(blockPos);

            if (blockState != null) {
                Block block = blockState.getBlock();
                block.setBlockBoundsBasedOnState(mc.theWorld, blockPos);
                double d0 = mc.getRenderManager().viewerPosX;
                double d1 = mc.getRenderManager().viewerPosY;
                double d2 = mc.getRenderManager().viewerPosZ;
                drawFilledBoundingBox(block.getSelectedBoundingBox(mc.theWorld, blockPos).expand(0.002D, 0.002D, 0.002D).offset(-d0, -d1, -d2), color, 0.7f, lineWidth);
            }
        }
    }

    public static void drawBlockBox(AxisAlignedBB bb, Color color, float lineWidth) {
        double d0 = mc.getRenderManager().viewerPosX;
        double d1 = mc.getRenderManager().viewerPosY;
        double d2 = mc.getRenderManager().viewerPosZ;
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

    public static class ModelData {
        public RendererLivingEntityAccessor renderer;
        public float rotationYaw;
        public float rotationPitch;
        public float limbSwing;
        public float limbSwingAmount;
        public float age;

        public ModelData(RendererLivingEntityAccessor renderer, float rotationYaw, float rotationPitch, float limbSwing, float limbSwingAmount, float age) {
            this.renderer = renderer;
            this.rotationYaw = rotationYaw;
            this.rotationPitch = rotationPitch;
            this.limbSwing = limbSwing;
            this.limbSwingAmount = limbSwingAmount;
            this.age = age;
        }
    }
}

