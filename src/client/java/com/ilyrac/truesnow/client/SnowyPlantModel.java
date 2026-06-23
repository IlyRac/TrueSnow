package com.ilyrac.truesnow.client;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public class SnowyPlantModel extends WrapperBlockStateModel {
    private final Predicate<BlockPos> biomeCondition;

    public SnowyPlantModel(BlockStateModel wrapped, Predicate<BlockPos> biomeCondition) {
        super(wrapped);
        this.biomeCondition = biomeCondition;
    }

    @Override
    public void emitQuads(
            QuadEmitter emitter, @NonNull BlockAndTintGetter level,
            @NonNull BlockPos pos, @NonNull BlockState state,
            @NonNull RandomSource random, @NonNull Predicate<@Nullable Direction> cullTest
    ) {
        boolean isSnowy = this.biomeCondition.test(pos);

        emitter.pushTransform(quad -> {
            if (quad.tintIndex() == 1) {
                quad.tintIndex(-1);
                return isSnowy;
            }
            return true;
        });
        super.emitQuads(emitter, level, pos, state, random, cullTest);
        emitter.popTransform();
    }
}