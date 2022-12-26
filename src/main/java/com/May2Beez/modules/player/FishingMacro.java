package com.May2Beez.modules.player;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.Module;
import com.May2Beez.events.SpawnParticleEvent;
import com.May2Beez.modules.combat.MobKiller;
import com.May2Beez.utils.*;
import com.May2Beez.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FishingMacro extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    private static final List<String> fishingMobs = JsonUtils.getListFromUrl("https://gist.githubusercontent.com/Apfelmus1337/da641d3805bddf800eef170cbb0068ec/raw", "mobs");

    private final Timer throwTimer = new Timer();
    private final Timer inWaterTimer = new Timer();

    private final Timer attackDelay = new Timer();

    private final Timer antiAfkTimer = new Timer();

    private double oldBobberPosY = 0.0D;
    private RotationUtils.Rotation startRotation = null;

    private static final CopyOnWriteArrayList<ParticleEntry> particles = new CopyOnWriteArrayList<>();
    private boolean killing = false;

    private int rodSlot = 0;

    private enum AutoFishState {
        THROWING,
        IN_WATER,
        FISH_BITE
    }

    private enum AntiAfkState {
        AWAY,
        BACK
    }

    private AntiAfkState antiAfkState = AntiAfkState.AWAY;

    private AutoFishState currentState = AutoFishState.THROWING;

    public FishingMacro() {
        super("Fishing Macro", new KeyBinding("Fishing Macro", Keyboard.KEY_NONE, May2BeezQoL.MODID + " - Player"));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        currentState = AutoFishState.THROWING;
        throwTimer.reset();
        inWaterTimer.reset();
        attackDelay.reset();
        antiAfkTimer.reset();
        oldBobberPosY = 0.0D;
        killing = true;
        particles.clear();
        rodSlot = SkyblockUtils.findItemInHotbar("Rod");
        if (rodSlot == -1) {
            SkyblockUtils.SendInfo("No rod found in hotbar!");
            this.toggle();
            return;
        }
        startRotation = new RotationUtils.Rotation(mc.thePlayer.rotationPitch, mc.thePlayer.rotationYaw);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), May2BeezQoL.config.sneakWhileFishing);
        MobKiller.Toggle();
        MobKiller.setMobsNames(false, fishingMobs.toArray(new String[0]));
        MobKiller.scanRange = May2BeezQoL.config.scScanRange;
        MobKiller.ShouldScan = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (May2BeezQoL.config.sneakWhileFishing) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }
        stopMovement();
        RotationUtils.reset();
        MobKiller.ShouldScan = false;
        MobKiller.Toggle();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !isToggled()) return;

        ItemStack heldItem;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat))
            return;

        particles.removeIf(p -> (System.currentTimeMillis() - p.timeAdded) > 1000);

        if (MobKiller.hasTarget()) {
            killing = true;
            return;
        }

        if (killing) {
            if (RotationUtils.done)
                RotationUtils.smoothLook(startRotation, 200);

            if (!RotationUtils.IsDiffLowerThan(0.1f))
                return;

            killing = false;
            throwTimer.reset();
        }

        stopMovement();

        if (May2BeezQoL.config.antiAfk && antiAfkTimer.hasReached(3000 + new Random().nextInt(1500))) {
            antiAfkTimer.reset();

            if (RotationUtils.done) {
                switch (antiAfkState) {
                    case AWAY: {
                        RotationUtils.smoothLook(new RotationUtils.Rotation(startRotation.pitch + (-2 + new Random().nextInt(4)), startRotation.yaw + (-2 + new Random().nextInt(4))), 160);
                        antiAfkState = AntiAfkState.BACK;
                        break;
                    }
                    case BACK: {
                        RotationUtils.smoothLook(startRotation, 180);
                        antiAfkState = AntiAfkState.AWAY;
                        break;
                    }
                }
            }
        }


        if (May2BeezQoL.config.sneakWhileFishing) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
        }

        switch (currentState) {
            case THROWING: {
                if (mc.thePlayer.fishEntity == null && throwTimer.hasReached(250) && RotationUtils.done) {
                    mc.thePlayer.inventory.currentItem = rodSlot;
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                    throwTimer.reset();
                    inWaterTimer.reset();
                    currentState = AutoFishState.IN_WATER;
                    break;
                }
                if (throwTimer.hasReached(2500) && mc.thePlayer.fishEntity != null) {
                    currentState = AutoFishState.FISH_BITE;
                }
                break;
            }
            case IN_WATER: {
                heldItem = mc.thePlayer.getHeldItem();
                if (heldItem != null && heldItem.getItem() == Items.fishing_rod) {
                    if (throwTimer.hasReached(500) && mc.thePlayer.fishEntity != null) {
                        if (mc.thePlayer.fishEntity.isInWater() || mc.thePlayer.fishEntity.isInLava()) {
                            EntityFishHook bobber = mc.thePlayer.fishEntity;
                            if (inWaterTimer.hasReached(2500) && Math.abs(bobber.motionX) < 0.01 && Math.abs(bobber.motionZ) < 0.01) {
                                double movement = bobber.posY - oldBobberPosY;
                                oldBobberPosY = bobber.posY;
                                if ((movement < -0.04 && bobberIsNearParticles(bobber)) || bobber.caughtEntity != null) {
                                    currentState = AutoFishState.FISH_BITE;
                                }
                            }
                            break;
                        }
                        if (inWaterTimer.hasReached(2500)) {
                            currentState = AutoFishState.FISH_BITE;
                        }
                        break;
                    }
                    if (throwTimer.hasReached(1000) && mc.thePlayer.fishEntity == null) {
                        throwTimer.reset();
                        currentState = AutoFishState.THROWING;
                    }
                    break;
                }
                mc.thePlayer.inventory.currentItem = rodSlot;
                break;
            }
            case FISH_BITE: {
                mc.thePlayer.inventory.currentItem = rodSlot;
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                RotationUtils.smoothLook(startRotation, 45);
                throwTimer.reset();
                currentState = AutoFishState.THROWING;
                break;
            }
        }
    }

    @SubscribeEvent
    public void handleParticles(SpawnParticleEvent packet) {
        if (packet.getParticleTypes() == EnumParticleTypes.WATER_WAKE || packet.getParticleTypes() == EnumParticleTypes.SMOKE_NORMAL || packet.getParticleTypes() == EnumParticleTypes.FLAME) {
            particles.add(new ParticleEntry(new Vec3(packet.getXCoord(), packet.getYCoord(), packet.getZCoord()), System.currentTimeMillis()));
        }
    }

    public double getHorizontalDistance(Vec3 vec1, Vec3 vec2) {
        double d0 = vec1.xCoord - vec2.xCoord;
        double d2 = vec1.zCoord - vec2.zCoord;
        return MathHelper.sqrt_double(d0 * d0 + d2 * d2);
    }

    private boolean bobberIsNearParticles(EntityFishHook bobber) {
        return particles.stream().anyMatch(v -> (getHorizontalDistance(bobber.getPositionVector(), v.position) < 0.2D));
    }

    public void stopMovement() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
    }

    private static class ParticleEntry {
        public Vec3 position;

        public long timeAdded;

        public ParticleEntry(Vec3 position, long timeAdded) {
            this.position = position;
            this.timeAdded = timeAdded;
        }
    }
}