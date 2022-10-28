package com.May2Beez.mixins;

import com.May2Beez.events.PlayerMoveEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {
    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onUpdateWalking(CallbackInfo cir) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerMoveEvent.Pre())) cir.cancel();
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"), cancellable = true)
    public void onWalking(CallbackInfo cir) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerMoveEvent.Post())) cir.cancel();
    }
}
