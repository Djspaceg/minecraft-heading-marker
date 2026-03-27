package com.daolan.headingmarker

import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

/**
 * Client-side entrypoint for Heading Marker. This allows the mod to be installed on clients without
 * causing issues. All actual functionality runs server-side only.
 */
class HeadingMarkerClientMod : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("headingmarker-client")

    override fun onInitializeClient() {
        logger.info("Heading Marker client-side initialized (client-optional mode)")
    }
}
