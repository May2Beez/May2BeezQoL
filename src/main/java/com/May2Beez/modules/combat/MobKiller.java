package com.May2Beez.modules.combat;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.Module;
import com.May2Beez.utils.*;
import com.May2Beez.utils.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.May2Beez.utils.SkyblockUtils.leftClick;
import static com.May2Beez.utils.SkyblockUtils.rightClick;

public class MobKiller extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();

    public static Target target;
    public static int scanRange = 20;

    private static boolean caseSensitive = false;
    private static String[] mobsNames = null;

    private final Timer attackDelay = new Timer();
    private final Timer blockedVisionDelay = new Timer();
    public static States currentState = States.SEARCHING;

    public static boolean ShouldScan = false;

    private final CopyOnWriteArrayList<Target> potentialTargets = new CopyOnWriteArrayList<>();

    private static class Target {
        public EntityLiving entity;
        public EntityArmorStand stand;
        public double distance() {
            return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(entity);
        }

        public Target(EntityLiving entity, EntityArmorStand stand) {
            this.entity = entity;
            this.stand = stand;
        }
    }

    private enum States {
        SEARCHING,
        ATTACKING,
        BLOCKED_VISION,
        KILLED
    }

    public MobKiller() {
        super("Mob Killer", new KeyBinding("Attack Mobs", 0, "May2BeezQoL - Combat"));
    }

    public static boolean hasTarget() {
        return target != null;
    }

    @Override
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        super.onKeyInput(event);

        if (this.isToggled()) {
            ShouldScan = true;
        }
    }

    @Override
    public void onEnable() {
        blockedVisionDelay.reset();
        attackDelay.reset();
        currentState = States.SEARCHING;
        target = null;
        potentialTargets.clear();
        String[] names = May2BeezQoL.config.mobsNames.split(",");
        for (int i = 0; i < names.length; i++) {
            names[i] = names[i].trim();
        }
        setMobsNames(false, names);
        super.onEnable();
    }

    public static void Toggle() {
        if (!May2BeezQoL.mobKiller.isToggled()) {
            May2BeezQoL.mobKiller.setToggled(true);
            May2BeezQoL.mobKiller.onEnable();
        } else {
            May2BeezQoL.mobKiller.setToggled(false);
            May2BeezQoL.mobKiller.onDisable();
        }
    }

    @Override
    public void onDisable() {
        target = null;
        potentialTargets.clear();
        RotationUtils.reset();
        ShouldScan = false;
        super.onDisable();
    }

    public static void setMobsNames(boolean caseSensitive, String... mobsNames) {
        MobKiller.caseSensitive = caseSensitive;
        MobKiller.mobsNames = mobsNames;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!isToggled()) return;
        if (!ShouldScan) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mobsNames == null || mobsNames.length == 0) return;

        if (mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat))
            return;

        switch (currentState) {
            case SEARCHING:
                potentialTargets.clear();
                List<Entity> entities = mc.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityArmorStand).filter(entity -> mc.thePlayer.getPositionEyes(1).distanceTo(entity.getPositionVector()) <= scanRange).collect(Collectors.toList());
                List<Entity> filtered = entities.stream().filter(v -> (!v.getName().contains(mc.thePlayer.getName()) && Arrays.stream(mobsNames).anyMatch(mobsName -> {
                    String mobsName1 = StringUtils.stripControlCodes(mobsName);
                    String vName = StringUtils.stripControlCodes(v.getName());
                    String vCustomNameTag = StringUtils.stripControlCodes(v.getCustomNameTag());
                    if (caseSensitive) {
                        return vName.contains(mobsName1) || vCustomNameTag.contains(mobsName1);
                    } else {
                        return vName.toLowerCase().contains(mobsName1.toLowerCase()) || vCustomNameTag.toLowerCase().contains(mobsName1.toLowerCase());
                    }
                }))).collect(Collectors.toList());

                if (filtered.isEmpty())
                    break;

                double distance = 9999;
                Target closestTarget = null;

                for (Entity entity : filtered) {
                    double currentDistance;
                    EntityArmorStand stand = (EntityArmorStand) entity;
                    Entity target = SkyblockUtils.getEntityCuttingOtherEntity(stand, null);

                    if (target == null) continue;
                    if (SkyblockUtils.getMobHp(stand) <= 0) continue;
                    boolean entity1 = SkyblockUtils.entityIsVisible(target);
                    if (!entity1) continue;

                    if (target instanceof EntityLiving) {

                        Target target1 = new Target((EntityLiving) target, stand);

                        if (closestTarget != null) {
                            currentDistance = target.getDistanceToEntity(mc.thePlayer);
                            if (currentDistance < distance) {
                                distance = currentDistance;
                                closestTarget = target1;
                            }
                        } else {
                            distance = target.getDistanceToEntity(mc.thePlayer);
                            closestTarget = target1;
                        }

                        potentialTargets.add(target1);
                    }
                }

                if (closestTarget != null && closestTarget.distance() < May2BeezQoL.config.mobKillerScanRange) {
                    target = closestTarget;
                    currentState = States.ATTACKING;
                }
                break;
            case ATTACKING:

                if (SkyblockUtils.getMobHp(target.stand) <= 0 || target.distance() > May2BeezQoL.config.mobKillerScanRange || target.stand == null || target.entity.isDead) {
                    currentState = States.KILLED;
                    break;
                }

                if (May2BeezQoL.config.useHyperionUnderPlayer) {
                    int weapon = SkyblockUtils.findItemInHotbar("Hyperion");

                    if (weapon == -1) {
                        SkyblockUtils.SendInfo("No Hyperion found");
                        return;
                    }

                    mc.thePlayer.inventory.currentItem = weapon;

                    if (target.distance() > 6) return;


                    if (RotationUtils.done)
                        RotationUtils.smoothLook(new RotationUtils.Rotation(89, mc.thePlayer.rotationYaw), May2BeezQoL.config.mobKillerCameraSpeed);

                    if (RotationUtils.IsDiffLowerThan(0.1f)) {
                        RotationUtils.reset();
                    }

                    if (!RotationUtils.done) return;

                    if (attackDelay.hasReached(May2BeezQoL.config.mobKillerAttackDelay) && target.distance() <= 6) {
                        rightClick();
                        attackDelay.reset();
                    }

                } else {

                    int weapon;

                    if (!May2BeezQoL.config.customItemToKill.isEmpty()) {
                        weapon = SkyblockUtils.findItemInHotbar(May2BeezQoL.config.customItemToKill);
                    } else {
                        weapon = SkyblockUtils.findItemInHotbar("Juju", "Terminator", "Bow", "Frozen Scythe", "Glacial Scythe");
                    }

                    if (weapon == -1) {
                        SkyblockUtils.SendInfo("No weapon found");
                        return;
                    }

                    mc.thePlayer.inventory.currentItem = weapon;

                    RotationUtils.smoothLook(RotationUtils.getRotation(target.entity), May2BeezQoL.config.mobKillerCameraSpeed);

                    if (RotationUtils.IsDiffLowerThan(0.5f)) {
                        RotationUtils.reset();
                    }

                    if (!RotationUtils.done) return;

                    boolean pointedEntity = SkyblockUtils.entityIsVisible(target.entity);
                    if (pointedEntity) {
                        if (attackDelay.hasReached(May2BeezQoL.config.mobKillerAttackDelay)) {
                            if (May2BeezQoL.config.attackButton == 0) {
                                leftClick();
                            } else {
                                rightClick();
                            }
                            attackDelay.reset();
                        }
                    } else {
                        SkyblockUtils.SendInfo("Something is blocking target, waiting for free shot...", false, name);
                        blockedVisionDelay.reset();
                        currentState = States.BLOCKED_VISION;
                    }
                }


                break;
            case BLOCKED_VISION:

                if (SkyblockUtils.getMobHp(target.stand) <= 0 || target.distance() > May2BeezQoL.config.mobKillerScanRange) {
                    currentState = States.KILLED;
                    break;
                }

                if (blockedVisionDelay.hasReached(5000)) {
                    currentState = States.ATTACKING;
                    break;
                }

                break;
            case KILLED:
                target = null;
                currentState = States.SEARCHING;
                break;
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (potentialTargets.size() > 0) {
            potentialTargets.forEach(v -> {
                if (v != target)
                    RenderUtils.drawEntityBox(v.entity, new Color(100, 200, 100, 200), 2, event.partialTicks);
            });
        }

        if (target != null) {
            RenderUtils.drawEntityBox(target.entity, new Color(200, 100, 100, 200), 2, event.partialTicks);
        }
    }
 }
