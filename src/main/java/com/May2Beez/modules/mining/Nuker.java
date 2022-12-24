package com.May2Beez.modules.mining;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.Module;
import com.May2Beez.events.MillisecondEvent;
import com.May2Beez.events.PlayerMoveEvent;
import com.May2Beez.events.SecondEvent;
import com.May2Beez.modules.player.PowderChest;
import com.May2Beez.utils.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

// RGA Nuker ported
public class Nuker extends Module {
    public Nuker() {
        super("Nuker", new KeyBinding("Nuker", Keyboard.KEY_NONE, May2BeezQoL.MODID + " - Mining"));
    }

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final ArrayList<BlockPos> broken = new ArrayList<>();
    public static BlockPos blockPos;
    private long lastBroken = 0;
    private BlockPos current;
    private final ArrayList<BlockPos> blocksInRange = new ArrayList<>();

    @SubscribeEvent
    public void onSecond(SecondEvent event) {
        if (!isEnabled()) return;

        if (broken.size() > 0)
            broken.clear();
    }

    public boolean isEnabled() {
        return (isToggled() && (May2BeezQoL.config.powderChestPauseNukerMode != 1 || (PowderChest.closestChest == null || PowderChest.closestChest.particle == null)) && mc.thePlayer != null && mc.theWorld != null);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (!isEnabled()) {
            if (current != null && mc.thePlayer != null) {
                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        blockPos,
                        EnumFacing.DOWN)
                );
            }
            current = null;
            return;
        }
        blocksInRange.clear();
        EntityPlayerSP player =  mc.thePlayer;
        BlockPos playerPos = new BlockPos((int) Math.floor(player.posX), (int) Math.floor(player.posY) + 1, (int) Math.floor(player.posZ));
        Vec3i vec3Top = new Vec3i(4, May2BeezQoL.config.nukerHeight, 4);
        Vec3i vec3Bottom = new Vec3i(4, May2BeezQoL.config.nukerDepth, 4);

        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.subtract(vec3Bottom), playerPos.add(vec3Top))) {
            Vec3 target = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            if (Math.abs(RotationUtils.wrapAngleTo180(RotationUtils.fovToVec3(target) - RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw))) < (float) May2BeezQoL.config.nukerFieldOfView / 2) blocksInRange.add(blockPos);
        }
        if (current != null) SkyblockUtils.swingHand(null);
    }

    @SubscribeEvent
    public void onMillisecond(MillisecondEvent event) {
        if (!isEnabled()) {
            current = null;
            if (broken.size() > 0) broken.clear();
            return;
        }
        if (event.timestamp - lastBroken > 1000f / May2BeezQoL.config.nukerSpeed) {
            lastBroken = event.timestamp;
            if (May2BeezQoL.config.nukerShape == 1) {
                if (broken.size() > 6) broken.clear();
            } else {
                if (broken.size() > May2BeezQoL.config.nukerPinglessCutoff) broken.clear();
            }

            if (May2BeezQoL.config.mineBlocksInFront) {
                blockPos = blockInFront();

                if (blockPos != null) {
                    if (current != null && current.compareTo(blockPos) != 0) {
                        current = null;
                    }
                    if (isSlow(getBlockState(blockPos))) {
                        if (current == null) {
                            if (May2BeezQoL.config.smoothServerSideRotations && (May2BeezQoL.config.powderChestPauseNukerMode != 2 || (PowderChest.closestChest.particle == null))) {
                                RotationUtils.serverSmoothLook(RotationUtils.getRotation(blockPos), 1000L / May2BeezQoL.config.nukerSpeed);
                            }
                            mineBlock(blockPos);
                        }
                    } else {
                        if (May2BeezQoL.config.smoothServerSideRotations && (May2BeezQoL.config.powderChestPauseNukerMode != 2 || PowderChest.closestChest.particle == null)) {
                            RotationUtils.serverSmoothLook(RotationUtils.getRotation(blockPos), 1000L / May2BeezQoL.config.nukerSpeed);
                        }
                        pinglessMineBlock(blockPos);
                        current = null;
                    }
                    return;
                }
            }

            switch (May2BeezQoL.config.nukerAlgorithm) {
                case 0:
                    blockPos = BlockUtils.getClosestBlock(4, May2BeezQoL.config.nukerHeight, May2BeezQoL.config.nukerDepth, this::canMine);
                    break;
                case 1:
                    blockPos = BlockUtils.getEasiestBlock(4, May2BeezQoL.config.nukerHeight, May2BeezQoL.config.nukerDepth, this::canMine);
                    break;
            }


            if (blockPos != null) {
                if (current != null && current.compareTo(blockPos) != 0) {
                    current = null;
                }
                if (isSlow(getBlockState(blockPos))) {
                    if (current == null) {
                        if (May2BeezQoL.config.smoothServerSideRotations && (May2BeezQoL.config.powderChestPauseNukerMode != 2 || PowderChest.closestChest.particle == null)) {
                            RotationUtils.serverSmoothLook(RotationUtils.getRotation(blockPos), 1000L / May2BeezQoL.config.nukerSpeed);
                        }
                        mineBlock(blockPos);
                    }
                } else {
                    if (May2BeezQoL.config.smoothServerSideRotations && (May2BeezQoL.config.powderChestPauseNukerMode != 2 || PowderChest.closestChest.particle == null)) {
                        RotationUtils.serverSmoothLook(RotationUtils.getRotation(blockPos), 1000L / May2BeezQoL.config.nukerSpeed);
                    }
                    pinglessMineBlock(blockPos);
                    current = null;
                }
            } else {
                current = null;
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!isEnabled()) return;
        if (blockPos == null) return;

        RenderUtils.drawBlockBox(blockPos, new Color(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue(), 100), 2f);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onUpdatePre(PlayerMoveEvent.Pre pre) {
        if (!isEnabled()) return;
        if (!May2BeezQoL.config.serverSideNukerRotations) return;
        if (May2BeezQoL.config.powderChestPauseNukerMode == 2 && PowderChest.closestChest.particle != null) return;
        if (blockPos != null) {
            if (May2BeezQoL.config.smoothServerSideRotations) {
                if (RotationUtils.done) {
                    RotationUtils.look(RotationUtils.getRotation(blockPos));
                }
            } else {
                RotationUtils.look(RotationUtils.getRotation(blockPos));
            }
        }
        if (current != null) {
            if (May2BeezQoL.config.smoothServerSideRotations) {
                if (RotationUtils.done) {
                    RotationUtils.look(RotationUtils.getRotation(current));
                }
            } else {
                RotationUtils.look(RotationUtils.getRotation(current));
            }
        }
    }

    private void mineBlock(BlockPos blockPos) {
        breakBlock(blockPos);
        current = blockPos;
    }

    private void pinglessMineBlock(BlockPos blockPos) {
        SkyblockUtils.swingHand(null);
        breakBlock(blockPos);
        broken.add(blockPos);
    }

    private void breakBlock(BlockPos blockPos) {
        MovingObjectPosition objectMouseOver = mc.objectMouseOver;
        objectMouseOver.hitVec = new Vec3(blockPos);
        if (objectMouseOver.sideHit != null) {
            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                    blockPos,
                    objectMouseOver.sideHit)
            );
        }
    }

    private BlockPos blockInFront() {
        EntityPlayerSP player = mc.thePlayer;
        BlockPos playerPos = new BlockPos((int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ));
        Vec3i axisVector = player.getHorizontalFacing().getDirectionVec();

        if (getBlockState(playerPos).getBlock() != Blocks.air && getBlockState(playerPos).getBlock() != Blocks.bedrock
                && !broken.contains(playerPos)) {
            return playerPos;
        }
        if (getBlockState(playerPos.add(new Vec3i(0, 1, 0))).getBlock() != Blocks.air &&
                getBlockState(playerPos).getBlock() != Blocks.bedrock && !broken.contains(playerPos.add(new Vec3i(0, 1, 0)))) {
            return playerPos.add(new Vec3i(0, 1, 0));
        }
        if (getBlockState(playerPos.add(axisVector)).getBlock() != Blocks.air && getBlockState(playerPos).getBlock() != Blocks.bedrock
                && !broken.contains(playerPos.add(axisVector))) {
            return playerPos.add(axisVector);
        }
        if (getBlockState(playerPos.add(axisVector).add(new Vec3i(0, 1, 0))).getBlock() != Blocks.air &&
                getBlockState(playerPos).getBlock() != Blocks.bedrock &&
                !broken.contains(playerPos.add(axisVector).add(new Vec3i(0, 1, 0)))) {
            return playerPos.add(axisVector).add(new Vec3i(0, 1, 0));
        }
        return null;
    }

    private boolean canMine(BlockPos blockPos) {
        if (canMineBlockType(blockPos) && !broken.contains(blockPos) && blocksInRange.contains(blockPos)) {
            EntityPlayerSP player = mc.thePlayer;
            EnumFacing axis = player.getHorizontalFacing();
            Vec3i ray = new Vec3i((int) Math.floor(player.posX), 0, (int) Math.floor(player.posZ));

            switch (May2BeezQoL.config.nukerShape) {
                case 1:
                    for (int i = 0; i < 5; i++) {
                        ray = VectorUtils.addVector(ray, axis.getDirectionVec());
                        if (ray.getX() == blockPos.getX() && ray.getZ() == blockPos.getZ()) {
                            return true;
                        }
                    }

                    return false;
                case 2:
                    for (int i = 0; i < 5; i++) {
                        ray = VectorUtils.addVector(ray, axis.getDirectionVec());
                        if (ray.getX() == blockPos.getX() && ray.getZ() == blockPos.getZ()) {
                            return true;
                        }
                        if (axis.getAxis() == EnumFacing.Axis.Z) {
                            if (ray.getX() + 2 == blockPos.getX() && ray.getZ() == blockPos.getZ()) {
                                return true;
                            }
                            if (ray.getX() - 2 == blockPos.getX() && ray.getZ() == blockPos.getZ()) {
                                return true;
                            }
                        } else if (axis.getAxis() == EnumFacing.Axis.X) {
                            if (ray.getX() == blockPos.getX() && ray.getZ() + 2 == blockPos.getZ()) {
                                return true;
                            }
                            if (ray.getX() == blockPos.getX() && ray.getZ() - 2 == blockPos.getZ()) {
                                return true;
                            }
                        }
                    }

                    return false;
            }

            return true;
        }

        return false;
    }

    private boolean canMineBlockType(BlockPos bp) {
        IBlockState blockState = getBlockState(bp);
        Block block = blockState.getBlock();
        if (LocationUtils.currentIsland == LocationUtils.Island.CRYSTAL_HOLLOWS &&
                May2BeezQoL.config.filterHardstone &&
                (block == Blocks.stone || block == Blocks.stained_hardened_clay)) return true;

        if (LocationUtils.currentIsland == LocationUtils.Island.CRYSTAL_HOLLOWS &&
                May2BeezQoL.config.filterGemstones &&
                (block == Blocks.stained_glass_pane ||
                        block == Blocks.stained_glass)) return true;

        if (May2BeezQoL.config.filterMithril &&
                (
                        (LocationUtils.currentIsland == LocationUtils.Island.CRYSTAL_HOLLOWS &&
                                (block == Blocks.prismarine ||
                                        (block == Blocks.wool && blockState.getValue(BlockColored.COLOR) == EnumDyeColor.LIGHT_BLUE))
                        ) || (LocationUtils.currentIsland == LocationUtils.Island.DWARVEN_MINES &&
                                (block == Blocks.prismarine ||
                                        block == Blocks.wool ||
                                        block == Blocks.stained_hardened_clay ||
                                        (block == Blocks.stone && blockState.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH))
                        )
                )
        ) return true;

        if (LocationUtils.currentIsland == LocationUtils.Island.CRIMSON_ISLE &&
                May2BeezQoL.config.filterExcavatable &&
                (block == Blocks.sand ||
                        block == Blocks.mycelium)) return true;

        if (May2BeezQoL.config.filterGold &&
                block == Blocks.gold_block) return true;

        if (May2BeezQoL.config.filterStone &&
                (block == Blocks.stone ||
                        block == Blocks.cobblestone)) return true;

        if (May2BeezQoL.config.filterOres &&
                (block == Blocks.coal_ore ||
                        block == Blocks.lapis_ore ||
                        block == Blocks.iron_ore ||
                        block == Blocks.gold_ore ||
                        block == Blocks.redstone_ore ||
                        block == Blocks.lit_redstone_ore ||
                        block == Blocks.diamond_ore ||
                        block == Blocks.emerald_ore ||
                        block == Blocks.quartz_ore)) return true;

        if (May2BeezQoL.config.filterSand &&
                May2BeezQoL.config.filterWood &&
                block == Blocks.chest) return true;

        if (May2BeezQoL.config.filterCrops &&
                (block == Blocks.carrots ||
                        block == Blocks.potatoes ||
                        block == Blocks.reeds ||
                        block == Blocks.cocoa ||
                        block == Blocks.melon_block ||
                        block == Blocks.pumpkin ||
                        block == Blocks.cactus ||
                        block == Blocks.brown_mushroom ||
                        block == Blocks.red_mushroom ||
                        block == Blocks.nether_wart ||
                        block == Blocks.wheat)) return true;

        if (May2BeezQoL.config.filterWood &&
                (block == Blocks.log ||
                        block == Blocks.log2)) return true;

        if (May2BeezQoL.config.filterSand &&
                block == Blocks.sand) return true;

        if (May2BeezQoL.config.filterGlowstone &&
                block == Blocks.glowstone) return true;

        if (May2BeezQoL.config.filterIce &&
                block == Blocks.ice) return true;


        return May2BeezQoL.config.filterNetherrack &&
                block == Blocks.netherrack;
    }

    private boolean isSlow(IBlockState blockState) {
        Block block = blockState.getBlock();
        return block == Blocks.prismarine || block == Blocks.wool || block == Blocks.stained_hardened_clay ||
                block == Blocks.gold_block || block == Blocks.stained_glass_pane || block == Blocks.stained_glass ||
                block == Blocks.glowstone || block == Blocks.chest;
    }

    private IBlockState getBlockState(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos);
    }
}
