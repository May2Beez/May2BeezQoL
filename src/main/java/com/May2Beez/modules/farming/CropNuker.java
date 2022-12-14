package com.May2Beez.modules.farming;

import com.May2Beez.Module;
import com.May2Beez.SkyblockMod;
import com.May2Beez.utils.RenderUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

public class CropNuker extends Module {
    private static BlockPos crop = null;
    private static final ArrayList<BlockPos> broken = new ArrayList<>();

    public CropNuker() {
        super("Crop Nuker", new KeyBinding("Crop Nuker", Keyboard.KEY_Z, SkyblockMod.MODID + " - Farming"));
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if(SkyblockMod.config.farmingSpeedIndex == 0 && event.phase == TickEvent.Phase.END) return;
        if (!isToggled() || Minecraft.getMinecraft().thePlayer == null) {
            broken.clear();
            return;
        }
        if(broken.size() > 40) {
            broken.clear();
        }
        crop = closestCrop();
        if (crop != null) {
            Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, crop, EnumFacing.DOWN));
            swingItem();
            broken.add(crop);
        }

    }

    public void swingItem() {
        MovingObjectPosition movingObjectPosition = Minecraft.getMinecraft().objectMouseOver;
        if (movingObjectPosition != null && movingObjectPosition.entityHit == null) {
            Minecraft.getMinecraft().thePlayer.swingItem();
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!isToggled()) return;
        if(crop != null) {
            RenderUtils.drawBlockBox(crop, new Color(255, 0, 0), SkyblockMod.config.lineWidth);
        }
    }

    private BlockPos closestCrop() {
        if(Minecraft.getMinecraft().theWorld == null) return null;
        double r = SkyblockMod.config.farmingRadius;
        BlockPos playerPos = Minecraft.getMinecraft().thePlayer.getPosition();
        playerPos = playerPos.add(0, 1, 0);
        Vec3 playerVec = Minecraft.getMinecraft().thePlayer.getPositionVector();
        Vec3i vec3i = new Vec3i(r, r, r);
        if (SkyblockMod.config.farmShapeIndex == 1) {
            vec3i = new Vec3i(r, SkyblockMod.config.downUpRange, r);
        }
        Vec3i vec3iCane = new Vec3i(r, 0, r);
        ArrayList<Vec3> warts = new ArrayList<>();
        if (playerPos != null) {
            switch (SkyblockMod.config.farmNukeIndex) {
                case 0:
                    for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
                        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
                        if (blockState.getBlock() == Blocks.nether_wart || blockState.getBlock() == Blocks.potatoes || blockState.getBlock() == Blocks.wheat || blockState.getBlock() == Blocks.carrots || blockState.getBlock() == Blocks.pumpkin || blockState.getBlock() == Blocks.melon_block || blockState.getBlock() == Blocks.brown_mushroom || blockState.getBlock() == Blocks.red_mushroom || blockState.getBlock() == Blocks.cocoa) {
                            if (SkyblockMod.config.farmShapeIndex == 0) {
                                if (!broken.contains(blockPos)) {
                                    warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            } else if (SkyblockMod.config.farmShapeIndex == 1) {
                                EnumFacing dir = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
                                int x = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posX);
                                int z = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posZ);
                                switch (dir) {
                                    case NORTH:
                                        if (blockPos.getZ() < z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case SOUTH:
                                        if (blockPos.getZ() > z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case WEST:
                                        if (blockPos.getX() < x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case EAST:
                                        if (blockPos.getX() > x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    break;
                case 1:
                    for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3iCane), playerPos.subtract(vec3iCane))) {
                        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
                        if (blockState.getBlock() == Blocks.reeds || blockState.getBlock() == Blocks.cactus) {
                            if (SkyblockMod.config.farmShapeIndex == 0) {
                                if (!broken.contains(blockPos)) {
                                    warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            } else if (SkyblockMod.config.farmShapeIndex == 1) {
                                EnumFacing dir = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
                                int x = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posX);
                                int z = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posZ);
                                switch (dir) {
                                    case NORTH:
                                        if (blockPos.getZ() <= z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case SOUTH:
                                        if (blockPos.getZ() >= z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case WEST:
                                        if (blockPos.getX() <= x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case EAST:
                                        if (blockPos.getX() >= x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    break;
                case 2:
                    for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
                        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
                        if (blockState.getBlock() == Blocks.nether_wart) {
                            if (SkyblockMod.config.farmShapeIndex == 0) {
                                if (!broken.contains(blockPos)) {
                                    warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            } else if (SkyblockMod.config.farmShapeIndex == 1) {
                                EnumFacing dir = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
                                int x = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posX);
                                int z = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posZ);
                                switch (dir) {
                                    case NORTH:
                                        if (blockPos.getZ() < z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case SOUTH:
                                        if (blockPos.getZ() > z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case WEST:
                                        if (blockPos.getX() < x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case EAST:
                                        if (blockPos.getX() > x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    break;
                case 3:
                    for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
                        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
                        if (blockState.getBlock() == Blocks.wheat) {
                            if (SkyblockMod.config.farmShapeIndex == 0) {
                                if (!broken.contains(blockPos)) {
                                    warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            } else if (SkyblockMod.config.farmShapeIndex == 1) {
                                EnumFacing dir = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
                                int x = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posX);
                                int z = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posZ);
                                switch (dir) {
                                    case NORTH:
                                        if (blockPos.getZ() < z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case SOUTH:
                                        if (blockPos.getZ() > z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case WEST:
                                        if (blockPos.getX() < x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case EAST:
                                        if (blockPos.getX() > x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    break;
                case 4:
                    for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
                        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
                        if (blockState.getBlock() == Blocks.carrots) {
                            if (SkyblockMod.config.farmShapeIndex == 0) {
                                if (!broken.contains(blockPos)) {
                                    warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            } else if (SkyblockMod.config.farmShapeIndex == 1) {
                                EnumFacing dir = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
                                int x = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posX);
                                int z = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posZ);
                                switch (dir) {
                                    case NORTH:
                                        if (blockPos.getZ() < z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case SOUTH:
                                        if (blockPos.getZ() > z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case WEST:
                                        if (blockPos.getX() < x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case EAST:
                                        if (blockPos.getX() > x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    break;
                case 5:
                    for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
                        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
                        if (blockState.getBlock() == Blocks.potatoes) {
                            if (SkyblockMod.config.farmShapeIndex == 0) {
                                if (!broken.contains(blockPos)) {
                                    warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            } else if (SkyblockMod.config.farmShapeIndex == 1) {
                                EnumFacing dir = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
                                int x = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posX);
                                int z = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posZ);
                                switch (dir) {
                                    case NORTH:
                                        if (blockPos.getZ() < z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case SOUTH:
                                        if (blockPos.getZ() > z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case WEST:
                                        if (blockPos.getX() < x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case EAST:
                                        if (blockPos.getX() > x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    break;
                case 6:
                    for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
                        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
                        if (blockState.getBlock() == Blocks.pumpkin) {
                            if (SkyblockMod.config.farmShapeIndex == 0) {
                                if (!broken.contains(blockPos)) {
                                    warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            } else if (SkyblockMod.config.farmShapeIndex == 1) {
                                EnumFacing dir = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
                                int x = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posX);
                                int z = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posZ);
                                switch (dir) {
                                    case NORTH:
                                        if (blockPos.getZ() < z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case SOUTH:
                                        if (blockPos.getZ() > z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case WEST:
                                        if (blockPos.getX() < x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case EAST:
                                        if (blockPos.getX() > x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    break;
                case 7:
                    for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
                        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
                        if (blockState.getBlock() == Blocks.melon_block) {
                            if (SkyblockMod.config.farmShapeIndex == 0) {
                                if (!broken.contains(blockPos)) {
                                    warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            } else if (SkyblockMod.config.farmShapeIndex == 1) {
                                EnumFacing dir = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
                                int x = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posX);
                                int z = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posZ);
                                switch (dir) {
                                    case NORTH:
                                        if (blockPos.getZ() < z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case SOUTH:
                                        if (blockPos.getZ() > z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case WEST:
                                        if (blockPos.getX() < x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case EAST:
                                        if (blockPos.getX() > x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    break;
                case 8:
                    for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
                        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
                        if (blockState.getBlock() == Blocks.cocoa) {
                            if (SkyblockMod.config.farmShapeIndex == 0) {
                                if (!broken.contains(blockPos)) {
                                    warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            } else if (SkyblockMod.config.farmShapeIndex == 1) {
                                EnumFacing dir = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
                                int x = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posX);
                                int z = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posZ);
                                switch (dir) {
                                    case NORTH:
                                        if (blockPos.getZ() < z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case SOUTH:
                                        if (blockPos.getZ() > z && blockPos.getX() == x) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case WEST:
                                        if (blockPos.getX() < x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                    case EAST:
                                        if (blockPos.getX() > x && blockPos.getZ() == z) {
                                            if (!broken.contains(blockPos)) {
                                                warts.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    break;
            }
        }
        double smallest = 9999;
        Vec3 closest = null;
        for (Vec3 wart : warts) {
            double dist = wart.distanceTo(playerVec);
            if (dist < smallest) {
                smallest = dist;
                closest = wart;
            }
        }
        if (closest != null && smallest < 5) {
            return new BlockPos(closest.xCoord, closest.yCoord, closest.zCoord);
        }
        return null;
    }
}
