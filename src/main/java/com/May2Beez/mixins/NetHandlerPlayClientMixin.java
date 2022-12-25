package com.May2Beez.mixins;

import com.May2Beez.events.SpawnParticleEvent;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {

    @Inject(method = "handleParticles", at = @At(value = "HEAD"), cancellable = true)
    public void handleParticles(
            S2APacketParticles packetIn, CallbackInfo ci
    ) {
        SpawnParticleEvent event = new SpawnParticleEvent(
                packetIn.getParticleType(),
                packetIn.isLongDistance(),
                packetIn.getXCoordinate(),
                packetIn.getYCoordinate(),
                packetIn.getZCoordinate(),
                packetIn.getXOffset(),
                packetIn.getYOffset(),
                packetIn.getZOffset(),
                packetIn.getParticleArgs()
        );
        if (MinecraftForge.EVENT_BUS.post(event)) ci.cancel();
    }
}
