package com.ilyrac.truesnow.client.sound;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class SnowyWindSound extends AbstractTickableSoundInstance {
    private final LocalPlayer player;
    private boolean fadingOut = false;

    public SnowyWindSound(LocalPlayer player) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.WEATHER, player.getRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;

        this.volume = 0.01f;
        this.pitch = 0.50f;

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

        if (this.fadingOut) {
            this.volume -= 0.01f;
            if (this.volume <= 0.0f) {
                this.stop();
            }
        } else {
            float maxVolume = 0.30f;
            if (this.volume < maxVolume) {
                this.volume += 0.01f;
            }
        }
    }

    public void fadeOut() {
        this.fadingOut = true;
    }

    public void cancelFadeOut() {
        this.fadingOut = false;
    }

    public boolean isFadingOut() {
        return this.fadingOut;
    }
}