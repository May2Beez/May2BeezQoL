package com.May2Beez.modules.player;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.Module;
import com.May2Beez.events.BlockChangeEvent;
import com.May2Beez.events.ReceivePacketEvent;
import com.May2Beez.utils.RenderUtils;
import com.May2Beez.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PowderChest extends Module {

    public static TreasureChest closestChest = null;
    private final CopyOnWriteArrayList<TreasureChest> allChests = new CopyOnWriteArrayList<>();
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static class TreasureChest {
        public BlockPos pos;
        public long time;
        public Vec3 particle = null;
        public AxisAlignedBB box;
        public TreasureChest(BlockPos pos) {
            this.pos = pos;
            this.box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            this.time = System.currentTimeMillis();
        }

        public double distance(double x, double y, double z) { return pos.distanceSqToCenter(x, y, z);}
        public boolean isExpired() {
            return (System.currentTimeMillis() - time) >= 60000;
        }
        public boolean isSolved = false;

        public boolean isOpen() {
            TileEntityChest chest = (TileEntityChest) mc.theWorld.getTileEntity(pos);
            if (chest == null) return false;
            int state = chest.numPlayersUsing;
            return state > 0;
        }
    }

    public PowderChest() {
        super("Powder Chest Macro", new KeyBinding("Powder Chest Macro", Keyboard.KEY_NONE, May2BeezQoL.MODID + " - Player"));
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (!isToggled() || mc.thePlayer == null) return;

        closestChest = getClosestChest();

        if (closestChest == null) return;

        if (!May2BeezQoL.config.solvePowderChestServerRotation) {
            normalRotation();
        } else {
            serverRotation();
        }
    }


    @SubscribeEvent
    public void onBlockChange(BlockChangeEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (((event.update.getBlock() == Blocks.chest || event.update.getBlock() == Blocks.trapped_chest))) {
            if (mc.thePlayer.getEntityBoundingBox().expand(8, 8, 8).isVecInside(new Vec3(event.pos))) {
                allChests.add(new TreasureChest(event.pos));
            }
        }

        if (event.update.getBlock() == Blocks.air) {
            allChests.removeIf(chest -> chest.pos.equals(event.pos));
        }
    }

    private void normalRotation() {
        if (closestChest.particle == null) return;

        RotationUtils.smoothLook(RotationUtils.getRotation(closestChest.particle), May2BeezQoL.config.cameraSpeed);
    }

    private void serverRotation() {
        if (closestChest.particle == null) return;

        RotationUtils.serverSmoothLook(RotationUtils.getRotation(closestChest.particle), May2BeezQoL.config.powderChestServerRotationSpeed);
    }

    @SubscribeEvent
    public void onWorldLastRender(RenderWorldLastEvent event) {
        if (!isToggled() || mc.thePlayer == null) return;
        if (allChests.size() > 0) {

            if (!May2BeezQoL.config.chestEsp) {

                for (TreasureChest allChest : allChests) {

                    if (allChest.isSolved || allChest.isExpired() || allChest.isOpen()) continue;

                    RenderUtils.drawBlockBox(allChest.pos, May2BeezQoL.config.chestEspColor, 3);
                }
            }
        }
        if (closestChest != null) {
            RenderUtils.drawBlockBox(closestChest.pos, new Color(Color.orange.getRed(), Color.orange.getGreen(), Color.orange.getBlue(), 150), 5);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        allChests.clear();
        closestChest = null;
    }

    @SubscribeEvent
    public void onPacketReceive(ReceivePacketEvent event) {
        if (!isToggled() || closestChest == null) return;

        if (event.packet instanceof S2APacketParticles) {
            if (((S2APacketParticles) event.packet).getParticleType() == EnumParticleTypes.CRIT && ((S2APacketParticles) event.packet).isLongDistance() && ((S2APacketParticles) event.packet).getParticleCount() == 1 && ((S2APacketParticles) event.packet).getParticleSpeed() == 0.0f && ((S2APacketParticles) event.packet).getXOffset() == 0 && ((S2APacketParticles) event.packet).getYOffset() == 0 && ((S2APacketParticles) event.packet).getZOffset() == 0) {
                double x = ((S2APacketParticles) event.packet).getXCoordinate();
                double y = ((S2APacketParticles) event.packet).getYCoordinate();
                double z = ((S2APacketParticles) event.packet).getZCoordinate();
                Optional<TreasureChest> optionalTreasureChest = allChests.stream().filter(chest -> chest.distance(x, y, z) < 2.5f).min(Comparator.comparingDouble(chest -> chest.distance(x, y, z)));
                optionalTreasureChest.ifPresent(treasureChest -> treasureChest.particle = new Vec3(x, y, z));
            }
        }

        if (event.packet instanceof S29PacketSoundEffect) {
            String sound = ((S29PacketSoundEffect) event.packet).getSoundName();
            float pitch = ((S29PacketSoundEffect) event.packet).getPitch();
            float volume = ((S29PacketSoundEffect) event.packet).getVolume();
            if (volume == 1f && pitch == 1f && (sound.equals("random.orb") || sound.equals("mob.villager.no"))) {
                if (closestChest.isOpen()) {
                    closestChest.particle = null;
                    closestChest.isSolved = true;
                    closestChest = null;
                }
            }
        }
    }

    private TreasureChest getClosestChest() {
        ArrayList<TreasureChest> notSolved = (ArrayList<TreasureChest>) allChests.stream().filter(chest -> !chest.isSolved && !chest.isOpen()).collect(Collectors.toList());
        ArrayList<TreasureChest> notSolvedAndNotExpired = (ArrayList<TreasureChest>) notSolved.stream().filter(chest -> !chest.isExpired()).collect(Collectors.toList());
        if (notSolvedAndNotExpired.size() == 0) return null;
        TreasureChest closest = notSolvedAndNotExpired.get(0);

        if (mc.thePlayer.getPositionEyes(1).distanceTo(new Vec3(closest.pos).add(new Vec3(0.5, 0.5, 0.5))) > 6) return null;

        if (notSolvedAndNotExpired.size() == 1) return closest;

        for (TreasureChest chest : notSolvedAndNotExpired) {
            if (mc.thePlayer.getPositionEyes(1).distanceTo(new Vec3(chest.pos).add(new Vec3(0.5, 0.5, 0.5))) < mc.thePlayer.getPositionEyes(1).distanceTo(new Vec3(closest.pos).add(new Vec3(0.5, 0.5, 0.5)))) {
                closest = chest;
            }
        }
        return closest;
    }
}
