package com.ilyrac.truesnow.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CloudRenderer.class)
public class CloudRendererMixin {

    @Unique
    private static float truesnow$cloudTransition = 0.0f;

    @ModifyVariable(
            method = "prepare(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V",
            at = @At("HEAD"),
            argsOnly = true,
            name = "color")
    private int truesnow$darkenCloudColor(int color) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return color;

        BlockPos pos = client.player.blockPosition();
        Holder<Biome> biomeHolder = client.level.getBiome(pos);

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
        truesnow$cloudTransition = isWinter
                ? Math.min(1.0f, truesnow$cloudTransition + step)
                : Math.max(0.0f, truesnow$cloudTransition - step);

        if (truesnow$cloudTransition > 0.0f) {
            int a = ARGB.alpha(color);
            int r = ARGB.red(color);
            int g = ARGB.green(color);
            int b = ARGB.blue(color);

            // Bumped cloud brightness up to 0.95
            double brightnessFactor = ((r * 0.299 + g * 0.587 + b * 0.114) / 255.0) * 0.95;

            // Lighter storm clouds
            int baseTargetR = 140;
            int baseTargetG = 145;
            int baseTargetB = 150;

            int targetR = (int) (baseTargetR * brightnessFactor);
            int targetG = (int) (baseTargetG * brightnessFactor);
            int targetB = (int) (baseTargetB * brightnessFactor);

            int finalR = (int) (r + (targetR - r) * truesnow$cloudTransition);
            int finalG = (int) (g + (targetG - g) * truesnow$cloudTransition);
            int finalB = (int) (b + (targetB - b) * truesnow$cloudTransition);

            return ARGB.color(a, Math.clamp(finalR, 0, 255), Math.clamp(finalG, 0, 255), Math.clamp(finalB, 0, 255));
        }

        return color;
    }
}