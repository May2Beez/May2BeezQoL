package com.May2Beez.modules.farming;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.modules.Module;
import com.May2Beez.utils.*;
import com.May2Beez.utils.Timer;
import com.google.common.base.Splitter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForagingMacro extends Module {
    public static Minecraft mc = Minecraft.getMinecraft();

    public ForagingMacro() {
        super("Foraging Macro", new KeyBinding("Foraging Macro", Keyboard.KEY_NONE, "May2BeezQoL - Farming"));
    }

    private enum MacroState {
        LOOK, PLACE, PLACE_BONE, BREAK, FIND_ROD, FIND_BONE, THROW_ROD, THROW_BREAK_DELAY, SWITCH
    }

    private static MacroState macroState = MacroState.LOOK;
    private static MacroState lastState = null;

    public static Vec3 bestDirt;

    public static boolean running = false;

    private final Timer stuckTimer = new Timer();
    private boolean stuck = false;

    public static boolean isRunning() {
        return running;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        running = true;
        bestDirt = null;
        macroState = MacroState.LOOK;
        startedAt = System.currentTimeMillis();
        earnedXp = 0;
        stuckTimer.reset();
        stuck = false;
        updateXpTimer.reset();
        if (May2BeezQoL.config.mouseUngrab)
            UngrabUtils.ungrabMouse();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode(), false);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode(), false);
        running = false;
        UngrabUtils.regrabMouse();
    }

    private static final Timer updateXpTimer = new Timer();
    private static final Timer waitTimer = new Timer();
    private static final Timer waitAfterFinishTimer = new Timer();
    private static double xpPerHour = 0;

    public static String[] drawFunction() {
        String[] textToDraw = new String[4];
        if (updateXpTimer.hasReached(100)) {
            xpPerHour = earnedXp / ((System.currentTimeMillis() - startedAt) / 3600000.0);
            updateXpTimer.reset();
        }
        textToDraw[0] = "§r§lForaging Macro";
        textToDraw[1] = "§r§lState: §f" + macroState;
        textToDraw[2] = "§r§lXP/H: §f" + String.format("%.2f", xpPerHour);
        textToDraw[3] = "§r§lXP Since start: §f" + String.format("%.2f", earnedXp);
        return textToDraw;
    }

    private static long startedAt = 0;
    private static double earnedXp = 0;

    private static final Splitter SPACE_SPLITTER = Splitter.on("  ").omitEmptyStrings().trimResults();
    private static final Pattern SKILL_PATTERN = Pattern.compile("\\+([\\d.]+)\\s+([A-Za-z]+)\\s+\\((\\d+(\\.\\d+)?)%\\)");

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onClientChatReceivedEvent(ClientChatReceivedEvent event) {
        if (!isToggled()) return;
        if (event.type != 2) return;

        String actionBar = StringUtils.stripControlCodes(event.message.getUnformattedText());

        List<String> components = SPACE_SPLITTER.splitToList(actionBar);

        for (String component : components) {
            Matcher matcher = SKILL_PATTERN.matcher(component);
            System.out.println(component);
            if (matcher.matches()) {
                String addedXp = matcher.group(1);
                String skillName = matcher.group(2);
                String percentage = matcher.group(3);
                if (skillName.equalsIgnoreCase("foraging")) {
                    earnedXp += Double.parseDouble(addedXp) * 6.5;
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isToggled()) return;

        if (bestDirt != null) {
            RenderUtils.miniBlockBox(new Vec3(bestDirt.xCoord, bestDirt.yCoord, bestDirt.zCoord), Color.green, 2f);
        }
    }

    private Vec3 getDirt() {
        Vec3 furthest = null;
        Vec3 player = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ);
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(mc.thePlayer.posX + 4.0D, mc.thePlayer.posY, mc.thePlayer.posZ + 4.0D), new BlockPos(mc.thePlayer.posX - 4.0D, mc.thePlayer.posY - 1.0D, mc.thePlayer.posZ - 4.0D))) {
            if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.dirt || mc.theWorld.getBlockState(pos).getBlock() == Blocks.grass) {
                Block block = mc.theWorld.getBlockState(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ())).getBlock();
                if (!(block instanceof net.minecraft.block.BlockLog) && block != Blocks.sapling) {
                    Vec3 distance = new Vec3(pos.getX() + 0.5D, (pos.getY() + 1), pos.getZ() + 0.5D);
                    if (furthest == null || player.squareDistanceTo(distance) > player.squareDistanceTo(furthest))
                        furthest = distance;
                }
            }
        }
        return furthest;
    }

    private void unstuck() {
        LogUtils.addMessage("I'm stuck! Unstuck process activated", EnumChatFormatting.RED);
        stuck = true;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(!isToggled()) {
            macroState = MacroState.LOOK;
            return;
        }

        if (!RotationUtils.done) return;

        if (stuck) {
            Vec3 closest = null;
            Vec3 player = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ);
            for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(mc.thePlayer.posX + 4.0D, mc.thePlayer.posY, mc.thePlayer.posZ + 4.0D), new BlockPos(mc.thePlayer.posX - 4.0D, mc.thePlayer.posY - 1.0D, mc.thePlayer.posZ - 4.0D))) {
                if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.dirt || mc.theWorld.getBlockState(pos).getBlock() == Blocks.grass) {
                    Block block = mc.theWorld.getBlockState(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ())).getBlock();
                    if ((block instanceof net.minecraft.block.BlockLog) || block == Blocks.sapling) {
                        Vec3 distance = new Vec3(pos.getX() + 0.5D, (pos.getY() + 1), pos.getZ() + 0.5D);
                        if (closest == null || player.squareDistanceTo(distance) <= player.squareDistanceTo(closest))
                            closest = distance;
                    }
                }
            }
            int treecapitator = InventoryUtils.findItemInHotbar("Treecapitator");
            if (treecapitator == -1) {
                LogUtils.addMessage("No Treecapitator found in hotbar!", EnumChatFormatting.RED);
                toggle();
                return;
            }

            mc.thePlayer.inventory.currentItem = treecapitator;

            MovingObjectPosition mop = mc.objectMouseOver;

            boolean shouldBreak = mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && !mc.theWorld.getBlockState(mop.getBlockPos()).getBlock().equals(Blocks.dirt);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), shouldBreak);

            if(closest != null) {
                RotationUtils.smoothLook(RotationUtils.getRotation(closest), 125);
            } else {
                stuck = false;
                macroState = MacroState.LOOK;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                stuckTimer.reset();
            }
            return;
        }

        if (stuckTimer.hasReached(May2BeezQoL.config.stuckTimeout)) {
            unstuck();
            return;
        }

        switch (macroState) {
            case LOOK:
                int saplingSlot = InventoryUtils.findItemInHotbar("Sapling");
                if (saplingSlot == -1) {
                    LogUtils.addMessage("No saplings found in hotbar!", EnumChatFormatting.RED);
                    toggle();
                    return;
                }
                mc.thePlayer.inventory.currentItem = saplingSlot;
                bestDirt = getDirt();
                if(bestDirt != null) {
                    RotationUtils.smoothLook(RotationUtils.getRotation(bestDirt), 150 + (new Random().nextInt(50)));
                    macroState = MacroState.PLACE;
                } else {
                    macroState = MacroState.FIND_BONE;
                }
                return;
            case PLACE:
                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                macroState = MacroState.LOOK;
                return;
            case FIND_BONE:
                int boneMeal = InventoryUtils.findItemInHotbar("Bone Meal");
                if (boneMeal == -1) {
                    LogUtils.addMessage("No Bone Meal found in hotbar!", EnumChatFormatting.RED);
                    toggle();
                    return;
                }
                mc.thePlayer.inventory.currentItem = boneMeal;
                macroState = MacroState.PLACE_BONE;
                waitTimer.reset();
                break;
            case PLACE_BONE:
                if(waitTimer.hasReached(May2BeezQoL.config.foragingDelay)) {
                    MovingObjectPosition mop = mc.objectMouseOver;
                    if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.theWorld.getBlockState(mop.getBlockPos()).getBlock().equals(Blocks.sapling)) {
                        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                    }
                    waitTimer.reset();
                    if(May2BeezQoL.config.foragingUseRod) {
                        macroState = MacroState.FIND_ROD;
                    } else {
                        macroState = MacroState.THROW_BREAK_DELAY;
                    }
                }
                break;
            case FIND_ROD:
                if(waitTimer.hasReached(May2BeezQoL.config.foragingDelay)) {
                    int rod = InventoryUtils.findItemInHotbar("Rod");
                    if (rod == -1) {
                        LogUtils.addMessage("No Fishing Rod found in hotbar!", EnumChatFormatting.RED);
                        toggle();
                        break;
                    }
                    mc.thePlayer.inventory.currentItem = rod;
                    waitTimer.reset();
                    macroState = MacroState.THROW_ROD;
                }
                break;
            case THROW_ROD:
                if(waitTimer.hasReached(May2BeezQoL.config.foragingDelay)) {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                    waitTimer.reset();
                    macroState = MacroState.THROW_BREAK_DELAY;
                }
                break;
            case THROW_BREAK_DELAY:
                if(waitTimer.hasReached(May2BeezQoL.config.foragingDelay)) {
                    waitTimer.reset();
                    macroState = MacroState.BREAK;
                }
                break;
            case BREAK:
                int treecapitator = InventoryUtils.findItemInHotbar("Treecapitator");
                if (treecapitator == -1) {
                    LogUtils.addMessage("No Treecapitator found in hotbar!", EnumChatFormatting.RED);
                    toggle();
                    break;
                }
                mc.thePlayer.inventory.currentItem = treecapitator;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                BlockPos logPos = mc.objectMouseOver.getBlockPos();
                if(logPos != null && !(mc.theWorld.getBlockState(logPos).getBlock() instanceof BlockLog)) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    waitAfterFinishTimer.reset();
                    macroState = MacroState.SWITCH;
                }
                break;
            case SWITCH:
                if(waitAfterFinishTimer.hasReached(May2BeezQoL.config.foragingWaitAfter)) {
                    macroState = MacroState.LOOK;
                }
                break;
        }

        if (lastState != macroState) {
            lastState = macroState;
            stuckTimer.reset();
        }
    }

}
