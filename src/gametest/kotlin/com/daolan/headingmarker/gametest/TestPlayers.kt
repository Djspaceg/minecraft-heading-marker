package com.daolan.headingmarker.gametest

import com.mojang.authlib.GameProfile
import io.netty.channel.embedded.EmbeddedChannel
import java.util.UUID
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.Connection
import net.minecraft.network.DisconnectionDetails
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec3

/** A fake connected player that records the chat and actionbar messages it receives. */
class RecordingPlayer(
    server: MinecraftServer,
    level: ServerLevel,
    profile: GameProfile,
    info: ClientInformation,
) : ServerPlayer(server, level, profile, info) {
    lateinit var testConnection: Connection
    val chat = mutableListOf<String>()
    val actionBar = mutableListOf<String>()

    override fun gameMode(): GameType = GameType.CREATIVE

    override fun sendSystemMessage(component: Component) = sendSystemMessage(component, false)

    override fun sendSystemMessage(component: Component, overlay: Boolean) {
        if (overlay) actionBar += component.string else chat += component.string
        super.sendSystemMessage(component, overlay)
    }
}

object TestPlayers {
    /**
     * Connects a fake player through the real PlayerList join path, so Fabric's JOIN event (and
     * the mod's join handling) runs exactly as for a real client.
     */
    fun join(
        helper: GameTestHelper,
        name: String,
        uuid: UUID = UUID.randomUUID(),
        at: Vec3 = helper.absoluteVec(Vec3(1.5, 2.0, 1.5)),
    ): RecordingPlayer {
        val level = helper.level
        val server = level.server
        val profile = GameProfile(uuid, name)
        val cookie = CommonListenerCookie.createInitial(profile, false)
        val player = RecordingPlayer(server, level, profile, cookie.clientInformation())
        val connection = Connection(PacketFlow.SERVERBOUND)
        EmbeddedChannel(connection)
        player.testConnection = connection
        server.playerList.placeNewPlayer(connection, player, cookie)
        if (player.level() != level) {
            player.teleportTo(level, at.x, at.y, at.z, emptySet(), 0f, 0f, false)
        } else {
            player.teleportTo(at.x, at.y, at.z)
        }
        return player
    }

    /** Disconnects [player] the same way a dropped client connection would. */
    fun leave(player: RecordingPlayer) {
        if (player.hasDisconnected()) return
        // Fabric fires DISCONNECT from Connection.handleDisconnection, which the network thread
        // calls for real clients once the channel closes.
        player.testConnection.disconnect(DisconnectionDetails(Component.literal("test done")))
        player.testConnection.handleDisconnection()
    }

    fun run(player: ServerPlayer, command: String) {
        player.level().server.commands.performPrefixedCommand(
            player.createCommandSourceStack(),
            command,
        )
    }
}
