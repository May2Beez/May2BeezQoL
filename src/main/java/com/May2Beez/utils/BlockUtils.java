package com.May2Beez.utils;

import com.May2Beez.SkyblockMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static int getUnitX() {
        double modYaw = (mc.thePlayer.rotationYaw % 360 + 360) % 360;
        if (modYaw < 45 || modYaw > 315) {
            return 0;
        } else if (modYaw < 135) {
            return -1;
        } else if (modYaw < 225) {
            return 0;
        } else {
            return 1;
        }
    }

    public static int getUnitZ() {
        double modYaw = (mc.thePlayer.rotationYaw % 360 + 360) % 360;
        if (modYaw < 45 || modYaw > 315) {
            return 1;
        } else if (modYaw < 135) {
            return 0;
        } else if (modYaw < 225) {
            return -1;
        } else {
            return 0;
        }
    }

    public static BlockPos getPlayerLoc() {
        return getRelativeBlockPos(0, 0);
    }

    public static BlockPos getRelativeBlockPos(float rightOffset, float frontOffset) {
        return getRelativeBlockPos(rightOffset, 0, frontOffset);
    }

    public static BlockPos getRelativeBlockPos(float rightOffset, float upOffset, float frontOffset) {
        int unitX = getUnitX();
        int unitZ = getUnitZ();
        return new BlockPos(
                mc.thePlayer.posX + (unitX * frontOffset) + (unitZ * -1 * rightOffset),
                mc.thePlayer.posY + upOffset,
                mc.thePlayer.posZ + (unitZ * frontOffset) + (unitX * rightOffset)
        );
    }

    public static boolean isBlockVisible(BlockPos pos) {
        return (getRandomVisibilityLine(pos) != null);
    }

    public static Vec3 getRandomVisibilityLine(BlockPos pos) {
        List<Vec3> lines = new ArrayList<>();
        int accuracyChecks = 10;
        for (int x = 0; x < accuracyChecks; x++) {
            for (int y = 0; y < accuracyChecks; y++) {
                for (int z = 0; z < accuracyChecks; z++) {
                    Vec3 target = new Vec3(pos.getX() + x / (float) accuracyChecks, pos.getY() + y / (float) accuracyChecks, pos.getZ() + z / (float) accuracyChecks);
                    BlockPos test = new BlockPos(target.xCoord, target.yCoord, target.zCoord);
                    MovingObjectPosition movingObjectPosition = mc.theWorld.rayTraceBlocks(mc.thePlayer.getPositionEyes(1.0F), target, true, false, true);
                    if (movingObjectPosition != null) {
                        BlockPos obj = movingObjectPosition.getBlockPos();
                        if (obj.equals(test) && mc.thePlayer.getDistance(target.xCoord, target.yCoord - mc.thePlayer.getEyeHeight(), target.zCoord) < 4.5D)
                            lines.add(target);
                    }
                }
            }
        }
        return lines.isEmpty() || lines.size() < 2 ? null : lines.get(1 + (new Random()).nextInt(lines.size() - 2));
    }
}
