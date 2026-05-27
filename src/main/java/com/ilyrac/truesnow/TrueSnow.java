package com.ilyrac.truesnow;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrueSnow implements ModInitializer {
	public static final String MOD_ID = "truesnow";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("TrueSnow Loaded Successfully!");
	}
}