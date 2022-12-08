package com.May2Beez.utils;

import com.May2Beez.events.PlayerMoveEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RotationUtils {
    public static float pitchDifference;
    public static float yawDifference;
    private static int ticks = -1;
    private static int tickCounter = 0;

    private static float serverPitch;
    private static float serverYaw;

    public static boolean running = false;

    public static class Rotation {
        public float pitch;
        public float yaw;

        public Rotation(float pitch, float yaw) {
            this.pitch = pitch;
            this.yaw = yaw;
        }
    }

    private static double wrapAngleTo180(double angle) {
        return angle - Math.floor(angle / 360 + 0.5) * 360;
    }

    private static float wrapAngleTo180(float angle) {
        return (float) (angle - Math.floor(angle / 360 + 0.5) * 360);
    }

    public static Rotation getRotationToBlock(BlockPos block) {
        double diffX = block.getX() - Minecraft.getMinecraft().thePlayer.posX + 0.5;
        double diffY = block.getY() - Minecraft.getMinecraft().thePlayer.posY + 0.5 - Minecraft.getMinecraft().thePlayer.getEyeHeight();
        double diffZ = block.getZ() - Minecraft.getMinecraft().thePlayer.posZ + 0.5;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90)*-1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        return new Rotation(pitch, yaw);
    }

    public static Rotation getRotationToEntity(Entity entity) {
        double diffX = entity.posX - Minecraft.getMinecraft().thePlayer.posX;
        double diffY = entity.posY + entity.getEyeHeight() - Minecraft.getMinecraft().thePlayer.posY - Minecraft.getMinecraft().thePlayer.getEyeHeight();
        double diffZ = entity.posZ - Minecraft.getMinecraft().thePlayer.posZ;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90)*-1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        return new Rotation(pitch, yaw);
    }

    public static Rotation vec3ToRotation(Vec3 vec) {
        double diffX = vec.xCoord - Minecraft.getMinecraft().thePlayer.posX;
        double diffY = vec.yCoord - Minecraft.getMinecraft().thePlayer.posY - Minecraft.getMinecraft().thePlayer.getEyeHeight();
        double diffZ = vec.zCoord - Minecraft.getMinecraft().thePlayer.posZ;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90)*-1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        return new Rotation(pitch, yaw);
    }

    public static void smoothLook(Rotation rotation, float ticks) {
        if(ticks == 0) {
            look(rotation);
            return;
        }

        pitchDifference = wrapAngleTo180(rotation.pitch - Minecraft.getMinecraft().thePlayer.rotationPitch);
        yawDifference = wrapAngleTo180(rotation.yaw - Minecraft.getMinecraft().thePlayer.rotationYaw);

        running = true;

        if (Math.abs(pitchDifference) < 0.06 && Math.abs(yawDifference) < 0.06) {
            resetRotation();
            System.out.println("Stopping smooth look");
            return;
        }

        RotationUtils.ticks = (int) (ticks * 20);
        RotationUtils.tickCounter = 0;
    }

    public static boolean IsDiffLowerThan(float diff) {
        return IsDiffLowerThan(diff, diff);
    }

    public static boolean IsDiffLowerThan(float pitch, float yaw) {
        return Math.abs(pitchDifference) < pitch && Math.abs(yawDifference) < yaw;
    }

    public static void smartLook(Rotation rotation, int ticksPer180) {
        float rotationDifference = Math.max(
                Math.abs(rotation.pitch - Minecraft.getMinecraft().thePlayer.rotationPitch),
                Math.abs(rotation.yaw - Minecraft.getMinecraft().thePlayer.rotationYaw)
        );
        smoothLook(rotation, (int) (rotationDifference / 180 * ticksPer180));
    }

    public static void look(Rotation rotation) {
        Minecraft.getMinecraft().thePlayer.rotationPitch = rotation.pitch;
        Minecraft.getMinecraft().thePlayer.rotationYaw = rotation.yaw;
    }

    public static void resetRotation() {
        running = false;
        ticks = 0;
        tickCounter = 0;
        pitchDifference = 0;
        yawDifference = 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdatePre(PlayerMoveEvent.Pre pre) {
        serverPitch = Minecraft.getMinecraft().thePlayer.rotationPitch;
        serverYaw = Minecraft.getMinecraft().thePlayer.rotationYaw;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdatePost(PlayerMoveEvent.Post post) {
        Minecraft.getMinecraft().thePlayer.rotationPitch = serverPitch;
        Minecraft.getMinecraft().thePlayer.rotationYaw = serverYaw;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if(Minecraft.getMinecraft().thePlayer == null) return;
//        if (event.phase == TickEvent.Phase.END) return;

        if(tickCounter < ticks) {
            running = true;
            Minecraft.getMinecraft().thePlayer.rotationPitch += pitchDifference / ticks;
            Minecraft.getMinecraft().thePlayer.rotationYaw += yawDifference / ticks;
            tickCounter++;
        } else {
            running = false;
        }
    }
}