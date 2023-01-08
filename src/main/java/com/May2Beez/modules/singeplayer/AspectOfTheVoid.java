package com.May2Beez.modules.singeplayer;

import com.May2Beez.modules.Module;
import com.May2Beez.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

public class AspectOfTheVoid extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private static int delay = 10;
    private static BlockPos lookingAt;

    public AspectOfTheVoid() {
        super("Aspect of the Void");
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (!mc.isIntegratedServerRunning()) return;
        if (delay != 0) return;
        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
        if (!mc.thePlayer.isSneaking()) return;
        ItemStack itemStack = mc.thePlayer.getHeldItem();
        if (itemStack != null && itemStack.getItem() == Items.diamond_shovel && lookingAt != null) {
            mc.thePlayer.setPosition(
                    lookingAt.getX() + 0.5,
                    lookingAt.getY() + 1,
                    lookingAt.getZ() + 0.5
            );
            delay = 7;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!mc.isIntegratedServerRunning()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event.phase == TickEvent.Phase.START) return;
        if (delay > 0) {
            delay--;
        }
        lookingAt = null;
        ItemStack itemStack = mc.thePlayer.getHeldItem();
        if (itemStack != null && itemStack.getItem() == Items.diamond_shovel && mc.thePlayer.isSneaking()) {
            MovingObjectPosition ray = mc.thePlayer.rayTrace(64, 1);
            if (ray != null) {
                if (ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    BlockPos blockPos = ray.getBlockPos();
                    Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                    IBlockState blockState = mc.theWorld.getBlockState(blockPos);
                    if (block.isCollidable() &&
                            block != Blocks.carpet && block != Blocks.skull &&
                            block.getCollisionBoundingBox(mc.theWorld, blockPos, blockState) != null &&
                            block != Blocks.wall_sign && block != Blocks.standing_sign) {
                        BlockPos blockPosAbove = blockPos.add(0, 1, 0);
                        Block blockAbove = mc.theWorld.getBlockState(blockPosAbove).getBlock();

                        Block twoBlockAbove = mc.theWorld.getBlockState(blockPos.add(0, 2, 0)).getBlock();

                        if (blockAbove == Blocks.air && twoBlockAbove == Blocks.air) {
                            lookingAt = blockPos;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!mc.isIntegratedServerRunning()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        RenderUtils.drawBlockBox(lookingAt, Color.BLUE, 2);
    }
}
