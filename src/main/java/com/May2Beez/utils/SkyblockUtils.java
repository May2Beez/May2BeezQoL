package com.May2Beez.utils;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.*;
import net.minecraft.util.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkyblockUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();


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
        if (!(entity instanceof EntityOtherPlayerMP))
            return false;
        EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
        return (entity.getUniqueID().version() == 2 && entityLivingBase.getHealth() == 20.0F);
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

    public static int findItemInHotbar(String ...name) {
        InventoryPlayer inv = mc.thePlayer.inventory;
        for (int i = 0; i < 9; i++) {
            ItemStack curStack = inv.getStackInSlot(i);
            if (curStack != null) {
                if (Arrays.stream(name).anyMatch(curStack.getDisplayName()::contains)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static void swingHand(MovingObjectPosition objectMouseOver) {
        if (objectMouseOver == null) {
            objectMouseOver = mc.objectMouseOver;
        }
        if (objectMouseOver != null && objectMouseOver.entityHit == null) {
            mc.thePlayer.swingItem();
        }
    }

    public static String getGuiName(GuiScreen gui) {
        if(gui instanceof GuiChest) {
            return ((ContainerChest) ((GuiChest) gui).inventorySlots).getLowerChestInventory().getDisplayName().getUnformattedText();
        }
        return "";
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
        Vec3 startPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        Vec3 endPos = new Vec3(entityToCheck.posX, entityToCheck.posY + entityToCheck.height / 2, entityToCheck.posZ);

        Vec3 direction = new Vec3(endPos.xCoord - startPos.xCoord, endPos.yCoord - startPos.yCoord, endPos.zCoord - startPos.zCoord);

        double maxDistance = startPos.distanceTo(endPos);

        double increment = 0.05;

        Vec3 currentPos = startPos;

        while (currentPos.distanceTo(startPos) < maxDistance) {

            ArrayList<BlockPos> blocks = AnyBlockAroundVec3(currentPos, 0.15f);

            boolean flag = false;

            for (BlockPos pos : blocks) {
                // Add the block to the list if it hasn't been added already
                if (!mc.theWorld.isAirBlock(pos)) {
                    flag = true;
                }
            }

            if (flag) {
                return true;
            }

            // Move along the line by the specified increment
            Vec3 scaledDirection = new Vec3(direction.xCoord * increment, direction.yCoord * increment, direction.zCoord * increment);
            currentPos = currentPos.add(scaledDirection);
        }
        return false;
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

    public static int getMobHp(EntityArmorStand aStand) {
        double mobHp = -1.0D;
        Pattern pattern = Pattern.compile(".+? ([.\\d]+)[BMk]?/[.\\d]+[BMk]?");
        String stripped = stripString(aStand.getName());
        Matcher mat = pattern.matcher(stripped);
        if (mat.matches())
            try {
                mobHp = Double.parseDouble(mat.group(1));
            } catch (NumberFormatException ignored) {

            }
        else {
            pattern = Pattern.compile("\\[Lv(\\d+)]\\s+(\\w+)\\s+(\\d+)+[BMk]?");
            stripped = stripString(aStand.getName());
            mat = pattern.matcher(stripped);
            if (mat.matches())
                try {
                    mobHp = Double.parseDouble(mat.group(3));
                } catch (NumberFormatException ignored) {

                }
        }
        return (int)Math.ceil(mobHp);
    }

    public static boolean hasOpenContainer() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat);
    }
}
