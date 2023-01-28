package com.May2Beez.mixins;

import com.May2Beez.May2BeezQoL;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({BlockStainedGlassPane.class})
public abstract class GlassPaneMixin extends BlockPane {

    protected GlassPaneMixin(Material materialIn, boolean canDrop) {
        super(materialIn, canDrop);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        if (May2BeezQoL.config.turnGlassPanesIntoFullBlock)
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        else
            super.setBlockBoundsBasedOnState(worldIn, pos);
    }
}
