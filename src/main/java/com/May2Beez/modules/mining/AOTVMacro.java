package com.May2Beez.modules.mining;

import com.May2Beez.AOTVWaypointsGUI;
import com.May2Beez.May2BeezQoL;
import com.May2Beez.Module;
import com.May2Beez.events.BlockChangeEvent;
import com.May2Beez.modules.combat.MobKiller;
import com.May2Beez.utils.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


public class AOTVMacro extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    private Structs.BlockData target = null;
    private Structs.BlockData oldTarget = null;
    private int currentWaypoint = -1;

    private final Timer stuckTimer = new Timer();
    private final Timer searchingTimer = new Timer();
    private final Timer stuckTimer2 = new Timer();
    private BlockPos blockToIgnoreBecauseOfStuck = null;

    private final ArrayList<BlockPos> blocksBlockingVision = new ArrayList<>();

    private final ArrayList<Structs.BlockData> blocksToMine = new ArrayList<>();

    private boolean killing = false;

    public enum State {
        SEARCHING,
        MINING,
        WARPING
    }

    private State currentState = State.SEARCHING;

    public AOTVMacro() {
        super("AOTV Macro", new KeyBinding("AOTV Macro", Keyboard.KEY_NONE, May2BeezQoL.MODID + " - Mining"));
    }

    public static void getAOTVWaypoints() {
    }

    @Override
    public void onEnable() {
        blocksToMine.clear();
        ArrayList<AOTVWaypointsGUI.Waypoint> Waypoints = May2BeezQoL.coordsConfig.getSelectedRoute().waypoints;

        BlockPos currentPos = BlockUtils.getPlayerLoc().down();

        int miningTool = SkyblockUtils.findItemInHotbar("Drill", "Pickaxe", "Gauntlet");

        if (miningTool == -1) {
            LogUtils.addMessage(getName() + " - You don't have a mining tool!", EnumChatFormatting.RED);
            this.toggle();
            return;
        }

        int voidTool = SkyblockUtils.findItemInHotbar("Void");

        if (voidTool == -1) {
            LogUtils.addMessage(getName() + " - You don't have a Aspect of the Void!", EnumChatFormatting.RED);
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
            LogUtils.addMessage(getName() + " - You are not at a valid waypoint!", EnumChatFormatting.RED);
            this.setToggled(false);
            return;
        }

        if (May2BeezQoL.config.yogKiller) {
            May2BeezQoL.mobKiller.Toggle();
            MobKiller.setMobsNames(false, "Yog");
            if (May2BeezQoL.config.useHyperionUnderPlayer) {
                MobKiller.scanRange = 5;
            } else {
                MobKiller.scanRange = 10;
            }
            MobKiller.ShouldScan = true;
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
        RotationUtils.reset();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        MobKiller.ShouldScan = false;
        May2BeezQoL.mobKiller.Toggle();
    }

    @SubscribeEvent
    public void onBlockChange(BlockChangeEvent event) {
        if (!isToggled()) return;
        if (target == null) return;

        BlockPos pos = event.pos;
        IBlockState newBlock = event.update;

        if (pos.equals(target.getPos()) && !newBlock.equals(target.getState())) {
            currentState = State.SEARCHING;
            oldTarget = target;
            target = null;
            RotationUtils.reset();
        }
    }


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (!this.isToggled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        ArrayList<AOTVWaypointsGUI.Waypoint> Waypoints = May2BeezQoL.coordsConfig.getSelectedRoute().waypoints;

        if (May2BeezQoL.config.yogKiller) {
            if (MobKiller.hasTarget()) {

                if (!killing) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    killing = true;
                }

                return;
            } else if (killing) {
                killing = false;
            }
        }

        switch (currentState) {
            case SEARCHING: {

                BlockPos currentPos = BlockUtils.getPlayerLoc().down();
                BlockPos waypoint = new BlockPos(Waypoints.get(currentWaypoint).x, Waypoints.get(currentWaypoint).y, Waypoints.get(currentWaypoint).z);

                if (!currentPos.equals(waypoint)) {
                    if (searchingTimer.hasReached(May2BeezQoL.config.aotvStuckTimeThreshold)) {
                        LogUtils.addMessage(getName() + " - You are not at a valid waypoint!", EnumChatFormatting.DARK_RED);
                        currentState = State.WARPING;
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    }
                    break;
                }

                if (blockToIgnoreBecauseOfStuck != null && !stuckTimer2.hasReached(100)) {
                    break;
                }

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);

                if (blocksToMine.isEmpty()) {
                    blocksToMine.addAll(getBlocksToMine());
                }

                target = getClosestGemstone();
                blockToIgnoreBecauseOfStuck = null;

                if (target != null) {
                    currentState = State.MINING;
                    int miningTool = SkyblockUtils.findItemInHotbar("Drill", "Pickaxe", "Gauntlet");

                    if (miningTool == -1) {
                        LogUtils.addMessage(getName() + " - You don't have a mining tool!", EnumChatFormatting.RED);
                        this.toggle();
                        return;
                    }
                    mc.thePlayer.inventory.currentItem = miningTool;
                } else {

                    LogUtils.addMessage(getName() + " - No gemstones found! Going to the next waypoint.", EnumChatFormatting.RED);
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

                if (RotationUtils.done)
                    RotationUtils.smoothLook(RotationUtils.getRotation(target.getRandomVisibilityLine()), May2BeezQoL.config.aotvCameraSpeed);

                boolean lookingAtTarget = mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos().equals(target.getPos());

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), lookingAtTarget);

                if (stuckTimer.hasReached(May2BeezQoL.config.aotvStuckTimeThreshold) && RotationUtils.IsDiffLowerThan(0.1f)) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    LogUtils.addMessage(getName() + " - Stuck for " + May2BeezQoL.config.aotvStuckTimeThreshold + " ms, restarting.", EnumChatFormatting.DARK_RED);
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
                    LogUtils.addMessage(getName() + " - You don't have a Aspect of the Void!", EnumChatFormatting.RED);
                    this.toggle();
                    return;
                }

                mc.thePlayer.inventory.currentItem = voidTool;

                BlockPos waypoint = new BlockPos(Waypoints.get(currentWaypoint).x, Waypoints.get(currentWaypoint).y, Waypoints.get(currentWaypoint).z);
                RotationUtils.smoothLook(RotationUtils.getRotation(waypoint), May2BeezQoL.config.aotvWaypointTargetingTime);

                if (RotationUtils.IsDiffLowerThan(May2BeezQoL.config.aotvTargetingWaypointAccuracy))
                    RotationUtils.reset();

                if (!RotationUtils.done) return;

                MovingObjectPosition movingObjectPosition = mc.thePlayer.rayTrace(55, 1);

                if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    if (movingObjectPosition.getBlockPos().equals(waypoint)) {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                        LogUtils.addMessage(getName() + " - Teleported to waypoint " + currentWaypoint, EnumChatFormatting.DARK_GREEN);
                        blocksToMine.clear();
                        currentState = State.SEARCHING;
                        searchingTimer.reset();
                        oldTarget = null;
                    } else {
                        if (stuckTimer.hasReached(2000) && RotationUtils.done) {
                            LogUtils.addMessage(getName() + " - Path is not cleared. Block: " + movingObjectPosition.getBlockPos().toString() + " is on the way.", EnumChatFormatting.RED);
                            this.toggle();
                            break;
                        }
                    }
                } else if (movingObjectPosition != null) {
                    LogUtils.addMessage(getName() + " - Something is on the way!", EnumChatFormatting.RED);
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

        if (!May2BeezQoL.config.drawBlocksBlockingAOTV) return;

        ArrayList<AOTVWaypointsGUI.Waypoint> Waypoints = May2BeezQoL.coordsConfig.getSelectedRoute().waypoints;


        if (Waypoints.size() > 1) {

            debugPoints.clear();

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

    private final CopyOnWriteArrayList<Vec3> debugPoints = new CopyOnWriteArrayList<>();

    private void GetAllBlocksInline(BlockPos pos1, BlockPos pos2) {
        Vec3 startPos = new Vec3(pos1.getX() + 0.5, pos1.getY() + 1 + mc.thePlayer.getDefaultEyeHeight() - 0.125, pos1.getZ() + 0.5);
        Vec3 endPos = new Vec3(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);

        Vec3 direction = new Vec3(endPos.xCoord - startPos.xCoord, endPos.yCoord - startPos.yCoord, endPos.zCoord - startPos.zCoord);

        RotationUtils.Rotation rotation = RotationUtils.getRotation(endPos, startPos);

        double maxDistance = startPos.distanceTo(endPos);

        double increment = May2BeezQoL.config.aotvVisionBlocksAccuracy;

        double x_offset = -Math.sin(Math.toRadians(rotation.yaw)) * Math.cos(Math.toRadians(rotation.pitch));
        double y_offset = -Math.sin(Math.toRadians(rotation.pitch));
        double z_offset = Math.cos(Math.toRadians(rotation.yaw)) * Math.cos(Math.toRadians(rotation.pitch));

        Vec3 currentPos = startPos;


        while (currentPos.distanceTo(startPos) < maxDistance) {

            debugPoints.add(currentPos);

            ArrayList<BlockPos> blocks = SkyblockUtils.AnyBlockAroundVec3(currentPos, 0.15f);

            for (BlockPos pos : blocks) {

                Block block = mc.theWorld.getBlockState(pos).getBlock();

                // Add the block to the list if it hasn't been added already
                if (!blocksBlockingVision.contains(pos) && !mc.theWorld.isAirBlock(pos) && !pos.equals(pos1) && !pos.equals(pos2) && block != Blocks.stained_glass && block != Blocks.stained_glass_pane) {
                    blocksBlockingVision.add(pos);
                }
            }

            // Move along the line by the specified increment
            currentPos = currentPos.add(new Vec3(x_offset * increment, y_offset * increment, z_offset * increment));
        }
    }

    private ArrayList<Structs.BlockData> getBlocksToMine() {
        float range = May2BeezQoL.config.scanRadius + 1;

        ArrayList<Structs.BlockData> blocks = new ArrayList<>();
        Iterable<BlockPos> blockss = BlockPos.getAllInBox(BlockUtils.getPlayerLoc().add(-range, -range, -range), BlockUtils.getPlayerLoc().add(range, range, range));
        for (BlockPos blockPos1 : blockss) {
            ArrayList<Block> blocksToCheck = new ArrayList<Block>() {{
                add(Blocks.stained_glass_pane);
                add(Blocks.stained_glass);
                if (May2BeezQoL.config.aotvGemstoneType == 8) {
                    add(Blocks.wool);
                    add(Blocks.prismarine);
                    add(Blocks.stained_hardened_clay);
                }
            }};
            if (blocksToCheck.stream().anyMatch(b -> b.equals(mc.theWorld.getBlockState(blockPos1).getBlock()))) {

                if (May2BeezQoL.config.aotvGemstoneType == 8) {
                    if (!IsThisAGoodMithril(blockPos1)) continue;
                }
                else if (May2BeezQoL.config.aotvGemstoneType > 0) {
                    if (!IsThisAGoodGemstone(blockPos1)) continue;
                }

                if (mc.thePlayer.getPositionEyes(1).distanceTo(new Vec3(blockPos1.getX() + 0.5, blockPos1.getY() + 0.5, blockPos1.getZ() + 0.5)) < May2BeezQoL.config.scanRadius) {

                    IBlockState bs = mc.theWorld.getBlockState(blockPos1);
                    blocks.add(new Structs.BlockData(blockPos1, bs.getBlock(), bs, null));
                }
            }
        }

        return blocks;
    }

    private Structs.BlockData getClosestGemstone() {
        Structs.BlockData blockPos = null;

        double distance = 9999;
        for (Structs.BlockData block : blocksToMine) {
            Structs.BlockData blockPos1 = block;
            double currentDistance;

            if (mc.theWorld.getBlockState(blockPos1.getPos()) == null || mc.theWorld.isAirBlock(blockPos1.getPos())) continue;

            if (blockPos1.getPos().equals(blockToIgnoreBecauseOfStuck)) continue;

            Vec3 vec3 = BlockUtils.getRandomVisibilityLine(blockPos1.getPos());
            if (vec3 == null) continue;

            blockPos1 = new Structs.BlockData(blockPos1.getPos(), blockPos1.getBlock(), blockPos1.getState(), vec3);

            if (oldTarget != null) {
                currentDistance = oldTarget.getPos().distanceSq(blockPos1.getPos());
            } else {
                currentDistance = BlockUtils.getPlayerLoc().distanceSq(blockPos1.getPos());
            }
            if (currentDistance < distance) {
                distance = currentDistance;
                blockPos = blockPos1;
            }
        }

        return blockPos;
    }

    private boolean IsThisAGoodMithril(BlockPos blockPos) {

        if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.wool) {
            EnumDyeColor color = mc.theWorld.getBlockState(blockPos).getValue(BlockColored.COLOR);
            return (color == EnumDyeColor.LIGHT_BLUE || color == EnumDyeColor.GRAY);
        }

        if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.prismarine) {
            return true;
        }

        if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.stained_hardened_clay) {
            EnumDyeColor color = mc.theWorld.getBlockState(blockPos).getValue(BlockColored.COLOR);
            return (color == EnumDyeColor.CYAN);
        }

        return false;
    }

    public boolean IsThisAGoodGemstone(BlockPos block) {

        EnumDyeColor color = mc.theWorld.getBlockState(block).getValue(BlockColored.COLOR);

        switch (color) {
            case RED: {
                return May2BeezQoL.config.aotvGemstoneType == 1;
            }
            case PURPLE: {
                return May2BeezQoL.config.aotvGemstoneType == 2;
            }
            case LIME: {
                return May2BeezQoL.config.aotvGemstoneType == 3;
            }
            case BLUE: {
                return May2BeezQoL.config.aotvGemstoneType == 4;
            }
            case ORANGE: {
                return May2BeezQoL.config.aotvGemstoneType == 5;
            }
            case YELLOW: {
                return May2BeezQoL.config.aotvGemstoneType == 6;
            }
            case MAGENTA: {
                return May2BeezQoL.config.aotvGemstoneType == 7;
            }

            default: {
                LogUtils.addMessage(getName() + " - Unknown gemstone color: " + color.getName(), EnumChatFormatting.RED);
                break;
            }
        }

        return false;
    }

    @SubscribeEvent
    public void onWorldLastRender(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
//        if (LocationUtils.currentIsland != LocationUtils.Island.CRYSTAL_HOLLOWS) return;
        ArrayList<AOTVWaypointsGUI.Waypoint> Waypoints = May2BeezQoL.coordsConfig.getSelectedRoute().waypoints;
        if (Waypoints == null || Waypoints.isEmpty()) return;

        if (target != null) {
            RenderUtils.drawBlockBox(target.getPos(), new Color(0, 255, 0, 100), 4f);
        }

        if (May2BeezQoL.config.showRouteBlocks) {
            for (AOTVWaypointsGUI.Waypoint waypoint : Waypoints) {
                BlockPos pos = new BlockPos(waypoint.x, waypoint.y, waypoint.z);
                RenderUtils.drawBlockBox(pos, May2BeezQoL.config.routeBlockColor, 4f);
            }

            if (May2BeezQoL.config.showRouteLines) {
                if (Waypoints.size() > 1) {

                    for (int i = 0; i < Waypoints.size() - 1; i++) {
                        BlockPos pos1 = new BlockPos(Waypoints.get(i).x, Waypoints.get(i).y, Waypoints.get(i).z);
                        BlockPos pos2 = new BlockPos(Waypoints.get(i + 1).x, Waypoints.get(i + 1).y, Waypoints.get(i + 1).z);
                        RenderUtils.drawLineBetweenPoints(new Vec3(pos1), new Vec3(pos2), May2BeezQoL.config.routeLineColor, event.partialTicks, 5f);
                    }
                    BlockPos pos1 = new BlockPos(Waypoints.get(Waypoints.size() - 1).x, Waypoints.get(Waypoints.size() - 1).y, Waypoints.get(Waypoints.size() - 1).z);
                    BlockPos pos2 = new BlockPos(Waypoints.get(0).x, Waypoints.get(0).y, Waypoints.get(0).z);
                    RenderUtils.drawLineBetweenPoints(new Vec3(pos1), new Vec3(pos2), May2BeezQoL.config.routeLineColor, event.partialTicks, 5f);
                }
            }
        }

        if (May2BeezQoL.config.drawBlocksBlockingAOTV && !isToggled()) {
            if (!blocksBlockingVision.isEmpty()) {
                for (BlockPos pos : blocksBlockingVision) {
                    RenderUtils.drawBlockBox(pos, May2BeezQoL.config.aotvVisionBlocksColor, 4f);
                }
            }
        }

        for (AOTVWaypointsGUI.Waypoint waypoint : Waypoints) {
            BlockPos pos = new BlockPos(waypoint.x, waypoint.y, waypoint.z);
            RenderUtils.drawText("§l§3[§f " + waypoint.name + " §3]", pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
        }

        if (!debugPoints.isEmpty()) {
            for (Vec3 vec3 : debugPoints) {
                RenderUtils.miniBlockBox(vec3, new Color(255, 0, 0, 100), 4f);
            }
        }
    }
}
