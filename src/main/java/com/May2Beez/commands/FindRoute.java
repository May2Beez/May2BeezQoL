package com.May2Beez.commands;

import com.May2Beez.Config.AOTVWaypointsStructs;
import com.May2Beez.May2BeezQoL;
import com.May2Beez.utils.LogUtils;
import com.May2Beez.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Queue;
import java.util.*;

public class FindRoute extends CommandBase {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static ArrayList<ArrayList<BlockPos>> debugVein = new ArrayList<>();

    @Override
    public String getCommandName() {
        return "findroute";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/findroute <num_of_veins> <radius_of_circle> <...vein_types>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reset")) {
                debugVein.clear();
                return;
            }
        }

        if (args.length < 2) {
            LogUtils.addMessage("Usage: /findroute <num_of_veins> <radius_of_circle> <...vein_types>", EnumChatFormatting.RED);
            return;
        }
        long startedAt = System.currentTimeMillis();
        int numVeins = Integer.parseInt(args[0]);
        int radius = Integer.parseInt(args[1]);

        ArrayList<BlockPos> circle = createDividedCircle(mc.thePlayer.getPosition(), mc.thePlayer.getLookVec(), numVeins, radius);

        ArrayList<ArrayList<BlockPos>> route = new ArrayList<>();

        VeinType[] veinTypes = new VeinType[args.length - 2];
        for (int i = 2; i < args.length; i++) {
            veinTypes[i - 2] = new VeinType(args[i], getVeinType(args[i]));
        }
        veinTypes = Arrays.stream(veinTypes).filter(veinType -> veinType.getColor() != null).toArray(VeinType[]::new);
        LogUtils.addMessage("Finding route for " + numVeins + " veins of " + veinTypes.length + " types", EnumChatFormatting.GREEN);
        LogUtils.addMessage("Vein types:", EnumChatFormatting.GREEN);

        for (VeinType veinType : veinTypes) {
            LogUtils.addMessage("  " + veinType.getName(), EnumChatFormatting.GREEN);
        }

        if (veinTypes.length == 0) {
            LogUtils.addMessage("  all", EnumChatFormatting.GREEN);
        }

        for (BlockPos pos : circle) {
            System.out.println("Checking " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
            BlockPos closestGemstone = getClosestGemstoneExcept(pos, route, veinTypes);
            if (closestGemstone == null) {
                LogUtils.addMessage("No next vein found in radius of 55 blocks", EnumChatFormatting.RED);
                continue;
            }
            ArrayList<BlockPos> nextVein = findGemstoneVein(closestGemstone, veinTypes);
            route.add(nextVein);
        }

        LogUtils.addMessage("Found " + route.size() + " veins in: " + String.format("%.2f", ((System.currentTimeMillis() - startedAt) / 1_000f)) + "s", EnumChatFormatting.GREEN);
        debugVein = route;

        if (Math.sqrt(getCenterOfTheVein(route.get(0)).distanceSq(getCenterOfTheVein(route.get(route.size() - 1)))) > 55) {
            LogUtils.addMessage("Last vein is further than 55 blocks away from the first vein", EnumChatFormatting.RED);
        }

        ArrayList<AOTVWaypointsStructs.Waypoint> waypoints = new ArrayList<>();

        int i = 0;

        for (ArrayList<BlockPos> vein : route) {
            BlockPos center = getCenterOfTheVein(vein);
            waypoints.add(new AOTVWaypointsStructs.Waypoint(String.valueOf(i), center));
            i++;
        }

        AOTVWaypointsStructs.WaypointList waypointList = new AOTVWaypointsStructs.WaypointList("Route Maker", false, false, waypoints);
        String routeString = Base64.getEncoder().encodeToString(May2BeezQoL.gson.toJson(waypointList).getBytes());
        StringSelection stringSelection = new StringSelection("#MightyMinerWaypoint#::" + routeString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        LogUtils.addMessage("Route copied to clipboard", EnumChatFormatting.GREEN);
    }

    private ArrayList<BlockPos> getClosestVein(BlockPos pos, VeinType[] veinTypes) {
        ArrayList<BlockPos> blocksAround = new ArrayList<>();
        for (int x = pos.getX() - 5; x <= pos.getX() + 5; x++) {
            for (int y = pos.getY() - 5; y <= pos.getY() + 5; y++) {
                for (int z = pos.getZ() - 5; z <= pos.getZ() + 5; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (isGemstone(blockPos, veinTypes)) {
                        blocksAround.add(blockPos);
                    }
                }
            }
        }
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (BlockPos blockPos : blocksAround) {
            double distance = blockPos.distanceSq(pos.getX(), pos.getY(), pos.getZ());
            if (distance < closestDistance) {
                closest = blockPos;
                closestDistance = distance;
            }
        }

        if (closest == null) {
            return new ArrayList<>();
        }

        return findGemstoneVein(closest, veinTypes);
    }

    private BlockPos getCenterOfTheVein(ArrayList<BlockPos> vein) {
        int x = 0;
        int y = 0;
        int z = 0;
        for (BlockPos blockPos : vein) {
            x += blockPos.getX();
            y += blockPos.getY();
            z += blockPos.getZ();
        }
        return new BlockPos(x / vein.size(), y / vein.size(), z / vein.size());
    }

    private boolean blockDoesntExistInAnyVein(BlockPos from, ArrayList<ArrayList<BlockPos>> route) {
        for (ArrayList<BlockPos> innerList : route) {
            if (innerList.contains(from)) {
                return false;
            }
        }
        return true;
    }

    private BlockPos getClosestGemstoneExcept(BlockPos from, ArrayList<ArrayList<BlockPos>> route, VeinType[] veinTypes) {
        System.out.println("Finding closest gemstone from " + from.getX() + " " + from.getY() + " " + from.getZ());
        ArrayList<ArrayList<BlockPos>> veinsAround = new ArrayList<>();
        for (int x = from.getX() - 55; x <= from.getX() + 55; x++) {
            for (int y = from.getY() - 55; y <= from.getY() + 55; y++) {
                for (int z = from.getZ() - 55; z <= from.getZ() + 55; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (isGemstone(blockPos, veinTypes) && blockDoesntExistInAnyVein(blockPos, route) && blockDoesntExistInAnyVein(blockPos, veinsAround)) {
                        ArrayList<BlockPos> vein = findGemstoneVein(blockPos, veinTypes);
                        if (vein.size() >= 4) {
                            veinsAround.add(vein);
                        }
                    }
                }
            }
        }
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (ArrayList<BlockPos> vein : veinsAround) {
            BlockPos center = getCenterOfTheVein(vein);
            double distance = center.distanceSq(from.getX(), from.getY(), from.getZ());
            if (distance < closestDistance) {
                closest = center;
                closestDistance = distance;
            }
        }
        if (closest != null)
            System.out.println("Found closest gemstone at " + closest.getX() + " " + closest.getY() + " " + closest.getZ());
        else
            System.out.println("No closest gemstone found");
        return closest;
    }

    private static boolean anyAroundIsFromTheVein(BlockPos pos, ArrayList<BlockPos> vein) {
        for (int x = pos.getX() - 1; x <= pos.getX() + 1; x++) {
            for (int y = pos.getY() - 1; y <= pos.getY() + 1; y++) {
                for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; z++) {
                    BlockPos adjacentPos = new BlockPos(x, y, z);
                    if (!adjacentPos.equals(pos) && vein.contains(adjacentPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isGemstone(BlockPos pos, VeinType[] veinTypes) {
        IBlockState blockState = mc.theWorld.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!block.equals(Blocks.stained_glass) && !block.equals(Blocks.stained_glass_pane)) {
            return false;
        }
        EnumDyeColor color = EnumDyeColor.byMetadata(block.getMetaFromState(blockState));
        return veinTypes.length == 0 || Arrays.stream(veinTypes).anyMatch(v -> v.getColor().equals(color));
    }

    private ArrayList<BlockPos> findGemstoneVein(BlockPos pos, VeinType[] veinTypes) {
        ArrayList<BlockPos> vein = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        vein.add(pos);
        visited.add(pos);
        queue.offer(pos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            for (int x = currentPos.getX() - 1; x <= currentPos.getX() + 1; x++) {
                for (int y = currentPos.getY() - 1; y <= currentPos.getY() + 1; y++) {
                    for (int z = currentPos.getZ() - 1; z <= currentPos.getZ() + 1; z++) {
                        BlockPos adjacentPos = new BlockPos(x, y, z);
                        if (!visited.contains(adjacentPos) && isGemstone(adjacentPos, veinTypes) &&
                                anyAroundIsFromTheVein(adjacentPos, vein) && !vein.contains(adjacentPos)) {
                            vein.add(adjacentPos);
                            visited.add(adjacentPos);
                            queue.offer(adjacentPos);
                        }
                    }
                }
            }
        }

        return vein;
    }

    public static ArrayList<BlockPos> createDividedCircle(BlockPos playerPos, Vec3 lookingVector, int numDivisions, double radius) {
        ArrayList<BlockPos> circleBlocks = new ArrayList<>();

        double angleIncrement = 360.0 / numDivisions; // Angle increment for each division

        for (int i = 0; i < numDivisions; i++) {
            double angle = Math.toRadians(angleIncrement * i);
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            BlockPos blockPos = new BlockPos(playerPos.getX() + offsetX, playerPos.getY(), playerPos.getZ() + offsetZ);
            circleBlocks.add(blockPos);
        }

        return circleBlocks;
    }

    private EnumDyeColor getVeinType(String veinType) {
        switch (veinType.toLowerCase()) {
            case "ruby": {
                return EnumDyeColor.RED;
            }
            case "sapphire": {
                return EnumDyeColor.LIGHT_BLUE;
            }
            case "amber": {
                return EnumDyeColor.ORANGE;
            }
            case "amethyst": {
                return EnumDyeColor.PURPLE;
            }
            case "jade": {
                return EnumDyeColor.LIME;
            }
            case "topaz": {
                return EnumDyeColor.YELLOW;
            }
            case "jasper": {
                return EnumDyeColor.MAGENTA;
            }
        }
        return null;
    }

    private Color getColorOfVein(EnumDyeColor veinColor) {
        switch (veinColor) {
            case RED: {
                return new Color(255, 0, 0, 120);
            }
            case LIGHT_BLUE: {
                return new Color(0, 0, 255, 120);
            }
            case ORANGE: {
                return new Color(255, 165, 0, 120);
            }
            case PURPLE: {
                return new Color(128, 0, 128, 120);
            }
            case LIME: {
                return new Color(0, 255, 0, 120);
            }
            case YELLOW: {
                return new Color(255, 255, 0, 120);
            }
            case MAGENTA: {
                return new Color(255, 0, 255, 120);
            }
        }
        return new Color(50, 50, 50, 120);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    private static class VeinType {
        @Getter
        private String name;
        @Getter
        private EnumDyeColor color;

        public VeinType(String name, EnumDyeColor color) {
            this.name = name;
            this.color = color;
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        int routeIndex = 1;
        for (ArrayList<BlockPos> vein : debugVein) {
            BlockPos center = getCenterOfTheVein(vein);
            RenderUtils.drawBlockBox(center, new Color(0, 255, 0, 120), event.partialTicks);
            RenderUtils.drawText(String.valueOf(routeIndex), center.getX(), center.getY() + 1.5, center.getZ(), 2f);
            for (BlockPos blockPos : vein) {
                EnumDyeColor veinType = EnumDyeColor.byMetadata(mc.theWorld.getBlockState(blockPos).getBlock().getMetaFromState(mc.theWorld.getBlockState(blockPos)));
                RenderUtils.drawBlockBox(blockPos, getColorOfVein(veinType), event.partialTicks);
            }
            routeIndex++;
        }
    }
}
