package com.May2Beez.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

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
        int accuracyChecks = 8;
        for (int x = 0; x < accuracyChecks; x++) {
            for (int y = 0; y < accuracyChecks; y++) {
                for (int z = 0; z < accuracyChecks; z++) {
                    Vec3 target = new Vec3(pos.getX() + x / (float) accuracyChecks, pos.getY() + y / (float) accuracyChecks, pos.getZ() + z / (float) accuracyChecks);
                    BlockPos test = new BlockPos(target.xCoord, target.yCoord, target.zCoord);
                    MovingObjectPosition movingObjectPosition = mc.theWorld.rayTraceBlocks(mc.thePlayer.getPositionEyes(1.0F), target, false, false, true);
                    if (movingObjectPosition != null) {
                        BlockPos obj = movingObjectPosition.getBlockPos();
                        if (obj.equals(test))
                            lines.add(target);
                    }
                }
            }
        }

        if (lines.size() > 2) {
            return lines.get(new Random().nextInt(lines.size() - 2) + 1);
        } else {
            return null;
        }
    }

    public static BlockPos getClosestBlock(int radius, int height, int depth, Predicate<? super BlockPos> predicate) {
        EntityPlayerSP player = mc.thePlayer;
        BlockPos playerPos = new BlockPos((int) Math.floor(player.posX), (int) Math.floor(player.posY) + 1, (int) Math.floor(player.posZ));
        Vec3i vec3Top = new Vec3i(radius, height, radius);
        Vec3i vec3Bottom = new Vec3i(radius, depth, radius);
        BlockPos closest = null;

        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.subtract(vec3Bottom), playerPos.add(vec3Top))) {
            if (predicate.test(blockPos)) {
                if (closest == null || player.getDistanceSq(blockPos) < player.getDistanceSq(closest)) {
                    closest = blockPos;
                }
            }
        }

        return closest;
    }

    public static BlockPos getEasiestBlock(int radius, int height, int depth, Predicate<? super BlockPos> predicate) {
        EntityPlayerSP player = mc.thePlayer;
        BlockPos playerPos = new BlockPos((int) Math.floor(player.posX), (int) Math.floor(player.posY) + 1, (int) Math.floor(player.posZ));
        Vec3i vec3Top = new Vec3i(radius, height, radius);
        Vec3i vec3Bottom = new Vec3i(radius, depth, radius);
        BlockPos easiest = null;

        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.subtract(vec3Bottom), playerPos.add(vec3Top))) {
            if (predicate.test(blockPos) && canBlockBeSeen(blockPos, 8)) {
                if (easiest == null || RotationUtils.getNeededChange(RotationUtils.getRotation(blockPos)).getValue() < RotationUtils.getNeededChange(RotationUtils.getRotation(easiest)).getValue()) {
                    easiest = blockPos;
                }
            }
        }

        if (easiest != null) return easiest;

        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.subtract(vec3Bottom), playerPos.add(vec3Top))) {
            if (predicate.test(blockPos)) {
                if (easiest == null || RotationUtils.getNeededChange(RotationUtils.getRotation(blockPos)).getValue() < RotationUtils.getNeededChange(RotationUtils.getRotation(easiest)).getValue()) {
                    easiest = blockPos;
                }
            }
        }

        return easiest;
    }

    public static boolean canBlockBeSeen(BlockPos blockPos, double dist) {
        Vec3 vec = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.99, blockPos.getZ() + 0.5);
        MovingObjectPosition mop = mc.theWorld.rayTraceBlocks(mc.thePlayer.getPositionEyes(1.0f), vec, false, true, false);
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            return mop.getBlockPos().equals(blockPos) && vec.distanceTo(mc.thePlayer.getPositionEyes(1.0f)) < dist;
        }

        return false;
    }
}
