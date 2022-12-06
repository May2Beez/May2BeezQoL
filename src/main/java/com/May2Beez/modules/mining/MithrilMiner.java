package com.May2Beez.modules.mining;

import com.May2Beez.Module;
import com.May2Beez.SkyblockMod;
import com.May2Beez.utils.BlockUtils;
import com.May2Beez.utils.RenderUtils;
import com.May2Beez.utils.RotationUtils;
import com.May2Beez.utils.SkyblockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStone;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemMap;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

public class MithrilMiner extends Module {

    private Minecraft mc = Minecraft.getMinecraft();
    private BlockPos target = null;
    private Vec3 targetRotation = null;
    private int ticksSeen = 0;
    private int ticksTargeting = 0;
    private int ticksMining = 0;
    private int ticks = 0;
    private int pause = 0;

    private Float yaw = null;
    private Float pitch = null;
    private boolean stopLoop = false;

    private boolean movingCursor = false;
    private static volatile boolean waitTime = false;
    private int lastKey = -1;
    private int timeLeft = 0;

    private final String[] modes = {"Wool", "Clay", "Prismarine", "Gold", "Blue"};

    public MithrilMiner() {
        super("Mithril Miner", new KeyBinding("Mithril Miner", Keyboard.KEY_I, SkyblockMod.MODID + " - Mining"));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.movingCursor = false;
        this.target = null;
        this.targetRotation = null;
        this.ticksSeen = 0;
        this.ticksMining = 0;
        this.ticksTargeting = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        KeyBinding.setKeyBindState((Minecraft.getMinecraft()).gameSettings.keyBindAttack.getKeyCode(), false);
        KeyBinding.setKeyBindState((Minecraft.getMinecraft()).gameSettings.keyBindSneak.getKeyCode(), false);
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!isToggled()) return;

        if (this.mc == null) {
            this.mc = Minecraft.getMinecraft();
        }

        if (this.target != null)
            RenderUtils.blockBox(this.target, Color.CYAN);
        if (this.targetRotation != null)
            RenderUtils.miniBlockBox(this.targetRotation, Color.GREEN);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        this.pause--;

        if (MithrilMiner.waitTime) return;

        if (isToggled() && !(this.mc.currentScreen instanceof GuiContainer) && !(this.mc.currentScreen instanceof GuiEditSign) && this.pause < 1) {
            this.ticks++;
            if (this.mc.thePlayer != null && this.mc.thePlayer.getHeldItem() != null && this.mc.thePlayer.getHeldItem().getItem() instanceof ItemMap) {
                setToggled(false);
            }
            if (this.mc.theWorld != null) {

                if (this.timeLeft-- <= 0) {
                    int[] keybinds = { this.mc.gameSettings.keyBindForward.getKeyCode(), this.mc.gameSettings.keyBindLeft.getKeyCode(), this.mc.gameSettings.keyBindBack.getKeyCode(), this.mc.gameSettings.keyBindRight.getKeyCode(), this.mc.gameSettings.keyBindLeft.getKeyCode(), this.mc.gameSettings.keyBindBack.getKeyCode(), this.mc.gameSettings.keyBindRight.getKeyCode(), this.mc.gameSettings.keyBindBack.getKeyCode(), this.mc.gameSettings.keyBindBack.getKeyCode() };
                    if (this.lastKey != -1) {
                        KeyBinding.setKeyBindState(this.lastKey, false);
                        KeyBinding.setKeyBindState((Minecraft.getMinecraft()).gameSettings.keyBindSneak.getKeyCode(), SkyblockMod.config.sneak);
                    }
                    if ((new Random()).nextFloat() < SkyblockMod.config.walking / 100.0D) {
                        this.lastKey = keybinds[(new Random()).nextInt(keybinds.length)];
                        KeyBinding.setKeyBindState((Minecraft.getMinecraft()).gameSettings.keyBindSneak.getKeyCode(), true);
                        KeyBinding.setKeyBindState(this.lastKey, true);
                        this.timeLeft = (int)SkyblockMod.config.walkingTime;
                    }
                } else {
                    KeyBinding.setKeyBindState(this.lastKey, true);
                    KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);
                }

                if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    Entity entity = this.mc.objectMouseOver.entityHit;
                    if (entity instanceof EntityPlayer) {
                        click();
                        this.pause = 5;
                        return;
                    }
                }
                if (this.mc.theWorld.playerEntities.stream().anyMatch(playerEntity -> (!playerEntity.equals(this.mc.thePlayer) && playerEntity instanceof EntityOtherPlayerMP && playerEntity.getDistanceToEntity((Entity) this.mc.thePlayer) < 10.0F && (!playerEntity.isInvisible() || playerEntity.posY - this.mc.thePlayer.posY <= 5.0D)))) {
                    this.ticksSeen++;
                } else {
                    this.ticksSeen = 0;
                }
                if ((SkyblockMod.config.panic <= this.ticksSeen && SkyblockMod.config.panic != 0.0D)) {
                    setToggled(false);
                    this.ticksSeen = 0;
                    BlockPos location = this.mc.thePlayer.getPosition();
                    this.mc.theWorld.playSound(location.getX(), location.getY(), location.getZ(), "may2beez:alarm", 1.0f, 1.0f, false);
                    SkyblockUtils.SendInfo("You have been seen by " + ((EntityPlayer) this.mc.theWorld.playerEntities.stream().filter(playerEntity -> (!playerEntity.equals(this.mc.thePlayer) && playerEntity.getDistanceToEntity((Entity) this.mc.thePlayer) < 10.0F)).findFirst().get()).getName(), false, getName());
                    onDisable();
                    return;
                }
                if (this.target == null) {
                    System.out.println("1");
                    if (!findTarget()) {
                        SkyblockUtils.SendInfo("No possible target found", false, getName());
                        WaitSecond();
                    }
                    return;
                }

                if (SkyblockMod.config.sneak || this.timeLeft != 0)
                    KeyBinding.setKeyBindState((Minecraft.getMinecraft()).gameSettings.keyBindSneak.getKeyCode(), true);

                if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    if (this.ticksTargeting++ == 40) {
                        setToggled(false);
                        return;
                    }
                } else {
                    this.ticksTargeting = 0;
                }

                if (this.mc.theWorld.getBlockState(this.target).getBlock().equals(Blocks.bedrock) || this.mc.theWorld.getBlockState(this.target).getBlock().equals(Blocks.air)) {
                    System.out.println("2");
                    findTarget();
                    return;
                }

                useMiningSpeedBoost();

                KeyBinding.setKeyBindState((Minecraft.getMinecraft()).gameSettings.keyBindAttack.getKeyCode(), true);
                if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                        this.mc.currentScreen != null && !(this.mc.currentScreen instanceof GuiContainer) && this.ticks % 2 == 0)
                    click();
                if (this.yaw != null) {
                    this.movingCursor = true;

                    RotationUtils.smoothLook(RotationUtils.vec3ToRotation(targetRotation), SkyblockMod.config.cameraSpeed);

                    this.yaw = RotationUtils.yawDifference;
                    this.pitch = RotationUtils.pitchDifference;

                    if (!RotationUtils.running) {
                        this.movingCursor = false;
                    }
                    getRotations(false);
                    if (this.stopLoop)
                        return;
                }


                if (this.mc.theWorld.getBlockState(this.target).getBlock().equals(Blocks.bedrock)) {
                    if (!findTarget()) {
                        WaitSecond();
                    }
                    return;
                }

                if (this.movingCursor) return;

                if (this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    BlockPos pos = this.mc.objectMouseOver.getBlockPos();
                    if (!pos.equals(this.target)) {
                        if (!findTarget()) {
                            WaitSecond();
                        }
                        return;
                    }
                } else {
                    if (!findTarget()) {
                        WaitSecond();
                    }
                    return;
                }
                if (this.ticksMining++ == SkyblockMod.config.maxBreakTime) {
                    SkyblockUtils.SendInfo("Mining one block took too long", false, getName());
                    findTarget();
                }
            }
        }
    }

    private void WaitSecond() {
        MithrilMiner.waitTime = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                }catch (Exception ignored) {}
                MithrilMiner.waitTime = false;
            }
        }).start();
    }

    private boolean findTarget() {


        ArrayList<BlockPos> blocks = new ArrayList<>();

        for (int x = -5; x < 6; x++) {
            for (int y = -5; y < 3; y++) {
                for (int z = -5; z < 6; z++)
                    blocks.add(new BlockPos(this.mc.thePlayer.getPosition().getX() + x, this.mc.thePlayer.getPosition().getY() + y, this.mc.thePlayer.getPosition().getZ() + z));
            }
        }

        BlockPos sortingCenter = (this.target != null) ? this.target : this.mc.thePlayer.getPosition();
        Optional<BlockPos> any = blocks.stream().filter(pos -> !pos.equals(this.target)).filter(this::matchesMode).filter(pos -> {
            System.out.println(pos);
            return
                    (this.mc.thePlayer.getDistance(pos.getX(), (pos.getY() - this.mc.thePlayer.getEyeHeight()), pos.getZ()) < 5.5D);
        }).filter(BlockUtils::isBlockVisible).min(Comparator.comparingDouble(pos -> (isTitanium(pos) && SkyblockMod.config.prioTitanium) ? 0.0D : getDistance(pos, sortingCenter)));
        if (any.isPresent()) {
            this.target = any.get();
            this.targetRotation = BlockUtils.getRandomVisibilityLine(any.get());
            getRotations(true);
        } else {
            any = blocks.stream().filter(pos -> !pos.equals(this.target)).filter(this::matchesAny).filter(pos -> (this.mc.thePlayer.getDistance(pos.getX(), (pos.getY() - this.mc.thePlayer.getEyeHeight()), pos.getZ()) < 5.5D)).filter(BlockUtils::isBlockVisible).min(Comparator.comparingDouble(pos -> (isTitanium(pos) && SkyblockMod.config.prioTitanium) ? 0.0D : getDistance(pos, sortingCenter)));
            if (any.isPresent()) {
                this.target = any.get();
                this.targetRotation = BlockUtils.getRandomVisibilityLine(any.get());
                getRotations(true);
            }
        }
        this.ticksMining = 0;
        return any.isPresent();
    }

    private void getRotations(boolean stop) {
        if (this.yaw != null) {
            this.yaw = null;
            this.pitch = null;
        }

        if (this.targetRotation == null || Minecraft.getMinecraft().thePlayer == null) return;

        final double diffX = this.targetRotation.xCoord - Minecraft.getMinecraft().thePlayer.posX;
        final double diffY = this.targetRotation.yCoord - (Minecraft.getMinecraft().thePlayer.posY + Minecraft.getMinecraft().thePlayer.eyeHeight);
        final double diffZ = this.targetRotation.zCoord - Minecraft.getMinecraft().thePlayer.posZ;


        float finalYaw = (float)MathHelper.wrapAngleTo180_double(MathHelper.atan2(diffZ, diffX) * (180.0 / Math.PI) - 90.0 - Minecraft.getMinecraft().thePlayer.rotationYaw);
        float finalPitch = (float)MathHelper.wrapAngleTo180_double(-(MathHelper.atan2(diffY, (double)MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ)) * (180.0 / Math.PI)) - Minecraft.getMinecraft().thePlayer.rotationPitch);


        this.yaw = finalYaw;
        this.pitch = finalPitch;

        this.stopLoop = stop;
    }

    private double getDistance(BlockPos pos1, BlockPos pos2) {
        double deltaX = (pos1.getX() - pos2.getX());
        double deltaY = (pos1.getY() - pos2.getY()) * 0.6;
        double deltaZ = (pos1.getZ() - pos2.getZ());
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    private boolean isTitanium(BlockPos pos) {
        IBlockState state = this.mc.theWorld.getBlockState(pos);
        return (state.getBlock() == Blocks.stone && ((BlockStone.EnumType)state.getValue((IProperty)BlockStone.VARIANT)).equals(BlockStone.EnumType.DIORITE_SMOOTH));
    }

    private boolean matchesMode(BlockPos pos) {
        IBlockState state = this.mc.theWorld.getBlockState(pos);
        if (isTitanium(pos))
            return true;
        switch (modes[SkyblockMod.config.mode]) {
            case "Clay":
                return (state.getBlock().equals(Blocks.stained_hardened_clay) || (state.getBlock().equals(Blocks.wool) && ((EnumDyeColor)state.getValue((IProperty)BlockColored.COLOR)).equals(EnumDyeColor.GRAY)));
            case "Prismarine":
                return state.getBlock().equals(Blocks.prismarine);
            case "Wool":
                return (state.getBlock().equals(Blocks.wool) && ((EnumDyeColor)state.getValue((IProperty)BlockColored.COLOR)).equals(EnumDyeColor.LIGHT_BLUE));
            case "Blue":
                return ((state.getBlock().equals(Blocks.wool) && ((EnumDyeColor)state.getValue((IProperty)BlockColored.COLOR)).equals(EnumDyeColor.LIGHT_BLUE)) || state.getBlock().equals(Blocks.prismarine));
            case "Gold":
                return state.getBlock().equals(Blocks.gold_block);
        }
        return false;
    }

    private boolean matchesAny(BlockPos pos) {
        IBlockState state = this.mc.theWorld.getBlockState(pos);
        return ((state.getBlock().equals(Blocks.wool) && state.getProperties().entrySet().stream().anyMatch(entry -> entry.toString().contains("lightBlue"))) || state
                .getBlock().equals(Blocks.prismarine) || state
                .getBlock().equals(Blocks.stained_hardened_clay) || (state.getBlock().equals(Blocks.wool) && state.getProperties().entrySet().stream().anyMatch(entry -> entry.toString().contains("gray"))) ||
                isTitanium(pos));
    }

    public void click() {
        try {
            Method clickMouse;
            try {
                clickMouse = Minecraft.class.getDeclaredMethod("func_147116_af", new Class[0]);
            } catch (NoSuchMethodException e) {
                clickMouse = Minecraft.class.getDeclaredMethod("clickMouse", new Class[0]);
            }
            clickMouse.setAccessible(true);
            clickMouse.invoke(Minecraft.getMinecraft(), new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
