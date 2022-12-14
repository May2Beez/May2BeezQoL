package com.May2Beez.modules.farming;

import com.May2Beez.Module;
import com.May2Beez.SkyblockMod;
import com.May2Beez.utils.RenderUtils;
import com.May2Beez.utils.RotationUtils;
import com.May2Beez.utils.SkyblockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.May2Beez.utils.SkyblockUtils.*;

public class ForagingMacro extends Module {
    public static ArrayList<Vec3> targets = null;
    public static int placedSaplings = 0;
    public static int waiting = 0;
    public static int waitTicks = 0;
    private Thread thread = null;
    private static long lastAxeUse = 0;
    private static long axeCooldown = 0;
    private static STATES state = STATES.PLANTING;
    public ForagingMacro() {
        super("Foraging Macro", new KeyBinding("Foraging Macro", Keyboard.KEY_SECTION, SkyblockMod.MODID + " - Farming"));
    }
    private int idleTicks = 0;

    private enum STATES {
        PLANTING,
        BONE_MEAL,
        CHOPPING,
        ROD
    }

    @Override
    public void onEnable() {
        super.onEnable();
        placedSaplings = 0;
        targets = null;
        waitTicks = 0;
        waiting = 0;
        state = STATES.PLANTING;
        axeCooldown = (long) Math.floor(2000 - (2000 * (Integer.parseInt(SkyblockMod.config.monkeyLVL) * 0.5 / 100)));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
    }

    @SubscribeEvent
    public void onGameRenderEvent(TickEvent.RenderTickEvent event) {
        if (!isToggled()) return;
        String[] textToDraw = new String[3];
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();
        long timeToNextAxe = axeCooldown - (System.currentTimeMillis() - lastAxeUse);
        textToDraw[0] = "??lState: ??r" + state;
        textToDraw[1] = "??lIdle time: ??r" + idleTicks;
        textToDraw[2] = "??lAxe ready in: ??r" + (timeToNextAxe > 0 ? String.format("%.2f", ((double) timeToNextAxe / 1000)) + "s" : "READY");
        RenderUtils.renderBoxedText(textToDraw, width - 130, height - 80, 1.0, Color.lightGray.getRGB());
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isToggled()) return;

        if (ForagingMacro.targets != null) {
            for (Vec3 target : targets)
                RenderUtils.miniBlockBox(new Vec3(target.xCoord + 0.5f, target.yCoord + 1, target.zCoord + 0.5f), Color.green, 2f);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isToggled() || event.phase == TickEvent.Phase.END) return;

        if (thread == null || !thread.isAlive()) {
            thread = new Thread(() -> {
                try {
                    int sapling = findItemInHotbar("Jungle Sapling");
                    int bonemeal = findItemInHotbar("Bone Meal");
                    int treecap = findItemInHotbar("Treecap");
                    int rod = findItemInHotbar("Rod");
                    if (sapling == -1) {
                        SkyblockUtils.SendInfo("??cNo saplings in hotbar", false, getName());
                    }
                    if (bonemeal == -1) {
                        SkyblockUtils.SendInfo("??cNo bonemeal in hotbar", false, getName());
                    }
                    if (treecap == -1) {
                        SkyblockUtils.SendInfo("??cNo Treecapitator in hotbar", false, getName());
                    }
                    if (rod == -1) {
                        SkyblockUtils.SendInfo("??cNo Fishing Rod in hotbar", false, getName());
                    }
                    if (sapling == -1 || bonemeal == -1 || treecap == -1 || rod == -1) {
                        playAlert();
                        toggle();
                        SkyblockUtils.SendInfo("??cForaging Alert Deactivated", false, getName());
                        return;
                    }
                    switch (state) {
                        case PLANTING: {
                            if (targets == null)
                                targets = getAllDirts(true);
                            if (targets.size() != 4) {
                                Thread.sleep(new Random().nextInt(50) + SkyblockMod.config.foragingDelay);
                                if (getAllDirts(false).size() != 4) {
                                    SkyblockUtils.SendInfo("??cThere is no exactly 4 dirts around you", false, getName());
                                    targets = null;
                                    return;
                                }
                                SkyblockUtils.SendInfo("??cThere are probably logs above dirts", false, getName());
                                Thread.sleep(new Random().nextInt(50) + SkyblockMod.config.foragingDelay);
                                RemoveLogs();
                                targets = null;
                                return;
                            } else {
                                RotationUtils.smoothLook(RotationUtils.vec3ToRotation(new Vec3(ForagingMacro.targets.get(placedSaplings).xCoord + 0.5f, ForagingMacro.targets.get(placedSaplings).yCoord + 1, ForagingMacro.targets.get(placedSaplings).zCoord + 0.5)), SkyblockMod.config.cameraSpeed);

                                if (Minecraft.getMinecraft().objectMouseOver != null && Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && Objects.equals(Minecraft.getMinecraft().objectMouseOver.getBlockPos(), new BlockPos(ForagingMacro.targets.get(placedSaplings)))) {
                                    Minecraft.getMinecraft().thePlayer.inventory.currentItem = sapling;
                                    rightClick();

                                    if (placedSaplings >= 3) {
                                        idleTicks = 0;
                                        state = STATES.BONE_MEAL;
                                    } else {
                                        placedSaplings++;
                                    }
                                }
                            }
                            break;
                        }
                        case BONE_MEAL: {
                            Minecraft.getMinecraft().thePlayer.inventory.currentItem = bonemeal;
                            if (Minecraft.getMinecraft().objectMouseOver != null && Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && Minecraft.getMinecraft().theWorld.getBlockState(Minecraft.getMinecraft().objectMouseOver.getBlockPos()).getBlock() == Blocks.sapling) {
                                Thread.sleep(new Random().nextInt(100) + SkyblockMod.config.foragingDelay);
                                rightClick();
                                if (SkyblockMod.config.normalBoneMeal) {
                                    Thread.sleep(new Random().nextInt(50) + 50);
                                    if (Minecraft.getMinecraft().objectMouseOver != null && Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && Minecraft.getMinecraft().theWorld.getBlockState(Minecraft.getMinecraft().objectMouseOver.getBlockPos()).getBlock() == Blocks.log) {
                                        Minecraft.getMinecraft().thePlayer.inventory.currentItem = treecap;
                                        idleTicks = 0;
                                        state = STATES.CHOPPING;
                                    } else {
                                        Thread.sleep(new Random().nextInt(50) + 50);
                                    }
                                } else {
                                    Thread.sleep(new Random().nextInt(50) + SkyblockMod.config.foragingDelay);
                                    Minecraft.getMinecraft().thePlayer.inventory.currentItem = treecap;
                                    idleTicks = 0;
                                    state = STATES.CHOPPING;
                                }
                            }
                            break;
                        }
                        case CHOPPING: {
                            Block block = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(ForagingMacro.targets.get(placedSaplings).xCoord, ForagingMacro.targets.get(placedSaplings).yCoord + 1, ForagingMacro.targets.get(placedSaplings).zCoord)).getBlock();
                            if (block.getMaterial() == Material.wood && ForagingMacro.lastAxeUse == 0) {
                                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), true);
                            }

                            if (ForagingMacro.lastAxeUse != 0) {
                                if (System.currentTimeMillis() - lastAxeUse + SkyblockMod.config.foragingDelay >= axeCooldown) {
                                    ForagingMacro.lastAxeUse = 0;
                                    return;
                                }
                            }

                            if (block == Blocks.air) {
                                Thread.sleep(new Random().nextInt(50) + SkyblockMod.config.foragingDelay);
                                KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
                                ForagingMacro.placedSaplings = 0;
                                ForagingMacro.targets = null;
                                ForagingMacro.lastAxeUse = System.currentTimeMillis();
                                if (SkyblockMod.config.foragingUseRod) {
                                    state = STATES.ROD;
                                } else {
                                    state = STATES.PLANTING;
                                }
                                idleTicks = 0;
                                Thread.sleep(new Random().nextInt(50) + SkyblockMod.config.foragingDelay);
                            }
                            break;
                        }
                        case ROD: {
                            Thread.sleep(new Random().nextInt(50) + SkyblockMod.config.preRodDelay);
                            Minecraft.getMinecraft().thePlayer.inventory.currentItem = rod;
                            Thread.sleep(new Random().nextInt(50) + SkyblockMod.config.foragingDelay);
                            rightClick();
                            Thread.sleep(new Random().nextInt(50) + SkyblockMod.config.foragingDelay);
                            state = STATES.PLANTING;
                            idleTicks = 0;
                            break;
                        }
                    }
                    if (idleTicks++ > SkyblockMod.config.maxIdleTicks) {
                        RemoveLogs();
                        idleTicks = 0;
                        state = STATES.PLANTING;
                    }
                } catch (Exception ignored) {}
            });
            thread.start();
        }
    }

    private void RemoveLogs() throws InterruptedException {
        ArrayList<Vec3> dirts = getAllDirts(false);
        Minecraft.getMinecraft().thePlayer.inventory.currentItem = findItemInHotbar("Treecap");
        for (int y = 1; y < 3; y++) {
            for (Vec3 dirt : dirts) {
                IBlockState blockAbove = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(dirt.xCoord, dirt.yCoord + y, dirt.zCoord));
                if (blockAbove.getBlock() == Blocks.log || blockAbove.getBlock() == Blocks.log2) {
                    RotationUtils.smoothLook(RotationUtils.vec3ToRotation(new Vec3(dirt.xCoord + 0.5, dirt.yCoord + y + 0.5, dirt.zCoord + 0.5)), SkyblockMod.config.cameraSpeed);
                    Thread.sleep(new Random().nextInt(50) + 100);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), true);
                    Thread.sleep(new Random().nextInt(250) + 200);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
                    lastAxeUse = System.currentTimeMillis();
                }
                if (blockAbove.getBlock() == Blocks.sapling) {
                    RotationUtils.smoothLook(RotationUtils.vec3ToRotation(new Vec3(dirt.xCoord + 0.5, dirt.yCoord + y + 0.5, dirt.zCoord + 0.5)), SkyblockMod.config.cameraSpeed);
                    Thread.sleep(new Random().nextInt(50) + 100);
                    click();
                }
            }
        }
        if (SkyblockMod.config.foragingUseRod) {
            Minecraft.getMinecraft().thePlayer.inventory.currentItem = findItemInHotbar("Rod");
            Thread.sleep(new Random().nextInt(50) + SkyblockMod.config.foragingDelay);
            rightClick();
        }
        Thread.sleep(new Random().nextInt(50) + SkyblockMod.config.foragingDelay);
    }
    private ArrayList<Vec3> getAllDirts(boolean onlyAir) {
        int r = 5;
        BlockPos playerPos = Minecraft.getMinecraft().thePlayer.getPosition();
        playerPos.add(0, 1, 0);
        Vec3i vec3i = new Vec3i(r, 0, r);
        ArrayList<Tuple<Double, Vec3>> dirts = new ArrayList<>();
        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i))) {
            IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
            IBlockState blockState2 = Minecraft.getMinecraft().theWorld.getBlockState(blockPos.add(0, 1, 0));
            if ((blockState.getBlock() == Blocks.grass || blockState.getBlock() == Blocks.dirt)) {
                if (onlyAir && blockState2.getBlock() != Blocks.air) {
                    continue;
                }
                Vec3 vec = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                dirts.add(new Tuple<Double, Vec3>(vec.distanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector()), vec));
            }
        }
        dirts.sort(Comparator.comparingDouble(Tuple::getFirst));
        ArrayList<Vec3> newDirts = dirts.stream().map(Tuple::getSecond).collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(newDirts);
        return newDirts;
    }

    private static void playAlert() {
        Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1, 0.5F);
    }

}
