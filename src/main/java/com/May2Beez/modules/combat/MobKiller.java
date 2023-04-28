package com.May2Beez.modules.combat;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.modules.Module;
import com.May2Beez.utils.*;
import com.May2Beez.utils.structs.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.May2Beez.utils.SkyblockUtils.leftClick;
import static com.May2Beez.utils.SkyblockUtils.rightClick;

public class MobKiller extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static Target target;
    public static int scanRange = 20;

    private static boolean caseSensitive = false;
    private static String[] mobsNames = null;

    private final Timer attackDelay = new Timer();
    private final Timer blockedVisionDelay = new Timer();
    private final Timer afterKillDelay = new Timer();
    public static States currentState = States.SEARCHING;

    public static boolean ShouldScan = false;

    private final CopyOnWriteArrayList<Target> potentialTargets = new CopyOnWriteArrayList<>();

    private static class Target {
        public Entity entity;
        public EntityArmorStand stand;
        public boolean worm;
        public double distance() {
            if (entity != null)
                return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(entity);
            else
                return Minecraft.getMinecraft().thePlayer.getDistanceToEntity(stand);
        }

        public Target(Entity entity, EntityArmorStand stand) {
            this.entity = entity;
            this.stand = stand;
        }

        public Target(Entity entity, EntityArmorStand stand, boolean worm) {
            this.entity = entity;
            this.stand = stand;
            this.worm = worm;
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
        scanRange = May2BeezQoL.config.mobKillerScanRange;
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

    public void Toggle() {
        if (!May2BeezQoL.mobKiller.isToggled()) {
            May2BeezQoL.mobKiller.setToggled(true);
            blockedVisionDelay.reset();
            attackDelay.reset();
            currentState = States.SEARCHING;
            target = null;
            potentialTargets.clear();
        } else {
            May2BeezQoL.mobKiller.setToggled(false);
            target = null;
            potentialTargets.clear();
            RotationUtils.reset();
            ShouldScan = false;
        }

        if (May2BeezQoL.config.debug) {
            LogUtils.addMessage("DEBUG: MobKiller Toggle - " + May2BeezQoL.mobKiller.isToggled(), EnumChatFormatting.LIGHT_PURPLE);
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
        if (SkyblockUtils.hasOpenContainer()) return;

        if (mobsNames == null || mobsNames.length == 0) return;


        switch (currentState) {
            case SEARCHING:
                potentialTargets.clear();
                List<Entity> entities = mc.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityArmorStand).filter(entity -> mc.thePlayer.getPositionEyes(1).distanceTo(entity.getPositionVector()) <= scanRange).collect(Collectors.toList());
                List<Entity> filtered = entities.stream().filter(v -> (!v.getName().contains(mc.thePlayer.getName()) && Arrays.stream(mobsNames).anyMatch(mobsName -> {
                    String mobsName1 = StringUtils.stripControlCodes(mobsName);
                    String vCustomNameTag = StringUtils.stripControlCodes(v.getCustomNameTag());
                    if (caseSensitive) {
                        return vCustomNameTag.contains(mobsName1);
                    } else {
                        return vCustomNameTag.toLowerCase().contains(mobsName1.toLowerCase());
                    }
                }))).collect(Collectors.toList());

                if (filtered.isEmpty())
                    break;

                double distance = 9999;
                Target closestTarget = null;

                for (Entity entity : filtered) {
                    double currentDistance;
                    EntityArmorStand stand = (EntityArmorStand) entity;

                    Target target1;
                    Entity target;

                    if (stand.getCustomNameTag().contains("Scatha") || stand.getCustomNameTag().contains("Worm")) {
                        target1 = new Target(null, stand, true);
                        target = stand;
                    } else {
                        target = SkyblockUtils.getEntityCuttingOtherEntity(stand, null);

                        if (target == null) continue;
                        if (SkyblockUtils.getMobHp(stand) <= 0) continue;
                        if (SkyblockUtils.entityIsNotVisible(target)) continue;

                        target1 = new Target(target, stand, false);
                    }

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

                if (closestTarget != null && closestTarget.distance() < scanRange) {
                    target = closestTarget;
                    currentState = States.ATTACKING;
                }
                break;
            case ATTACKING:

                if (SkyblockUtils.getMobHp(target.entity) <= 0 || target.distance() > scanRange || target.stand == null || (target.entity != null && (target.entity.isDead))) {
                    currentState = States.KILLED;
                    afterKillDelay.reset();
                    break;
                }


                if (May2BeezQoL.config.useHyperionUnderPlayer) {
                    int weapon = InventoryUtils.findItemInHotbar("Hyperion");

                    if (weapon == -1) {
                        LogUtils.addMessage("MobKiller - No Hyperion found", EnumChatFormatting.RED);
                        return;
                    }

                    mc.thePlayer.inventory.currentItem = weapon;

                    if (target.distance() > 5.5) return;

                    if (!RotationUtils.done) return;

                    RotationUtils.smoothLook(new Rotation(mc.thePlayer.rotationYaw, 89), May2BeezQoL.config.mobKillerCameraSpeed);

                    if (RotationUtils.isDiffLowerThan(0.5f)) {
                        RotationUtils.reset();
                    } else {
                        return;
                    }

                    if (attackDelay.hasReached(May2BeezQoL.config.mobKillerAttackDelay) && target.distance() < 6) {
                        rightClick();
                        attackDelay.reset();
                    }

                } else {

                    int weapon;

                    if (!May2BeezQoL.config.customItemToKill.isEmpty()) {
                        weapon = InventoryUtils.findItemInHotbar(May2BeezQoL.config.customItemToKill);
                    } else {
                        weapon = InventoryUtils.findItemInHotbar("Juju", "Terminator", "Bow", "Frozen Scythe", "Glacial Scythe");
                    }

                    if (weapon == -1) {
                        LogUtils.addMessage("MobKiller - No weapon found", EnumChatFormatting.RED);
                        return;
                    }

                    mc.thePlayer.inventory.currentItem = weapon;

                    boolean visible;
                    boolean targeted;

                    if (target.worm) {
                        visible = !SkyblockUtils.entityIsNotVisible(target.stand);
                    } else {
                        visible = !SkyblockUtils.entityIsNotVisible(target.entity);
                    }

                    if (!RotationUtils.done) return;

                    if (!visible) {
                        LogUtils.addMessage("MobKiller - Something is blocking target, waiting for free shot...", EnumChatFormatting.RED);
                        blockedVisionDelay.reset();
                        currentState = States.BLOCKED_VISION;
                    } else {
                        targeted = SkyblockUtils.entityIsTargeted(target.entity);
                        if (targeted) {
                            if (attackDelay.hasReached(May2BeezQoL.config.mobKillerAttackDelay)) {
                                if (May2BeezQoL.config.attackButton == 0) {
                                    if (target.distance() <= 4.5) {
                                        leftClick();
                                    }
                                } else {
                                    rightClick();
                                }
                                attackDelay.reset();
                            }
                        }
                    }
                }

                break;
            case BLOCKED_VISION:
                if (SkyblockUtils.getMobHp(target.entity) <= 0 || target.distance() > May2BeezQoL.config.mobKillerScanRange) {
                    currentState = States.KILLED;
                    afterKillDelay.reset();
                    break;
                }

                if (blockedVisionDelay.hasReached(5000)) {
                    currentState = States.ATTACKING;
                    break;
                }

                break;
            case KILLED:

                if (!afterKillDelay.hasReached(150))
                    return;

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
                    RenderUtils.drawEntityBox(v.worm ? v.stand : v.entity, new Color(100, 200, 100, 200), 2, event.partialTicks);
            });
        }

        if (target != null) {
            if (RotationUtils.done) {
                Rotation angles;
                if (target.worm) {
                    angles = RotationUtils.getRotation(target.stand.getPositionVector().add(new Vec3(0, 0.2f, 0)));
                } else {
                    angles = RotationUtils.getRotation(target.entity.getPositionVector().add(new Vec3(0, target.entity.height / 2, 0)));
                }
                RotationUtils.smoothLook(angles, May2BeezQoL.config.mobKillerCameraSpeed);
            }
            RenderUtils.drawEntityBox(target.worm ? target.stand : target.entity, new Color(200, 100, 100, 200), 2, event.partialTicks);
        }
    }

    public static String[] drawInfo() {
        return new String[]{
                "§r§lTarget:",
                "§rName: §f" + (target != null ? SkyblockUtils.stripString(target.stand.getCustomNameTag()) : "None"),
                "§rDistance: §f" + (target != null ? (String.format("%.2f", target.distance()) + "m") : "No target"),
                "§rHealth: §f" + (target != null ? (SkyblockUtils.getMobHp(target.stand)) : "No target"),
                "§rState: §f" + currentState.name(),
                "§rVisible: §f" + (target != null ? (!SkyblockUtils.entityIsNotVisible(target.entity != null ? target.entity : target.stand) ? "Yes" : "No") : "No target"),

        };
    }
 }
