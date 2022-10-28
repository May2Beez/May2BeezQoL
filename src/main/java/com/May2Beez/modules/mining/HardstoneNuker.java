package com.May2Beez.modules.mining;

import com.May2Beez.Module;
import com.May2Beez.SkyblockMod;
import com.May2Beez.utils.RenderUtils;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

// RGA Nuker ported
public class HardstoneNuker extends Module {
    public HardstoneNuker() {
        super("Hardstone Nuker", Keyboard.KEY_NONE);
    }

    private ArrayList<BlockPos> broken = new ArrayList<>();
    private static int currentDamage;
    private static BlockPos closestStone;
    private boolean stopHardstone = false;
    private static int ticks = 0;
    private static BlockPos gemstone;
    private static BlockPos lastGem;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isToggled()) {
            currentDamage = 0;
            broken.clear();
            return;
        }
        if (!stopHardstone) {
            ticks++;
            if (SkyblockMod.config.hardIndex == 0) {
                if (broken.size() > 10) {
                    broken.clear();
                }
            }
            if (SkyblockMod.config.hardIndex == 1) {
                if (broken.size() > 6) {
                    broken.clear();
                }
            }
            if (ticks > 30) {
                broken.clear();
                ticks = 0;
            }
            closestStone = closestStone();
            if (currentDamage > 200) {
                currentDamage = 0;
            }
            if (gemstone != null && Minecraft.getMinecraft().thePlayer != null) {
                if (lastGem != null && !lastGem.equals(gemstone)) {
                    currentDamage = 0;
                }
                lastGem = gemstone;
                if (currentDamage == 0) {
                    Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, gemstone, EnumFacing.DOWN));
                }
                swingItem();
                currentDamage++;
            }
            if (closestStone != null && gemstone == null) {
                currentDamage = 0;
                MovingObjectPosition fake = Minecraft.getMinecraft().objectMouseOver;
                fake.hitVec = new Vec3(closestStone);
                EnumFacing enumFacing = fake.sideHit;
                if (enumFacing != null && Minecraft.getMinecraft().thePlayer != null) {
                    Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, closestStone, enumFacing));
                }
                swingItem();
                broken.add(closestStone);
            }
        }
    }

    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        if (!isToggled()) return;
        closestStone = closestStone();
        if (closestStone != null) {
            RenderUtils.drawBlockBox(closestStone, new Color(128, 128, 128), SkyblockMod.config.lineWidth, event.partialTicks);
        }
        if (gemstone != null) {
            IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(gemstone);
            EnumDyeColor dyeColor = null;
            Color color = Color.BLACK;
            if (blockState.getBlock() == Blocks.stained_glass) {
                dyeColor = blockState.getValue(BlockStainedGlass.COLOR);
            }
            if (blockState.getBlock() == Blocks.stained_glass_pane) {
                dyeColor = blockState.getValue(BlockStainedGlassPane.COLOR);
            }
            if (dyeColor == EnumDyeColor.RED) {
                color = new Color(188, 3, 29);
            } else if (dyeColor == EnumDyeColor.PURPLE) {
                color = new Color(137, 0, 201);
            } else if (dyeColor == EnumDyeColor.LIME) {
                color = new Color(157, 249, 32);
            } else if (dyeColor == EnumDyeColor.LIGHT_BLUE) {
                color = new Color(60, 121, 224);
            } else if (dyeColor == EnumDyeColor.ORANGE) {
                color = new Color(237, 139, 35);
            } else if (dyeColor == EnumDyeColor.YELLOW) {
                color = new Color(249, 215, 36);
            } else if (dyeColor == EnumDyeColor.MAGENTA) {
                color = new Color(214, 15, 150);
            }
            RenderUtils.drawBlockBox(gemstone, color, SkyblockMod.config.lineWidth, event.partialTicks);
        }
    }

    private BlockPos closestStone() {
        if (Minecraft.getMinecraft().theWorld == null) return null;
        if (Minecraft.getMinecraft().thePlayer == null) return null;
        int r = 4;
        BlockPos playerPos = Minecraft.getMinecraft().thePlayer.getPosition();
        playerPos.add(0, 1, 0);
        Vec3 playerVec = Minecraft.getMinecraft().thePlayer.getPositionVector();
        Vec3i vec3i = new Vec3i(r, 1 + SkyblockMod.config.hardrange, r);
        Vec3i vec3i2 = new Vec3i(r, SkyblockMod.config.hardrangeDown, r);
        ArrayList<Vec3> stones = new ArrayList<Vec3>();
        ArrayList<Vec3> gemstones = new ArrayList<Vec3>();
        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i2))) {
            IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(blockPos);
            if (SkyblockMod.config.hardIndex == 0) {
                if (!SkyblockMod.config.includeExcavatable && blockState.getBlock() == Blocks.stone && !broken.contains(blockPos)) {
                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                }
                if (SkyblockMod.config.includeOres) {
                    if ((blockState.getBlock() == Blocks.coal_ore || blockState.getBlock() == Blocks.diamond_ore
                            || blockState.getBlock() == Blocks.gold_ore || blockState.getBlock() == Blocks.redstone_ore
                            || blockState.getBlock() == Blocks.iron_ore || blockState.getBlock() == Blocks.lapis_ore
                            || blockState.getBlock() == Blocks.emerald_ore || blockState.getBlock() == Blocks.netherrack
                            || blockState.getBlock() == Blocks.lit_redstone_ore)
                            && !broken.contains(blockPos)) {
                        stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                    }
                }
                if (SkyblockMod.config.includeExcavatable) {
                    if ((blockState.getBlock() == Blocks.gravel || blockState.getBlock() == Blocks.sand) && !broken.contains(blockPos)) {
                        stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                    }
                }
            }
            if (SkyblockMod.config.hardIndex == 1) {
                EnumFacing dir = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
                int x = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posX);
                int z = (int) Math.floor(Minecraft.getMinecraft().thePlayer.posZ);
                switch (dir) {
                    case NORTH:
                        if (blockPos.getZ() <= z && blockPos.getX() == x) {
                            if (isSlow(blockState)) {
                                gemstones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                            } else if (!SkyblockMod.config.includeExcavatable && blockState.getBlock() == Blocks.stone && !broken.contains(blockPos)) {
                                stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                            }
                            if (SkyblockMod.config.includeOres) {
                                if ((blockState.getBlock() == Blocks.coal_ore || blockState.getBlock() == Blocks.diamond_ore
                                        || blockState.getBlock() == Blocks.gold_ore || blockState.getBlock() == Blocks.redstone_ore
                                        || blockState.getBlock() == Blocks.iron_ore || blockState.getBlock() == Blocks.lapis_ore
                                        || blockState.getBlock() == Blocks.emerald_ore || blockState.getBlock() == Blocks.netherrack
                                        || blockState.getBlock() == Blocks.lit_redstone_ore)
                                        && !broken.contains(blockPos)) {
                                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            }
                            if (SkyblockMod.config.includeExcavatable) {
                                if ((blockState.getBlock() == Blocks.gravel || blockState.getBlock() == Blocks.sand) && !broken.contains(blockPos)) {
                                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            }
                        }
                        break;
                    case SOUTH:
                        if (blockPos.getZ() >= z && blockPos.getX() == x) {
                            if (isSlow(blockState)) {
                                gemstones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                            } else if (!SkyblockMod.config.includeExcavatable && blockState.getBlock() == Blocks.stone && !broken.contains(blockPos)) {
                                stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                            }
                            if (SkyblockMod.config.includeOres) {
                                if ((blockState.getBlock() == Blocks.coal_ore || blockState.getBlock() == Blocks.diamond_ore
                                        || blockState.getBlock() == Blocks.gold_ore || blockState.getBlock() == Blocks.redstone_ore
                                        || blockState.getBlock() == Blocks.iron_ore || blockState.getBlock() == Blocks.lapis_ore
                                        || blockState.getBlock() == Blocks.emerald_ore || blockState.getBlock() == Blocks.netherrack
                                        || blockState.getBlock() == Blocks.lit_redstone_ore)
                                        && !broken.contains(blockPos)) {
                                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            }
                            if (SkyblockMod.config.includeExcavatable) {
                                if ((blockState.getBlock() == Blocks.gravel || blockState.getBlock() == Blocks.sand) && !broken.contains(blockPos)) {
                                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            }
                        }
                        break;
                    case WEST:
                        if (blockPos.getX() <= x && blockPos.getZ() == z) {
                            if (isSlow(blockState)) {
                                gemstones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                            } else if (!SkyblockMod.config.includeExcavatable && blockState.getBlock() == Blocks.stone && !broken.contains(blockPos)) {
                                stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                            }
                            if (SkyblockMod.config.includeOres) {
                                if ((blockState.getBlock() == Blocks.coal_ore || blockState.getBlock() == Blocks.diamond_ore
                                        || blockState.getBlock() == Blocks.gold_ore || blockState.getBlock() == Blocks.redstone_ore
                                        || blockState.getBlock() == Blocks.iron_ore || blockState.getBlock() == Blocks.lapis_ore
                                        || blockState.getBlock() == Blocks.emerald_ore || blockState.getBlock() == Blocks.netherrack
                                        || blockState.getBlock() == Blocks.lit_redstone_ore)
                                        && !broken.contains(blockPos)) {
                                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            }
                            if (SkyblockMod.config.includeExcavatable) {
                                if ((blockState.getBlock() == Blocks.gravel || blockState.getBlock() == Blocks.sand) && !broken.contains(blockPos)) {
                                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            }
                        }
                        break;
                    case EAST:
                        if (blockPos.getX() >= x && blockPos.getZ() == z) {
                            if (isSlow(blockState)) {
                                gemstones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                            } else if (!SkyblockMod.config.includeExcavatable && blockState.getBlock() == Blocks.stone && !broken.contains(blockPos)) {
                                stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                            }
                            if (SkyblockMod.config.includeOres) {
                                if ((blockState.getBlock() == Blocks.coal_ore || blockState.getBlock() == Blocks.diamond_ore
                                        || blockState.getBlock() == Blocks.gold_ore || blockState.getBlock() == Blocks.redstone_ore
                                        || blockState.getBlock() == Blocks.iron_ore || blockState.getBlock() == Blocks.lapis_ore
                                        || blockState.getBlock() == Blocks.emerald_ore || blockState.getBlock() == Blocks.netherrack
                                        || blockState.getBlock() == Blocks.lit_redstone_ore)
                                        && !broken.contains(blockPos)) {
                                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            }
                            if (SkyblockMod.config.includeExcavatable) {
                                if ((blockState.getBlock() == Blocks.gravel || blockState.getBlock() == Blocks.sand) && !broken.contains(blockPos)) {
                                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                                }
                            }
                        }
                        break;
                }
            }
        }
        double smallest = 9999;
        Vec3 closest = null;
        for (Vec3 stone : stones) {
            double dist = stone.distanceTo(playerVec);
            if (dist < smallest) {
                smallest = dist;
                closest = stone;
            }
        }

        double smallestgem = 9999;
        Vec3 closestgem = null;
        for (Vec3 gem : gemstones) {
            double dist = gem.distanceTo(playerVec);
            if (dist < smallestgem) {
                smallestgem = dist;
                closestgem = gem;
            }
        }
        if (closestgem != null) {
            gemstone = new BlockPos(closestgem.xCoord, closestgem.yCoord, closestgem.zCoord);
        } else {
            gemstone = null;
        }
        if (closest != null && smallest < 5) {
            return new BlockPos(closest.xCoord, closest.yCoord, closest.zCoord);
        }
        return null;
    }

    private boolean isSlow(IBlockState blockState) {
        if (blockState.getBlock() == Blocks.prismarine) {
            return true;
        } else if (blockState.getBlock() == Blocks.wool) {
            return true;
        } else if (blockState.getBlock() == Blocks.stained_hardened_clay) {
            return true;
        } else if (SkyblockMod.config.prioTitanium && blockState.getBlock() == Blocks.stone && blockState.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH) {
            return true;
        } else if (blockState.getBlock() == Blocks.gold_block) {
            return true;
        } else if (blockState.getBlock() == Blocks.stained_glass_pane || blockState.getBlock() == Blocks.stained_glass) {
            return true;
        }
        return false;
    }

    public void swingItem() {
        MovingObjectPosition movingObjectPosition = Minecraft.getMinecraft().objectMouseOver;
        if (movingObjectPosition != null && movingObjectPosition.entityHit == null) {
            Minecraft.getMinecraft().thePlayer.swingItem();
        }
    }
}
