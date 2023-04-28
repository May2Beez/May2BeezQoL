package com.May2Beez.utils;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkyblockUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final Pattern healthPattern = Pattern.compile("(?:§8[§7Lv(\\d)§8])?\\s*(?:§c)?(.+)(?:§r)? §[ae]([\\dBMk]+)§c❤");
    private static final Pattern healthPattern2 = Pattern.compile("(?:§8[§7Lv(\\d)§8])?\\s*(?:§c)?(.+)(?:§r)? §[ae]([\\dBMk]+)§f/§[ae]([\\dBMk]+)§c❤");


    public static boolean hasScoreboardTitle(String title) {
        if (mc.thePlayer == null || mc.thePlayer.getWorldScoreboard() == null || mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(1) == null)
            return false;
        return ChatFormatting.stripFormatting(mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(1).getDisplayName()).equalsIgnoreCase(title);
    }

    public static boolean hasLine(String line) {
        try {
            Scoreboard sb = (Minecraft.getMinecraft()).thePlayer.getWorldScoreboard();
            List<Score> list = new ArrayList<>(sb.getSortedScores(sb.getObjectiveInDisplaySlot(1)));
            for (Score score : list) {
                String s;
                ScorePlayerTeam team = sb.getPlayersTeam(score.getPlayerName());
                try {
                    s = ChatFormatting.stripFormatting(team.getColorPrefix() + score.getPlayerName() + team.getColorSuffix());
                } catch (Exception e) {
                    return false;
                }
                StringBuilder builder = new StringBuilder();
                for (char c : s.toCharArray()) {
                    if (c < 'Ā')
                        builder.append(c);
                }
                if (builder.toString().toLowerCase().contains(line.toLowerCase()))
                    return true;
                try {
                    s = ChatFormatting.stripFormatting(team.getColorPrefix() + team.getColorSuffix());
                } catch (Exception e) {
                    return false;
                }
                builder = new StringBuilder();
                for (char c : s.toCharArray()) {
                    if (c < 'Ā')
                        builder.append(c);
                }
                if (builder.toString().toLowerCase().contains(line.toLowerCase()))
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static boolean isNPC(Entity entity) {
        return !TablistUtils.getTabListPlayersSkyblock().contains(entity.getName());
    }

    public static void rightClick() {
        try {
            Method rightClickMouse;
            try {
                rightClickMouse = Minecraft.class.getDeclaredMethod("func_147121_ag");
            } catch (NoSuchMethodException e) {
                rightClickMouse = Minecraft.class.getDeclaredMethod("rightClickMouse");
            }
            rightClickMouse.setAccessible(true);
            rightClickMouse.invoke(Minecraft.getMinecraft());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void leftClick() {
        try {
            Method clickMouse;
            try {
                clickMouse = Minecraft.class.getDeclaredMethod("func_147116_af");
            } catch (NoSuchMethodException e) {
                clickMouse = Minecraft.class.getDeclaredMethod("clickMouse");
            }
            clickMouse.setAccessible(true);
            clickMouse.invoke(Minecraft.getMinecraft());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void swingHand(MovingObjectPosition objectMouseOver) {
        if (objectMouseOver == null) {
            objectMouseOver = mc.objectMouseOver;
        }
        if (objectMouseOver != null && objectMouseOver.entityHit == null) {
            mc.thePlayer.swingItem();
        }
    }

    public static Entity getEntityCuttingOtherEntity(Entity e, Class<?> entityType) {
        List<Entity> possible = mc.theWorld.getEntitiesInAABBexcluding(e, e.getEntityBoundingBox().expand(0.3D, 2.0D, 0.3D), a -> {
            boolean flag1 = (!a.isDead && !a.equals(mc.thePlayer));
            boolean flag2 = !(a instanceof EntityArmorStand);
            boolean flag3 = !(a instanceof net.minecraft.entity.projectile.EntityFireball);
            boolean flag4 = !(a instanceof net.minecraft.entity.projectile.EntityFishHook);
            boolean flag5 = (entityType == null || entityType.isInstance(a));
            return flag1 && flag2 && flag3 && flag4 && flag5;
        });
        if (!possible.isEmpty())
            return Collections.min(possible, Comparator.comparing(e2 -> e2.getDistanceToEntity(e)));
        return null;
    }

    public static boolean entityIsNotVisible(Entity entityToCheck) {
        return !mc.thePlayer.canEntityBeSeen(entityToCheck);
    }

    public static ArrayList<BlockPos> AnyBlockAroundVec3(Vec3 pos, float around) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (double x = (pos.xCoord - around); x <= pos.xCoord + around; x += around) {
            for (double y = (pos.yCoord - around); y <= pos.yCoord + around; y += around) {
                for (double z = (pos.zCoord - around); z <= pos.zCoord + around; z += around) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (!blocks.contains(blockPos)) {
                        blocks.add(blockPos);
                    }
                }
            }
        }
        return blocks;
    }

    public static ArrayList<String> getItemLore(ItemStack item) {
        NBTTagList loreTag = item.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        ArrayList<String> loreList = new ArrayList<>();
        for (int i = 0; i < loreTag.tagCount(); i++) {
            loreList.add(StringUtils.stripControlCodes(loreTag.getStringTagAt(i)));
        }
        return loreList;
    }

    public static void sendPingAlert() {
        new Thread(() -> {
            for (int i = 0; i < 15; i++) {
                mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "random.orb", 10.0F, 1.0F, false);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static String stripString(String s) {
        char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
        StringBuilder validated = new StringBuilder();
        for (char a : nonValidatedString) {
            if (a < '' && a > '\024')
                validated.append(a);
        }
        return validated.toString();
    }

    public static int getMobHp(Entity entity) {
        if (entity instanceof EntityArmorStand) {
            String name = entity.getCustomNameTag();
            if (name.contains("❤")) {
                Matcher matcher = healthPattern.matcher(name);
                Matcher matcher2 = healthPattern2.matcher(name);
                System.out.println(name);
                if (matcher.find() || matcher2.find()) {
                    String hp = matcher.find() ? matcher.group(2) : matcher2.group(2);
                    int modifer = 1;
                    if (name.contains("k§c❤")) {
                        modifer = 1000;
                    } else if (name.contains("M§c❤")) {
                        modifer = 1000000;
                    } else if (name.contains("B§c❤")) {
                        modifer = 1000000000;
                    }
                    System.out.println(hp);
                    return (int) (Double.parseDouble(hp.replace("k", "").replace("M", "").replace("B", "")) * modifer);
                }
            }
        } else if (entity instanceof EntityLivingBase) {
            System.out.println(((EntityLivingBase) entity).getHealth());
            return (int) ((EntityLivingBase) entity).getHealth();
        }
        return -1;
    }

    public static boolean hasOpenContainer() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat);
    }

    public static boolean entityIsTargeted(Entity entity) {
        int reach = 50;
        Vec3 eyesPos = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 lookVec = mc.thePlayer.getLook(1.0F);
        List<Entity> entityList = mc.theWorld.getEntitiesInAABBexcluding(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().addCoord(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach).expand(1.0D, 1.0D, 1.0D), Entity::canBeCollidedWith);
        Entity entityMouseOver = null;
        for (Entity e : entityList) {
            AxisAlignedBB entityBoundingBox = e.getEntityBoundingBox().expand(0.3D, 0.3D, 0.3D);
            MovingObjectPosition movingObjectPosition = entityBoundingBox.calculateIntercept(eyesPos, eyesPos.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach));
            if (movingObjectPosition != null) {
                double distanceToEntity = eyesPos.distanceTo(movingObjectPosition.hitVec);
                if (distanceToEntity < reach) {
                    entityMouseOver = e;
                    reach = (int) distanceToEntity;
                }
            }
        }
        return entityMouseOver != null && entityMouseOver.equals(entity);
    }
}
