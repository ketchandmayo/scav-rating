package ru.ketch.scavrating;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScavRating implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("scav-rating");
    public static final String BACKEND_URL = "https://rating.unionsmp.ru";

    @Override
    public void onInitialize() {
        LOGGER.info("ScavRating Addon Initialized!");
    }
}
