package com.May2Beez.modules.player;

import com.May2Beez.Module;
import com.May2Beez.SkyblockMod;
import com.May2Beez.events.BlockChangeEvent;
import com.May2Beez.events.PlayerMoveEvent;
import com.May2Beez.events.ReceivePacketEvent;
import com.May2Beez.utils.RenderUtils;
import com.May2Beez.utils.RotationUtils;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.May2Beez.utils.SkyblockUtils.isBlockVisible;

public class PowderChest extends Module {

    private TreasureChest closestChest = null;
    private final ArrayList<TreasureChest> solvedChests = new ArrayList<>();
    private ArrayList<TreasureChest> allChests = new ArrayList<>();
    private final Minecraft mc = Minecraft.getMinecraft();

    private static class TreasureChest {
        public BlockPos pos;
        public int progress = 0;
        public long time = System.currentTimeMillis();
        public Vec3 particle = null;
        public AxisAlignedBB box;
        public TreasureChest(BlockPos pos) {
            this.pos = pos;
            this.box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        }

        public double distance(double x, double y, double z) {
            return pos.distanceSqToCenter(x, y, z);
        };
    }

    public PowderChest() {
        super("Powder Chest Macro", Keyboard.KEY_NONE);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (!isToggled() || mc.thePlayer == null) return;

        closestChest = getClosestChest();

        if (closestChest == null) return;

        if (!SkyblockMod.config.solvePowderChestServerRotation) {
            normalRotation();
        }
    }

    @SubscribeEvent
    public void onUpdatePre(PlayerMoveEvent.Pre event) {
        if (isToggled() && SkyblockMod.config.solvePowderChestServerRotation && closestChest != null && closestChest.particle != null) {
            RotationUtils.smoothLook(RotationUtils.vec3ToRotation(closestChest.particle), 0, () -> {});
        }
    }

    @SubscribeEvent
    public void onBlockChange(BlockChangeEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (((event.old.getBlock() == Blocks.air || event.old.getBlock() == Blocks.stone) && event.update.getBlock() == Blocks.chest)) {
            if (mc.thePlayer.getEntityBoundingBox().expand(8, 8, 8).isVecInside(new Vec3(event.pos))) {
                if (solvedChests.stream().noneMatch(solved -> new Vec3(solved.pos).equals(new Vec3(event.pos)))) {
                    allChests.add(new TreasureChest(event.pos));
                }
            }
        } else if (event.old.getBlock() == Blocks.chest && event.update.getBlock() == Blocks.air) {
            Optional<TreasureChest> chest = allChests.stream().filter(chestT -> chestT.pos == event.pos).findFirst();
            chest.ifPresent(allChests::remove);
        }
    }

    private void normalRotation() {
        if (closestChest.particle == null) return;

        RotationUtils.smoothLook(RotationUtils.vec3ToRotation(closestChest.particle), SkyblockMod.config.cameraSpeed, () -> {});
    }

    @SubscribeEvent
    public void onWorldLastRender(RenderWorldLastEvent event) {
        if (!isToggled()) return;
        allChests.forEach(chest -> {
            RenderUtils.drawBlockBox(chest.pos, Color.green, 5, event.partialTicks);
        });
        if (closestChest == null) return;
        RenderUtils.drawBlockBox(closestChest.pos, Color.ORANGE, 5, event.partialTicks);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        solvedChests.clear();
        closestChest = null;
    }

    @SubscribeEvent
    public void onPacketReceive(ReceivePacketEvent event) {
        if (!isToggled() || closestChest == null) return;

        if (event.packet instanceof S02PacketChat) {
            String message = ((S02PacketChat) event.packet).getChatComponent().getUnformattedText();
            if (message.toLowerCase().contains("successfully picked the lock") || message.toLowerCase().contains("remaining contents of this treasure chest")) {
                // Sometimes doesnt delete solved chests?? idk
                allChests = (ArrayList<TreasureChest>) allChests.stream().filter(chest -> {
                    IBlockState blockState = mc.theWorld.getBlockState(chest.pos);
                    return blockState.getBlock() == Blocks.chest;
                }).collect(Collectors.toList());
            }
        }

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
                if (sound.equals("random.orb")) {
                    closestChest.progress++;
                } else {
                    closestChest.progress = 0;
                }
                if (closestChest.progress >= 5) {
                    closestChest.particle = null;
                    // Please delete solved chest, holy moly
                    if (!solvedChests.contains(closestChest)) {
                        solvedChests.add(closestChest);
                    }
                    if (allChests.contains(closestChest)) {
                        allChests.remove(closestChest);
                    }
                    closestChest = null;
                }
            }
        }
    }

    private TreasureChest getClosestChest() {
        double smallest = 9999;
        TreasureChest closest = null;

        for (TreasureChest chest : allChests) {
            if (isBlockVisible(chest.pos)) {
                double dist = new Vec3(chest.pos).distanceTo(mc.thePlayer.getPositionVector());
                if (dist < smallest) {
                    smallest = dist;
                    closest = chest;
                }
            }
        }
        return closest;
    }
}
