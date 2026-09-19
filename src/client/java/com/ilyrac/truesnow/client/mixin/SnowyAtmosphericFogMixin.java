package com.ilyrac.truesnow.client.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AtmosphericFogEnvironment.class)
public class SnowyAtmosphericFogMixin {

    @Unique
    private static float truesnow$transitionProgress = 0.0f;

    @Unique
    private static float truesnow$approach(float current, float target, float step) {
        if (Float.isNaN(current)) return target;
        return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
    }

    @Inject(at = @At("TAIL"), method = "setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V")
    public void truesnow$applySoftWinterFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
        BlockPos pos = camera.blockPosition();
        Holder<Biome> biomeHolder = level.getBiome(pos);

        boolean isWinter = false;

        if (biomeHolder.unwrapKey().isPresent()) {
            String path = biomeHolder.unwrapKey().get().identifier().getPath();

            isWinter = path.equals("snowy_plains") || path.equals("ice_spikes") ||
                    path.equals("snowy_taiga") || path.equals("grove") ||
                    path.equals("snowy_slopes") || path.equals("jagged_peaks") ||
                    path.equals("frozen_peaks") || path.equals("snowy_beach") ||
                    path.equals("frozen_river") || path.equals("frozen_ocean") ||
                    path.equals("deep_frozen_ocean");
        }

        // 1. Calculate how much the fog should shift this exact frame.
        float step = 0.02f * deltaTracker.getRealtimeDeltaTicks();
        truesnow$transitionProgress = truesnow$approach(truesnow$transitionProgress, isWinter ? 1.0f : 0.0f, step);

        if (truesnow$transitionProgress <= 0.0f) {
            return;
        }

        // --- DISTANCE MODIFICATIONS ---
        float targetStart = renderDistance * 0.05f;
        float targetEnd = renderDistance * 0.75f;

        fog.environmentalStart = Mth.lerp(truesnow$transitionProgress, fog.environmentalStart, targetStart);
        fog.environmentalEnd = Mth.lerp(truesnow$transitionProgress, fog.environmentalEnd, targetEnd);

        fog.cloudEnd = fog.environmentalEnd;
        fog.skyEnd = Math.min(fog.environmentalEnd, renderDistance);

        // --- COLOR MODIFICATIONS ---
        // Grab the original, vanilla fog colors (converted from JOML Vector4f)
        float origR = fog.color.x();
        float origG = fog.color.y();
        float origB = fog.color.z();

        // Calculate natural brightness so it respects the day/night cycle (multiplied by 0.75 for storm darkening)
        float brightnessFactor = (origR * 0.299f + origG * 0.587f + origB * 0.114f) * 0.75f;

        // Define our target heavy gray blizzard fog (Normalized from 0-255 down to 0.0-1.0)
        float targetR = 115.0f / 255.0f;
        float targetG = 122.0f / 255.0f;
        float targetB = 132.0f / 255.0f;

        // Apply the brightness factor to our custom colors
        float customR = targetR * brightnessFactor;
        float customG = targetG * brightnessFactor;
        float customB = targetB * brightnessFactor;

        // Linearly interpolate the final colors based on the biome transition
        float finalR = Mth.lerp(truesnow$transitionProgress, origR, customR);
        float finalG = Mth.lerp(truesnow$transitionProgress, origG, customG);
        float finalB = Mth.lerp(truesnow$transitionProgress, origB, customB);

        // Inject the final manipulated colors back into the FogData object
        fog.color.set(finalR, finalG, finalB, fog.color.w());
    }
}