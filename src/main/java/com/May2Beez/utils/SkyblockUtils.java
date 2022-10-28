package com.May2Beez.utils;

import com.May2Beez.SkyblockMod;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkyblockUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean inDungeon;

    public static boolean isInOtherGame;

    public static boolean onSkyblock;

    public static boolean onBedwars;

    public static boolean onSkywars;

    public static boolean inBlood;

    public static boolean inP3;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        inBlood = false;
        inP3 = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.theWorld != null)
            inDungeon = (hasLine("Cleared:") || hasLine("Start"));
        isInOtherGame = isInOtherGame();
        onSkyblock = isOnSkyBlock();
        onBedwars = hasScoreboardTitle("bed wars");
        onSkywars = hasScoreboardTitle("SKYWARS");
    }

    public static boolean isInOtherGame() {
        try {
            Scoreboard sb = mc.thePlayer.getWorldScoreboard();
            List<Score> list = new ArrayList<>(sb.getSortedScores(sb.getObjectiveInDisplaySlot(1)));
            for (Score score : list) {
                ScorePlayerTeam team = sb.getPlayersTeam(score.getPlayerName());
                String s = ChatFormatting.stripFormatting(ScorePlayerTeam.formatPlayerName((Team)team, score.getPlayerName()));
                if (s.contains("Map"))
                    return true;
            }
        } catch (Exception exception) {}
        return false;
    }

    public static boolean hasScoreboardTitle(String title) {
        if (mc.thePlayer == null || mc.thePlayer.getWorldScoreboard() == null || mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(1) == null)
            return false;
        return ChatFormatting.stripFormatting(mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(1).getDisplayName()).equalsIgnoreCase(title);
    }

    public static boolean isOnSkyBlock() {
        try {
            ScoreObjective titleObjective = mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(1);
            if (mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(0) != null)
                return ChatFormatting.stripFormatting(mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(0).getDisplayName()).contains("SKYBLOCK");
            return ChatFormatting.stripFormatting(mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(1).getDisplayName()).contains("SKYBLOCK");
        } catch (Exception e) {
            return false;
        }
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

    public static Vec3 getRandomVisibilityLine(BlockPos pos) {
        List<Vec3> lines = new ArrayList<>();
        for (int x = 0; x < SkyblockMod.config.accuracyChecks; x++) {
            for (int y = 0; y < SkyblockMod.config.accuracyChecks; y++) {
                for (int z = 0; z < SkyblockMod.config.accuracyChecks; z++) {
                    Vec3 target = new Vec3(pos.getX() + x / (float) SkyblockMod.config.accuracyChecks, pos.getY() + y / (float) SkyblockMod.config.accuracyChecks, pos.getZ() + z / (float) SkyblockMod.config.accuracyChecks);
                    BlockPos test = new BlockPos(target.xCoord, target.yCoord, target.zCoord);
                    MovingObjectPosition movingObjectPosition = Minecraft.getMinecraft().theWorld.rayTraceBlocks(Minecraft.getMinecraft().thePlayer.getPositionEyes(0.0F), target, true, false, true);
                    if (movingObjectPosition != null) {
                        BlockPos obj = movingObjectPosition.getBlockPos();
                        if (obj.equals(test) && Minecraft.getMinecraft().thePlayer.getDistance(target.xCoord, target.yCoord - Minecraft.getMinecraft().thePlayer.getEyeHeight(), target.zCoord) < 4.5D && (
                                SkyblockMod.config.under || Math.abs(Minecraft.getMinecraft().thePlayer.posY - target.yCoord) > 1.3D))
                            lines.add(target);
                    }
                }
            }
        }
        return lines.isEmpty() ? null : lines.get((new Random()).nextInt(lines.size()));
    }

    public static boolean isBlockVisible(BlockPos pos) {
        return (getRandomVisibilityLine(pos) != null);
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

    public static void click() {
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

    public static void SendInfo(String message, boolean enable, String moduleName) {
        if (Minecraft.getMinecraft().thePlayer == null) return;

        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(String.format("§d[QoL] §%s§l%s§r§%s %s", enable ? "2" : "c", moduleName, enable ? "2" : "c", message)));
    }

    public static int findItemInHotbar(String name) {
        InventoryPlayer inv = Minecraft.getMinecraft().thePlayer.inventory;
        for (int i = 0; i < 9; i++) {
            ItemStack curStack = inv.getStackInSlot(i);
            if (curStack != null) {
                if (curStack.getDisplayName().contains(name)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String getGuiName(GuiScreen gui) {
        if(gui instanceof GuiChest) {
            return ((ContainerChest) ((GuiChest) gui).inventorySlots).getLowerChestInventory().getDisplayName().getUnformattedText();
        }
        return "";
    }
}
