package com.May2Beez.utils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class Structs {
    public static class BlockData {
        private final BlockPos pos;
        private final Block block;
        private final IBlockState state;

        private final Vec3 randomVisibilityLine;

        public BlockData(BlockPos pos, Block block, IBlockState state, Vec3 randomVisibilityLine) {
            this.pos = pos;
            this.block = block;
            this.state = state;
            this.randomVisibilityLine = randomVisibilityLine;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Block getBlock() {
            return block;
        }

        public IBlockState getState() {
            return state;
        }

        public Vec3 getRandomVisibilityLine() {
            return randomVisibilityLine;
        }
    }
}
