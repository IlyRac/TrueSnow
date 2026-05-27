package com.ilyrac.truesnow.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class TrueSnowClient implements ClientModInitializer {

	public static boolean isStrictWinterBiome(BlockPos pos) {
		var clientLevel = Minecraft.getInstance().level;
		if (clientLevel == null) return false;

		var biomeHolder = clientLevel.getBiome(pos);
		var unwrapped = biomeHolder.unwrapKey();

		if (unwrapped.isPresent()) {
			String path = unwrapped.get().identifier().getPath();
			return path.equals("snowy_plains") ||
					path.equals("ice_spikes") ||
					path.equals("snowy_taiga") ||
					path.equals("grove") ||
					path.equals("snowy_slopes") ||
					path.equals("jagged_peaks") ||
					path.equals("frozen_peaks") ||
					path.equals("snowy_beach") ||
					path.equals("frozen_river") ||
					path.equals("frozen_ocean") ||
					path.equals("deep_frozen_ocean");
		}
		return false;
	}

	@Override
	public void onInitializeClient() {

		// 1. FIXED GRASS TINT SOURCE (For natural grass colors in normal biomes)
		BlockTintSource grassSnowTintSource = new BlockTintSource() {
			@Override public int color(@NonNull BlockState state) { return -1; }

			@Override
			public int colorInWorld(@NonNull BlockState state, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos) {
				if (isStrictWinterBiome(pos)) {
					return 0xFFFFFFFF; // Pure white for winter grass
				}
				return BiomeColors.getAverageGrassColor(level, pos); // Vanilla grass color lookup fallback
			}
		};

		// 2. LEAVES/FOLIAGE TINT SOURCE
		BlockTintSource whiteTintSource = new BlockTintSource() {
			@Override public int color(@NonNull BlockState state) { return -1; }

			@Override
			public int colorInWorld(@NonNull BlockState state, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos) {
				if (isStrictWinterBiome(pos)) {
					return 0xFFFFFFFF; // Pure white for winter leaves
				}
				return BiomeColors.getAverageFoliageColor(level, pos); // Vanilla foliage color lookup fallback
			}
		};

		// Register Grass blocks with the grass-specific lookup
		BlockColorRegistry.register(
				List.of(grassSnowTintSource),
				Blocks.SHORT_GRASS,
				Blocks.TALL_GRASS,
				Blocks.GRASS_BLOCK,
				Blocks.FERN,
				Blocks.LARGE_FERN
		);

		// Register Leaves blocks with the foliage-specific lookup
		BlockColorRegistry.register(
				List.of(whiteTintSource),
				Blocks.OAK_LEAVES,
				Blocks.SPRUCE_LEAVES
		);

		// --- SYSTEM 2: Sodium-Compatible Custom Model Wrapper ---
		ModelLoadingPlugin.register(pluginContext ->
				pluginContext.modifyBlockModelAfterBake().register((model, context) -> {
					BlockState state = context.state();

                    boolean isTargetPlant =
							state.is(Blocks.POPPY)
									|| state.is(Blocks.DANDELION)
									|| state.is(Blocks.BUSH)
									|| state.is(Blocks.BROWN_MUSHROOM)
									|| state.is(Blocks.RED_MUSHROOM)
									|| state.is(Blocks.SUGAR_CANE)
									|| state.is(Blocks.FIREFLY_BUSH)
									|| state.is(Blocks.SWEET_BERRY_BUSH);

					if (isTargetPlant) {
						return new SnowyPlantModel(model, TrueSnowClient::isStrictWinterBiome);
					}

					return model;
				}));

		// Built-in resource pack priority loader remains intact
		FabricLoader.getInstance().getModContainer("truesnow").ifPresent(modContainer -> {
			//noinspection deprecation
			ResourceManagerHelper.registerBuiltinResourcePack(
					Identifier.fromNamespaceAndPath("truesnow", "truesnow_overlays"),
					modContainer,
					Component.literal("TrueSnow Overlays"),
					ResourcePackActivationType.DEFAULT_ENABLED
			);
		});
	}
}