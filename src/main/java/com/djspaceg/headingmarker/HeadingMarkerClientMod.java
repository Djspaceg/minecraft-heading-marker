package com.djspaceg.headingmarker;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side entrypoint for Heading Marker.
 * This allows the mod to be installed on clients without causing issues.
 * All actual functionality runs server-side only.
 */
public class HeadingMarkerClientMod implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("headingmarker-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Heading Marker client-side initialized (client-optional mode)");
        // No client-side functionality needed - all commands and waypoints are server-side
    }
}

