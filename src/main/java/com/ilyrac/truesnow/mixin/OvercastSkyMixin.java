package com.ilyrac.truesnow.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnvironmentAttributeProbe.class)
public class OvercastSkyMixin {

    @Shadow private @Nullable Level level;
    @Shadow private @Nullable Vec3 position;

    @Unique private float truesnow$transitionProgress = 0.0f;

    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void truesnow$applyCustomCloudyEnvironment(EnvironmentAttribute<?> attribute, float partialTicks, CallbackInfoReturnable<Object> cir) {
        if (attribute == null) return;

        String attributeName = attribute.toString();

        boolean isSky = attributeName.contains("sky_color");
        boolean isClouds = attributeName.contains("cloud_color");
        boolean isFogColor = attributeName.contains("fog_color");
        boolean isFogStart = attributeName.contains("fog_start");
        boolean isFogEnd = attributeName.contains("fog_end");

        if (isSky || isClouds || isFogColor || isFogStart || isFogEnd) {
            Object value = cir.getReturnValue();
            boolean insideWinterBiome = false;

            if (this.level != null && this.position != null) {
                BlockPos pos = BlockPos.containing(this.position.x, this.position.y, this.position.z);
                Holder<Biome> biomeHolder = this.level.getBiome(pos);

                if (biomeHolder.unwrapKey().isPresent()) {
                    net.minecraft.resources.Identifier id = biomeHolder.unwrapKey().get().identifier();
                    String path = id.getPath();

                    if (path.equals("snowy_plains") ||
                            path.equals("ice_spikes") ||
                            path.equals("snowy_taiga") ||
                            path.equals("grove") ||
                            path.equals("snowy_slopes") ||
                            path.equals("jagged_peaks") ||
                            path.equals("frozen_peaks") ||
                            path.equals("snowy_beach") ||
                            path.equals("frozen_river") ||
                            path.equals("frozen_ocean") ||
                            path.equals("deep_frozen_ocean")) {

                        insideWinterBiome = true;
                    }
                }
            }

            if (isSky) {
                float blendSpeed = 0.03f;
                if (insideWinterBiome) {
                    this.truesnow$transitionProgress = Math.min(1.0f, this.truesnow$transitionProgress + blendSpeed);
                } else {
                    this.truesnow$transitionProgress = Math.max(0.0f, this.truesnow$transitionProgress - blendSpeed);
                }
            }

            if (this.truesnow$transitionProgress <= 0.0f) {
                return;
            }

            if (value instanceof Float) {
                float finalDistance = getFinalDistance((Float) value, isFogStart, isFogEnd);
                cir.setReturnValue(finalDistance);
                return;
            }

            if (value instanceof Integer) {
                int originalColor = (Integer) value;
                int alpha = ARGB.alpha(originalColor);
                int origR = ARGB.red(originalColor);
                int origG = ARGB.green(originalColor);
                int origB = ARGB.blue(originalColor);

                double brightnessFactor = (origR * 0.299 + origG * 0.587 + origB * 0.114) / 255.0;

                int targetR, targetG, targetB;

                if (isSky) {
                    targetR = 86;
                    targetG = 111;
                    targetB = 170;
                } else if (isClouds) {
                    targetR = 139;
                    targetG = 160;
                    targetB = 199;
                } else {
                    targetR = 245;
                    targetG = 248;
                    targetB = 252;
                }

                int customR = (int) (targetR * brightnessFactor);
                int customG = (int) (targetG * brightnessFactor);
                int customB = (int) (targetB * brightnessFactor);

                if (isSky) {
                    customR = (int) (customR * 0.88 + origR * 0.12);
                    customG = (int) (customG * 0.88 + origG * 0.12);
                    customB = (int) (customB * 0.88 + origB * 0.12);
                } else if (isClouds) {
                    customR = (int) (customR * 0.60 + origR * 0.40);
                    customG = (int) (customG * 0.60 + origG * 0.40);
                    customB = (int) (customB * 0.60 + origB * 0.40);
                } else {
                    customR = (int) (customR * 0.98 + origR * 0.02);
                    customG = (int) (customG * 0.98 + origG * 0.02);
                    customB = (int) (customB * 0.98 + origB * 0.02);
                }

                int finalR = (int) (origR + (customR - origR) * this.truesnow$transitionProgress);
                int finalG = (int) (origG + (customG - origG) * this.truesnow$transitionProgress);
                int finalB = (int) (origB + (customB - origB) * this.truesnow$transitionProgress);

                cir.setReturnValue(ARGB.color(
                        alpha,
                        Math.clamp(finalR, 0, 255),
                        Math.clamp(finalG, 0, 255),
                        Math.clamp(finalB, 0, 255)
                ));
            }
        }
    }

    @Unique
    private float getFinalDistance(Float value, boolean isFogStart, boolean isFogEnd) {
        float originalDistance = value;

        if (isFogStart) {
            return 0.0f;
        }

        if (isFogEnd) {
            float curveFactor = (float) Math.pow(this.truesnow$transitionProgress, 3);
            return originalDistance - (originalDistance * 0.60f * curveFactor);
        }

        return originalDistance;
    }
}