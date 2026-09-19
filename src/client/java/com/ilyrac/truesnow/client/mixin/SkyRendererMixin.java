package com.ilyrac.truesnow.client.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {

    @Unique
    private static float truesnow$skyTransition = 0.0f;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void truesnow$darkenSkyRendering(ClientLevel level, float partialTicks, Camera camera, SkyRenderState state, CallbackInfo ci) {
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

        float step = 0.02f;
        truesnow$skyTransition = isWinter
                ? Math.min(1.0f, truesnow$skyTransition + step)
                : Math.max(0.0f, truesnow$skyTransition - step);

        if (truesnow$skyTransition > 0.0f) {
            float origR = state.skyColor.x();
            float origG = state.skyColor.y();
            float origB = state.skyColor.z();

            // Calculate the natural brightness of the vanilla sky to respect the day/night cycle
            // (Multiplied by 0.75 so the daytime blizzard is appropriately dark)
            float brightnessFactor = (origR * 0.299f + origG * 0.587f + origB * 0.114f) * 0.75f;

            // Base heavy overcast gray
            float baseTargetR = 0.45f;
            float baseTargetG = 0.48f;
            float baseTargetB = 0.52f;

            // Apply the brightness factor so it gets pitch black at night
            float targetR = baseTargetR * brightnessFactor;
            float targetG = baseTargetG * brightnessFactor;
            float targetB = baseTargetB * brightnessFactor;

            float finalR = origR + (targetR - origR) * truesnow$skyTransition;
            float finalG = origG + (targetG - origG) * truesnow$skyTransition;
            float finalB = origB + (targetB - origB) * truesnow$skyTransition;

            state.skyColor = new Vector3f(finalR, finalG, finalB);

            // Dim the sun/moon
            state.rainBrightness = state.rainBrightness * (1.0f - (truesnow$skyTransition * 0.75f));
            state.starBrightness = state.starBrightness * (1.0f - truesnow$skyTransition);
        }
    }
}