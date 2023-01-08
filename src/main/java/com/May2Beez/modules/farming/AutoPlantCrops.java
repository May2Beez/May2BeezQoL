package com.May2Beez.modules.farming;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.modules.Module;
import com.May2Beez.utils.BlockUtils;
import com.May2Beez.utils.RenderUtils;
import com.May2Beez.utils.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;

import static com.May2Beez.utils.SkyblockUtils.*;

public class AutoPlantCrops extends Module {

    private BlockPos closestBlock;

    public AutoPlantCrops() {
        super("Auto Plant Crops", new KeyBinding("Auto Plant Crops", Keyboard.KEY_NONE, May2BeezQoL.MODID + " - Farming"));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        closestBlock = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), false);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isToggled()) return;

        if (closestBlock == null) {
            closestBlock = getClosestBlock();
        } else {
            RotationUtils.smoothLook(RotationUtils.getRotation(new Vec3(closestBlock.getX() + 0.5, closestBlock.getY() + 0.5, closestBlock.getZ() + 0.5)), May2BeezQoL.config.cameraSpeed);
            int indexOfCrop = -1;
            switch (May2BeezQoL.config.cropTypeIndex) {
                case 0: {
                    indexOfCrop= findItemInHotbar("Cocoa Beans");
                    break;
                }
                case 1: {
                    indexOfCrop = findItemInHotbar("Cactus");
                    break;
                }
            }
            if (indexOfCrop == -1) {
                toggle();
                return;
            }
            MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
            if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mop.getBlockPos().equals(closestBlock)) {
                Minecraft.getMinecraft().thePlayer.inventory.currentItem = indexOfCrop;
                rightClick();
                closestBlock = null;
            }
        }
    }


    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!isToggled()) return;

        if(closestBlock != null) {
            RenderUtils.drawBlockBox(closestBlock, new Color(255, 0, 0), May2BeezQoL.config.lineWidth);
        }
    }

    private BlockPos getClosestBlock() {
        if (Minecraft.getMinecraft().theWorld == null) return null;

        double r = May2BeezQoL.config.autoCropRange;
        BlockPos playerPos = Minecraft.getMinecraft().thePlayer.getPosition();
        Vec3i vec3i = new Vec3i(r, r, r);

        ArrayList<Vec3> blocks = new ArrayList<>();
        if (playerPos == null) return null;

        switch (May2BeezQoL.config.cropTypeIndex) {
            case 0: {
                for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
                    IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);

                    if (blockState.getBlock() == Blocks.log) {
                        if (NoCropAround(blockPos, Blocks.cocoa) && BlockUtils.isBlockVisible(blockPos)) {
                            blocks.add(new Vec3(blockPos));
                        }
                    }
                }
                break;
            }
            case 1: {
                for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
                    IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);

                    if (blockState.getBlock() == Blocks.sand) {
                        if (NoCropAround(blockPos, Blocks.cactus) && Objects.equals(Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ())).getBlock(), Blocks.air) && NoCropAround(new BlockPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ()), Blocks.cactus) && BlockUtils.isBlockVisible(blockPos)) {
                            blocks.add(new Vec3(blockPos));
                        }
                    }
                }
                break;
            }
        }
        Optional<Vec3> block = blocks.stream().findAny();
        return block.map(BlockPos::new).orElse(null);
    }

    private boolean NoCropAround(BlockPos blockPos, Block crop) {
        return Minecraft.getMinecraft().theWorld.getBlockState(blockPos.add(1, 0, 0)).getBlock() != crop &&
                Minecraft.getMinecraft().theWorld.getBlockState(blockPos.add(0, 1, 0)).getBlock() != crop &&
                Minecraft.getMinecraft().theWorld.getBlockState(blockPos.add(0, 0, 1)).getBlock() != crop &&
                Minecraft.getMinecraft().theWorld.getBlockState(blockPos.add(-1, 0, 0)).getBlock() != crop &&
                Minecraft.getMinecraft().theWorld.getBlockState(blockPos.add(0, -1, 0)).getBlock() != crop &&
                Minecraft.getMinecraft().theWorld.getBlockState(blockPos.add(0, 0, -1)).getBlock() != crop;
    }
}
