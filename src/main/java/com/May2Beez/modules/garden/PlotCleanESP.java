package com.May2Beez.modules.garden;

import com.May2Beez.May2BeezQoL;
import com.May2Beez.utils.LocationUtils;
import com.May2Beez.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlotCleanESP {

    private final CopyOnWriteArrayList<BlockPos> blocks = new CopyOnWriteArrayList<>();
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean shouldScanAgain = false;
    private final ArrayList<BlockPos> temp = new ArrayList<>();


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!May2BeezQoL.config.plotCleanerEsp) return;
        if (LocationUtils.currentIsland != LocationUtils.Island.GARDEN) return;
        if (!shouldScanAgain) return;

        shouldScanAgain = false;

        new Thread(() -> {
            for (int x = -50; x < 50; x++) {
                for (int y = 63; y < 75; y++) {
                    for (int z = -50; z < 50; z++) {
                        if (May2BeezQoL.config.grassEsp) {
                            if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z)).getBlock().equals(Blocks.tallgrass) ||
                            mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z)).getBlock().equals(Blocks.double_plant)) {
                                temp.add(new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z));
                            }
                        }
                        if (May2BeezQoL.config.flowersEsp) {
                            if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z)).getBlock().equals(Blocks.red_flower)) {
                                temp.add(new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z));
                            }
                            if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z)).getBlock().equals(Blocks.yellow_flower)) {
                                temp.add(new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z));
                            }
                        }
                        if (May2BeezQoL.config.leavesEsp) {
                            if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z)).getBlock().equals(Blocks.leaves) ||
                            mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z)).getBlock().equals(Blocks.leaves2)) {
                                if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + x, y - 1, mc.thePlayer.posZ + z)).getBlock().equals(Blocks.grass) ||
                                mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + x, y - 1, mc.thePlayer.posZ + z)).getBlock().equals(Blocks.dirt) ||
                                mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + x, y - 2, mc.thePlayer.posZ + z)).getBlock().equals(Blocks.grass)
                                || mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + x, y - 2, mc.thePlayer.posZ + z)).getBlock().equals(Blocks.dirt)) {
                                    temp.add(new BlockPos(mc.thePlayer.posX + x, y, mc.thePlayer.posZ + z));
                                }
                            }
                        }
                    }
                }
            }
            blocks.clear();
            blocks.addAll(temp);
            temp.clear();
            shouldScanAgain = true;
        }).start();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        shouldScanAgain = true;
        blocks.clear();
    }

    @SubscribeEvent
    public void onWorldLastRender(RenderWorldLastEvent event) {
        if (!May2BeezQoL.config.plotCleanerEsp) return;
        if (LocationUtils.currentIsland != LocationUtils.Island.GARDEN) return;

        for (BlockPos block : blocks) {
            if (mc.theWorld.getBlockState(block).getBlock().equals(Blocks.air)) continue;

            RenderUtils.drawBlockBox(block, May2BeezQoL.config.plotCleanerEspColor.toJavaColor(), 2);
        }
    }
}
