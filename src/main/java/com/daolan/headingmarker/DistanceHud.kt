package com.daolan.headingmarker

import com.daolan.headingmarker.model.Waypoint
import java.util.UUID
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

/** Shows each player the distance to their waypoints on the actionbar. */
class DistanceHud {
    private val lastSent = HashMap<UUID, String>()

    /** Sends [player] a fresh distance line for [waypoints] if it changed since last time. */
    fun update(player: ServerPlayer, waypoints: Collection<Waypoint>) {
        val text = render(player.position(), waypoints)
        val plain = text.string
        if (lastSent[player.uuid] == plain) return
        lastSent[player.uuid] = plain
        player.sendSystemMessage(text, true)
    }

    fun forget(player: UUID) {
        lastSent.remove(player)
    }

    companion object {
        private const val MAX_LABEL_LENGTH = 12

        fun render(from: Vec3, waypoints: Collection<Waypoint>): MutableComponent {
            if (waypoints.isEmpty()) return Component.empty()
            return waypoints
                .sortedBy { it.key }
                .map { waypoint ->
                    val distance = from.distanceTo(Vec3(waypoint.x, waypoint.y, waypoint.z)).toInt()
                    Component.literal("${waypoint.color.emoji} ${waypoint.label.take(MAX_LABEL_LENGTH)} ")
                        .append(Component.literal("$distance").withStyle(waypoint.color.formatting))
                }
                .reduce { line, next -> line.append("  ").append(next) }
        }
    }
}
