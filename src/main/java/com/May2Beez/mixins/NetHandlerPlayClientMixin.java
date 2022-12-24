package com.May2Beez.mixins;

import com.May2Beez.modules.player.FishingMacro;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S2APacketParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {NetHandlerPlayClient.class}, remap = false)
public class NetHandlerPlayClientMixin {
    @Inject(method = {"handleParticles"}, at = {@At("HEAD")})
    private void handleParticles(S2APacketParticles packet, CallbackInfo ci) {
        FishingMacro.handleParticles(packet);
    }
}
