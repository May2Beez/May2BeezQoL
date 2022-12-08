package com.May2Beez.modules.mining;

import com.May2Beez.AOTVWaypointsGUI;
import com.May2Beez.Module;
import com.May2Beez.SkyblockMod;
import com.May2Beez.events.BlockChangeEvent;
import com.May2Beez.utils.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;


public class AOTVMacro extends Module {

    private static ArrayList<AOTVWaypointsGUI.Waypoint> Waypoints;

    private final Minecraft mc = Minecraft.getMinecraft();

    private BlockData target = null;
    private BlockData oldTarget = null;
    private int currentWaypoint = -1;

    private final Timer stuckTimer = new Timer();
    private final Timer searchingTimer = new Timer();
    private final Timer stuckTimer2 = new Timer();
    private BlockPos blockToIgnoreBecauseOfStuck = null;

    private final ArrayList<BlockPos> blocksBlockingVision = new ArrayList<>();

    public enum State {
        SEARCHING,
        MINING,
        WARPING,
        KILLING
    }

    private State currentState = State.SEARCHING;

    public AOTVMacro() {
        super("AOTV Macro", new KeyBinding("AOTV Macro", Keyboard.KEY_NONE, SkyblockMod.MODID + " - Mining"));
        Waypoints = SkyblockMod.coordsConfig.getSelectedRoute().waypoints;
    }

    public static void getAOTVWaypoints() {
        Waypoints = SkyblockMod.coordsConfig.getSelectedRoute().waypoints;
    }

    @Override
    public void onEnable() {
        Waypoints = SkyblockMod.coordsConfig.getSelectedRoute().waypoints;

        BlockPos currentPos = BlockUtils.getPlayerLoc().down();

        int miningTool = SkyblockUtils.findItemInHotbar("Drill", "Pickaxe", "Gauntlet");

        if (miningTool == -1) {
            SkyblockUtils.SendInfo("You don't have a mining tool!", false, name);
            this.toggle();
            return;
        }

        int voidTool = SkyblockUtils.findItemInHotbar("Void");

        if (voidTool == -1) {
            SkyblockUtils.SendInfo("You don't have a Aspect of the Void!", false,  name);
            this.toggle();
            return;
        }

        for (int i = 0; i < Waypoints.size(); i++) {
            BlockPos waypoint = new BlockPos(Waypoints.get(i).x, Waypoints.get(i).y, Waypoints.get(i).z);
            if (waypoint.equals(currentPos)) {
                currentWaypoint = i;
                break;
            }
        }

        if (currentWaypoint == -1) {
            SkyblockUtils.SendInfo("You are not at a valid waypoint!", false, name);
            this.setToggled(false);
            return;
        }

        searchingTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        target = null;
        oldTarget = null;
        currentWaypoint = -1;
        currentState = State.SEARCHING;
        stuckTimer.reset();
        searchingTimer.reset();
        stuckTimer2.reset();
        RotationUtils.resetRotation();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
    }

    @SubscribeEvent
    public void onBlockChange(BlockChangeEvent event) {
        if (!isToggled()) return;
        if (target == null) return;

        BlockPos pos = event.pos;
        IBlockState oldBlock = event.old;
        IBlockState newBlock = event.update;

        if (pos.equals(target.getPos()) && oldBlock.equals(target.getState()) && !newBlock.equals(target.getState())) {
            currentState = State.SEARCHING;
            oldTarget = target;
            target = null;
        }
    }


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (!this.isToggled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;


        switch (currentState) {
            case SEARCHING: {

                BlockPos currentPos = BlockUtils.getPlayerLoc().down();
                BlockPos waypoint = new BlockPos(Waypoints.get(currentWaypoint).x, Waypoints.get(currentWaypoint).y, Waypoints.get(currentWaypoint).z);

                if (!currentPos.equals(waypoint)) {
                    if (searchingTimer.hasReached(SkyblockMod.config.aotvStuckTimeThreshold)) {
                        SkyblockUtils.SendInfo("You are not at a valid waypoint!", false, name);
                        currentState = State.WARPING;
                    }
                    break;
                }

                if (blockToIgnoreBecauseOfStuck != null && !stuckTimer2.hasReached(30)) {
                    break;
                }

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                target = getClosestGemstone();
                blockToIgnoreBecauseOfStuck = null;
                if (target != null) {
                    currentState = State.MINING;
                    int miningTool = SkyblockUtils.findItemInHotbar("Drill", "Pickaxe", "Gauntlet");

                    if (miningTool == -1) {
                        SkyblockUtils.SendInfo("You don't have a mining tool!", false, name);
                        this.toggle();
                        return;
                    }
                    mc.thePlayer.inventory.currentItem = miningTool;
                } else {

                    SkyblockUtils.SendInfo("No gemstones found! Going to the next waypoint.", false, name);
                    if (currentWaypoint == Waypoints.size() - 1) {
                        currentWaypoint = 0;
                    } else {
                        currentWaypoint++;
                    }
                    currentState = State.WARPING;
                }

                stuckTimer.reset();
                break;
            }

            case MINING: {

                if (target == null) {
                    currentState = State.SEARCHING;
                    searchingTimer.reset();
                    break;
                }

                useMiningSpeedBoost();

                if (!RotationUtils.running)
                    RotationUtils.smoothLook(RotationUtils.vec3ToRotation(target.getRandomVisibilityLine()), SkyblockMod.config.aotvCameraSpeed);

                boolean lookingAtTarget = mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos().equals(target.getPos());

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), lookingAtTarget);

                if (stuckTimer.hasReached(SkyblockMod.config.aotvStuckTimeThreshold) && !RotationUtils.running) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    SkyblockUtils.SendInfo("Stuck for " + SkyblockMod.config.aotvStuckTimeThreshold + " ms, restarting.", false, name);
                    stuckTimer.reset();
                    currentState = State.SEARCHING;
                    searchingTimer.reset();
                    blockToIgnoreBecauseOfStuck = target.getPos();
                    target = null;
                    oldTarget = null;
                    stuckTimer2.reset();
                }

                break;
            }

            case WARPING: {

                int voidTool = SkyblockUtils.findItemInHotbar("Void");

                if (voidTool == -1) {
                    SkyblockUtils.SendInfo("You don't have a Aspect of the Void!", false, name);
                    this.toggle();
                    return;
                }

                mc.thePlayer.inventory.currentItem = voidTool;

                BlockPos waypoint = new BlockPos(Waypoints.get(currentWaypoint).x, Waypoints.get(currentWaypoint).y, Waypoints.get(currentWaypoint).z);
                if (!RotationUtils.IsDiffLowerThan(SkyblockMod.config.aotvTargetingWaypointAccuracy))
                    RotationUtils.smoothLook(RotationUtils.getRotationToBlock(waypoint), SkyblockMod.config.aotvWaypointTargetingTime);
                else
                    RotationUtils.resetRotation();

                if (RotationUtils.running) return;

                MovingObjectPosition movingObjectPosition = mc.thePlayer.rayTrace(55, 1);

                if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    if (movingObjectPosition.getBlockPos().equals(waypoint)) {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                        SkyblockUtils.SendInfo("Teleported to waypoint " + currentWaypoint, true, name);
                        currentState = State.SEARCHING;
                        searchingTimer.reset();
                        oldTarget = null;
                    } else {
                        if (stuckTimer.hasReached(2000) && !RotationUtils.running) {
                            SkyblockUtils.SendInfo("Path is not cleared. Block: " + movingObjectPosition.getBlockPos().toString() + " is on the way.", false, name);
                            this.toggle();
                            break;
                        }
                    }
                } else if (movingObjectPosition != null) {
                    SkyblockUtils.SendInfo("Something is on the way!", false, name);
                    this.toggle();
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public void onTick2(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (isToggled()) return;

        if (!blocksBlockingVision.isEmpty())
            blocksBlockingVision.clear();

        if (!SkyblockMod.config.drawBlocksBlockingAOTV) return;

        if (Waypoints.size() > 1) {

            for (int i = 0; i < Waypoints.size() - 1; i++) {
                BlockPos pos1 = new BlockPos(Waypoints.get(i).x, Waypoints.get(i).y, Waypoints.get(i).z);
                BlockPos pos2 = new BlockPos(Waypoints.get(i + 1).x, Waypoints.get(i + 1).y, Waypoints.get(i + 1).z);

                GetAllBlocksInline(pos1, pos2);
            }

            BlockPos pos1 = new BlockPos(Waypoints.get(Waypoints.size() - 1).x, Waypoints.get(Waypoints.size() - 1).y, Waypoints.get(Waypoints.size() - 1).z);
            BlockPos pos2 = new BlockPos(Waypoints.get(0).x, Waypoints.get(0).y, Waypoints.get(0).z);

            GetAllBlocksInline(pos1, pos2);
        }
    }

    private void GetAllBlocksInline(BlockPos pos1, BlockPos pos2) {
        Vec3 startPos = new Vec3(pos1.getX() + 0.5, pos1.getY() + 1 + 1.6 - 0.125, pos1.getZ() + 0.5);
        Vec3 endPos = new Vec3(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);

        Vec3 direction = new Vec3(endPos.xCoord - startPos.xCoord, endPos.yCoord - startPos.yCoord, endPos.zCoord - startPos.zCoord);

        double maxDistance = startPos.distanceTo(endPos);

        double increment = SkyblockMod.config.aotvVisionBlocksAccuracy;

        Vec3 currentPos = startPos;

        while (currentPos.distanceTo(startPos) < maxDistance) {


            ArrayList<BlockPos> blocks = AnyBlockAroundVec3(currentPos, 0.35f);

            for (BlockPos pos : blocks) {

                Block block = mc.theWorld.getBlockState(pos).getBlock();

                // Add the block to the list if it hasn't been added already
                if (!blocksBlockingVision.contains(pos) && !mc.theWorld.isAirBlock(pos) && !pos.equals(pos1) && !pos.equals(pos2) && block != Blocks.stained_glass && block != Blocks.stained_glass_pane) {
                    blocksBlockingVision.add(pos);
                }
            }

            // Move along the line by the specified increment
            Vec3 scaledDirection = new Vec3(direction.xCoord * increment, direction.yCoord * increment, direction.zCoord * increment);
            currentPos = currentPos.add(scaledDirection);
        }
    }

    private ArrayList<BlockPos> AnyBlockAroundVec3(Vec3 pos, float around) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (double x = (pos.xCoord - around); x < pos.xCoord + around; x += around) {
            for (double y = (pos.yCoord - around); y < pos.yCoord + around; y += around) {
                for (double z = (pos.zCoord - around); z < pos.zCoord + around; z += around) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (!blocks.contains(blockPos)) {
                        blocks.add(blockPos);
                    }
                }
            }
        }
        return blocks;
    }

    private BlockData getClosestGemstone() {
        BlockData blockPos = null;

        int range = 6;

        ArrayList<BlockData> blocks = new ArrayList<>();

        Iterable<BlockPos> blockss = BlockPos.getAllInBox(BlockUtils.getPlayerLoc().add(-range, -range, -range), BlockUtils.getPlayerLoc().add(range, range, range));
        for (BlockPos blockPos1 : blockss) {
            if (mc.theWorld.getBlockState(blockPos1).getBlock() == Blocks.stained_glass_pane || mc.theWorld.getBlockState(blockPos1).getBlock() == Blocks.stained_glass) {

                if (SkyblockMod.config.aotvGemstoneType > 0) {
                    if (!IsThisAGoodGemstone(blockPos1)) continue;
                }

                if (blockPos1.equals(blockToIgnoreBecauseOfStuck)) continue;

                mc.playerController.getBlockReachDistance();
                if (mc.thePlayer.getDistanceSq(blockPos1) < 4.5f * 4.5f) {
                    IBlockState bs = mc.theWorld.getBlockState(blockPos1);
                    Vec3 vec3 = BlockUtils.getRandomVisibilityLine(blockPos1);
                    if (vec3 != null)
                        blocks.add(new BlockData(blockPos1, bs.getBlock(), bs, vec3));
                }
            }
        }

        double distance = 9999;
        for (BlockData block : blocks) {
            double currentDistance;
            if (oldTarget != null) {
                currentDistance = oldTarget.getPos().distanceSqToCenter(block.getPos().getX(), block.getPos().getY(), block.getPos().getZ());
            } else {
                currentDistance = mc.thePlayer.getDistanceSqToCenter(block.getPos());
            }
            if (currentDistance < distance) {
                distance = currentDistance;
                blockPos = block;
            }
        }

        return blockPos;
    }

    public boolean IsThisAGoodGemstone(BlockPos block) {

        // Check if glass_pane block is color red
        int meta = mc.theWorld.getBlockState(block).getBlock().getMetaFromState(mc.theWorld.getBlockState(block));

        switch (meta) {
            // red
            case 14: {
                if (SkyblockMod.config.aotvGemstoneType == 1) return true;
                break;
            }
            // purple
            case 10: {
                if (SkyblockMod.config.aotvGemstoneType == 2) return true;
                break;
            }
            // lime
            case 5: {
                if (SkyblockMod.config.aotvGemstoneType == 3) return true;
                break;
            }
            // blue
            case 11: {
                if (SkyblockMod.config.aotvGemstoneType == 4) return true;
                break;
            }
            // orange
            case 1: {
                if (SkyblockMod.config.aotvGemstoneType == 5) return true;
                break;
            }
            // yellow
            case 4: {
                if (SkyblockMod.config.aotvGemstoneType == 6) return true;
                break;
            }
            default: {
                SkyblockUtils.SendInfo("Unknown gemstone color: " + meta, false, name);
                break;
            }
        }

        return false;
    }

    @SubscribeEvent
    public void onWorldLastRender(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (Waypoints == null || Waypoints.isEmpty()) return;

        RenderUtils.preDraw();
        if (target != null) {
            RenderUtils.drawBlockBox(target.getPos(), new Color(0, 255, 0, 100), 4f);
        }

        if (SkyblockMod.config.showRouteBlocks) {
            for (AOTVWaypointsGUI.Waypoint waypoint : Waypoints) {
                BlockPos pos = new BlockPos(waypoint.x, waypoint.y, waypoint.z);
                RenderUtils.drawBlockBox(pos, SkyblockMod.config.routeBlockColor, 4f);
            }

            if (SkyblockMod.config.showRouteLines) {
                if (Waypoints.size() > 1) {

                    for (int i = 0; i < Waypoints.size() - 1; i++) {
                        BlockPos pos1 = new BlockPos(Waypoints.get(i).x, Waypoints.get(i).y, Waypoints.get(i).z);
                        BlockPos pos2 = new BlockPos(Waypoints.get(i + 1).x, Waypoints.get(i + 1).y, Waypoints.get(i + 1).z);
                        RenderUtils.drawLineBetweenPoints(pos1, pos2, SkyblockMod.config.routeLineColor);
                    }
                    BlockPos pos1 = new BlockPos(Waypoints.get(Waypoints.size() - 1).x, Waypoints.get(Waypoints.size() - 1).y, Waypoints.get(Waypoints.size() - 1).z);
                    BlockPos pos2 = new BlockPos(Waypoints.get(0).x, Waypoints.get(0).y, Waypoints.get(0).z);
                    RenderUtils.drawLineBetweenPoints(pos1, pos2, SkyblockMod.config.routeLineColor);
                }
            }
        }

        if (SkyblockMod.config.drawBlocksBlockingAOTV && !isToggled()) {
            if (!blocksBlockingVision.isEmpty()) {
                for (BlockPos pos : blocksBlockingVision) {
                    RenderUtils.drawBlockBox(pos, SkyblockMod.config.aotvVisionBlocksColor, 4f);
                }
            }
        }

        RenderUtils.postDraw();

        for (AOTVWaypointsGUI.Waypoint waypoint : Waypoints) {
            BlockPos pos = new BlockPos(waypoint.x, waypoint.y, waypoint.z);
            RenderUtils.drawText(waypoint.name, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, new Color(255, 255, 255, 255), true, 1, true);
        }
    }

    private static class BlockData {
        private final BlockPos pos;
        private final Block block;
        private final IBlockState state;

        private final Vec3 randomVisibilityLine;

        public BlockData(BlockPos pos, Block block, IBlockState state, Vec3 randomVisibilityLine) {
            this.pos = pos;
            this.block = block;
            this.state = state;
            this.randomVisibilityLine = randomVisibilityLine;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Block getBlock() {
            return block;
        }

        public IBlockState getState() {
            return state;
        }

        public Vec3 getRandomVisibilityLine() {
            return randomVisibilityLine;
        }
    }
}
