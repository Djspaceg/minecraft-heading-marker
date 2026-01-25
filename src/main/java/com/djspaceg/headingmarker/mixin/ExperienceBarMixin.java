package com.djspaceg.headingmarker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.djspaceg.headingmarker.HeadingMarkerClient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(ExperienceBar.class)
public class ExperienceBarMixin {
    
    @Inject(method = "renderAddons", at = @At("TAIL"))
    private void renderWaypointMarkers(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        HeadingMarkerClient.renderWaypointMarkers(context, tickCounter);
    }
}
