package com.May2Beez.modules.mining;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.modules.Module;
import com.May2Beez.modules.combat.MobKiller;
import com.May2Beez.modules.player.PowderChest;
import com.May2Beez.utils.LogUtils;
import com.May2Beez.utils.RenderUtils;
import com.May2Beez.utils.RotationUtils;
import com.May2Beez.utils.SkyblockUtils;
import com.May2Beez.utils.structs.Rotation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.May2Beez.utils.SkyblockUtils.leftClick;
import static com.May2Beez.utils.SkyblockUtils.rightClick;

public class PowderMacro extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    enum States {
        CHANGING_ROUTE,
        MINING,
        DODGE_CHEST,
        CHEST,
    }

    enum ChangingRouteStates {
        WARPING,
        CHANGING_ROTATION,
    }

    enum DodgeChestStates {
        BLOCKS_TO_BREAK,
        GET_CLOSEST_BLOCK,
        BREAK_BLOCK,
    }

    enum ChestPosition {
        TOP,
        BOTTOM,
    }

    private ChestPosition chestPosition = null;
    private int chestY = 0;

    private States currentState = States.MINING;
    private ChangingRouteStates currentChangingRouteState = ChangingRouteStates.WARPING;
    private DodgeChestStates currentDodgeChestState = DodgeChestStates.BLOCKS_TO_BREAK;

    private BlockPos dodgeChestTargetBlock = null;

    List<BlockPos> blocksToBreak = new ArrayList<>();

    private boolean killing = false;

    private Rotation startRotation;

    public PowderMacro() {
        super("Powder Macro", new KeyBinding("Powder Macro", 0, May2BeezQoL.MODID + " - Mining"));
    }

    @Override
    public void onDisable() {
        RotationUtils.reset();
        killing = false;
        currentState = States.MINING;
        currentDodgeChestState = DodgeChestStates.BLOCKS_TO_BREAK;
        currentChangingRouteState = ChangingRouteStates.WARPING;
        dodgeChestTargetBlock = null;
        startRotation = null;
        blocksToBreak.clear();

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);

        if (May2BeezQoL.powderChestMacro.isToggled())
            May2BeezQoL.powderChestMacro.toggle();

        super.onDisable();
    }

    @Override
    public void onEnable() {
//        if (LocationUtils.currentIsland != LocationUtils.Island.CRYSTAL_HOLLOWS) {
//            this.toggle();
//            LogUtils.addMessage("You must be in Crystal Hollows to use this macro.", EnumChatFormatting.RED);
//            return;
//        }
//
//        int miningTool = SkyblockUtils.findItemInHotbar("Drill", "Pickaxe", "Gauntlet");
//
//        if (miningTool == -1) {
//            LogUtils.addMessage(getName() + " - You don't have a mining tool!", EnumChatFormatting.RED);
//            this.toggle();
//            return;
//        }
//
//        int voidTool = SkyblockUtils.findItemInHotbar("Void");
//
//        if (voidTool == -1) {
//            LogUtils.addMessage(getName() + " - You don't have a Aspect of the Void!", EnumChatFormatting.RED);
//            this.toggle();
//            return;
//        }

        if (May2BeezQoL.config.killScathasAndWorms) {
            MobKiller.scanRange = 20;
            MobKiller.setMobsNames(false, "Scatha", "Worm");
            MobKiller.ShouldScan = true;
        }

        if (RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) > -45 && RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) < 45) {
            startRotation = new Rotation(0, 27);
        } else if (RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) > 45 && RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) < 135) {
            startRotation = new Rotation(90, 27);
        } else if (RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) > 135 && RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) < 180) {
            startRotation = new Rotation(180, 27);
        } else if (RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) > -180 && RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) < -135) {
            startRotation = new Rotation(-180, 27);
        } else if (RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) > -135 && RotationUtils.wrapAngleTo180(mc.thePlayer.rotationYaw) < -45) {
            startRotation = new Rotation(-90, 27);
        }

        if (startRotation != null) {
            RotationUtils.smoothLook(startRotation, 500);
        } else {
            LogUtils.addMessage("Something went wrong, change looking direction a little and try again", EnumChatFormatting.RED);
            this.toggle();
            return;
        }

//        if (!May2BeezQoL.powderChestMacro.isToggled())
//            May2BeezQoL.powderChestMacro.toggle();

        super.onEnable();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isToggled()) return;
        if (SkyblockUtils.hasOpenContainer()) return;


        if (May2BeezQoL.config.killScathasAndWorms) {
            if (MobKiller.hasTarget()) {
                if (!killing) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                    killing = true;
                    RotationUtils.reset();
                }
                return;
            } else {
                killing = false;
            }
        }

        if (currentState != States.CHEST) {
            if (PowderChest.closestChest != null) {
                currentState = States.CHEST;
            }
        }

        switch (currentState) {
            case CHANGING_ROUTE: {
                switch (currentChangingRouteState) {
                    case WARPING: {
                        int aotv = SkyblockUtils.findItemInHotbar("Void");

//                        if (aotv == -1) {
//                            LogUtils.addMessage(getName() + " - You don't have a Aspect of the Void!", EnumChatFormatting.RED);
//                            this.toggle();
//                            return;
//                        }
//
//                        mc.thePlayer.inventory.currentItem = aotv;

                        if (RotationUtils.done)
                            RotationUtils.smoothLook(new Rotation(mc.thePlayer.rotationYaw, 89), 500);

                        if (RotationUtils.IsDiffLowerThan(0.1f)) {
                            RotationUtils.reset();
                        }

                        if (!RotationUtils.done) return;

                        rightClick();

                        currentChangingRouteState = ChangingRouteStates.CHANGING_ROTATION;
                        break;
                    }
                    case CHANGING_ROTATION: {

                        Vec3 eyePos = mc.thePlayer.getPositionEyes(1);
                        double x = eyePos.xCoord;
                        double y = eyePos.yCoord;
                        double z = eyePos.zCoord;
                        double pitch = RotationUtils.wrapAngleTo180(startRotation.pitch);
                        double yaw = RotationUtils.wrapAngleTo180(startRotation.yaw);

                        double x_offset = Math.sin(Math.toRadians(yaw + 90)) * Math.cos(Math.toRadians(pitch));
                        double y_offset = -Math.sin(Math.toRadians(pitch));
                        double z_offset = -Math.cos(Math.toRadians(yaw + 90)) * Math.cos(Math.toRadians(pitch));

                        double eye_level_x = x + x_offset;
                        double eye_level_y = y + y_offset;
                        double eye_level_z = z + z_offset;

                        double leg_level_x = x + x_offset;
                        double leg_level_y = y - 1 + y_offset;
                        double leg_level_z = z + z_offset;

                        BlockPos eye_level_block = new BlockPos(eye_level_x, eye_level_y, eye_level_z);
                        BlockPos leg_level_block = new BlockPos(leg_level_x, leg_level_y, leg_level_z);

                        IBlockState eye_level_state = mc.theWorld.getBlockState(eye_level_block);
                        IBlockState leg_level_state = mc.theWorld.getBlockState(leg_level_block);

                        if (eye_level_state.getBlock().equals(Blocks.bedrock) || leg_level_state.getBlock().equals(Blocks.bedrock)) {
                            startRotation = new Rotation(startRotation.yaw + 90, 27);
                            RotationUtils.smoothLook(startRotation, 500);
                        } else {
                            startRotation = new Rotation(startRotation.yaw - 90, 27);
                            RotationUtils.smoothLook(startRotation, 500);
                        }

                        currentChangingRouteState = ChangingRouteStates.WARPING;
                        currentState = States.MINING;

                        break;
                    }
                }
                break;
            }
            case MINING: {

                if (!RotationUtils.done) return;

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);

                Vec3 eyePos = mc.thePlayer.getPositionEyes(1);
                double x = eyePos.xCoord;
                double y = eyePos.yCoord;
                double z = eyePos.zCoord;
                double pitch = RotationUtils.wrapAngleTo180(startRotation.pitch);
                double yaw = RotationUtils.wrapAngleTo180(startRotation.yaw);

                double x_offset = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
                double y_offset = -Math.sin(Math.toRadians(pitch));
                double z_offset = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));

                double eye_level_x = x + x_offset;
                double eye_level_y = y + y_offset;
                double eye_level_z = z + z_offset;

                double leg_level_x = x + x_offset;
                double leg_level_y = y - 1 + y_offset;
                double leg_level_z = z + z_offset;

                BlockPos eye_level_block = new BlockPos(eye_level_x, eye_level_y, eye_level_z);
                BlockPos leg_level_block = new BlockPos(leg_level_x, leg_level_y, leg_level_z);

                IBlockState eye_level_state = mc.theWorld.getBlockState(eye_level_block);
                IBlockState leg_level_state = mc.theWorld.getBlockState(leg_level_block);

                if (eye_level_state.getBlock().equals(Blocks.bedrock) || leg_level_state.getBlock().equals(Blocks.bedrock)) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    currentState = States.CHANGING_ROUTE;
                    RotationUtils.reset();
                    return;
                } else if (!mc.theWorld.isAirBlock(eye_level_block) || !mc.theWorld.isAirBlock(leg_level_block)) {

                    if (PowderChest.closestChest == null) {
                        if (eye_level_state.getBlock().equals(Blocks.chest) || eye_level_state.getBlock().equals(Blocks.trapped_chest)) {
                            chestPosition = ChestPosition.TOP;
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                            currentState = States.DODGE_CHEST;
                            chestY = leg_level_block.getY();
                            RotationUtils.reset();
                            return;
                        } else if (leg_level_state.getBlock().equals(Blocks.chest) || leg_level_state.getBlock().equals(Blocks.trapped_chest)) {
                            chestPosition = ChestPosition.BOTTOM;
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                            currentState = States.DODGE_CHEST;
                            chestY = leg_level_block.getY();
                            RotationUtils.reset();
                            return;
                        }
                    }

                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                }

                break;
            }
            case CHEST: {

                if (!RotationUtils.done) return;

                if (PowderChest.closestChest != null && PowderChest.closestChest.distance(mc.thePlayer.getPositionEyes(1)) < 3.5f) {
                    MovingObjectPosition mouseOver = mc.objectMouseOver;
                    if (mouseOver != null && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        BlockPos pos = mouseOver.getBlockPos();
                        if (!mc.theWorld.getBlockState(pos).getBlock().equals(Blocks.chest) && !mc.theWorld.getBlockState(pos).getBlock().equals(Blocks.trapped_chest)) {
                            leftClick();
                            return;
                        }
                    }
                } else {
                    //move to chest
                }

                break;
            }
            case DODGE_CHEST: {

                switch (currentDodgeChestState) {
                    case BLOCKS_TO_BREAK: {
                        Vec3 eyePos = mc.thePlayer.getPositionEyes(1);
                        double x = eyePos.xCoord;
                        double y = eyePos.yCoord;
                        double z = eyePos.zCoord;
                        double pitch = RotationUtils.wrapAngleTo180(startRotation.pitch);
                        double yaw = RotationUtils.wrapAngleTo180(startRotation.yaw);

                        double x_offset = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
                        double y_offset = -Math.sin(Math.toRadians(pitch));
                        double z_offset = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));

                        if (chestPosition == ChestPosition.TOP) {
                            BlockPos first_block = new BlockPos(x, y - 2, z);

                            blocksToBreak.add(first_block);

                            BlockPos second_block_top = new BlockPos(Math.floor(x + x_offset), Math.floor(y - 1), Math.floor(z + z_offset));
                            BlockPos second_block_bottom = new BlockPos(x + x_offset, y - 2, Math.floor(z + z_offset));

                            blocksToBreak.add(second_block_top);
                            blocksToBreak.add(second_block_bottom);

                            BlockPos third_block_top = new BlockPos(x + x_offset * 2, y - 1, z + z_offset * 2);
                            BlockPos third_block_bottom = new BlockPos(x + x_offset * 2, y - 2, z + z_offset * 2);

                            blocksToBreak.add(third_block_top);
                            blocksToBreak.add(third_block_bottom);

                            BlockPos block_behind_chest = new BlockPos(x + x_offset * 2, y, z + z_offset * 2);
                            blocksToBreak.add(block_behind_chest);

                            BlockPos fourth_block_top = new BlockPos(x + x_offset * 3, y, z + z_offset * 3);
                            BlockPos fourth_block_bottom = new BlockPos(Math.floor(x + x_offset * 3), y - 1, Math.floor(z + z_offset * 3));

                            blocksToBreak.add(fourth_block_top);
                            blocksToBreak.add(fourth_block_bottom);
                        } else {
                            BlockPos first_block = new BlockPos(x, y + 1, z);

                            blocksToBreak.add(first_block);

                            BlockPos second_block_top = new BlockPos(Math.floor(x + x_offset), Math.floor(y + 1), Math.floor(z + z_offset));
                            BlockPos second_block_bottom = new BlockPos(x + x_offset, y, Math.floor(z + z_offset));

                            blocksToBreak.add(second_block_top);
                            blocksToBreak.add(second_block_bottom);

                            BlockPos third_block_top = new BlockPos(x + x_offset * 2, y + 1, z + z_offset * 2);
                            BlockPos third_block_bottom = new BlockPos(x + x_offset * 2, y, z + z_offset * 2);

                            blocksToBreak.add(third_block_top);
                            blocksToBreak.add(third_block_bottom);

                            BlockPos block_behind_chest = new BlockPos(x + x_offset * 2, y - 1, z + z_offset * 2);
                            blocksToBreak.add(block_behind_chest);

                            BlockPos fourth_block_top = new BlockPos(x + x_offset * 3, y, z + z_offset * 3);
                            BlockPos fourth_block_bottom = new BlockPos(Math.floor(x + x_offset * 3), y - 1, Math.floor(z + z_offset * 3));

                            blocksToBreak.add(fourth_block_top);
                            blocksToBreak.add(fourth_block_bottom);
                        }

                        currentDodgeChestState = DodgeChestStates.GET_CLOSEST_BLOCK;

                        break;
                    }

                    case GET_CLOSEST_BLOCK: {
                        for (BlockPos pos : blocksToBreak) {
                            if (mc.theWorld.isAirBlock(pos)) continue;

                            dodgeChestTargetBlock = pos;
                            break;
                        }
                        if (dodgeChestTargetBlock != null) {
                            currentDodgeChestState = DodgeChestStates.BREAK_BLOCK;
                            return;
                        } else {
                            if (RotationUtils.done)
                                RotationUtils.smoothLook(startRotation, 500);

                            if (RotationUtils.IsDiffLowerThan(0.5f)) {
                                RotationUtils.reset();
                            }

                            if (!RotationUtils.done) return;

                            currentState = States.MINING;
                            currentChangingRouteState = ChangingRouteStates.WARPING;
                            currentDodgeChestState = DodgeChestStates.BLOCKS_TO_BREAK;
                            blocksToBreak.clear();
                        }
                        break;
                    }

                    case BREAK_BLOCK: {
                        if (dodgeChestTargetBlock != null) {
                            // Move and break these blocks, baritone maybe?

                        } else {
                            currentState = States.MINING;
                            RotationUtils.smoothLook(startRotation, 500);
                            RotationUtils.reset();
                        }
                    }
                    break;
                }

                break;
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isToggled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (blocksToBreak.isEmpty()) return;

        for (BlockPos blockPos : blocksToBreak) {
            RenderUtils.drawBlockBox(blockPos, Color.green, 2);
        }
    }
}
