package com.daolan.headingmarker

import java.nio.file.Files
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Mod entrypoint: wires Fabric events to the [WaypointService] of the running server. */
class HeadingMarkerMod : ModInitializer {

    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register(HeadingMarkerCommands::register)

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            val storageDir = FabricLoader.getInstance().gameDir.resolve("waypoints")
            Files.createDirectories(storageDir)
            active = WaypointService(server, storageDir).also { it.load() }
        }
        ServerLifecycleEvents.SERVER_STOPPING.register { _ -> active?.saveChanged() }
        ServerLifecycleEvents.SERVER_STOPPED.register { _ -> active = null }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> active?.onJoin(handler.player) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            active?.onDisconnect(handler.player)
        }
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
