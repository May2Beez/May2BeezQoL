package com.May2Beez.modules.player;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.mixins.accessors.RendererLivingEntityAccessor;
import com.May2Beez.modules.Module;
import com.May2Beez.utils.LocationUtils;
import com.May2Beez.utils.RenderUtils;
import com.May2Beez.utils.SkyblockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ESP extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    public ESP() {
        super("ESP");
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.NORMAL)
    public void onRenderWorldLastCreeperESP(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!May2BeezQoL.config.showGhosts) return;

        Entity entity = event.entity;

        if (entity instanceof EntityCreeper && entity.onGround) {
            if (LocationUtils.currentIsland == LocationUtils.Island.DWARVEN_MINES) {
                event.entity.setInvisible(false);
                RenderUtils.drawEntityBox(event.entity, May2BeezQoL.config.espColor.toJavaColor(), 2, 1);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRenderMob(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!May2BeezQoL.config.mobEsp) return;

        Entity entity = event.entity;

        if (!entity.hasCustomName()) return;

        if (entity instanceof EntityArmorStand) {
            EntityArmorStand stand = (EntityArmorStand) entity;
            if (!StringUtils.stripControlCodes(entity.getCustomNameTag()).startsWith("[Lv")) return;

            if (stand.getCustomNameTag().contains("Scatha") || stand.getCustomNameTag().contains("Worm")) {
                RenderUtils.drawBlockBox(new AxisAlignedBB(stand.posX - 0.5D,
                        stand.posY - 0.5D,
                        stand.posZ - 0.5D,
                        stand.posX + 0.5D,
                        stand.posY + 0.5D,
                        stand.posZ + 0.5D), May2BeezQoL.config.espColor.toJavaColor(), 2);

                if (SkyblockUtils.entityIsNotVisible(stand) && May2BeezQoL.config.drawMobNames) {
                    RenderUtils.drawText(stand.getName(), stand.posX, stand.posY + stand.height + 1, stand.posZ);
                }

                return;
            }

            Entity target = SkyblockUtils.getEntityCuttingOtherEntity(stand, null);

            if (target == null) return;

            if (target instanceof EntityPlayerMP) {
                if (((EntityPlayerMP) target).ping == 1) return;
            }

            if (!(target instanceof EntityLivingBase)) return;
            EntityLivingBase living = (EntityLivingBase) target;
            if (living.getHealth() > 0) {

                ModelBase model = ((RendererLivingEntityAccessor) (mc.getRenderManager().getEntityRenderObject(target))).getMainModel();

                RenderUtils.drawEntityESP((EntityLivingBase) target, model, May2BeezQoL.config.espColor.toJavaColor(), 1.0f);
                if (SkyblockUtils.entityIsNotVisible(target) && May2BeezQoL.config.drawMobNames) {
                    RenderUtils.drawText(stand.getName(), target.posX, target.posY + target.height + 1, target.posZ);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRenderWorldLastPlayerESP(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if(!May2BeezQoL.config.playerEsp) return;

        EntityLivingBase entity = event.entity;

        if (entity instanceof EntityPlayer) {
            if (entity == mc.thePlayer) return;
            if (SkyblockUtils.isNPC(entity)) return;
            ModelBase model = ((RendererLivingEntityAccessor) (mc.getRenderManager().getEntityRenderObject(entity))).getMainModel();


            RenderUtils.drawEntityESP(entity, model, May2BeezQoL.config.playerEspColor.toJavaColor(), 1.0f);
            if (SkyblockUtils.entityIsNotVisible(entity)) {
                RenderUtils.drawText(entity.getName(), entity.posX, entity.posY + entity.height + 1, entity.posZ);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRenderWorldLastChestESP(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!May2BeezQoL.config.chestEsp) return;
        if (LocationUtils.currentIsland == LocationUtils.Island.PRIVATE_ISLAND) return;

        int range = May2BeezQoL.config.espRange;
        for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
            if (tileEntity instanceof TileEntityChest) {
                final TileEntityChest chest = (TileEntityChest) tileEntity;
                if (chest.getChestType() != 0) {
                    continue;
                }
                if (chest.getDistanceSq(mc.thePlayer.getPosition().getX(), mc.thePlayer.getPosition().getY(), mc.thePlayer.getPosition().getZ()) > range * range) {
                    continue;
                }
                RenderUtils.drawBlockBox(chest.getPos(), May2BeezQoL.config.chestEspColor.toJavaColor(), 2);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastGemstoneESP(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!May2BeezQoL.config.gemstoneEsp) return;
        if (LocationUtils.currentIsland != LocationUtils.Island.CRYSTAL_HOLLOWS) return;

        int range = May2BeezQoL.config.espRange;
        List<BlockPos> gemstones = StreamSupport.stream(BlockPos.getAllInBox(mc.thePlayer.getPosition().add(-range, -range, -range), mc.thePlayer.getPosition().add(range, range, range)).spliterator(), false)
                .filter(pos -> mc.theWorld.getBlockState(pos).getBlock() == Blocks.stained_glass || mc.theWorld.getBlockState(pos).getBlock() == Blocks.stained_glass_pane)
                .collect(Collectors.toList());

        int alpha = May2BeezQoL.config.gemstoneEspAlpha;

        for (BlockPos pos : gemstones) {

            int meta = mc.theWorld.getBlockState(pos).getBlock().getMetaFromState(mc.theWorld.getBlockState(pos));
            if (meta == EnumDyeColor.RED.getMetadata()) {
                RenderUtils.drawOutline(pos, new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), alpha), 3);
            }
            if (meta == EnumDyeColor.ORANGE.getMetadata()) {
                RenderUtils.drawOutline(pos, new Color(Color.orange.getRed(), Color.orange.getGreen(), Color.orange.getBlue(), alpha), 3);
            }
            if (meta == EnumDyeColor.YELLOW.getMetadata()) {
                RenderUtils.drawOutline(pos, new Color(Color.yellow.getRed(), Color.yellow.getGreen(), Color.yellow.getBlue(), alpha), 3);
            }
            if (meta == EnumDyeColor.PURPLE.getMetadata()) {
                RenderUtils.drawOutline(pos, new Color(Color.magenta.getRed(), Color.magenta.getGreen(), Color.magenta.getBlue(), alpha), 3);
            }
            if (meta == EnumDyeColor.LIME.getMetadata()) {
                RenderUtils.drawOutline(pos, new Color(Color.green.getRed(), Color.green.getGreen(), Color.green.getBlue(), alpha), 3);
            }
            if (meta == EnumDyeColor.LIGHT_BLUE.getMetadata()) {
                RenderUtils.drawOutline(pos, new Color(Color.blue.getRed(), Color.blue.getGreen(), Color.blue.getBlue(), alpha), 3);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRenderWorldLastGiftESP(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!May2BeezQoL.config.giftEsp) return;
        if (LocationUtils.currentIsland != LocationUtils.Island.JERRY_WORKSHOP) return;

        EntityLivingBase entity = event.entity;

        if (entity instanceof EntityArmorStand) {
            if (entity.getEquipmentInSlot(4) == null) return;
            if (entity.getEquipmentInSlot(4).getItem() != Items.skull) return;
            if (entity.getEquipmentInSlot(4).getTagCompound() == null) return;
            if (!entity.getEquipmentInSlot(4).getTagCompound().hasKey("SkullOwner")) return;
            if (!entity.getEquipmentInSlot(4).getTagCompound().getCompoundTag("SkullOwner").hasKey("Properties")) return;
            if (!entity.getEquipmentInSlot(4).getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").hasKey("textures")) return;
            if (!entity.getEquipmentInSlot(4).getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0).hasKey("Value")) return;
            if (entity.getEquipmentInSlot(4).getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0).getString("Value").contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTBmNTM5ODUxMGIxYTA1YWZjNWIyMDFlYWQ4YmZjNTgzZTU3ZDcyMDJmNTE5M2IwYjc2MWZjYmQwYWUyIn19fQ==") // white gift
            || entity.getEquipmentInSlot(4).getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0).getString("Value").contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQ5N2Y0ZjQ0ZTc5NmY3OWNhNDMw0TdmYWE3YjRmZTkxYzQ0NWM3NmU1YzI2YTVhZDc5NGY1ZTQ3OTgzNyJ9fX0=") // green gift
            || entity.getEquipmentInSlot(4).getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0).getString("Value").contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjczYTIxMTQxMzZiOGVlNDkyNmNhYTUxNzg1NDE0MD2M2YTJiNzZlNGYxNjY4Y2I4OWQ5OTcxNmM0MjEifX19") // red gift
            ) {
                if (SkyblockUtils.entityIsNotVisible(entity)) {
                    RenderUtils.drawGiftBox(event.entity, May2BeezQoL.config.giftEspColor.toJavaColor(), 2, 1);
                    RenderUtils.drawText("Gift", entity.posX, entity.posY + entity.height + 0.5, entity.posZ);
                }
            }
        }
    }
}

