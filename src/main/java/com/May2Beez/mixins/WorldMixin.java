package com.May2Beez.mixins;

import com.May2Beez.events.BlockChangeEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = World.class)
public abstract class WorldMixin {


    @Shadow public abstract IBlockState getBlockState(BlockPos pos);

    @Inject(method = {"setBlockState"}, at = @At("HEAD"), cancellable = true)
    public void setBlockState(final BlockPos pos, final IBlockState newState, final int flags, final CallbackInfoReturnable<Boolean> callback) {
        IBlockState old = this.getBlockState(pos);
        BlockChangeEvent event = new BlockChangeEvent(pos, old, newState);
        if (newState != old)
            MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            callback.setReturnValue(false);
            callback.cancel();
        }
    }
}
