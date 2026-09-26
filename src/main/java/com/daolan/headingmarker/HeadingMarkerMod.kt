package com.daolan.headingmarker

import com.daolan.headingmarker.storage.StorageLocation
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Mod entrypoint: wires Fabric events to the [WaypointService] of the running server. */
class HeadingMarkerMod : ModInitializer {

    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register(HeadingMarkerCommands::register)

        // STARTING runs before the levels load, so ENTITY_LOAD sees every stand saved in the world.
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            active = WaypointService(server, StorageLocation.forServer(server)).also { it.load() }
        }
        // Save alongside the world (autosave, /save-all, shutdown) so a crash loses at most one
        // autosave interval. STOPPING also saves in case the final world save fails.
        ServerLifecycleEvents.AFTER_SAVE.register { _, _, _ -> active?.saveChanged() }
        ServerLifecycleEvents.SERVER_STOPPING.register { _ -> active?.saveChanged() }
        ServerLifecycleEvents.SERVER_STOPPED.register { _ -> active = null }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> active?.onJoin(handler.player) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            active?.onDisconnect(handler.player)
        }
        ServerEntityEvents.ENTITY_LOAD.register { entity, level -> active?.onEntityLoad(entity, level) }
        ServerTickEvents.END_SERVER_TICK.register { _ -> active?.tick() }

        LOGGER.info("Heading Marker Mod initialized.")
    }

    companion object {
        const val MOD_ID = "headingmarker"
        @JvmField val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

        @Volatile private var active: WaypointService? = null

        /** The service for the running server. Only valid while a server is running. */
        fun service(): WaypointService =
            checkNotNull(active) { "Heading Marker is not attached to a running server" }
    }
}
