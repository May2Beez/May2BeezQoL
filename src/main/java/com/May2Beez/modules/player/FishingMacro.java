package com.May2Beez.modules.player;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.Module;
import com.May2Beez.utils.*;
import com.May2Beez.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.May2Beez.utils.SkyblockUtils.click;
import static com.May2Beez.utils.SkyblockUtils.rightClick;

public class FishingMacro extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    private static final List<String> fishingMobs = JsonUtils.getListFromUrl("https://gist.githubusercontent.com/Apfelmus1337/da641d3805bddf800eef170cbb0068ec/raw", "mobs");

    private final Timer throwTimer = new Timer();
    private final Timer inWaterTimer = new Timer();

    private final Timer attackDelay = new Timer();

    private double oldBobberPosY = 0.0D;
    private int ticks = 0;
    private Entity target = null;
    private EntityArmorStand targetStand = null;
    private RotationUtils.Rotation startRotation = null;

    private static final List<ParticleEntry> particles = new ArrayList<>();
    private boolean killing = false;

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
        ticks = 0;
        oldBobberPosY = 0.0D;
        target = null;
        killing = true;
        particles.clear();
        startRotation = new RotationUtils.Rotation(mc.thePlayer.rotationPitch, mc.thePlayer.rotationYaw);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), May2BeezQoL.config.sneakWhileFishing);

    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (May2BeezQoL.config.sneakWhileFishing) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }
        stopMovement();
        RotationUtils.reset();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !isToggled()) return;

        ItemStack heldItem;
        if (mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat))
            return;

        stopMovement();

        if (May2BeezQoL.config.antiAfk && ++ticks > (30 + new Random().nextInt(30))) {
            ticks = 0;

            if (RotationUtils.done) {
                switch (antiAfkState) {
                    case AWAY: {
                        RotationUtils.smoothLook(new RotationUtils.Rotation(startRotation.pitch + (-2 + new Random().nextInt(4)), startRotation.yaw + (-2 + new Random().nextInt(4))), 5);
                        antiAfkState = AntiAfkState.BACK;
                        break;
                    }
                    case BACK: {
                        RotationUtils.smoothLook(startRotation, 5);
                        antiAfkState = AntiAfkState.AWAY;
                        break;
                    }
                }
            }
        }

        particles.removeIf(p -> (System.currentTimeMillis() - p.timeAdded) > 1000);

        if (target != null) {
            if (SkyblockUtils.getMobHp(targetStand) <= 0) {
                target = null;
                mc.thePlayer.inventory.currentItem = May2BeezQoL.config.rodSlot - 1;
            }
        }

        if (target == null && killing) {
            RotationUtils.smoothLook(startRotation, 5);
            killing = false;
        }


        if (May2BeezQoL.config.sneakWhileFishing) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
        }

        if (May2BeezQoL.config.prioritizeSCs) {
            findAndSetCurrentSeaCreature();
            if (target != null) {
                throwTimer.reset();
            }
        } else if (targetStand != null && SkyblockUtils.getMobHp(targetStand) <= 0) {
            targetStand = null;
            target = null;
            mc.thePlayer.inventory.currentItem = May2BeezQoL.config.rodSlot - 1;
        }

        switch (currentState) {
            case THROWING: {
                if (mc.thePlayer.fishEntity == null && throwTimer.hasReached(250)) {
                    mc.thePlayer.inventory.currentItem = May2BeezQoL.config.rodSlot - 1;
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
                            if (!May2BeezQoL.config.prioritizeSCs) {
                                findAndSetCurrentSeaCreature();
                            }
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
                mc.thePlayer.inventory.currentItem = May2BeezQoL.config.rodSlot - 1;
                break;
            }
            case FISH_BITE: {
                mc.thePlayer.inventory.currentItem = May2BeezQoL.config.rodSlot - 1;
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                RotationUtils.smoothLook(startRotation, 5);
                throwTimer.reset();
                currentState = AutoFishState.THROWING;
                break;
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(RenderWorldLastEvent event) {
        if (!isToggled()) return;

        if (target != null) {
            RenderUtils.drawEntityBox(target, Color.orange, 2, event.partialTicks);
        }
        if (mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat))
            return;

        if (target != null && attackDelay.hasReached(150)) {
            if (!RotationUtils.done) return;

            mc.thePlayer.inventory.currentItem = May2BeezQoL.config.weaponSlot - 1;
            switch (May2BeezQoL.config.weaponAttackMode) {
                case 0: {
                    click();
                    break;
                }
                case 1: {
                    rightClick();
                    break;
                }
            }
            attackDelay.reset();
        }
    }

    public static void handleParticles(S2APacketParticles packet) {
        if (packet.getParticleType() == EnumParticleTypes.WATER_WAKE || packet.getParticleType() == EnumParticleTypes.SMOKE_NORMAL) {
            particles.add(new ParticleEntry(new Vec3(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate()), System.currentTimeMillis()));
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

    private void findAndSetCurrentSeaCreature() {
        int ranga = May2BeezQoL.config.scScanRange;
        List<Entity> mobs = mc.theWorld.getEntitiesInAABBexcluding(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().expand(ranga, (ranga >> 1), ranga), e -> e instanceof EntityArmorStand);
        Optional<Entity> filtered = mobs.stream().filter(v -> (v.getDistanceToEntity(mc.thePlayer) < ranga && !v.getName().contains(mc.thePlayer.getName()) && fishingMobs.stream().anyMatch(f -> f.contains(v.getCustomNameTag())))).min(Comparator.comparing(v -> v.getDistanceToEntity((Entity) mc.thePlayer)));
        if (filtered.isPresent()) {
            targetStand = (EntityArmorStand)filtered.get();
            target = SkyblockUtils.getEntityCuttingOtherEntity(targetStand, null);
            if (target != null && SkyblockUtils.getMobHp(targetStand) > 0) {
                killing = true;

                if (May2BeezQoL.config.lookDownWhenAttacking) {
                    RotationUtils.smoothLook(new RotationUtils.Rotation(0, mc.thePlayer.rotationYaw), 150);
                } else {
                    RotationUtils.smoothLook(RotationUtils.getRotation(target), 150);
                }

            } else if (SkyblockUtils.getMobHp(targetStand) <= 0) {
                targetStand = null;
                target = null;
                mc.thePlayer.inventory.currentItem = May2BeezQoL.config.rodSlot - 1;
            }
        }
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