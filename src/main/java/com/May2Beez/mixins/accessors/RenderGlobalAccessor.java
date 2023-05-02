package com.May2Beez.mixins.accessors;

import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;
import java.util.Map;

@Mixin({RenderGlobal.class})
public interface RenderGlobalAccessor {

    @Accessor("damagedBlocks")
    Map<Integer, DestroyBlockProgress> getDamagedBlocks();
}
