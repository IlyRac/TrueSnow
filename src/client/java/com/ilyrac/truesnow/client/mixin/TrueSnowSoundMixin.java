package com.ilyrac.truesnow.client.mixin;

import com.ilyrac.truesnow.client.sound.SnowyAtmosphereSound;
import com.ilyrac.truesnow.client.sound.SnowyWindSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class TrueSnowSoundMixin {

    @Unique private SnowyWindSound truesnow$windSound = null;
    @Unique private SnowyAtmosphereSound truesnow$atmosphereSound = null;
    @Unique private int truesnow$atmosphereCooldown = 20;

    @Inject(method = "tick", at = @At("TAIL"))
    private void truesnow$playWinterAtmosphereSounds(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        Minecraft client = Minecraft.getInstance();
        Level level = client.level;

        if (level == null || !level.isClientSide()) return;

        boolean inSnowyBiome = false;

        BlockPos pos = player.blockPosition();
        Holder<Biome> biomeHolder = level.getBiome(pos);

        // Explicit ID checks for all snowy, icy, and cold mountain biomes
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

                inSnowyBiome = true;
            }
        }

        // The Heavy Blizzard ---
        boolean shouldPlayBlizzard = inSnowyBiome && level.isRaining();

        if (shouldPlayBlizzard) {
            if (this.truesnow$windSound != null && this.truesnow$windSound.isFadingOut()) {
                this.truesnow$windSound.cancelFadeOut();
            }
            else if (this.truesnow$windSound == null || this.truesnow$windSound.isStopped()) {
                this.truesnow$windSound = new SnowyWindSound(player);
                client.getSoundManager().play(this.truesnow$windSound);
            }
        } else {
            if (this.truesnow$windSound != null && !this.truesnow$windSound.isFadingOut()) {
                this.truesnow$windSound.fadeOut();
            }
            if (this.truesnow$windSound != null && this.truesnow$windSound.isStopped()) {
                this.truesnow$windSound = null;
            }
        }

        // The Occasional Background Atmosphere ---
        if (inSnowyBiome) {
            if (this.truesnow$atmosphereSound == null || this.truesnow$atmosphereSound.isStopped()) {
                this.truesnow$atmosphereSound = null;

                if (this.truesnow$atmosphereCooldown <= 0) {
                    int playDurationTicks = 300 + player.getRandom().nextInt(200);
                    this.truesnow$atmosphereSound = new SnowyAtmosphereSound(player, playDurationTicks);
                    client.getSoundManager().play(this.truesnow$atmosphereSound);

                    this.truesnow$atmosphereCooldown = 60 + player.getRandom().nextInt(140);
                } else {
                    this.truesnow$atmosphereCooldown--;
                }
            }
        } else {
            if (this.truesnow$atmosphereSound != null && !this.truesnow$atmosphereSound.isFadingOut()) {
                this.truesnow$atmosphereSound.forceFadeOut();
            }
            if (this.truesnow$atmosphereSound != null && this.truesnow$atmosphereSound.isStopped()) {
                this.truesnow$atmosphereSound = null;
            }
        }
    }
}