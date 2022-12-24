package com.May2Beez.modules.mining;

import com.May2Beez.Module;
import com.May2Beez.May2BeezQoL;
import com.May2Beez.events.BlockChangeEvent;
import com.May2Beez.utils.*;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class MithrilMiner extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private Structs.BlockData target = null;
    private Structs.BlockData oldTarget = null;

    private final Timer stuckTimer = new Timer();
    private final Timer searchingTimer = new Timer();
    private BlockPos blockToIgnoreBecauseOfStuck = null;

    private enum State {
        SEARCHING,
        MINING,
    }

    private State currentState = State.SEARCHING;

    public MithrilMiner() {
        super("Mithril Miner", new KeyBinding("Mithril Miner", Keyboard.KEY_NONE, May2BeezQoL.MODID + " - Mining"));
    }

    @Override
    public void onEnable() {

        int miningTool = SkyblockUtils.findItemInHotbar("Drill", "Pickaxe", "Gauntlet");

        if (miningTool == -1) {
            SkyblockUtils.SendInfo("You don't have a mining tool!", false, name);
            this.toggle();
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
        blockToIgnoreBecauseOfStuck = null;
        currentState = State.SEARCHING;
        stuckTimer.reset();
        searchingTimer.reset();
        RotationUtils.reset();
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
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
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (!isToggled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        switch (currentState) {
            case SEARCHING: {

                if (blockToIgnoreBecauseOfStuck != null && !stuckTimer.hasReached(100)) {
                    break;
                }

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                target = getClosestBlock();
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

                    if (!searchingTimer.hasReached(2000))
                        break;

                    SkyblockUtils.SendInfo("No blocks found!", false, name);
                    target = null;
                    blockToIgnoreBecauseOfStuck = null;

                    searchingTimer.reset();
                    RotationUtils.reset();
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
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
                    RotationUtils.smoothLook(RotationUtils.getRotation(target.getRandomVisibilityLine()), May2BeezQoL.config.cameraSpeed);

                boolean lookingAtTarget = mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.objectMouseOver.getBlockPos().equals(target.getPos());

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), lookingAtTarget);

                if (stuckTimer.hasReached(May2BeezQoL.config.maxBreakTime) && RotationUtils.done) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    SkyblockUtils.SendInfo("Stuck for " + May2BeezQoL.config.maxBreakTime + " ms, restarting.", false, name);
                    stuckTimer.reset();
                    currentState = State.SEARCHING;
                    searchingTimer.reset();
                    blockToIgnoreBecauseOfStuck = target.getPos();
                    target = null;
                    oldTarget = null;
                }
                break;
            }
        }
    }

    private Structs.BlockData getClosestBlock() {
        Structs.BlockData blockPos = null;
        int range = 6;
        ArrayList<Structs.BlockData> blocks = new ArrayList<>();
        Iterable<BlockPos> blockss = BlockPos.getAllInBox(BlockUtils.getPlayerLoc().add(-range, -range, -range), BlockUtils.getPlayerLoc().add(range, range, range));

        for (BlockPos blockPos1 : blockss) {
            if (blockPos1.equals(blockToIgnoreBecauseOfStuck)) continue;
            IBlockState blockState = mc.theWorld.getBlockState(blockPos1);

            if (blockState.getBlock() == Blocks.air) continue;
            if (BlockMatchConfig(blockPos1)) {
                if (mc.thePlayer.getDistanceSq(blockPos1) < May2BeezQoL.config.scanRange * May2BeezQoL.config.scanRange) {
                    Vec3 vec3 = BlockUtils.getRandomVisibilityLine(blockPos1);
                    if (vec3 != null) {
                        IBlockState bs = mc.theWorld.getBlockState(blockPos1);
                        blocks.add(new Structs.BlockData(blockPos1, bs.getBlock(), bs, vec3));
                    }
                }
            }
        }
        ArrayList<Structs.BlockData> titaniumBlocks = blocks.stream().filter(blockData -> isTitanium(blockData.getPos())).sorted(Comparator.comparingDouble(this::CompareDistance)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Structs.BlockData> blocksData = new ArrayList<>();

        if (May2BeezQoL.config.prioTitanium) {
            if (titaniumBlocks.size() > 0) {
                return titaniumBlocks.get(0);
            }
        } else {
            if (titaniumBlocks.size() > 0) {
                blocksData.addAll(titaniumBlocks);
            }
        }


        if (May2BeezQoL.config.filterBlueWool) {
            ArrayList<Structs.BlockData> sortedBlueWool = blocks.stream().filter(blockData -> blockData.getBlock() == Blocks.wool && blockData.getState().getValue(BlockColored.COLOR) == EnumDyeColor.LIGHT_BLUE).collect(Collectors.toCollection(ArrayList::new));
            if (sortedBlueWool.size() > 0) {
                blocksData.addAll(sortedBlueWool);
            }
        }

        if (May2BeezQoL.config.filterPrismarine) {
            ArrayList<Structs.BlockData> sortedPrismarine = blocks.stream().filter(blockData -> blockData.getBlock() == Blocks.prismarine).collect(Collectors.toCollection(ArrayList::new));
            if (sortedPrismarine.size() > 0) {
                blocksData.addAll(sortedPrismarine);
            }
        }

        if (May2BeezQoL.config.filterGrayWool) {
            ArrayList<Structs.BlockData> sortedGrayWool = blocks.stream().filter(blockData -> blockData.getBlock() == Blocks.wool && blockData.getState().getValue(BlockColored.COLOR) == EnumDyeColor.GRAY).collect(Collectors.toCollection(ArrayList::new));
            if (sortedGrayWool.size() > 0) {
                blocksData.addAll(sortedGrayWool);
            }
        }

        if (May2BeezQoL.config.filterClay) {
            ArrayList<Structs.BlockData> sortedClay = blocks.stream().filter(blockData -> blockData.getBlock() == Blocks.stained_hardened_clay).collect(Collectors.toCollection(ArrayList::new));
            if (sortedClay.size() > 0) {
                blocksData.addAll(sortedClay);
            }
        }

        double distance = 9999;

        for (Structs.BlockData block : blocksData) {
            double currentDistance;

            if (oldTarget != null) {
                currentDistance = oldTarget.getPos().distanceSq(block.getPos());
            } else {

                if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null) {
                    currentDistance = mc.objectMouseOver.getBlockPos().distanceSq(block.getPos());
                } else {
                    currentDistance = BlockUtils.getPlayerLoc().distanceSq(block.getPos());
                }
            }

            if (currentDistance < distance) {
                distance = currentDistance;
                blockPos = block;
            }
        }

        return blockPos;
    }


    private double CompareDistance(Structs.BlockData blockData) {
        if (oldTarget != null) {
            return oldTarget.getPos().distanceSq(blockData.getPos());
        } else {

            if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null) {
                return mc.objectMouseOver.getBlockPos().distanceSq(blockData.getPos());
            } else {
                return BlockUtils.getPlayerLoc().distanceSq(blockData.getPos());
            }
        }
    }

    private boolean isTitanium(BlockPos pos) {
        IBlockState state = this.mc.theWorld.getBlockState(pos);
        return (state.getBlock() == Blocks.stone && (state.getValue(BlockStone.VARIANT)).equals(BlockStone.EnumType.DIORITE_SMOOTH));
    }

    private boolean BlockMatchConfig(BlockPos blockPos) {
        IBlockState state = this.mc.theWorld.getBlockState(blockPos);
        if (isTitanium(blockPos))
            return May2BeezQoL.config.prioTitanium;

        if (Blocks.stained_hardened_clay.equals(state.getBlock())) {
            return May2BeezQoL.config.filterClay;
        }

        if (Blocks.prismarine.equals(state.getBlock())) {
            return May2BeezQoL.config.filterPrismarine;
        }

        if (Blocks.wool.equals(state.getBlock()) && state.getValue(BlockColored.COLOR) == EnumDyeColor.LIGHT_BLUE) {
            return May2BeezQoL.config.filterBlueWool;
        }

        if (Blocks.wool.equals(state.getBlock()) && state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY) {
            return May2BeezQoL.config.filterGrayWool;
        }

        return false;
    }

    @SubscribeEvent
    public void onWorldLastRender(RenderWorldLastEvent event) {
        if (!isToggled()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (target != null) {
            RenderUtils.drawBlockBox(target.getPos(), new Color(0, 255, 0, 100), 2.5f);
            RenderUtils.miniBlockBox(target.getRandomVisibilityLine(), new Color(0, 255, 247, 166), 1.5f);
        }
    }
}
