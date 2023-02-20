package com.May2Beez.utils;

import com.May2Beez.May2BeezQoL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

import java.util.ArrayList;
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
        BlockPos playerLoc = BlockUtils.getPlayerLoc();
        boolean lowerY = (pos.getY() < playerLoc.getY() && Math.abs(pos.getX() - playerLoc.getX()) <= 1 && Math.abs(pos.getZ() - playerLoc.getZ()) <= 1);
        ArrayList<Vec3> lines = getAllVisibilityLines(pos, mc.thePlayer.getPositionVector().add(new Vec3(0, mc.thePlayer.getEyeHeight(), 0)).subtract(new Vec3(0, lowerY ? May2BeezQoL.config.miningAccuracy : 0, 0)), lowerY);
        if (lines.isEmpty()) {
            return null;
        } else {
            return lines.get(new Random().nextInt(lines.size()));
        }
    }

    public static ArrayList<Vec3> getAllVisibilityLines(BlockPos pos, Vec3 from) {
        return getAllVisibilityLines(pos, from, false);
    }

    public static ArrayList<Vec3> getAllVisibilityLines(BlockPos pos, Vec3 from, boolean lowerY) {
        ArrayList<Vec3> lines = new ArrayList<>();
        int accuracyChecks = May2BeezQoL.config.miningAccuracyChecks;
        float accuracy = 1f / accuracyChecks;
        float spaceFromEdge = lowerY ? 0.1f : May2BeezQoL.config.miningAccuracy;
        for (float x = pos.getX() + spaceFromEdge; x <= pos.getX() + (1f - spaceFromEdge); x += accuracy) {
            for (float y = pos.getY() + spaceFromEdge; y <= pos.getY() + (1f - spaceFromEdge); y += accuracy) {
                for (float z = pos.getZ() + spaceFromEdge; z <= pos.getZ() + (1f - spaceFromEdge); z += accuracy) {
                    Vec3 target = new Vec3(x, y, z);
                    if (from.distanceTo(target) > May2BeezQoL.config.scanRadius) {
                        continue;
                    }
                    BlockPos test = new BlockPos(target.xCoord, target.yCoord, target.zCoord);
                    MovingObjectPosition movingObjectPosition = mc.theWorld.rayTraceBlocks(from, target, false, false, true);
                    if (movingObjectPosition != null) {
                        BlockPos obj = movingObjectPosition.getBlockPos();
                        if (obj.equals(test))
                            lines.add(target);
                    }
                }
            }
        }

        return lines;
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
