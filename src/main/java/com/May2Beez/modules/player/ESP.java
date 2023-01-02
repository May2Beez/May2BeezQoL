package com.May2Beez.modules.player;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.Module;
import com.May2Beez.utils.LocationUtils;
import com.May2Beez.utils.RenderUtils;
import com.May2Beez.utils.SkyblockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
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

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.LOWEST)
    public void onRenderWorldLastCreeperESP(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!May2BeezQoL.config.showGhosts) return;

        Entity entity = event.entity;

        if (entity instanceof EntityCreeper && entity.onGround) {
            if (LocationUtils.currentIsland == LocationUtils.Island.DWARVEN_MINES) {
                event.entity.setInvisible(false);
                RenderUtils.drawEntityBox(event.entity, May2BeezQoL.config.espColor, 2);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastMobESP(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!May2BeezQoL.config.mobEsp) return;

        List<Entity> entities = mc.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityArmorStand).collect(Collectors.toList());

        for (Entity entity : entities) {
            if (!entity.hasCustomName()) continue;

            if (entity instanceof EntityArmorStand) {
                EntityArmorStand stand = (EntityArmorStand) entity;

                if (stand.getCustomNameTag().contains("Scatha") || stand.getCustomNameTag().contains("Worm")) {
                    RenderUtils.drawBlockBox(new AxisAlignedBB(stand.posX - 0.5D,
                            stand.posY - 0.5D,
                            stand.posZ - 0.5D,
                            stand.posX + 0.5D,
                            stand.posY + 0.5D,
                            stand.posZ + 0.5D), May2BeezQoL.config.espColor, 2);

                    if (SkyblockUtils.entityIsNotVisible(stand) && May2BeezQoL.config.drawMobNames) {
                        RenderUtils.drawText(stand.getName(), stand.posX, stand.posY + stand.height + 1, stand.posZ);
                    }

                    continue;
                }

                Entity target = SkyblockUtils.getEntityCuttingOtherEntity(stand, null);

                if (target == null) continue;

                if (target instanceof EntityPlayerMP) {
                    if (((EntityPlayerMP) target).ping == 1) continue;
                }

                if (SkyblockUtils.isNPC(target)) continue;

                if (stand.getCustomNameTag().contains("§c") || stand.getCustomNameTag().contains("❤️") || SkyblockUtils.getMobHp(stand) > 0) {
                    RenderUtils.drawEntityBox(target, May2BeezQoL.config.espColor, 2);

                    if (SkyblockUtils.entityIsNotVisible(target) && May2BeezQoL.config.drawMobNames) {
                        RenderUtils.drawText(stand.getName(), target.posX, target.posY + target.height + 1, target.posZ);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastChestESP(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (!May2BeezQoL.config.chestEsp) return;
        if (LocationUtils.currentIsland == LocationUtils.Island.PRIVATE_ISLAND) return;

        int range = May2BeezQoL.config.espRange;
        List<BlockPos> chests = StreamSupport.stream(BlockPos.getAllInBox(mc.thePlayer.getPosition().add(-range, -range, -range), mc.thePlayer.getPosition().add(range, range, range)).spliterator(), false)
                .filter(pos -> mc.theWorld.getBlockState(pos).getBlock() == Blocks.chest || mc.theWorld.getBlockState(pos).getBlock() == Blocks.trapped_chest)
                .filter(pos -> {
                    TileEntityChest chest = (TileEntityChest) mc.theWorld.getTileEntity(pos);
                    if (chest == null) return false;
                    int state = chest.numPlayersUsing;
                    return state == 0;
                })
                .filter(pos -> {
                    if (PowderChest.closestChest == null) return true;
                    if (PowderChest.closestChest.pos.equals(pos)) {
                        return PowderChest.closestChest.distance(mc.thePlayer.getPositionEyes(1).xCoord, mc.thePlayer.getPositionEyes(1).yCoord, mc.thePlayer.getPositionEyes(1).zCoord) > 3.5f;
                    }
                    return false;
                })
                .collect(Collectors.toList());

        for (BlockPos pos : chests) {

            // check if chest is a double chest
            if (mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.chest || mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.trapped_chest) {
                RenderUtils.drawDoubleChestBlockBox(pos, pos.add(1, 0, 0), May2BeezQoL.config.chestEspColor, 2);
                continue;
            }

            if (mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.chest || mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.trapped_chest) {
                RenderUtils.drawDoubleChestBlockBox(pos.add(-1, 0, 0), pos, May2BeezQoL.config.chestEspColor, 2);
                continue;
            }

            if (mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.chest || mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.trapped_chest) {
                RenderUtils.drawDoubleChestBlockBox(pos, pos.add(0, 0, 1), May2BeezQoL.config.chestEspColor, 2);
                continue;
            }

            if (mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.chest || mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.trapped_chest) {
                RenderUtils.drawDoubleChestBlockBox(pos.add(0, 0, -1), pos, May2BeezQoL.config.chestEspColor, 2);
                continue;
            }

            RenderUtils.drawBlockBox(pos, May2BeezQoL.config.chestEspColor, 2);
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
            if (meta == EnumDyeColor.BLUE.getMetadata()) {
                RenderUtils.drawOutline(pos, new Color(Color.blue.getRed(), Color.blue.getGreen(), Color.blue.getBlue(), alpha), 3);
            }
        }
    }
}

