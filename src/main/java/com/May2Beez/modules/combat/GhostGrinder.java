package com.May2Beez.modules.combat;

import com.May2Beez.modules.Module;
import com.May2Beez.May2BeezQoL;
import com.May2Beez.utils.RenderUtils;
import com.May2Beez.utils.RotationUtils;
import com.May2Beez.utils.SkyblockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;

import static com.May2Beez.utils.SkyblockUtils.leftClick;

public class GhostGrinder extends Module {
//    private int lastClick = 0;
//
//    private Entity creeper;
    public GhostGrinder() {
        super("Ghost Grinder", new KeyBinding("Ghost Grinder", Keyboard.KEY_COMMA, May2BeezQoL.MODID + " - Combat"));
    }
//
//    @Override
//    public void onEnable() {
//        super.onEnable();
//        creeper = null;
//        lastClick = 0;
//    }
//
//    @Override
//    public void onDisable() {
//        super.onDisable();
//        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
//    }
//
//    @SubscribeEvent
//    public void onTick(TickEvent.ClientTickEvent event) {
//        if (!isToggled()) return;
//        if (SkyblockUtils.hasOpenContainer()) return;
//
//        if (Minecraft.getMinecraft().theWorld.playerEntities.stream().anyMatch(playerEntity -> (!playerEntity.equals(Minecraft.getMinecraft().thePlayer) && playerEntity instanceof EntityOtherPlayerMP && playerEntity.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) < 10.0F && (!playerEntity.isInvisible() || playerEntity.posY - Minecraft.getMinecraft().thePlayer.posY <= 5.0D)))) {
//            BlockPos location = Minecraft.getMinecraft().thePlayer.getPosition();
//            Minecraft.getMinecraft().theWorld.playSound(location.getX(), location.getY(), location.getZ(), "may2beez:alarm", 1.0f, 1.0f, false);
//        }
//
//        creeper = getClosestCreeper();
//        if (creeper == null) {
//            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
//            return;
//        }
//        RotationUtils.smoothLook(RotationUtils.getRotation(new Vec3(creeper.getPositionVector().xCoord, creeper.getPositionVector().yCoord + 1.4, creeper.getPositionVector().zCoord)), May2BeezQoL.config.cameraSpeed);
//
//        double dist = creeper.getDistance(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ);
//        if (lastClick-- <= 0) {
//            if (dist < 5) {
//                leftClick();
//                lastClick = May2BeezQoL.config.clickDelay;
//            }
//        }
//        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), dist > 2);
//    }
//
//    @SubscribeEvent
//    public void onRenderWorld(RenderWorldLastEvent event) {
//        if (!isToggled()) return;
//        if (creeper == null) return;
//        RenderUtils.drawEntityBox(creeper, Color.orange, May2BeezQoL.config.lineWidth, event.partialTicks);
//    }
//
//    private static Entity getClosestCreeper() {
//        Entity eman = null;
//        double closest = 9999.0;
//        if (Minecraft.getMinecraft().theWorld == null) return null;
//        for (Entity entity1 : (Minecraft.getMinecraft().theWorld.loadedEntityList)) {
//            if (entity1 instanceof EntityCreeper && !(((EntityCreeper) entity1).getHealth() == 0)) {
//                double dist = entity1.getDistance(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ);
//                if (dist < closest) {
//                    if(May2BeezQoL.config.radius != 0 && dist < May2BeezQoL.config.radius) {
//                        closest = dist;
//                        eman = entity1;
//                    }
//                    if(May2BeezQoL.config.radius == 0) {
//                        closest = dist;
//                        eman = entity1;
//                    }
//                }
//            }
//        }
//        return eman;
//    }
}
