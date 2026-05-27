package com.ilyrac.truesnow.client.sound;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SnowyAtmosphereSound extends AbstractTickableSoundInstance {
    private final LocalPlayer player;
    private boolean fadingOut = false;
    private int ticksAlive = 0;
    private final int maxDuration;

    public SnowyAtmosphereSound(LocalPlayer player, int durationTicks) {
        super(
                SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(
                        "minecraft", "ambient.soul_sand_valley.loop")),
                SoundSource.AMBIENT,
                player.getRandom()
        );
        this.player = player;
        this.maxDuration = durationTicks;
        this.looping = true;
        this.delay = 0;

        this.volume = 0.01f;
        this.pitch = 0.60f;

        this.relative = true;
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
    }

    @Override
    public void tick() {
        if (this.player.isRemoved()) {
            this.stop();
            return;
        }

        this.ticksAlive++;

        if (this.ticksAlive >= this.maxDuration) {
            this.fadingOut = true;
        }

        if (this.fadingOut) {
            this.volume -= 0.005f;
            if (this.volume <= 0.0f) {
                this.stop();
            }
        } else {
            float maxVolume = 0.20f;
            if (this.volume < maxVolume) {
                this.volume += 0.005f;
            }
        }
    }

    public void forceFadeOut() {
        this.fadingOut = true;
    }

    public boolean isFadingOut() {
        return this.fadingOut;
    }
}